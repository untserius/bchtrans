package com.evg.ocpp.txns.ServiceImpl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.evg.ocpp.txns.Service.StationService;
import com.evg.ocpp.txns.Service.alertsService;
import com.evg.ocpp.txns.Service.currencyConversionService;
import com.evg.ocpp.txns.Service.ocppMeterValueService;
import com.evg.ocpp.txns.Service.ocppUserService;
import com.evg.ocpp.txns.Service.paymentService;
import com.evg.ocpp.txns.dao.GeneralDao;
import com.evg.ocpp.txns.forms.FinalData;
import com.evg.ocpp.txns.forms.OCPPForm;
import com.evg.ocpp.txns.forms.OCPPMeterValuesPojo;
import com.evg.ocpp.txns.forms.SessionImportedValues;
import com.evg.ocpp.txns.forms.StopTransaction;
import com.evg.ocpp.txns.forms.TransactionForm;
import com.evg.ocpp.txns.model.ocpp.AccountTransactionForGuestUser;
import com.evg.ocpp.txns.model.ocpp.AccountTransactions;
import com.evg.ocpp.txns.model.ocpp.Accounts;
import com.evg.ocpp.txns.model.ocpp.DeviceDetails;
import com.evg.ocpp.txns.model.ocpp.NotificationTracker;
import com.evg.ocpp.txns.model.ocpp.OCPITransactionData;
import com.evg.ocpp.txns.model.ocpp.OCPPActiveTransaction;
import com.evg.ocpp.txns.model.ocpp.OCPPSessionsData;
import com.evg.ocpp.txns.model.ocpp.OCPPStartTransaction;
import com.evg.ocpp.txns.model.ocpp.OCPPStopTransaction;
import com.evg.ocpp.txns.model.ocpp.OCPPTransactionData;
import com.evg.ocpp.txns.model.ocpp.PreferredNotification;
import com.evg.ocpp.txns.model.ocpp.Price;
import com.evg.ocpp.txns.model.ocpp.Session;
import com.evg.ocpp.txns.model.ocpp.TariffPrice;
import com.evg.ocpp.txns.model.ocpp.freeChargingForDriverGrp;
import com.evg.ocpp.txns.model.ocpp.session_pricings;
import com.evg.ocpp.txns.repository.ExecuteRepository;
import com.evg.ocpp.txns.utils.EsLoggerUtil;
import com.evg.ocpp.txns.utils.LoggerUtil;
import com.evg.ocpp.txns.utils.PushNotification;
import com.evg.ocpp.txns.utils.Utils;
import com.evg.ocpp.txns.utils.smsIntegration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OCPPMeterValueServiceImpl implements ocppMeterValueService{
	static Logger logger = LoggerFactory.getLogger(OCPPMeterValueServiceImpl.class);

	ObjectMapper objectMapper = new ObjectMapper();

//	private ConcurrentHashMap<String, SessionBillableValues> sessionValues = new ConcurrentHashMap<>();

	@Autowired
	private ExecuteRepository executeRepository;

	@Autowired
	private OCPPDeviceDetailsService ocppDeviceDetailsService;

	@Value("${ocpi.url}")
	private String ocpiUrl;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private EsLoggerUtil esLoggerUtil;

	@Autowired
	private StationService stationService;

	@Autowired
	private currencyConversionService crncyConversionService;

	@Autowired
	private paymentService paymentService;

	@Autowired
	private  GeneralDao<?, ?> generalDao;

	@Autowired
	private propertiesServiceImpl propertiesServiceImpl;

	@Autowired
	private CurrencyConversion currencyConversion;

	@Autowired
	private smsIntegration smsIntegrationImpl;

	@Value("${kWh.Alert}")
	protected double kWhAlert;

	@Value("${duration.AlertInMins}")
	protected double durationAlertInMins;

	@Value("${revenue.Alert}")
	protected double revenueAlert;

	@Value("${mobileServerUrl}")
	protected String mobileServerUrl;

	@Value("${mobileAuthKey}")
	private String mobileAuthKey;

	@Autowired
	private LoggerUtil customLogger;

	@Autowired
	private OCPPAccountAndCredentialService OCPPAccountAndCredentialService;

	@Autowired
	private OCPPDeviceDetailsService OCPPDeviceDetailsService;

	@Autowired
	public PushNotification pushNotification;

	@Autowired
	private Utils utils;

	@Autowired
	private alertsService alertsService;

	@Autowired
	private ocppUserService ocppUserService;

	@Override
	public Map<String,Object> getPreviousSessionData(String sessionId){
		Map<String,Object> sessionData = new HashMap<>();
		try {
			String query="select id as sessionUniqeId,kilowattHoursUsed,sessionElapsedInMin,endTimeStamp from session where sessionId='"+sessionId+"' order by id desc";
			List<Map<String,Object>> list=executeRepository.findAll(query);
			logger.info(Thread.currentThread().getId()+"previousSessionData query : "+query);
			if(list.size()>0) {
				sessionData=list.get(0);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return sessionData;
	}

	@Override
	public SessionImportedValues billing(SessionImportedValues siv) {
		try {
			OCPPTransactionData txnData = siv.getTxnData();
			if(siv.getSesspricings()!=null) {
				siv.setCost_pricings(siv.getSesspricings().getCost_info());
			}
			System.err.println(txnData.getMinkWhEnergy() + " & " + siv.getTotalKwUsed());
			if(txnData != null && Double.parseDouble(String.valueOf(siv.getTotalKwUsed()))>txnData.getMinkWhEnergy()) {
				if(txnData != null && txnData.getBillingCases() != null && txnData.getBillingCases().equalsIgnoreCase("TOU")) {
					siv = touBilling(siv);
				}else if(txnData != null && txnData.getBillingCases().equalsIgnoreCase("Freeven")){

				}else if(txnData != null && txnData.getBillingCases().equalsIgnoreCase("TOU+Free")) {
					if(txnData.getDriverGroupId() > 0) {
						siv = freeChargingForDriverGrp(siv);
						siv = touBilling(siv);
					}
				}else if(txnData != null && txnData.getBillingCases().equalsIgnoreCase("TOU+Rewards")) {
					siv = RewardKwhForNormalUser(siv);
					siv = touBilling(siv);
					siv = RewardAmountForNormalUser(siv);
				}else if(txnData != null && txnData.getBillingCases().equalsIgnoreCase("TOU+Free+Rewards")) {
					siv = freeChargingForDriverGrp(siv);
					siv = RewardKwhForNormalUser(siv);
					siv = touBilling(siv);
					siv = RewardAmountForNormalUser(siv);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return siv;
	}

	@Override
	public SessionImportedValues touBilling(SessionImportedValues siv) {
		try {
			BigDecimal finalCost=new BigDecimal("0.0");
			BigDecimal finalCostWithVat=new BigDecimal("0.0");
			if(siv.getTxnData().getBillingCases()!= null) {
				try {
					if(siv.getTxnData().getTariff_prices()!=null) {
						JsonNode tariff = objectMapper.readTree(siv.getTxnData().getTariff_prices());
						List<Map<String,Object>> tariffLs = new ArrayList<>();
						if(tariff.size() > 0) {
							Map<String,Object> tariffMap = new HashMap<>();
							tariffMap.put("tariffId", tariff.get(0).get("tariffId"));
							tariffMap.put("max_price_id", tariff.get(0).get("max_price_id"));
							tariffMap.put("min_price_id", tariff.get(0).get("min_price_id"));
							tariffMap.put("tariffName", tariff.get(0).get("tariffName"));
							tariffMap.put("startTime", tariff.get(0).get("startTime"));
							tariffMap.put("endTime", tariff.get(0).get("endTime"));
							JsonNode prices = objectMapper.readTree(String.valueOf(tariff.get(0).get("cost_info")));
							List<Map<String,Object>> pricesLs = new ArrayList<>();
							if(prices.size() > 0) {
								JsonNode aditional = objectMapper.readTree(String.valueOf(prices.get(0).get("aditional")));
								Map<String,Object> costInfo = new HashMap<>();
								Map<String,Object> aditionalSubMap = new HashMap<>();
								String rateRiderType="";
								BigDecimal rateRiderPercent=new BigDecimal(0.00);
								BigDecimal rateRiderAmt=new BigDecimal(0.00);
								Map<String,Object> rateRiderMap = new HashMap<>();
								Map<String,Object> idleChargeMap = new HashMap<>();

								List<Map<String,Object>> taxls = new ArrayList<>();
								if(aditional.size() > 0) {
									JsonNode rateRider = objectMapper.readTree(String.valueOf(aditional.get("rateRider")));
									if(rateRider.size() > 0) {
										rateRiderType = rateRider.get("type").asText();
										rateRiderPercent = new BigDecimal(String.valueOf(rateRider.get("percnt").asDouble()));
										rateRiderMap.put("type", rateRiderType);
										rateRiderMap.put("percnt", rateRiderPercent);
										rateRiderMap.put("restrictionType", "Rate Rider");
										rateRiderMap.put("amount", 0);
										aditionalSubMap.put("rateRider", rateRiderMap);
									}
									JsonNode taxJsonLs = objectMapper.readTree(String.valueOf(aditional.get("tax")));
									if(taxJsonLs.size() > 0) {
										for(int i=0;i < taxJsonLs.size();i++) {
											JsonNode taxJsonMap = objectMapper.readTree(String.valueOf(taxJsonLs.get(i)));
											if(taxJsonMap.size() > 0) {
												Map<String,Object> taxSubMap = new HashMap<>();
												String name = taxJsonMap.get("name").asText();
												BigDecimal taxPercent = new BigDecimal(taxJsonMap.get("percnt").asText());
												taxSubMap.put("name", name);
												taxSubMap.put("percnt", taxPercent);
												taxSubMap.put("restrictionType", "TAX");
												taxSubMap.put("amount", 0.00);
												taxSubMap.put("idleAmount", 0.00);
												taxSubMap.put("chargingAmount", 0.00);
												taxls.add(taxSubMap);
											}
										}
										aditionalSubMap.put("tax", taxls);
									}
									JsonNode idleCharge = objectMapper.readTree(String.valueOf(aditional.get("idleCharge")));
									if(idleCharge.size() > 0) {
										idleChargeMap.put("price", idleCharge.get("price").asDouble());
										idleChargeMap.put("step", idleCharge.get("step").asDouble());
										idleChargeMap.put("type", idleCharge.get("type").asText());
										idleChargeMap.put("gracePeriod", idleCharge.get("gracePeriod").asDouble());
										idleChargeMap.put("restrictionType", "Idle Charge");
										idleChargeMap.put("inActiveCost", 0.0);
										idleChargeMap.put("inActiveduration", 0.0);
										if(!String.valueOf(siv.getTxnData().getIdleStatus()).equalsIgnoreCase("Available")) {
											siv.setIdleBilling(true);
										}
									}
								}


								JsonNode standard = objectMapper.readTree(String.valueOf(prices.get(0).get("standard")));
								Map<String,Object> standardSubMap = new HashMap<>();
								if(standard.size() > 0) {
									BigDecimal maximumRevenue=siv.getTxnData().getMaximumRevenue()>0 ?new BigDecimal(String.valueOf(siv.getTxnData().getMaximumRevenue())) : new BigDecimal("150");
									maximumRevenue=maximumRevenue.setScale(2, RoundingMode.HALF_UP);
									JsonNode time = objectMapper.readTree(String.valueOf(standard.get("time")));
									if(time.size() > 0) {
										BigDecimal timeCost=new BigDecimal(0.00);
										Map<String,Object> timeMap = new HashMap<>();
										double price = time.get("price").asDouble();
										double step = time.get("step").asDouble();
										BigDecimal tax_excl = new BigDecimal(time.get("tax_excl").asText());
										BigDecimal tax_incl = new BigDecimal(time.get("tax_incl").asText());
										BigDecimal vatAmt = new BigDecimal(0.00);
										BigDecimal taxAmt = new BigDecimal(0.00);
										BigDecimal totalTaxPer=new BigDecimal("0");
										siv.setVendingPrice(price);
										if(step == 3600) {
											siv.setVendingUnit("Hr");
											tax_incl = tax_excl = timeCost = (siv.getBillSessionDuration().divide(new BigDecimal(60),9, RoundingMode.HALF_UP).multiply(new BigDecimal(price))).setScale(2, RoundingMode.HALF_UP);
										}else {
											siv.setVendingUnit("Min");
											tax_incl = tax_excl = timeCost = (siv.getBillSessionDuration().multiply(new BigDecimal(price))).setScale(2, RoundingMode.HALF_UP);
										}

										for(Map<String, Object> map : taxls) {
											totalTaxPer=totalTaxPer.add(new BigDecimal(String.valueOf(map.get("percnt"))));
										}
										if(totalTaxPer.doubleValue()>0 && timeCost.doubleValue()>0) {
											BigDecimal multiply = (timeCost.divide(new BigDecimal(100),9, RoundingMode.HALF_UP).multiply(totalTaxPer)).setScale(2, RoundingMode.HALF_UP);
											BigDecimal finalCostStore=multiply.add(timeCost);
											if(finalCostStore.doubleValue()>maximumRevenue.doubleValue()) {
												BigDecimal costWithoutTax=(new BigDecimal(String.valueOf("100")).multiply(maximumRevenue)).divide(new BigDecimal(String.valueOf("100")).add(totalTaxPer), 2, RoundingMode.HALF_UP);
												timeCost=costWithoutTax.setScale(2, RoundingMode.HALF_UP);
											}
										}
										if(timeCost.doubleValue()>=maximumRevenue.doubleValue()) {
											timeCost=maximumRevenue;
											tax_incl = tax_excl = timeCost;
											maximumRevenue=new BigDecimal("0");
										}else {
											maximumRevenue=maximumRevenue.subtract(timeCost);
											tax_incl = tax_excl = timeCost;
										}
										finalCost=finalCost.add(timeCost);
										if(rateRiderType.equalsIgnoreCase("-ve")) {
											vatAmt = (timeCost.divide(new BigDecimal(100),9, RoundingMode.HALF_UP).multiply(rateRiderPercent)).setScale(2, RoundingMode.HALF_UP);
											timeCost = timeCost.subtract(vatAmt);
											rateRiderAmt = rateRiderAmt.add(vatAmt);
											tax_excl = timeCost;
											tax_incl = timeCost;
										}else if(rateRiderType.equalsIgnoreCase("+ve")){
											vatAmt = (timeCost.divide(new BigDecimal(100),9, RoundingMode.HALF_UP).multiply(rateRiderPercent)).setScale(2, RoundingMode.HALF_UP);
											timeCost = timeCost.add(vatAmt);
											rateRiderAmt = rateRiderAmt.add(vatAmt);
											tax_excl = timeCost;
											tax_incl = timeCost;
										}
										List<Map<String,Object>> taxLsTemp = new ArrayList<>();
										for(Map<String, Object> map : taxls) {
											Map<String,Object> temp = new HashMap<>();
											BigDecimal taxAmount = new BigDecimal(String.valueOf(map.get("amount")));
											BigDecimal taxPercent = new BigDecimal(String.valueOf(map.get("percnt")));
											BigDecimal multiply = (timeCost.divide(new BigDecimal(100),9, RoundingMode.HALF_UP).multiply(taxPercent)).setScale(2, RoundingMode.HALF_UP);
											taxAmt = taxAmt.add(multiply).setScale(2, RoundingMode.HALF_UP);
											taxAmount = taxAmount.add(multiply).setScale(2, RoundingMode.HALF_UP);
											temp.put("percnt", map.get("percnt"));
											temp.put("name", map.get("name"));
											temp.put("restrictionType", "TAX");
											temp.put("amount", taxAmount);
											temp.put("idleAmount", 0.00);
											temp.put("chargingAmount", taxAmount);
											taxLsTemp.add(temp);
										}
										tax_incl = tax_incl.add(taxAmt).setScale(2, RoundingMode.HALF_UP);
										taxls.clear();
										taxls = taxLsTemp;
										timeMap.put("price", price);
										timeMap.put("step", step);
										timeMap.put("type", "Time");
										timeMap.put("tax_excl", tax_excl);
										timeMap.put("tax_incl", tax_incl);
										standardSubMap.put("time", timeMap);
									}
									JsonNode energy = objectMapper.readTree(String.valueOf(standard.get("energy")));
									if(energy.size() > 0) {
										BigDecimal energyCost=new BigDecimal("0.0");
										Map<String,Object> energyMap = new HashMap<>();
										double price = energy.get("price").asDouble();
										double step = energy.get("step").asDouble();
										BigDecimal tax_excl = new BigDecimal(energy.get("tax_excl").asText());
										BigDecimal tax_incl = new BigDecimal(energy.get("tax_incl").asText());
										BigDecimal totalTaxPer=new BigDecimal("0");
										siv.setVendingPrice(price);
										if(step == 3600) {
											tax_incl = tax_excl = energyCost = (siv.getBillTotalKwUsed().divide(new BigDecimal(60),9, RoundingMode.HALF_UP).multiply(new BigDecimal(price))).setScale(2, RoundingMode.HALF_UP);
										}else {
											tax_incl = tax_excl = energyCost = (siv.getBillTotalKwUsed().multiply(new BigDecimal(price))).setScale(2, RoundingMode.HALF_UP);
										}
										siv.setVendingUnit("kWh");
										for(Map<String, Object> map : taxls) {
											totalTaxPer=totalTaxPer.add(new BigDecimal(String.valueOf(map.get("percnt"))));
										}
										if(totalTaxPer.doubleValue()>0 && energyCost.doubleValue()>0) {
											BigDecimal multiply = (energyCost.divide(new BigDecimal(100),9, RoundingMode.HALF_UP).multiply(totalTaxPer)).setScale(2, RoundingMode.HALF_UP);
											BigDecimal finalCostStore=multiply.add(energyCost);
											if(finalCostStore.doubleValue()>maximumRevenue.doubleValue()) {
												BigDecimal costWithoutTax=(new BigDecimal(String.valueOf("100")).multiply(maximumRevenue)).divide(new BigDecimal(String.valueOf("100")).add(totalTaxPer), 2, RoundingMode.HALF_UP);
												energyCost=costWithoutTax;
											}
										}
										energyCost=energyCost.setScale(2, RoundingMode.HALF_UP);
										if(energyCost.doubleValue()>=maximumRevenue.doubleValue()) {
											energyCost=maximumRevenue;
											tax_incl = tax_excl = energyCost;
											maximumRevenue=new BigDecimal("0");
										}else {
											tax_incl = tax_excl = energyCost;
											maximumRevenue=maximumRevenue.subtract(energyCost);
										}
										finalCost=finalCost.add(energyCost);
										BigDecimal vatAmt = new BigDecimal(0.00);
										BigDecimal taxAmt = new BigDecimal(0.00);
										if(rateRiderType.equalsIgnoreCase("-ve")) {
											vatAmt = (energyCost.divide(new BigDecimal(100),9, RoundingMode.HALF_UP).multiply(rateRiderPercent)).setScale(2, RoundingMode.HALF_UP);
											energyCost = energyCost.subtract(vatAmt);
											rateRiderAmt = rateRiderAmt.add(vatAmt);
											tax_excl = energyCost;
											tax_incl = energyCost;
										}else if(rateRiderType.equalsIgnoreCase("+ve")){
											vatAmt = (energyCost.divide(new BigDecimal(100),9, RoundingMode.HALF_UP).multiply(rateRiderPercent)).setScale(2, RoundingMode.HALF_UP);
											energyCost =energyCost.add(vatAmt);
											rateRiderAmt = rateRiderAmt.add(vatAmt);
											tax_excl = energyCost;
											tax_incl = energyCost;
										}
										List<Map<String,Object>> taxLsTemp = new ArrayList<>();
										for(Map<String, Object> map : taxls) {
											Map<String,Object> temp = new HashMap<>();
											BigDecimal taxAmount = new BigDecimal(String.valueOf(map.get("amount")));
											BigDecimal taxPercent = new BigDecimal(String.valueOf(map.get("percnt")));
											BigDecimal multiply = (energyCost.divide(new BigDecimal(100),9, RoundingMode.HALF_UP).multiply(taxPercent)).setScale(2, RoundingMode.HALF_UP);
											taxAmt = taxAmt.add(multiply).setScale(2, RoundingMode.HALF_UP);
											taxAmount = taxAmount.add(multiply).setScale(2, RoundingMode.HALF_UP);
											temp.put("percnt", map.get("percnt"));
											temp.put("name", map.get("name"));
											temp.put("restrictionType", "TAX");
											temp.put("amount", taxAmount);
											temp.put("idleAmount", 0.00);
											temp.put("chargingAmount", taxAmount);
											taxLsTemp.add(temp);
										}
										tax_incl = tax_incl.add(taxAmt).setScale(2, RoundingMode.HALF_UP);
										taxls.clear();
										taxls = taxLsTemp;
										energyMap.put("price", price);
										energyMap.put("type", "Energy");
										energyMap.put("tax_excl", tax_excl);
										energyMap.put("tax_incl", tax_incl);
										standardSubMap.put("energy", energyMap);
									}

									JsonNode flat = objectMapper.readTree(String.valueOf(standard.get("flat")));
									if(flat.size() > 0 && finalCost.doubleValue() > 0) {
										BigDecimal flatCost=new BigDecimal("0.0");
										Map<String,Object> flatMap = new HashMap<>();
										double price = flat.get("price").asDouble();
										BigDecimal tax_excl = new BigDecimal(flat.get("tax_excl").asText());
										BigDecimal tax_incl = new BigDecimal(flat.get("tax_incl").asText());
										tax_incl = tax_excl = flatCost = new BigDecimal(price).setScale(2,RoundingMode.HALF_UP);
										BigDecimal vatAmt = new BigDecimal(0.00);
										BigDecimal taxAmt = new BigDecimal(0.00);
										BigDecimal totalTaxPer=new BigDecimal("0");
										finalCost=finalCost.add(flatCost);
										if(rateRiderType.equalsIgnoreCase("-ve")) {
											vatAmt = (flatCost.divide(new BigDecimal(100),9, RoundingMode.HALF_UP).multiply(rateRiderPercent)).setScale(2, RoundingMode.HALF_UP);
											flatCost =flatCost.subtract(vatAmt);
											rateRiderAmt = rateRiderAmt.add(vatAmt);
											tax_excl = flatCost;
											tax_incl = flatCost;
										}else if(rateRiderType.equalsIgnoreCase("+ve")){
											vatAmt = (flatCost.divide(new BigDecimal(100),9, RoundingMode.HALF_UP).multiply(rateRiderPercent)).setScale(2, RoundingMode.HALF_UP);
											flatCost = flatCost.add(vatAmt);
											rateRiderAmt = rateRiderAmt.add(vatAmt);
											tax_excl = flatCost;
											tax_incl = flatCost;
										}
										for(Map<String, Object> map : taxls) {
											totalTaxPer=totalTaxPer.add(new BigDecimal(String.valueOf(map.get("percnt"))));
										}
										if(totalTaxPer.doubleValue()>0 && flatCost.doubleValue()>0) {
											BigDecimal multiply = (flatCost.divide(new BigDecimal(100),9, RoundingMode.HALF_UP).multiply(totalTaxPer)).setScale(2, RoundingMode.HALF_UP);
											BigDecimal finalCostStore=multiply.add(flatCost);
											if(finalCostStore.doubleValue()>maximumRevenue.doubleValue()) {
												BigDecimal costWithoutTax=(new BigDecimal(String.valueOf("100")).multiply(maximumRevenue)).divide(new BigDecimal(String.valueOf("100")).add(totalTaxPer), 2, RoundingMode.HALF_UP);
												flatCost=costWithoutTax;
											}
										}
										if(flatCost.doubleValue()>=maximumRevenue.doubleValue()) {
											flatCost=maximumRevenue;
											tax_incl = tax_excl = flatCost;
											maximumRevenue=new BigDecimal("0");
										}else {
											tax_incl = tax_excl = flatCost;
											maximumRevenue=maximumRevenue.subtract(flatCost);
										}
										flatCost=flatCost.setScale(2, RoundingMode.HALF_UP);
										List<Map<String,Object>> taxLsTemp = new ArrayList<>();
										for(Map<String, Object> map : taxls) {
											Map<String,Object> temp = new HashMap<>();
											BigDecimal taxAmount = new BigDecimal(String.valueOf(map.get("amount")));
											BigDecimal taxPercent = new BigDecimal(String.valueOf(map.get("percnt")));
											BigDecimal multiply = (flatCost.divide(new BigDecimal(100),9, RoundingMode.HALF_UP).multiply(taxPercent)).setScale(2, RoundingMode.HALF_UP);
											taxAmt = taxAmt.add(multiply).setScale(2, RoundingMode.HALF_UP);;
											taxAmount= taxAmount.add(multiply).setScale(2, RoundingMode.HALF_UP);;
											temp.put("percnt", map.get("percnt"));
											temp.put("name", map.get("name"));
											temp.put("restrictionType", "TAX");
											temp.put("amount", taxAmount);
											temp.put("idleAmount", 0.00);
											temp.put("chargingAmount", taxAmount);
											taxLsTemp.add(temp);
										}
										tax_incl = tax_incl.add(taxAmt).setScale(2, RoundingMode.HALF_UP);;
										taxls.clear();
										taxls = taxLsTemp;
										flatMap.put("price", price);
										flatMap.put("type", "Flat");
										flatMap.put("tax_excl", tax_excl);
										flatMap.put("tax_incl", tax_incl);
										standardSubMap.put("flat", flatMap);
									}
									costInfo.put("standard", standardSubMap);
								}

								if(aditional.size() > 0) {
									JsonNode rateRider = objectMapper.readTree(String.valueOf(aditional.get("rateRider")));
									finalCostWithVat = finalCost;
									if(rateRider.size() > 0) {
										rateRiderAmt=rateRiderAmt.setScale(2, RoundingMode.HALF_UP);
										rateRiderMap.put("amount", rateRiderAmt);
										if(rateRiderType.equalsIgnoreCase("-ve")) {
											finalCostWithVat = finalCostWithVat.subtract(rateRiderAmt);
										}else if(rateRiderType.equalsIgnoreCase("+ve")){
											finalCostWithVat = finalCostWithVat.add(rateRiderAmt);
										}
									}
									List<Map<String,Object>> taxls1 = new ArrayList<>();
									for(Map<String,Object> map : taxls) {
										Map<String,Object> temp = new HashMap<>();
										temp = map;
										BigDecimal amount=new BigDecimal(String.valueOf(map.get("amount"))).setScale(2, RoundingMode.HALF_UP);
										if(siv.isIdleBilling()) {
											temp.put("amount",  amount);
											temp.put("idleAmount", 0.00);
											temp.put("chargingAmount", amount);
										}else {
											temp.put("amount", amount);
											temp.put("idleAmount", 0.00);
											temp.put("chargingAmount", amount);
										}
										temp.put("amount", amount);
										finalCostWithVat = finalCostWithVat.add(amount);
										taxls1.add(temp);
									}
//									 JsonNode idleCharge = objectMapper.readTree(String.valueOf(aditional.get("idleCharge")));
//									 if(idleCharge.size() > 0) {
//										 Double lastkWh = Double.valueOf(String.valueOf(siv.getPreviousSessionData().get("kilowattHoursUsed")));
//										 Date lastTime = utils.stringToDate(String.valueOf(siv.getPreviousSessionData().get("endTimeStamp")));
//										 Date lastidleTime = siv.getTxnData().getIdleStartTime();
//										 BigDecimal gracePeriod = new BigDecimal(idleCharge.get("gracePeriod").asDouble());
//										 BigDecimal step = new BigDecimal(idleCharge.get("step").asDouble());
//										 BigDecimal idleChargePrice = new BigDecimal(idleCharge.get("price").asDouble());
//										 BigDecimal tempIdleMins=new BigDecimal("0.0");
//										 if(siv.getTotalKwUsed().doubleValue() == lastkWh) {
//											 Map<String, Double> idleTime = utils.getTimeDifferenceInMiliSec(lastidleTime, siv.getMeterValueTimeStatmp());
//											 BigDecimal idleTimeMins = new BigDecimal(String.valueOf(idleTime.get("Minutes")));
//											 if(idleTimeMins.doubleValue() > 0) {
//												 tempIdleMins = idleTimeMins.subtract(gracePeriod);
//												 if(lastTime.compareTo(lastidleTime)!=0) {
//													 Map<String, Double> lastTimeMap = utils.getTimeDifferenceInMiliSec(lastidleTime, lastTime);
//													 BigDecimal lastTimeDiff = new BigDecimal(String.valueOf(lastTimeMap.get("Minutes")));
//													 if(lastTimeDiff.doubleValue()>gracePeriod.doubleValue()) {
//														 tempIdleMins = tempIdleMins.subtract(lastTimeDiff);
//														 tempIdleMins=tempIdleMins.add(gracePeriod);
//													 }
//												 }
//												 if(tempIdleMins.doubleValue()<0) {
//													 tempIdleMins=new BigDecimal("0.0"); 
//												 }
//											 }
//										 }else {
//											 siv.getTxnData().setIdleStartTime(siv.getMeterValueTimeStatmp());
//										 }
//										 session_pricings sessionpricings=siv.getSesspricings();
//										 if(sessionpricings.getCost_info()!=null && !String.valueOf(sessionpricings.getCost_info()).equalsIgnoreCase("null")) {
//											 JsonNode lastCostInfo = objectMapper.readTree(sessionpricings.getCost_info());
//											 if(lastCostInfo.size() > 0) {
//												 JsonNode lastIdle = objectMapper.readTree(String.valueOf(lastCostInfo.get(0).get("cost_info").get(0).get("aditional").get("idle")));
//												 if(lastIdle!=null && lastIdle.size()>0) {
//													 tempIdleMins = tempIdleMins.add(new BigDecimal(lastIdle.get("idleDuration").asDouble())); 
//												 }
//											 }
//										 }
//										 BigDecimal cost = new BigDecimal("0.00");
//										 if(step.doubleValue() == 3600) {
//											 cost = cost.add(tempIdleMins.multiply(new BigDecimal("60")).multiply(idleChargePrice.divide(new BigDecimal(3600), 15, RoundingMode.HALF_UP)));
//										 }
//										 if(step.doubleValue() == 60) {
//											 cost = cost.add(tempIdleMins.multiply(new BigDecimal("60")).multiply(idleChargePrice.divide(new BigDecimal("60"), 15, RoundingMode.HALF_UP))).setScale(10,BigDecimal.ROUND_HALF_UP);
//										 }
//										 cost = utils.decimalwithtwodecimals(cost);
//										 finalCostWithVat = finalCostWithVat.add(cost);
//										 idleChargeMap.put("idleDuration", tempIdleMins.doubleValue());
//										 idleChargeMap.put("idleCost", cost);
//									 }
									aditionalSubMap.put("idle", idleChargeMap);
									aditionalSubMap.put("tax", taxls1);
									aditionalSubMap.put("rateRider", rateRiderMap);
									costInfo.put("aditional", aditionalSubMap);
								}else {
									finalCostWithVat = finalCost;
								}
								pricesLs.add(costInfo);
								logger.info(Thread.currentThread().getId()+"finalCost : "+finalCost);
								logger.info(Thread.currentThread().getId()+"finalCostWithVat : "+finalCostWithVat);
								finalCost=finalCost.setScale(2, RoundingMode.HALF_UP);
								finalCostWithVat=finalCostWithVat.setScale(2, RoundingMode.HALF_UP);
								siv.setFinalCosttostore(finalCost.doubleValue());
								siv.setFinalCostInslcCurrency(finalCostWithVat.doubleValue());
								siv.setNeedToDebit(siv.getFinalCostInslcCurrency());
							}
							tariffMap.put("cost_info", pricesLs);
							tariffLs.add(tariffMap);
							ObjectMapper objectMapper = new ObjectMapper();
							String json = objectMapper.writeValueAsString(tariffLs);
							siv.setCost_pricings(json);
						}
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return siv;
	}
	@Override
	public session_pricings getSessesionPricing(String sessionId) {
		session_pricings sessionpricings=null;
		try {
			sessionpricings= generalDao.findOne("FROM session_pricings where sessionId='"+sessionId+"'", new session_pricings());
		}catch(Exception e) {
			e.printStackTrace();
		}
		return sessionpricings;
	}

	@Override
	public OCPPTransactionData getTxnData(OCPPStartTransaction startTxn) {
		OCPPTransactionData txnData = null;
		try {
			txnData = generalDao.findOne("FROM OCPPTransactionData where sessionId='"+startTxn.getSessionId()+"' and stop=0", new OCPPTransactionData());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return txnData;
	}
	@Override
	public OCPITransactionData getOCPITxnData(OCPPStartTransaction startTxn) {
		OCPITransactionData txnData = null;
		try {
			txnData = generalDao.findOne("FROM OCPITransactionData where sessionId='"+startTxn.getSessionId()+"' and stop=0", new OCPITransactionData());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return txnData;
	}
	public Map<String,Object> getFreeChargeMinsOfStn(Long stationId, String stnRefNo) {
		Map<String,Object> freeChargeHrs = null;
		try {
			String query = "SELECT ISNULL((select ((t.hour * 60) + (t.min) + (t.sec/60)) from Time t where t.id = s.freeCharging),0) as freeCharging,s.freeKwh FROM Station s WHERE s.id = " + stationId + "";
			List<Map<String,Object>> durationInString = generalDao.getMapData(query);
			if (durationInString == null||durationInString.isEmpty()) {
				freeChargeHrs.put("freeCharging", 0.00);
				freeChargeHrs.put("freeKwh", 0.00);
			}else {
				freeChargeHrs=durationInString.get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info(Thread.currentThread().getId()+""+stnRefNo + " , station free mins : " + freeChargeHrs);
		return freeChargeHrs;
	}

	public Boolean getMaxSessionflag(String sessionId) {
		try {
			Boolean singleRecord = generalDao.getStationMaxSessionFlag("select isNull(maxSessionFlag,0) from Session where sessionId='" + sessionId + "'");
			return Boolean.valueOf(singleRecord);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String transactionIdValidation(long transactionId, OCPPStartTransaction startTrans, String stnRfrNo, long orgId) {
		String transactionType = "InvalideTransaction";
		try {
			String query="select paymentType,rewardType from ocpp_activeTransaction where transactionId='"+transactionId+"'";
			List<Map<String, Object>> listData=executeRepository.findAll(query);
			if(listData!=null && listData.size()>0) {
				transactionType="ValideTransaction";
			}else {
				String query1="select * from ActiveAndSessionForChargingActivityData where transactionId='"+transactionId+"'";
				List<Map<String, Object>> listData1=executeRepository.findAll(query1);
				if(listData1!=null && listData1.size()>0) {
					transactionType="activeAndSessionData";
				}else {
					transactionType="InvalideTransaction";
				}
			}


			// If TransactionData Deleted in TiggerCalling Then Again Meter Values comes So
			// need to Add the Data
			if (transactionType.equalsIgnoreCase("activeAndSessionData")) {

				// This table used for Active Transaction Status for android or ios
				activeTransactions(startTrans.getConnectorId(), transactionId,
						startTrans.getStationId(), "RemoteStartTransaction", startTrans.getUserId(),
						startTrans.getIdTag(), startTrans.getSessionId(), "Normal", "Preparing", "Charging",
						startTrans.getMeterStart(), "", startTrans.getRequestedID(), stnRfrNo, "", orgId,startTrans.getPaymentType(),startTrans.getRewardType());

				transactionType = "ValideTransaction";
				deleteActiveAndSessionForChargingActivityData(startTrans.getUserId(), startTrans.getSessionId(),startTrans.getStationId(), startTrans.getConnectorId());
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return transactionType;
	}
	public Boolean getActiveTransactionId(Long TransactionId) {
		Boolean val = false;
		try {

			String query="select oa.* from ocpp_activeTransaction oa inner join ocpp_startTransaction os on os.transactionId = oa.transactionId where oa.transactionId='"+TransactionId+"' and os.transactionStatus = 'Accepted'";
			List<Map<String, Object>> listData=executeRepository.findAll(query);
			if(listData!=null && listData.size()>0) {
				val=true;
			}else {
				String query1="select * from ActiveAndSessionForChargingActivityData where transactionId='"+ TransactionId+"'";
				List<Map<String, Object>> listData1=executeRepository.findAll(query1);
				if(listData1!=null && listData1.size()>0) {
					val=true;
				}else {
					val=false;
				}
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}
	public OCPPActiveTransaction activeTransactions(long connectorId, long transactionId, long stationId, String msgType,
													Long userId, String idTag, String sessionId, String sessionStatus, String status, String statusMobile,
													double meterStart, String portalStation,String requestedID,String stnRfrNo,String appVersion,long orgId,String paymentType,String rewardType)  {
		OCPPActiveTransaction activeTransactionObj = null;
		try{
			activeTransactionObj = generalDao.findOne("From OCPPActiveTransaction where connectorId=" + connectorId + " and " + "stationId ="
					+ stationId + "and messageType='" + msgType + "' and userId =" + userId + " and Status in ('Accepted','Preparing','Charging') order by id desc", new OCPPActiveTransaction());

			if(activeTransactionObj==null) {
				OCPPActiveTransaction ocppActiveTransaction = new OCPPActiveTransaction();
				ocppActiveTransaction.setConnectorId(connectorId);
				ocppActiveTransaction.setMessageType(msgType);
				ocppActiveTransaction.setRfId(idTag);
				ocppActiveTransaction.setSessionId(sessionId);
				ocppActiveTransaction.setStationId(stationId);
				ocppActiveTransaction.setStatus(status);
				ocppActiveTransaction.setTransactionId(transactionId);
				ocppActiveTransaction.setUserId(userId);
				ocppActiveTransaction.setRequestedID(requestedID);
				ocppActiveTransaction.setOrgId(orgId);
				ocppActiveTransaction.setTimeStamp(Utils.getUTCDate());
				generalDao.save(ocppActiveTransaction);
			}else {
				//Update the Session reason to EvDisconnect If any Active Session Available without any Stop Transaction and Get again StartTransaction
				activeTransactionObj.setRfId(idTag);
				activeTransactionObj.setUserId(userId);
				activeTransactionObj.setTransactionId(transactionId);
				activeTransactionObj.setSessionId(sessionId);
				activeTransactionObj.setTimeStamp(Utils.getUTCDate());
				generalDao.update(activeTransactionObj);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return activeTransactionObj;
	}
	public void deleteActiveAndSessionForChargingActivityData(Long userId, String sessionId, long stationId, long portId) {
		try {
			String deleteActiveTrans = "delete from ActiveAndSessionForChargingActivityData where sessionid='" + sessionId + "' and userid='" + userId + "'  and connectorId = '" + portId + "';";
			executeRepository.update(deleteActiveTrans);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public Map<String,Object> accntsBeanObj(long userid){
		Map<String,Object> map = new HashMap<String, Object>();
		try {
			String query = "select a.id as accid,a.accountBalance,a.accountName,a.activeAccount,a.autoReload,a.creationDate,a.lowBalanceFlag,a.oldRefId,a.user_id,a.notificationFlag,"
					+ "isnull(a.currencyType,'USD') as currencyType,isnull(a.currencySymbol,'&#36;') as currencySymbol,u.UserId,u.email,convert(varchar,isnull((select DATEADD(HOUR,"
					+ "CAST(SUBSTRING(replace(z.utc_code,'GMT',''),1,3) as int),DATEADD(MINUTE,CAST(SUBSTRING(replace(z.utc_code,'GMT',''),5,2) as int),getutcdate())) from zone z "
					+ "where z.zone_id = p.zone_id),GETUTCDATE()), 9)  + ' ' + isnull((select z.zone_name from zone z where z.zone_id = p.zone_id),'UTC') as userTime,u.uid as uuid from accounts a "
					+ "inner join profile p on a.user_id = p.user_id inner join  Users u  on a.user_id=u.UserId where a.user_id= '"+ userid +"' ";
			List<Map<String, Object>> mapData = executeRepository.findAll(query);
			if(mapData.size() > 0) {
				map = mapData.get(0);
			}else {
				map.put("accid", "0");
				map.put("accountBalance", "0");
				map.put("accountName", "");
				map.put("activeAccount", "0");
				map.put("creationDate", "");
				map.put("lowBalanceFlag", "0");
				map.put("oldRefId", "0");
				map.put("user_id", "0");
				map.put("notificationFlag", "0");
				map.put("currencyType", "USD");
				map.put("currencySymbol", "");
				map.put("UserId", "0");
				map.put("email", "");
				map.put("userTime", "");
				map.put("uuid", "");
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	public List<Map<String, Object>> previousMeterValuesData(String sessionId, Date startTransTimeStamp,
															 BigDecimal startTransactionMeterValue, String currentImportUnit, String stnRefNo) {
		List<Map<String, Object>> listMapData = new ArrayList<>();
		try {
			String hqlQuery = "select top 1 endTimeStamp as previousMeterEndTimeStamp,s.kilowattHoursUsed as previousMeterWattsecondsused,s.sameMeterValueCount as sameMeterValueCount, s.id as sessionId,isnull(s.creationDate,GETUTCDATE()) as creationDate,cost as cost,isnull(0.00,0) as currentImportValue ,isnull(powerActiveImport_value,0) as powerActiveImportValue,isnull(atx.amtDebit,0) as amtDebit,os.kWhAlert,os.revenueAlert, os.durationAlert,'false' as isFirstMeterReading,socStartVal,socEndVal,sessionElapsedInMin,avg_power,s.paymentMode as paymentType,os.promoCodeUsedTime,isnull(0.00,0) as userProcessingFee from session s inner join ocpp_sessionData os on s.sessionId =  os.sessionId left join account_transaction atx on s.accountTransaction_id = atx.id where os.sessionId='"
					+ sessionId + "' order by os.id desc";
			listMapData = executeRepository.findAll(hqlQuery);
			if (listMapData.isEmpty()) {
				HashMap<String, Object> mapData = new HashMap<>();
				mapData.put("previousMeterEndTimeStamp", startTransTimeStamp);
				mapData.put("sameMeterValueCount", 0);
				// Need to change the startTransaction wattSecondsUsedBecause charger send Wh so need to divide with 1000
				if (currentImportUnit.equalsIgnoreCase("kWh")) {
					mapData.put("previousMeterWattsecondsused",startTransactionMeterValue.divide(new BigDecimal("1000"), 5, RoundingMode.HALF_UP) );
				} else {
					mapData.put("previousMeterWattsecondsused", startTransactionMeterValue);
				}
				mapData.put("previousMeterWattsecondsused", "0");
				mapData.put("cost", "0");
				mapData.put("amtDebit", 0.0);
				mapData.put("kWhAlert", 0);
				mapData.put("revenueAlert", 0);
				mapData.put("durationAlert", 0);
				mapData.put("powerActiveImportValue", 0);
				mapData.put("isFirstMeterReading", "true");
				mapData.put("socStartVal", 0.0);
				mapData.put("socEndVal", 0.0);
				mapData.put("sessionElapsedInMin", 0);
				mapData.put("creationDate", Utils.getUTCDateString());
				mapData.put("avg_power", 0.0);
				mapData.put("sessionId", 0);
				mapData.put("paymentType", "Wallet");
				mapData.put("promoCodeUsedTime", "0.00");
				listMapData.add(mapData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listMapData;
	}
	public OCPPSessionsData getSessionData(String sessionId) {
		try {
			OCPPSessionsData sessData = generalDao.findOne("FROM OCPPSessionsData WHERE sessionid = '" + sessionId + "'",
					new OCPPSessionsData());
			return sessData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public SessionImportedValues additionalBilling(String stationMode,SessionImportedValues sessionImportedValues, BigDecimal totalKwUsed, BigDecimal durationInMinutes) {
		/*
		 * try { BigDecimal additionalVendingPricePerUnit=new
		 * BigDecimal(String.valueOf(sessionImportedValues.
		 * getAdditionalVendingPricePerUnit())); String
		 * additionalVendingPriceUnit=sessionImportedValues.
		 * getAdditionalVendingPriceUnit(); String
		 * portPriceUnit=sessionImportedValues.getPortPriceUnit();
		 *
		 * BigDecimal totalParallelPrice=new BigDecimal("0.0"); BigDecimal duration=new
		 * BigDecimal("0.0"); if(!stationMode.equalsIgnoreCase("Freeven")) {
		 * if(additionalVendingPriceUnit.equalsIgnoreCase("kWh")&&
		 * Double.parseDouble(String.valueOf(additionalVendingPricePerUnit))>0 &&
		 * !portPriceUnit.equalsIgnoreCase("kWh")) {
		 * totalParallelPrice=totalKwUsed.multiply(additionalVendingPricePerUnit); }
		 * if(!additionalVendingPriceUnit.equalsIgnoreCase("kWh")&&
		 * Double.parseDouble(String.valueOf(additionalVendingPricePerUnit))>0 &&
		 * portPriceUnit.equalsIgnoreCase("kWh")) {
		 * if(additionalVendingPriceUnit.equalsIgnoreCase("Hr")) {
		 * duration=durationInMinutes.divide(new BigDecimal("60"), 5,
		 * RoundingMode.HALF_UP);
		 * totalParallelPrice=duration.multiply(additionalVendingPricePerUnit);
		 * }if(additionalVendingPriceUnit.equalsIgnoreCase("Min")) {
		 * totalParallelPrice=durationInMinutes.multiply(additionalVendingPricePerUnit);
		 * } }
		 *
		 * } sessionImportedValues.setAdditionalVendingPriceUnit(
		 * additionalVendingPriceUnit);
		 * sessionImportedValues.setAdditionalVendingPricePerUnit(Double.parseDouble(
		 * String.valueOf(additionalVendingPricePerUnit)));
		 * sessionImportedValues.setTotalAdditionalPrice(Double.parseDouble(String.
		 * valueOf(totalParallelPrice)));
		 *
		 * } catch (Exception e) { e.printStackTrace(); }
		 */
		return sessionImportedValues;

	}

	public List<Map<String, Object>> getPreviousSessionsValues(String randomSessionId) {
		List<Map<String, Object>> dataReturnMap = new ArrayList<Map<String, Object>>();

		try {
			String sqlQuery = "SELECT ISNULL(usedGraceTime,0) AS usedGraceTime,isnull(costOfSmeEnergy,0) as costOfSmeEnergy,ISNULL(oneTimeFeeCost,0) AS oneTimeFeeCost,"
					+ " Isnull(durationMinsofSmeEnergy,0) as durationMinsofSmeEnergy,cost FROM Session WHERE SessionId ='"
					+ randomSessionId + "'";
			dataReturnMap = executeRepository.findAll(sqlQuery);

			if (dataReturnMap.size() == 0) {
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("costOfSmeEnergy", 0);
				tempMap.put("usedGraceTime", 0);
				tempMap.put("cost", 0);
				tempMap.put("oneTimeFeeCost", 0);
				tempMap.put("durationMinsofSmeEnergy", 0.0);
				dataReturnMap.add(tempMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataReturnMap;

	}
	public List<Map<String, Object>> getPreviousSessionsEnergyValues(String randomSessionId) {
		List<Map<String, Object>> mapData = new ArrayList<Map<String, Object>>();
		try {
			String sqlQuery = "SELECT top 1 sessionId,CONVERT(varchar,endTime,120) as DataRcvdTimeStamp,"
					+ " ISNULL(EnergyCounsumptionFlag,0) AS EnergyCounsumptionFlag FROM ocpp_sessionData os WHERE os.sessionId='"
					+ randomSessionId + "' order by os.id DESC";
			mapData = executeRepository.findAll(sqlQuery);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapData;
	}

	public List<Map<String, Object>> getsumOfSameEnergyValues(String sessionId) {
		List<Map<String, Object>> mapData = new ArrayList<>();
		try {
			String query = "SELECT ISNULL(SUM(se.durationMinsOfSmeEnergy),0) AS durationMinsOfSmeEnergy ,ISNULL(SUM(se.costOfSmeEnergy),0) AS costOfSmeEnergy "
					+ " FROM SessionEnergyUse se Inner Join session s ON se.session_id = s.id WHERE EnergyCounsumptionFlag = '1' AND SessionId = '"
					+ sessionId + "'";
			mapData = executeRepository.findAll(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapData;
	}
	public SessionImportedValues RewardKwhForNormalUser(SessionImportedValues siv) {
		try {
			JsonNode rewardPrices = objectMapper.readTree(siv.getTxnData().getReward());
			if(rewardPrices!=null && rewardPrices.size()>0 && siv.getStTxnObj().getRewardType().equalsIgnoreCase("kWh")) {
				double rewardAmount = rewardPrices.get(0).get("amount").asDouble();
				double rewardkWh = rewardPrices.get(0).get("kWh").asDouble();
				double usedTime=rewardPrices.get(0).get("usedTime").asDouble();
				double usedkWh=rewardPrices.get(0).get("usedkWh").asDouble();
				String lastUpdatedTime=rewardPrices.get(0).get("lastUpdatedTime").asText();
				Map<String,Object> map = new HashMap<>();
				map.put("amount", rewardAmount);
				map.put("kWh", rewardkWh);
				map.put("usedTime", usedTime);
				map.put("usedkWh", usedkWh);
				map.put("lastUpdatedTime", lastUpdatedTime);
				if(rewardkWh >= Double.parseDouble(String.valueOf(siv.getTotalKwUsed()))) {
					siv.setBillTotalKwUsed(new BigDecimal("0.0"));
					siv.setBillSessionDuration(new BigDecimal("0.0"));
					map.put("usedTime", siv.getSessionDuration());
					map.put("usedkWh", siv.getTotalKwUsed());
				}else if(rewardkWh < Double.parseDouble(String.valueOf(siv.getTotalKwUsed()))) {
					map=getSessionElaspedDataForPromoCode(map,siv);
					map.put("usedkWh", rewardkWh);
					siv.setBillSessionDuration(siv.getSessionDuration().subtract(new BigDecimal(String.valueOf(map.get("usedTime")))));
					siv.setBillTotalKwUsed(siv.getTotalKwUsed().subtract(new BigDecimal(String.valueOf(rewardkWh))).setScale(4,RoundingMode.HALF_UP));
				}
				map.put("lastUpdatedTime", utils.stringToDate(siv.getMeterValueTimeStatmp()));
				siv.setBillSessionDuration(Double.parseDouble(String.valueOf(siv.getBillSessionDuration()))< 0 ? new BigDecimal("0") : siv.getBillSessionDuration());
				siv.setBillTotalKwUsed(Double.parseDouble(String.valueOf(siv.getBillTotalKwUsed()))< 0 ? new BigDecimal("0") : siv.getBillTotalKwUsed());
				List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
				list.add(map);
				siv.getTxnData().setReward(objectMapper.writeValueAsString(list));

				BigDecimal remainingkWh=new BigDecimal(String.valueOf(rewardkWh)).subtract(new BigDecimal(String.valueOf(map.get("usedkWh")))).setScale(4,RoundingMode.HALF_UP);
				String update = "update promoCode_reward set kWh='"+remainingkWh+"' where userId="+Long.parseLong(String.valueOf(siv.getUserObj().get("UserId")));
				executeRepository.update(update);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return siv;
	}
	public SessionImportedValues RewardAmountForNormalUser(SessionImportedValues siv) {
		try {
			JsonNode rewardPrices = objectMapper.readTree(siv.getTxnData().getReward());
			siv.setNeedToDebit(siv.getFinalCostInslcCurrency());
			logger.info(Thread.currentThread().getId()+"rewardPrices.size : "+rewardPrices.size()+" , siv.getStTxnObj().getRewardType() : "+siv.getStTxnObj().getRewardType());
			if(rewardPrices!=null && rewardPrices.size()>0 && siv.getStTxnObj().getRewardType().equalsIgnoreCase("Amount")) {
				double rewardAmount = rewardPrices.get(0).get("amount").asDouble();
				double rewardkWh = rewardPrices.get(0).get("kWh").asDouble();
				double usedTime=rewardPrices.get(0).get("usedTime").asDouble();
				double usedkWh=rewardPrices.get(0).get("usedkWh").asDouble();
				String lastUpdatedTime=rewardPrices.get(0).get("lastUpdatedTime").asText();
				Map<String,Object> map = new HashMap<>();
				map.put("amount", rewardAmount);
				map.put("kWh", rewardkWh);
				map.put("usedTime", usedTime);
				map.put("usedkWh", usedkWh);
				map.put("lastUpdatedTime", lastUpdatedTime);
				double remainingAmt=rewardAmount;
				if(siv.getStTxnObj().getRewardType().equalsIgnoreCase("Amount")&&siv.getFinalCostInslcCurrency()<=rewardAmount) {
					siv.setNeedToDebit(0.0);
//					remainingAmt=rewardAmount-siv.getFinalCostInslcCurrency();
					remainingAmt=new BigDecimal(String.valueOf(rewardAmount)).subtract(new BigDecimal(String.valueOf(siv.getFinalCostInslcCurrency()))).setScale(2,RoundingMode.HALF_UP).doubleValue();
					map.put("usedAmount", siv.getFinalCostInslcCurrency());
					siv.setFinalCostInslcCurrency(0.0);
				}else if(siv.getStTxnObj().getRewardType().equalsIgnoreCase("Amount")&& siv.getFinalCostInslcCurrency()>rewardAmount){
					siv.setNeedToDebit(new BigDecimal(String.valueOf(siv.getFinalCostInslcCurrency())).subtract(new BigDecimal(String.valueOf(rewardAmount))).setScale(2,RoundingMode.HALF_UP).doubleValue());
					siv.setFinalCostInslcCurrency(new BigDecimal(String.valueOf(siv.getFinalCostInslcCurrency())).subtract(new BigDecimal(String.valueOf(rewardAmount))).setScale(2,RoundingMode.HALF_UP).doubleValue());
					remainingAmt=0;
					map.put("usedAmount", rewardAmount);
				}
				map.put("lastUpdatedTime", utils.stringToDate(siv.getMeterValueTimeStatmp()));

				List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
				list.add(map);
				siv.getTxnData().setReward(objectMapper.writeValueAsString(list));

				String update = "update promoCode_reward set amount='"+remainingAmt+"' where userId="+Long.parseLong(String.valueOf(siv.getUserObj().get("UserId")));
				executeRepository.update(update);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return siv;
	}
	public Map<String,Object> getSessionElaspedDataForPromoCode(Map<String,Object> map,SessionImportedValues siv){
		try {
			if(Double.parseDouble(String.valueOf(map.get("usedkWh")))<Double.parseDouble(String.valueOf(map.get("kWh")))) {
				Map<String, BigDecimal> timeDifferenceInMilisecondsForStart_MesterStart = utils.getTimeDifferenceInMiliSec(utils.stringToDate(String.valueOf(map.get("lastUpdatedTime"))),siv.getMeterValueTimeStatmp());
				BigDecimal sessionElapsedMinutesInStartMeterTimeStamp =new BigDecimal(String.valueOf(timeDifferenceInMilisecondsForStart_MesterStart.get("Minutes")));
				BigDecimal firstHalf = sessionElapsedMinutesInStartMeterTimeStamp.divide(new BigDecimal("2"), 9, RoundingMode.HALF_UP);
				map.put("usedTime", firstHalf.add(new BigDecimal(String.valueOf(map.get("usedTime")))));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public SessionImportedValues freeChargingForDriverGrp(SessionImportedValues siv) {
		try {
			JsonNode freePrices = objectMapper.readTree(siv.getTxnData().getFree_prices());
			if(freePrices.size() > 0) {
				logger.info(Thread.currentThread().getId()+"freePrices : "+freePrices);
				logger.info("Starting freeChargingForDriverGrp - Input free_prices: " + freePrices);
				// Current free pricing limits
				double freeKwh = freePrices.get(0).get("freeKwh").asDouble();
				double freeMins = freePrices.get(0).get("freeMins").asDouble();
				double usedFreeKwh = 0.00;
				double usedFreeMins = 0.00;
				double remainingKwh = 0.00;
				double remainingMins = 0.00;
				double originalFreeKwh = 0.00;
				double originalFreeMins = 0.00;
				double usedAdditionalKwh = 0.00;
				double usedAdditionalMins = 0.00;
				double additionalFreeKwh = 0.00;
				double additionalFreeMins = 0.00;

				String chargeSessionId = siv.getChargeSessUniqId();
				System.err.println(chargeSessionId);

				String getCurrentSessionSql = "SELECT last_billable_kwh, last_billable_duration " +
						"FROM ocpp_sessionBillableData " +
						"WHERE charge_session_id = '" + chargeSessionId + "'";

				List<Map<String, Object>> currentSessionValues = generalDao.getMapData(getCurrentSessionSql);

				BigDecimal lastBillableKwh = null;
				BigDecimal lastBillableDuration = null;

				if (!currentSessionValues.isEmpty()) {
					Map<String, Object> sessionValues = currentSessionValues.get(0);
					lastBillableKwh = sessionValues.get("last_billable_kwh") != null ?
							new BigDecimal(String.valueOf(sessionValues.get("last_billable_kwh"))) : null;
					lastBillableDuration = sessionValues.get("last_billable_duration") != null ?
							new BigDecimal(String.valueOf(sessionValues.get("last_billable_duration"))) : null;
				} else {
					String insertSql = "INSERT INTO ocpp_sessionBillableData " +
							"(charge_session_id, created_date, updated_date, original_free_kwh, original_free_mins, " +
							"used_additional_kwh, used_additional_mins, additional_free_kwh, additional_free_mins) " +
							"VALUES ('" + chargeSessionId + "', '"+utils.getDate(siv.getMeterValueTimeStatmp())+"', '"+utils.getDate(siv.getMeterValueTimeStatmp())+"', '0', '0', '0', '0', '0', '0')";
					executeRepository.update(insertSql);
				}

				logger.info("Initial setup - freeKwh: " + freeKwh + ", freeMins: " + freeMins);

				String str = "select top 1 id,createdDate,userId,usedFreeMins,usedFreekWhs,remainingFreeKwh,remainingFreeMins " +
						"From freeChargingForDriverGrp Where userId='" + siv.getUserObj().get("UserId").asLong() +
						"' and createdDate='" + utils.getDate(siv.getMeterValueTimeStatmp()) + "' order by id desc";

				List<Map<String, Object>> fcdg = generalDao.getMapData(str);

				if(fcdg.size() > 0) {

					usedFreeKwh = Double.valueOf(String.valueOf(fcdg.get(0).get("usedFreekWhs")));
					usedFreeMins = Double.valueOf(String.valueOf(fcdg.get(0).get("usedFreeMins")));

					// Get first transaction's original values and current session's values
					String getOriginalValuesSql = "SELECT TOP 1 osb.original_free_kwh, osb.original_free_mins, " +
							"osb.used_additional_kwh, osb.used_additional_mins " +
							"FROM ocpp_sessionBillableData osb " +
							"INNER JOIN freeChargingForDriverGrp fcdg ON fcdg.userId = '" + siv.getUserObj().get("UserId").asLong() + "' " +
							"AND CAST(fcdg.createdDate AS DATE) = '" + utils.getDate(siv.getMeterValueTimeStatmp()) + "' " +
							"WHERE CAST(osb.created_date AS DATE) = '" + utils.getDate(siv.getMeterValueTimeStatmp()) + "' " +
							"ORDER BY osb.created_date ASC";

					List<Map<String, Object>> originalValuesList = generalDao.getMapData(getOriginalValuesSql);

					if (!originalValuesList.isEmpty()) {
						Map<String, Object> originalValues = originalValuesList.get(0);
						originalFreeKwh = originalValues.get("original_free_kwh") != null ?
								((BigDecimal)originalValues.get("original_free_kwh")).doubleValue() : 0.00;
						originalFreeMins = originalValues.get("original_free_mins") != null ?
								((BigDecimal)originalValues.get("original_free_mins")).doubleValue() : 0.00;
						usedAdditionalKwh = originalValues.get("used_additional_kwh") != null ?
								((BigDecimal)originalValues.get("used_additional_kwh")).doubleValue() : 0.00;
						usedAdditionalMins = originalValues.get("used_additional_mins") != null ?
								((BigDecimal)originalValues.get("used_additional_mins")).doubleValue() : 0.00;
					}

					logger.info("Retrieved original values - kWh: " + originalFreeKwh + ", mins: " + originalFreeMins);
					logger.info("Retrieved used additional - kWh: " + usedAdditionalKwh + ", mins: " + usedAdditionalMins);

//					// Calculate new additional based on difference from original
//					additionalFreeKwh = Math.max(0, freeKwh - originalFreeKwh);
//					additionalFreeMins = Math.max(0, freeMins - originalFreeMins);
//
//					// Update additional values
//					String updateAdditionalValues = "UPDATE ocpp_sessionBillableData SET " +
//							"additional_free_kwh = '" + additionalFreeKwh + "', " +
//							"additional_free_mins = '" + additionalFreeMins + "', " +
//							"updated_date = '"+utils.getDate(siv.getMeterValueTimeStatmp())+"' " +
//							"WHERE charge_session_id = '" + chargeSessionId + "'";
//					executeRepository.update(updateAdditionalValues);

					// Calculate new additional based on difference from original, accounting for what's already been used
					double totalAdditionalFreeKwh = Math.max(0, freeKwh - originalFreeKwh);
					double totalAdditionalFreeMins = Math.max(0, freeMins - originalFreeMins);

					// Get total used additional across all sessions for this user today
					String totalUsedAdditionalQuery =
							"SELECT SUM(used_additional_kwh) as total_used_additional_kwh, " +
									"SUM(used_additional_mins) as total_used_additional_mins " +
									"FROM session s " +
									"LEFT JOIN ocpp_sessionBillableData osb ON s.sessionId = osb.charge_session_id " +
									"WHERE s.userId = '" + siv.getUserObj().get("UserId").asLong() + "' " +
									"AND CAST(s.startTimeStamp AS DATE) = '" + utils.getDate(siv.getMeterValueTimeStatmp()) + "'";

					List<Map<String, Object>> totalUsedAdditionalData = generalDao.getMapData(totalUsedAdditionalQuery);
					double totalUsedAdditionalKwh = 0;
					double totalUsedAdditionalMins = 0;

					if (!totalUsedAdditionalData.isEmpty()) {
						if (totalUsedAdditionalData.get(0).get("total_used_additional_kwh") != null) {
							totalUsedAdditionalKwh = Double.valueOf(String.valueOf(totalUsedAdditionalData.get(0).get("total_used_additional_kwh")));
						}
						if (totalUsedAdditionalData.get(0).get("total_used_additional_mins") != null) {
							totalUsedAdditionalMins = Double.valueOf(String.valueOf(totalUsedAdditionalData.get(0).get("total_used_additional_mins")));
						}
					}

					// Calculate available additional resources for this session
					double availableAdditionalFreeKwh = Math.max(0, totalAdditionalFreeKwh - totalUsedAdditionalKwh);
					double availableAdditionalFreeMins = Math.max(0, totalAdditionalFreeMins - totalUsedAdditionalMins);

					// Set these values for use in the rest of the function
					additionalFreeKwh = availableAdditionalFreeKwh;
					additionalFreeMins = availableAdditionalFreeMins;

					// Update additional values with what's actually available
					String updateAdditionalValues = "UPDATE ocpp_sessionBillableData SET " +
							"additional_free_kwh = '" + availableAdditionalFreeKwh + "', " +
							"additional_free_mins = '" + availableAdditionalFreeMins + "', " +
							"updated_date = '"+utils.getDate(siv.getMeterValueTimeStatmp())+"' " +
							"WHERE charge_session_id = '" + chargeSessionId + "'";
					executeRepository.update(updateAdditionalValues);

					// Calculate how much of original was used
					double usedFromOriginalKwh = Math.min(usedFreeKwh, originalFreeKwh);
					double usedFromOriginalMins = Math.min(usedFreeMins, originalFreeMins);

					// If total usage exceeds original capacity, the excess is from additional
					double newUsedAdditionalKwh = 0;
					double newUsedAdditionalMins = 0;

					if (usedFreeKwh > originalFreeKwh) {
						newUsedAdditionalKwh = usedFreeKwh - originalFreeKwh;
					}
					if (usedFreeMins > originalFreeMins) {
						newUsedAdditionalMins = usedFreeMins - originalFreeMins;
					}

//					String updateUsedAdditional = "UPDATE ocpp_sessionBillableData SET " +
//							"used_additional_kwh = '" + newUsedAdditionalKwh + "', " +
//							"used_additional_mins = '" + newUsedAdditionalMins + "', " +
//							"updated_date = '"+utils.getDate(siv.getMeterValueTimeStatmp())+"' " +
//							"WHERE charge_session_id = (SELECT TOP 1 charge_session_id FROM ocpp_sessionBillableData osb " +
//							"WHERE CAST(created_date AS DATE) = '" + utils.getDate(siv.getMeterValueTimeStatmp()) + "' " +
//							"ORDER BY created_date ASC)";

					String updateUsedAdditional = "UPDATE ocpp_sessionBillableData SET " +
							"used_additional_kwh = '" + newUsedAdditionalKwh + "', " +
							"used_additional_mins = '" + newUsedAdditionalMins + "', " +
							"updated_date = '"+utils.getDate(siv.getMeterValueTimeStatmp())+"' " +
							"WHERE charge_session_id = '" + chargeSessionId + "'";
					executeRepository.update(updateUsedAdditional);

					// Calculate remaining additional
					double remainingAdditionalKwh;
					double remainingAdditionalMins;

					// Calculate remaining
					remainingKwh = Math.max(0, (originalFreeKwh - usedFromOriginalKwh) +
							(additionalFreeKwh - newUsedAdditionalKwh));
					remainingMins = Math.max(0, (originalFreeMins - usedFromOriginalMins) +
							(additionalFreeMins - newUsedAdditionalMins));

					logger.info("Usage breakdown - Original kWh: used " + usedFromOriginalKwh + " of " + originalFreeKwh);
					logger.info("Usage breakdown - Original mins: used " + usedFromOriginalMins + " of " + originalFreeMins);
					logger.info("Usage breakdown - Additional kWh: used " + newUsedAdditionalKwh + " of " + additionalFreeKwh);
					logger.info("Usage breakdown - Additional mins: used " + newUsedAdditionalMins + " of " + additionalFreeMins);
					logger.info("Remaining - Total kWh: " + remainingKwh + ", Total mins: " + remainingMins);

					BigDecimal previousKwhUsed = new BigDecimal(String.valueOf(siv.getPreviousSessionData().get("kilowattHoursUsed")));
					BigDecimal incrementalKwhUsed = siv.getBillTotalKwUsed().subtract(previousKwhUsed);
					logger.info("kWh calculation - previousKwhUsed: " + previousKwhUsed +
							", currentTotal: " + siv.getBillTotalKwUsed() +
							", incrementalKwhUsed: " + incrementalKwhUsed);

					if(siv.getBillTotalKwUsed().compareTo(previousKwhUsed) > 0) {
						if (incrementalKwhUsed.doubleValue() >= 0) {
							if (remainingKwh >= incrementalKwhUsed.doubleValue()) {
								logger.info("Case 1: Full coverage by remaining free kWh");

								// Get current session's already used free kWh
								String currentSessionUsageQuery =
										"SELECT ISNULL(used_free_kwh, 0) as session_free_kwh " +
												"FROM ocpp_sessionBillableData " +
												"WHERE charge_session_id = '" + chargeSessionId + "'";
								List<Map<String, Object>> currentSessionUsage = generalDao.getMapData(currentSessionUsageQuery);
								double sessionAlreadyUsedFreeKwh = Double.valueOf(String.valueOf(currentSessionUsage.get(0).get("session_free_kwh")));

								// Calculate new total used
								double newSessionFreeKwhUsed = sessionAlreadyUsedFreeKwh + incrementalKwhUsed.doubleValue();

								remainingKwh = remainingKwh - incrementalKwhUsed.doubleValue();
								usedFreeKwh = usedFreeKwh + incrementalKwhUsed.doubleValue();
								logger.info("After full coverage - remainingKwh: " + remainingKwh +
										", usedFreeKwh: " + usedFreeKwh +
										", sessionFreeKwhUsed: " + newSessionFreeKwhUsed);
								siv.setBillTotalKwUsed(new BigDecimal(0.00));

								// Update session tracking
								String updateKwhSql = "UPDATE ocpp_sessionBillableData " +
										"SET last_billable_kwh = '0.00', " +
										"used_free_kwh = '" + newSessionFreeKwhUsed + "', " +
										"updated_date = '"+utils.getDate(siv.getMeterValueTimeStatmp())+"' " +
										"WHERE charge_session_id = '" + chargeSessionId + "'";
								executeRepository.update(updateKwhSql);

							} else if (remainingKwh > 0) {
								logger.info("Case 2: Partial coverage by remaining free kWh");

								// Get current session's already used free kWh
								String currentSessionUsageQuery =
										"SELECT ISNULL(used_free_kwh, 0) as session_free_kwh " +
												"FROM ocpp_sessionBillableData " +
												"WHERE charge_session_id = '" + chargeSessionId + "'";
								List<Map<String, Object>> currentSessionUsage = generalDao.getMapData(currentSessionUsageQuery);
								double sessionAlreadyUsedFreeKwh = Double.valueOf(String.valueOf(currentSessionUsage.get(0).get("session_free_kwh")));

								// Calculate new total used
								double newSessionFreeKwhUsed = sessionAlreadyUsedFreeKwh + remainingKwh;

								usedFreeKwh = usedFreeKwh + remainingKwh;
								BigDecimal billableKwh = incrementalKwhUsed.subtract(new BigDecimal(remainingKwh));
								siv.setBillTotalKwUsed(billableKwh.setScale(4, RoundingMode.HALF_UP));
								remainingKwh = 0.00;

								// Update session tracking
								String updateKwhSql = "UPDATE ocpp_sessionBillableData " +
										"SET last_billable_kwh = '" + siv.getBillTotalKwUsed() + "', " +
										"used_free_kwh = '" + newSessionFreeKwhUsed + "', " +
										"updated_date = '"+utils.getDate(siv.getMeterValueTimeStatmp())+"' " +
										"WHERE charge_session_id = '" + chargeSessionId + "'";
								executeRepository.update(updateKwhSql);

							} else {
								logger.info("Case 3: No free kWh remaining");
								BigDecimal totalUsage = siv.getBillTotalKwUsed();

								// Get current session's already used free kWh
								String currentSessionUsageQuery =
										"SELECT ISNULL(used_free_kwh, 0) as session_free_kwh " +
												"FROM ocpp_sessionBillableData " +
												"WHERE charge_session_id = '" + chargeSessionId + "'";
								List<Map<String, Object>> currentSessionUsage = generalDao.getMapData(currentSessionUsageQuery);
								double sessionAlreadyUsedFreeKwh = Double.valueOf(String.valueOf(currentSessionUsage.get(0).get("session_free_kwh")));

								// Get total free kWh used across all sessions
								String totalUsageQuery =
										"SELECT SUM(used_free_kwh) as total_free_kwh " +
												"FROM session s " +
												"LEFT JOIN ocpp_sessionBillableData osb ON s.sessionId = osb.charge_session_id " +
												"WHERE s.userId = '" + siv.getUserObj().get("UserId").asLong() + "' " +
												"AND CAST(s.startTimeStamp AS DATE) = '" + utils.getDate(siv.getMeterValueTimeStatmp()) + "'";
								List<Map<String, Object>> totalUsageData = generalDao.getMapData(totalUsageQuery);
								double totalFreeKwhUsed = 0;
								if (!totalUsageData.isEmpty() && totalUsageData.get(0).get("total_free_kwh") != null) {
									totalFreeKwhUsed = Double.valueOf(String.valueOf(totalUsageData.get(0).get("total_free_kwh")));
								}

								// Calculate total free allocation and this session's available allocation
								double totalFreeKwhAllocation = originalFreeKwh + additionalFreeKwh;

								// CRITICAL FIX: Calculate how much of the free resources this session can claim
								// This is the min of: the session's current usage or what's left in the free pool for this session
								double availableForThisSession = Math.min(
										totalUsage.doubleValue(),
										Math.max(0, totalFreeKwhAllocation - (totalFreeKwhUsed - sessionAlreadyUsedFreeKwh))
								);

								// Calculate billable amount (total minus this session's free allocation)
								BigDecimal billableKwh = totalUsage.subtract(BigDecimal.valueOf(availableForThisSession));
								if (billableKwh.compareTo(BigDecimal.ZERO) < 0) {
									billableKwh = BigDecimal.ZERO;
								}

								logger.info("Corrected billing - totalUsage: " + totalUsage +
										", totalFreeAllocation: " + totalFreeKwhAllocation +
										", totalUsed: " + totalFreeKwhUsed +
										", sessionAlreadyUsed: " + sessionAlreadyUsedFreeKwh +
										", availableForSession: " + availableForThisSession +
										", billableKwh: " + billableKwh);

								siv.setBillTotalKwUsed(billableKwh.setScale(4, RoundingMode.HALF_UP));

								// Update session tracking - store how much free kWh this session has used
								String updateKwhSql = "UPDATE ocpp_sessionBillableData SET " +
										"used_free_kwh = '" + availableForThisSession + "', " +
										"last_billable_kwh = '" + siv.getBillTotalKwUsed() + "', " +
										"updated_date = '"+utils.getDate(siv.getMeterValueTimeStatmp())+"' " +
										"WHERE charge_session_id = '" + chargeSessionId + "'";
								executeRepository.update(updateKwhSql);
							}
						}
					} else {
						logger.info("Invalid meter value - less than or equal to previous reading");
						if (lastBillableKwh != null) {
							siv.setBillTotalKwUsed(lastBillableKwh);
						}
					}

					BigDecimal previousDuration = new BigDecimal(String.valueOf(siv.getPreviousSessionData().get("sessionElapsedInMin")));
					BigDecimal incrementalDuration = siv.getBillSessionDuration().subtract(previousDuration);
					logger.info("mins calculation - previousDuration: " + previousDuration +
							", currentTotal: " + siv.getBillSessionDuration() +
							", incrementalDuration: " + incrementalDuration);

					if(siv.getBillSessionDuration().compareTo(previousDuration) > 0) {
						if(incrementalDuration.doubleValue() > 0) {
							if (remainingMins >= incrementalDuration.doubleValue()) {
								logger.info("Case 1: Full coverage by remaining free mins");

								// Get current session's already used free minutes
								String currentSessionUsageQuery =
										"SELECT ISNULL(used_free_mins, 0) as session_free_mins " +
												"FROM ocpp_sessionBillableData " +
												"WHERE charge_session_id = '" + chargeSessionId + "'";
								List<Map<String, Object>> currentSessionUsage = generalDao.getMapData(currentSessionUsageQuery);
								double sessionAlreadyUsedFreeMins = Double.valueOf(String.valueOf(currentSessionUsage.get(0).get("session_free_mins")));

								// Calculate new total used
								double newSessionFreeMinsUsed = sessionAlreadyUsedFreeMins + incrementalDuration.doubleValue();

								remainingMins = remainingMins - incrementalDuration.doubleValue();
								usedFreeMins = usedFreeMins + incrementalDuration.doubleValue();
								logger.info("After full coverage - remainingMins: " + remainingMins +
										", usedFreeMins: " + usedFreeMins +
										", sessionFreeMinsUsed: " + newSessionFreeMinsUsed);
								siv.setBillSessionDuration(new BigDecimal(0.00));

								// Update session tracking
								String updateFreeMinsql = "UPDATE ocpp_sessionBillableData SET " +
										"used_free_mins = '" + newSessionFreeMinsUsed + "', " +
										"last_billable_duration = '0.00', " +
										"updated_date = '"+utils.getDate(siv.getMeterValueTimeStatmp())+"' " +
										"WHERE charge_session_id = '" + chargeSessionId + "'";
								executeRepository.update(updateFreeMinsql);

							} else if (remainingMins > 0) {
								logger.info("Case 2: Partial coverage by remaining free mins");

								// Get current session's already used free minutes
								String currentSessionUsageQuery =
										"SELECT ISNULL(used_free_mins, 0) as session_free_mins " +
												"FROM ocpp_sessionBillableData " +
												"WHERE charge_session_id = '" + chargeSessionId + "'";
								List<Map<String, Object>> currentSessionUsage = generalDao.getMapData(currentSessionUsageQuery);
								double sessionAlreadyUsedFreeMins = Double.valueOf(String.valueOf(currentSessionUsage.get(0).get("session_free_mins")));

								// Calculate new total used
								double newSessionFreeMinsUsed = sessionAlreadyUsedFreeMins + remainingMins;

								usedFreeMins = usedFreeMins + remainingMins;
								BigDecimal billableDuration = incrementalDuration.subtract(new BigDecimal(remainingMins));
								siv.setBillSessionDuration(billableDuration.setScale(4, RoundingMode.HALF_UP));
								remainingMins = 0.00;

								// Update session tracking
								String updateFreeMinsql = "UPDATE ocpp_sessionBillableData SET " +
										"used_free_mins = '" + newSessionFreeMinsUsed + "', " +
										"last_billable_duration = '" + siv.getBillSessionDuration() + "', " +
										"updated_date = '"+utils.getDate(siv.getMeterValueTimeStatmp())+"' " +
										"WHERE charge_session_id = '" + chargeSessionId + "'";
								executeRepository.update(updateFreeMinsql);

							} else {
								logger.info("Case 3: No free minutes remaining");
								BigDecimal totalDuration = siv.getBillSessionDuration();

								// Get current session's already used free minutes
								String currentSessionUsageQuery =
										"SELECT ISNULL(used_free_mins, 0) as session_free_mins " +
												"FROM ocpp_sessionBillableData " +
												"WHERE charge_session_id = '" + chargeSessionId + "'";
								List<Map<String, Object>> currentSessionUsage = generalDao.getMapData(currentSessionUsageQuery);
								double sessionAlreadyUsedFreeMins = Double.valueOf(String.valueOf(currentSessionUsage.get(0).get("session_free_mins")));

								// Get total free minutes used across all sessions
								String totalUsageQuery =
										"SELECT SUM(used_free_mins) as total_free_mins " +
												"FROM session s " +
												"LEFT JOIN ocpp_sessionBillableData osb ON s.sessionId = osb.charge_session_id " +
												"WHERE s.userId = '" + siv.getUserObj().get("UserId").asLong() + "' " +
												"AND CAST(s.startTimeStamp AS DATE) = '" + utils.getDate(siv.getMeterValueTimeStatmp()) + "'";
								List<Map<String, Object>> totalUsageData = generalDao.getMapData(totalUsageQuery);
								double totalFreeMinutesUsed = 0;
								if (!totalUsageData.isEmpty() && totalUsageData.get(0).get("total_free_mins") != null) {
									totalFreeMinutesUsed = Double.valueOf(String.valueOf(totalUsageData.get(0).get("total_free_mins")));
								}

								// Calculate total free allocation and this session's available allocation
								double totalFreeMinutesAllocation = originalFreeMins + additionalFreeMins;

								// CRITICAL FIX: Calculate how much of the free resources this session can claim
								// This is the min of: the session's current usage or what's left in the free pool for this session
								double availableForThisSession = Math.min(
										totalDuration.doubleValue(),
										Math.max(0, totalFreeMinutesAllocation - (totalFreeMinutesUsed - sessionAlreadyUsedFreeMins))
								);

								// Calculate billable amount (total minus this session's free allocation)
								BigDecimal billableDuration = totalDuration.subtract(BigDecimal.valueOf(availableForThisSession));
								if (billableDuration.compareTo(BigDecimal.ZERO) < 0) {
									billableDuration = BigDecimal.ZERO;
								}

								logger.info("Corrected billing - totalDuration: " + totalDuration +
										", totalFreeAllocation: " + totalFreeMinutesAllocation +
										", totalUsed: " + totalFreeMinutesUsed +
										", sessionAlreadyUsed: " + sessionAlreadyUsedFreeMins +
										", availableForSession: " + availableForThisSession +
										", billableDuration: " + billableDuration);

								siv.setBillSessionDuration(billableDuration.setScale(4, RoundingMode.HALF_UP));

								// Update session tracking - store how much free minutes this session has used
								String updateMinsql = "UPDATE ocpp_sessionBillableData SET " +
										"used_free_mins = '" + availableForThisSession + "', " +
										"last_billable_duration = '" + siv.getBillSessionDuration() + "', " +
										"updated_date = '"+utils.getDate(siv.getMeterValueTimeStatmp())+"' " +
										"WHERE charge_session_id = '" + chargeSessionId + "'";
								executeRepository.update(updateMinsql);
							}
						}
					} else {
						logger.info("Invalid duration - less than or equal to previous reading");
						if (lastBillableDuration != null) {
							siv.setBillSessionDuration(lastBillableDuration);
						}
					}

					executeRepository.update("update freeChargingForDriverGrp set usedFreeMins='" + usedFreeMins +
							"', usedFreekWhs='" + usedFreeKwh + "', remainingFreeKwh='" + remainingKwh +
							"', remainingFreeMins='" + remainingMins + "' where id=" + fcdg.get(0).get("id"));

				} else {
					// First transaction of the day

					String updateSql = "UPDATE ocpp_sessionBillableData SET " +
							"original_free_kwh = '" + freeKwh + "', " +
							"original_free_mins = '" + freeMins + "', " +
							"used_additional_kwh = '0', " +
							"used_additional_mins = '0', " +
							"additional_free_kwh = '0', " +
							"additional_free_mins = '0', " +
							"updated_date = '"+utils.getDate(siv.getMeterValueTimeStatmp())+"' " +
							"WHERE charge_session_id = '" + chargeSessionId + "'";
					executeRepository.update(updateSql);
					logger.info("Set original values - kWh: " + freeKwh + ", mins: " + freeMins);

					// Initialize usage values for first transaction
					originalFreeKwh = freeKwh;
					originalFreeMins = freeMins;
					double usedFromOriginalKwh = 0.00;
					double usedFromOriginalMins = 0.00;
					double newUsedAdditionalKwh = 0.00;
					double newUsedAdditionalMins = 0.00;
					additionalFreeKwh = 0.00;
					additionalFreeMins = 0.00;

					remainingKwh = freeKwh;
					remainingMins = freeMins;

					// Add logger statements
					logger.info("Usage breakdown - Original kWh: used " + usedFromOriginalKwh + " of " + originalFreeKwh);
					logger.info("Usage breakdown - Original mins: used " + usedFromOriginalMins + " of " + originalFreeMins);
					logger.info("Usage breakdown - Additional kWh: used " + newUsedAdditionalKwh + " of " + additionalFreeKwh);
					logger.info("Usage breakdown - Additional mins: used " + newUsedAdditionalMins + " of " + additionalFreeMins);
					logger.info("Remaining - Total kWh: " + remainingKwh + ", Total mins: " + remainingMins);

					BigDecimal incrementalKwhUsed = siv.getBillTotalKwUsed();

					if(incrementalKwhUsed.doubleValue() > 0) {
						if(freeKwh >= incrementalKwhUsed.doubleValue()) {
							remainingKwh = freeKwh - incrementalKwhUsed.doubleValue();
							usedFreeKwh = incrementalKwhUsed.doubleValue();
							usedFromOriginalKwh = usedFreeKwh; // Update for logging
							siv.setBillTotalKwUsed(new BigDecimal(0.00));
						} else {
							usedFreeKwh = freeKwh;
							usedFromOriginalKwh = freeKwh; // Update for logging
							remainingKwh = 0.00;
							BigDecimal totalUsage = siv.getBillTotalKwUsed();
							BigDecimal freeAllocation = new BigDecimal(freeKwh);
							BigDecimal billableKwh = totalUsage.subtract(freeAllocation);
							siv.setBillTotalKwUsed(billableKwh.setScale(4, RoundingMode.HALF_UP));
						}
						String updateKwhSql = "UPDATE ocpp_sessionBillableData " +
								"SET last_billable_kwh = '" + siv.getBillTotalKwUsed() + "', " +
								"used_free_kwh = '" + usedFreeKwh + "', " +
								"updated_date = '"+utils.getDate(siv.getMeterValueTimeStatmp())+"' " +
								"WHERE charge_session_id = '" + chargeSessionId + "'";
						executeRepository.update(updateKwhSql);

						// Add updated logger statements after processing
						logger.info("Usage breakdown after kWh processing - Original kWh: used " + usedFromOriginalKwh + " of " + originalFreeKwh);
						logger.info("Remaining kWh after processing: " + remainingKwh);
					}

					BigDecimal incrementalDuration = siv.getBillSessionDuration();
					if(incrementalDuration.doubleValue() > 0) {
						if(freeMins >= incrementalDuration.doubleValue()) {
							remainingMins = freeMins - incrementalDuration.doubleValue();
							usedFreeMins = incrementalDuration.doubleValue();
							usedFromOriginalMins = usedFreeMins; // Update for logging
							siv.setBillSessionDuration(new BigDecimal(0.00));
						} else {
							usedFreeMins = freeMins;
							usedFromOriginalMins = freeMins; // Update for logging
							remainingMins = 0.00;
							siv.setBillSessionDuration(incrementalDuration.subtract(new BigDecimal(freeMins))
									.setScale(4, RoundingMode.HALF_UP));
						}
						String updateDurationSql = "UPDATE ocpp_sessionBillableData " +
								"SET last_billable_duration = '" + siv.getBillSessionDuration() + "', " +
								"used_free_mins = '" + usedFreeMins + "', " +
								"updated_date = '"+utils.getDate(siv.getMeterValueTimeStatmp())+"' " +
								"WHERE charge_session_id = '" + chargeSessionId + "'";
						executeRepository.update(updateDurationSql);

						// Add updated logger statements after processing
						logger.info("Usage breakdown after minutes processing - Original mins: used " + usedFromOriginalMins + " of " + originalFreeMins);
						logger.info("Remaining mins after processing: " + remainingMins);
					}

					// Final logger statements
					logger.info("Usage breakdown - Final - Original kWh: used " + usedFromOriginalKwh + " of " + originalFreeKwh);
					logger.info("Usage breakdown - Final - Original mins: used " + usedFromOriginalMins + " of " + originalFreeMins);
					logger.info("Usage breakdown - Final - Additional kWh: used " + newUsedAdditionalKwh + " of " + additionalFreeKwh);
					logger.info("Usage breakdown - Final - Additional mins: used " + newUsedAdditionalMins + " of " + additionalFreeMins);
					logger.info("Remaining - Final - Total kWh: " + remainingKwh + ", Total mins: " + remainingMins);

					freeChargingForDriverGrp fcdgObj = new freeChargingForDriverGrp();
					fcdgObj.setCreatedDate(utils.getDateFrmt(siv.getMeterValueTimeStatmp()));
					fcdgObj.setUsedFreekWhs(usedFreeKwh);
					fcdgObj.setUsedFreeMins(usedFreeMins);
					fcdgObj.setRemainingFreeKwh(remainingKwh);
					fcdgObj.setRemainingFreeMins(remainingMins);
					fcdgObj.setUserId(siv.getUserObj().get("UserId").asLong());
					generalDao.save(fcdgObj);
				}

				logger.info(Thread.currentThread().getId() + "Final billing values - Duration: " + siv.getBillSessionDuration() +
						", kWh: " + siv.getBillTotalKwUsed() +
						", Remaining free kWh: " + remainingKwh +
						", Used free kWh: " + usedFreeKwh);

				// Get all session usage for this user today
				String allSessionsQuery =
						"SELECT SUM(used_free_kwh) as total_kwh_used, SUM(used_free_mins) as total_mins_used " +
								"FROM session s " +
								"LEFT JOIN ocpp_sessionBillableData osb ON s.sessionId = osb.charge_session_id " +
								"WHERE s.userId = '" + siv.getUserObj().get("UserId").asLong() + "' " +
								"AND CAST(s.startTimeStamp AS DATE) = '" + utils.getDate(siv.getMeterValueTimeStatmp()) + "'";

				List<Map<String, Object>> totalSessionUsage = generalDao.getMapData(allSessionsQuery);
				if (!totalSessionUsage.isEmpty() && totalSessionUsage.get(0).get("total_kwh_used") != null) {
					double totalKwhUsed = Double.valueOf(String.valueOf(totalSessionUsage.get(0).get("total_kwh_used")));
					double totalMinsUsed = Double.valueOf(String.valueOf(totalSessionUsage.get(0).get("total_mins_used")));

					logger.info("TOTAL USAGE ACROSS ALL SESSIONS - kWh: " + totalKwhUsed +
							", mins: " + totalMinsUsed);
					logger.info("PERCENTAGE USED - kWh: " + (totalKwhUsed/(originalFreeKwh + additionalFreeKwh))*100 + "%" +
							", mins: " + (totalMinsUsed/(originalFreeMins + additionalFreeMins))*100 + "%");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Thread.currentThread().getId() + "Error in freeChargingForDriverGrp: " + e.getMessage());
		}
		return siv;
	}

//	public void clearSessionValues(String chargeSessionId) {
//		String deleteSql = "DELETE FROM ocpp_sessionBillableData WHERE charge_session_id = '" + chargeSessionId + "'";
//		executeRepository.update(deleteSql);
//		logger.info("Cleared session values for sessionId: " + chargeSessionId);
//	}

	public SessionImportedValues calcOfTariff(Map<String, Object> siteObj,BigDecimal transactionFee,boolean combination, String vendingUnit,BigDecimal vendingPrice,String vendingUnit2,BigDecimal vendingPrice2,
											  SessionImportedValues sessionImportedValues, BigDecimal durationInMinutes,
											  OCPPStartTransaction startTransactionObj, BigDecimal totalKwUsed) {
		try {
			BigDecimal finalCost = new BigDecimal("0.0");
			BigDecimal totalCost = new BigDecimal("0.0");
			BigDecimal flatPrice =new BigDecimal("0.0");

			BigDecimal enegryCost1 = new BigDecimal("0.0");
			BigDecimal parkingCost1 = new BigDecimal("0.0");
			BigDecimal timeCost1 = new BigDecimal("0.0");
			BigDecimal cost1 = new BigDecimal("0.0");

			BigDecimal enegryCost2 = new BigDecimal("0.0");
			BigDecimal parkingCost2 = new BigDecimal("0.0");
			BigDecimal timeCost2 = new BigDecimal("0.0");
			BigDecimal cost2 = new BigDecimal("0.0");

			BigDecimal salesTaxPer=new BigDecimal(String.valueOf(siteObj.get("saleTexPerc")));
			BigDecimal salesTaxVal1=new BigDecimal(0.00);
			BigDecimal salesTaxVal2=new BigDecimal(0.00);

			BigDecimal portPrice =vendingPrice;
			String portPriceUnit =vendingUnit;
			flatPrice =transactionFee;

			if (portPriceUnit.equalsIgnoreCase("Hr")) {
				portPrice = portPrice.divide(new BigDecimal("60"), 5, RoundingMode.HALF_UP);
				timeCost1 = portPrice.multiply(portPrice);
				salesTaxVal1 = (salesTaxPer.divide(new BigDecimal("100"), 5, RoundingMode.HALF_UP)).multiply(timeCost1);
			}else if(portPriceUnit.equalsIgnoreCase("kWh")){
				enegryCost1 = portPrice.multiply(totalKwUsed);
				salesTaxVal1 = (salesTaxPer.divide(new BigDecimal("100"), 5, RoundingMode.HALF_UP)).multiply(enegryCost1);
			}else if(combination) {
				BigDecimal portPrice1 =vendingPrice2;
				String portPriceUnit1 =vendingUnit2;
				if(portPriceUnit1.equalsIgnoreCase("Hr")) {
					portPrice1 = portPrice1.divide(new BigDecimal("60"), 5, RoundingMode.HALF_UP);
					timeCost1 = portPrice1.multiply(portPrice1);
					salesTaxVal1 = (salesTaxPer.divide(new BigDecimal("100"), 5, RoundingMode.HALF_UP)).multiply(timeCost1);
				}else if(portPriceUnit1.equalsIgnoreCase("Min")) {
					timeCost1 = portPrice1.multiply(portPrice1);
					salesTaxVal1 = (salesTaxPer.divide(new BigDecimal("100"), 5, RoundingMode.HALF_UP)).multiply(timeCost1);
				}else if(portPriceUnit1.equalsIgnoreCase("kWh")){
					enegryCost1 = portPrice1.multiply(totalKwUsed);
					salesTaxVal1 = (salesTaxPer.divide(new BigDecimal("100"), 5, RoundingMode.HALF_UP)).multiply(enegryCost1);
				}

				BigDecimal portPrice2 =vendingPrice2;
				String portPriceUnit2 = vendingUnit2;
				if(portPriceUnit2.equalsIgnoreCase("Hr")) {
					portPrice2 = portPrice1.divide(new BigDecimal("60"), 5, RoundingMode.HALF_UP);
					timeCost2 = portPrice1.multiply(portPrice2);
					salesTaxVal2 = (salesTaxPer.divide(new BigDecimal("100"), 5, RoundingMode.HALF_UP)).multiply(timeCost2);
				}else if(portPriceUnit2.equalsIgnoreCase("Min")) {
					timeCost2 = portPrice2.multiply(portPrice1);
					salesTaxVal2 = (salesTaxPer.divide(new BigDecimal("100"), 5, RoundingMode.HALF_UP)).multiply(timeCost2);
				}else if(portPriceUnit1.equalsIgnoreCase("kWh")){
					enegryCost2 = portPrice1.multiply(totalKwUsed);
					salesTaxVal2 = (salesTaxPer.divide(new BigDecimal("100"), 5, RoundingMode.HALF_UP)).multiply(enegryCost2);
				}
			}else if(portPriceUnit.equalsIgnoreCase("min")) {
				portPrice = portPrice.divide(new BigDecimal("60"), 5, RoundingMode.HALF_UP);
				timeCost1 = portPrice.multiply(durationInMinutes);
				salesTaxVal1 = (salesTaxPer.divide(new BigDecimal("100"), 5, RoundingMode.HALF_UP)).multiply(timeCost1);
			}
			cost1 = enegryCost1.add(parkingCost1).add(timeCost1);
			cost2 = enegryCost2.add(parkingCost2).add(timeCost2);
			totalCost = cost1.add(cost2);
			finalCost = totalCost.add(flatPrice);

			BigDecimal excTotlCost = cost1;
			TariffPrice minPriceBillDtls = getMinPriceBillDetails(Long.valueOf(0));
			if (minPriceBillDtls != null && (Double.parseDouble(String.valueOf(excTotlCost)) < minPriceBillDtls.getExcl_vat())) {
				excTotlCost = new BigDecimal(String.valueOf(minPriceBillDtls.getExcl_vat()));
			}
			TariffPrice maxPriceBillDtls = getMaxPriceBillDetails(Long.valueOf(0));
			if (maxPriceBillDtls != null && (Double.parseDouble(String.valueOf(excTotlCost)) > maxPriceBillDtls.getExcl_vat())) {
				excTotlCost = new BigDecimal(String.valueOf(minPriceBillDtls.getExcl_vat()));
			}

			// double vat = vatEneCost + vatFltCost + vatPrkCost + vatTimCost;
			Price price = null;
			List<Map<String, Object>> mapData = executeRepository.findAll("select isnull(total_cost,'0') as total_cost from ocpi_session where authorization_reference = '"+ startTransactionObj.getSessionId() + "'");
			if (mapData.size() > 0) {
				String valueOf = String.valueOf(mapData.get(0).get("total_cost"));
				if (valueOf.equalsIgnoreCase("0")) {
					price = new Price();
				} else {
					price = generalDao.findOne("FROM Price where id = '" + valueOf + "'", new Price());
				}
			} else {
				price = new Price();
			}
			//finalCost = excTotlCost ;

			price.setExcl_vat(Double.parseDouble(String.valueOf(finalCost)));
			price.setIncl_vat(Double.parseDouble(String.valueOf(finalCost.add(salesTaxVal1).add(salesTaxVal2))));
			price = generalDao.saveOrupdate(price);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sessionImportedValues;
	}
	public TariffPrice getMinPriceBillDetails(long tariff_id) {
		TariffPrice findOne = null;
		try {
			String query = "Select * FROM price WHERE id  =  (Select min_price_id from tariff where id =" + tariff_id
					+ ")";
			findOne = generalDao.findOneBySQLQuery(query, new TariffPrice());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return findOne;
	}

	public TariffPrice getMaxPriceBillDetails(long tariff_id) {
		TariffPrice findOne = null;
		try {
			String query = "Select * FROM price WHERE id  =  (Select max_price_id from tariff where id =" + tariff_id
					+ ")";
			findOne = generalDao.findOneBySQLQuery(query, new TariffPrice());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return findOne;
	}
	public SessionImportedValues unknownUserBilling(SessionImportedValues sessionImportedValues,OCPPTransactionData transactionData,String additionalVendingPriceUnit,BigDecimal additionalVendingPricePerUnit,BigDecimal sessionElapsedInMinutes,boolean additionalPricing) {
		String stationMode = transactionData.getStationMode();
//		try {
//			if(stationMode.equalsIgnoreCase("normal") || stationMode.equalsIgnoreCase("paymentMode")) {
//				BigDecimal finalCosttostore = new BigDecimal("0.0");
//				BigDecimal totalKwUsed = new BigDecimal(String.valueOf(sessionImportedValues.getTotalKwUsed()));
//				
//				sessionImportedValues.setFinalCostInslcCurrency(Double.parseDouble(String.valueOf(finalCosttostore)));
//				sessionImportedValues.setFinalCosttostore(Double.parseDouble(String.valueOf(finalCosttostore)));
//				sessionImportedValues.setProcessingFee(0.0);
//				sessionImportedValues.setTxnType("Prod");
//				sessionImportedValues.setUserId(0);
//				sessionImportedValues.setTotalAdditionalPrice(0.00);
//			} else {
//				sessionImportedValues.setFinalCostInslcCurrency(0.00);
//				sessionImportedValues.setFinalCosttostore(0.00);
//				sessionImportedValues.setProcessingFee(0.0);
//				sessionImportedValues.setTxnType("Prod");
//				sessionImportedValues.setUserId(0);
//				sessionImportedValues.setTotalAdditionalPrice(0.00);
//			}
//		} catch (NumberFormatException e) {
//			e.printStackTrace();
//		}
		return sessionImportedValues;
	}
	public AccountTransactions getAccTxns(String sessionId) {
		AccountTransactions accountTransactionObj = null;
		try {
			String queryForaccRxId = "select ISNULL(accountTransaction_id,0) as accountTransaction_id from session where sessionId = '"+sessionId+"'";
			List<Map<String,Object>> durationInString = executeRepository.findAll(queryForaccRxId);

			if(durationInString.size()>0) {
				Long accTxnId = Long.valueOf(String.valueOf(durationInString.get(0).get("accountTransaction_id")));
				String queryForacc = "select top 1 * from account_transaction where id = '" +accTxnId+ "'";
				List<AccountTransactions> findAllSQLQuery = generalDao.findAllSQLQuery(new AccountTransactions(), queryForacc);
				if(findAllSQLQuery.size() > 0) {
					accountTransactionObj = findAllSQLQuery.get(0);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return accountTransactionObj;
	}
	public AccountTransactionForGuestUser insertIntoAccountTransactionForGuestUser(double revenue, Date utcTime,
																				   String sessionId, String phone, Boolean flag, Long stationId) {
		AccountTransactionForGuestUser guestUserTransaction = null;
		try {
			guestUserTransaction = generalDao.findOne(
					"From AccountTransactionForGuestUser where sessionId='" + sessionId + "'",
					new AccountTransactionForGuestUser());

			if (guestUserTransaction == null)
				guestUserTransaction = new AccountTransactionForGuestUser();
			else
				guestUserTransaction.setId(guestUserTransaction.getId());
			guestUserTransaction.setRevenue(revenue);
			guestUserTransaction.setSessionId(sessionId);
			guestUserTransaction.setTime(utcTime);
			guestUserTransaction.setPhone(phone);
			guestUserTransaction.setFlag(flag);
			guestUserTransaction.setStationId(stationId);

			generalDao.saveOrupdate(guestUserTransaction);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return guestUserTransaction;
	}
	public boolean getUserCards(long accountId) {
		boolean flag = false;
		try {
			String str = "select * from worldPay_creditCard where accountId="+accountId+" and flag=1";
			List<Map<String, Object>> strData = executeRepository.findAll(str);
			if(strData.size() == 0) {
				flag = true;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	public void notificationFlagUpdates(String id, boolean flag, String typeOfFlag, String stationRefNum) {
		try {
			if (typeOfFlag.equalsIgnoreCase("1") || typeOfFlag.equalsIgnoreCase("1.0")) {
				executeRepository.update("update ocpp_sessionData set lowBalanceFlag = '" + flag + "' where sessionId='" + id + "'");
			} else if (typeOfFlag.equalsIgnoreCase("5")) {
				executeRepository.update("update ocpp_sessionData set notificationFlag = '" + flag + "' where sessionId='" + id + "'");
			} else if (typeOfFlag.equalsIgnoreCase("3PlusHrs")) {
				String query = "update pushNotification SET flag = '1' where userId = '" + id + "'";
				customLogger.info(stationRefNum, "inside 3PlusHrs condition : " + query);
				long updateSQL = Long.valueOf(String.valueOf(executeRepository.update(query)));
				if (updateSQL == 0) {
					query = "insert into pushNotification (flag,userId) values('1','" + id + "')";
					customLogger.info(stationRefNum, "inside insert into 3PlusHrs condition : " + query);
					executeRepository.update(query);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public boolean insertMeterValues(SessionImportedValues siv,FinalData finalData) {
		try {
			OCPPMeterValuesPojo meterValuesPojo = new OCPPMeterValuesPojo();
			meterValuesPojo.setPortId(Long.valueOf(String.valueOf(siv.getPortId())));
			meterValuesPojo.setFinalCost(siv.getFinalCostInslcCurrency());
			meterValuesPojo.setSessionId(siv.getChargeSessUniqId());
			meterValuesPojo.setStartTimeStamp(siv.getStartTransTimeStamp());
			meterValuesPojo.setTransactionId(siv.getStTxnObj().getTransactionId());
			meterValuesPojo.setUnit("kWh");
			meterValuesPojo.setValue(siv.getTotalKwUsed().doubleValue());
			meterValuesPojo.setStationId(Long.valueOf(String.valueOf(siv.getStnId())));
			meterValuesPojo.setTimeStamp(siv.getMeterValueTimeStatmp());
			meterValuesPojo.setSoc(Double.valueOf(siv.getSoCValue().equalsIgnoreCase("null") ? "0" : siv.getSoCValue()));
			meterValuesPojo.setPowerActiveImport(Double.valueOf(siv.getPowerActiveImportValue().equalsIgnoreCase("null") ? "0" : siv.getPowerActiveImportValue()));
			meterValuesPojo.setEnergyActiveExportRegisterUnit(siv.getEnergyActiveExportRegisterUnit()==null ? "-" : siv.getEnergyActiveExportRegisterUnit().equalsIgnoreCase("null") ? "-": siv.getEnergyActiveExportRegisterUnit());
			meterValuesPojo.setEnergyActiveImportRegisterUnit(siv.getEnergyActiveImportRegisterUnitES()== null ? "kWh" : siv.getEnergyActiveImportRegisterUnitES().equalsIgnoreCase("null") ? "kWh": siv.getEnergyActiveImportRegisterUnitES());
			meterValuesPojo.setEnergyReactiveImportRegisterUnit(siv.getEnergyReactiveImportRegisterUnit()== null ? "-" : siv.getEnergyReactiveImportRegisterUnit().equalsIgnoreCase("null") ? "-": siv.getEnergyReactiveImportRegisterUnit());
			meterValuesPojo.setEnergyReactiveExportRegisterUnit(siv.getEnergyReactiveExportRegisterUnit()== null ? "-" : siv.getEnergyReactiveExportRegisterUnit().equalsIgnoreCase("null") ? "-": siv.getEnergyReactiveExportRegisterUnit());
			meterValuesPojo.setEnergyActiveExportIntervalUnit(siv.getEnergyActiveExportIntervalUnit()== null ? "-" : siv.getEnergyActiveExportIntervalUnit().equalsIgnoreCase("null") ? "-": siv.getEnergyActiveExportIntervalUnit());
			meterValuesPojo.setEnergyActiveImportIntervalUnit(siv.getEnergyActiveImportIntervalUnit()== null ? "-" : siv.getEnergyActiveImportIntervalUnit().equalsIgnoreCase("null") ? "-": siv.getEnergyActiveImportIntervalUnit());
			meterValuesPojo.setEnergyReactiveExportIntervalUnit(siv.getEnergyActiveExportIntervalUnit()== null ? "-" : siv.getEnergyActiveExportIntervalUnit().equalsIgnoreCase("null") ? "-": siv.getEnergyActiveExportIntervalUnit());
			meterValuesPojo.setEnergyReactiveImportIntervalUnit(siv.getEnergyReactiveImportIntervalUnit()== null ? "-" : siv.getEnergyReactiveImportIntervalUnit().equalsIgnoreCase("null") ? "-": siv.getEnergyReactiveImportIntervalUnit());
			meterValuesPojo.setPowerActiveExportUnit(siv.getPowerActiveExportUnit()== null ? "-" : siv.getPowerActiveExportUnit().equalsIgnoreCase("null") ? "-": siv.getPowerActiveExportUnit());
			meterValuesPojo.setPowerActiveImportUnit(siv.getPowerActiveImportUnitES()== null ? "kW" : siv.getPowerActiveImportUnitES().equalsIgnoreCase("null") ? "kW": siv.getPowerActiveImportUnitES());
			meterValuesPojo.setPowerOfferedUnit(siv.getPowerOfferedUnit()== null ? "-" : siv.getPowerOfferedUnit().equalsIgnoreCase("null") ? "-": siv.getPowerOfferedUnit());
			meterValuesPojo.setPowerReactiveExportUnit(siv.getPowerReactiveExportUnit()== null ? "-" : siv.getPowerReactiveExportUnit().equalsIgnoreCase("null") ? "-": siv.getPowerReactiveExportUnit());
			meterValuesPojo.setPowerReactiveImportUnit(siv.getPowerReactiveImportUnit()== null ? "-" : siv.getPowerReactiveImportUnit().equalsIgnoreCase("null") ? "-": siv.getPowerReactiveImportUnit());
			meterValuesPojo.setPowerFactorUnit(siv.getPowerFactorUnit()== null ? "-" : siv.getPowerFactorUnit().equalsIgnoreCase("null") ? "-": siv.getPowerFactorUnit());
			meterValuesPojo.setCurrentImportUnit(siv.getCurrentImportUnit()== null ? "A" : siv.getCurrentImportUnit().equalsIgnoreCase("null") ? "A": siv.getCurrentImportUnit());
			meterValuesPojo.setCurrentExportUnit(siv.getCurrentExportUnit()== null ? "-" : siv.getCurrentExportUnit().equalsIgnoreCase("null") ? "-": siv.getCurrentExportUnit());
			meterValuesPojo.setCurrentOfferedUnit(siv.getCurrentOfferedUnit()== null ? "-" : siv.getCurrentOfferedUnit().equalsIgnoreCase("null") ? "-": siv.getCurrentOfferedUnit());
			meterValuesPojo.setVoltageUnit(siv.getVoltageUnit()== null ? "-" : siv.getVoltageUnit().equalsIgnoreCase("null") ? "-": siv.getVoltageUnit());
			meterValuesPojo.setFrequencyUnit(siv.getFrequencyUnit()== null ? "-" : siv.getFrequencyUnit().equalsIgnoreCase("null") ? "-": siv.getFrequencyUnit());
			meterValuesPojo.setTemperatureUnit(siv.getTemperatureUnit()== null ? "-" : siv.getTemperatureUnit().equalsIgnoreCase("null") ? "-": siv.getTemperatureUnit());
			meterValuesPojo.setSoCUnit(siv.getSoCUnit()== null ? "Percent" : siv.getSoCUnit().equalsIgnoreCase("null") ? "Percent": siv.getSoCUnit());
			meterValuesPojo.setRPMUnit(siv.getRPMUnit()== null ? "-" : siv.getRPMUnit().equalsIgnoreCase("null") ? "-": siv.getRPMUnit());

			meterValuesPojo.setEnergyActiveExportRegisterValue(siv.getEnergyActiveExportRegisterValue()== null ? 0.0 :siv.getEnergyActiveExportRegisterValue().equalsIgnoreCase("null") ? 0.0 : Double.parseDouble(String.valueOf(siv.getEnergyActiveExportRegisterValue())));
			meterValuesPojo.setEnergyActiveImportRegisterValue(siv.getEnergyActiveImportRegisterValueES()== null ? 0.0 :siv.getEnergyActiveImportRegisterValueES().equalsIgnoreCase("null") ? 0.0 : Double.parseDouble(String.valueOf(siv.getEnergyActiveImportRegisterValueES())));
			meterValuesPojo.setEnergyReactiveExportRegisterValue(siv.getEnergyReactiveExportRegisterValue()== null ? 0.0 : siv.getEnergyReactiveExportRegisterValue().equalsIgnoreCase("null") ? 0.0 :Double.parseDouble(String.valueOf(siv.getEnergyReactiveExportRegisterValue())));
			meterValuesPojo.setEnergyReactiveImportRegisterValue(siv.getEnergyReactiveImportRegisterValue()== null ? 0.0 :siv.getEnergyReactiveImportRegisterValue().equalsIgnoreCase("null") ? 0.0 :Double.parseDouble(String.valueOf(siv.getEnergyReactiveImportRegisterValue())));
			meterValuesPojo.setEnergyActiveExportIntervalValue(siv.getEnergyActiveExportIntervalValue()== null ? 0.0 :siv.getEnergyActiveExportIntervalValue().equalsIgnoreCase("null") ? 0.0:Double.parseDouble(String.valueOf(siv.getEnergyActiveExportIntervalValue())));
			meterValuesPojo.setEnergyActiveImportIntervalValue(siv.getEnergyActiveImportIntervalValue()== null ? 0.0 :siv.getEnergyActiveImportIntervalValue().equalsIgnoreCase("null") ? 0.0:Double.parseDouble(String.valueOf(siv.getEnergyActiveImportIntervalValue())));
			meterValuesPojo.setEnergyReactiveExportIntervalValue(siv.getEnergyReactiveExportIntervalValue()== null ? 0.0 :siv.getEnergyReactiveExportIntervalValue().equalsIgnoreCase("null") ? 0.0:Double.parseDouble(String.valueOf(siv.getEnergyReactiveExportIntervalValue())));
			meterValuesPojo.setEnergyReactiveImportIntervalValue(siv.getEnergyReactiveImportIntervalValue()== null ? 0.0 :siv.getEnergyReactiveImportIntervalValue().equalsIgnoreCase("null") ? 0.0 :Double.parseDouble(String.valueOf(siv.getEnergyReactiveImportIntervalValue())));
			meterValuesPojo.setPowerActiveExportValue(siv.getPowerActiveExportValue()==null ? 0.0 :siv.getPowerActiveExportValue().equalsIgnoreCase("null") ? 0.0 :Double.parseDouble(String.valueOf(siv.getPowerActiveExportValue())));
			meterValuesPojo.setPowerActiveImportValue(siv.getPowerActiveImportValueES()== null ? 0.0 :siv.getPowerActiveImportValueES().equalsIgnoreCase("null") ? 0.0 : Double.parseDouble(String.valueOf(siv.getPowerActiveImportValueES())));
			meterValuesPojo.setPowerOfferedValue(siv.getPowerOfferedValue()==null ? 0.0 :siv.getPowerOfferedValue().equalsIgnoreCase("null") ? 0.0 :Double.parseDouble(String.valueOf(siv.getPowerOfferedValue())));
			meterValuesPojo.setPowerReactiveExportValue(siv.getPowerReactiveExportValue()== null ? 0.0 :siv.getPowerReactiveExportValue().equalsIgnoreCase("null") ? 0.0 :Double.parseDouble(String.valueOf(siv.getPowerReactiveExportValue())));
			meterValuesPojo.setPowerReactiveImportValue(siv.getPowerReactiveImportValue()== null ? 0.0 :siv.getPowerReactiveImportValue().equalsIgnoreCase("null") ? 0.0 :Double.parseDouble(String.valueOf(siv.getPowerReactiveImportValue())));
			meterValuesPojo.setPowerFactorValue(siv.getPowerFactorValue()==null ? 0.0 : siv.getPowerFactorValue().equalsIgnoreCase("null") ? 0.0 :Double.parseDouble(String.valueOf(siv.getPowerFactorValue())));
			meterValuesPojo.setCurrentImportValue(siv.getCurrentImportValue()== null ? 0.0 :siv.getCurrentImportValue().equalsIgnoreCase("null") ? 0.0 :Double.parseDouble(String.valueOf(siv.getCurrentImportValue())));
			meterValuesPojo.setCurrentExportValue(siv.getCurrentExportValue()==null ? 0.0 :siv.getCurrentExportValue().equalsIgnoreCase("null") ? 0.0 :Double.parseDouble(String.valueOf(siv.getCurrentExportValue())));
			meterValuesPojo.setCurrentOfferedValue(siv.getCurrentOfferedValue()== null ? 0.0 :siv.getCurrentOfferedValue().equalsIgnoreCase("null") ? 0.0 :Double.parseDouble(String.valueOf(siv.getCurrentOfferedValue())));
			meterValuesPojo.setVoltageValue(siv.getVoltageValue()==null ? 0.0 :siv.getVoltageValue().equalsIgnoreCase("null") ? 0.0 : Double.parseDouble(String.valueOf(siv.getVoltageValue())));
			meterValuesPojo.setFrequencyValue(siv.getFrequencyValue()==null ? 0.0 :siv.getFrequencyValue().equalsIgnoreCase("null") ? 0.0 : Double.parseDouble(String.valueOf(siv.getFrequencyValue())));
			meterValuesPojo.setTemperatureValue(siv.getTemperatureValue()==null ? 0.0 :siv.getTemperatureValue().equalsIgnoreCase("null") ? 0.0 :Double.parseDouble(String.valueOf(siv.getTemperatureValue())));
			meterValuesPojo.setSoCValue(siv.getSoCValue()==null ? 0.0 :siv.getSoCValue().equalsIgnoreCase("null") ? 0.0 :Double.parseDouble(String.valueOf(siv.getSoCValue())));
			meterValuesPojo.setRPMValue(siv.getRPMValue()== null ? 0.0 :siv.getRPMValue().equalsIgnoreCase("null") ? 0.0 :Double.parseDouble(String.valueOf(siv.getRPMValue())));
			meterValuesPojo.setEnergyActiveImportRegisterDiffValue(siv.getEnergyActiveImportRegisterDiffValue()== null ? 0.0 :siv.getEnergyActiveImportRegisterDiffValue().equalsIgnoreCase("null") ? 0.0 :Double.parseDouble(String.valueOf(siv.getEnergyActiveImportRegisterDiffValue())));
			meterValuesPojo.setPowerActiveImportDiffValue(siv.getPowerActiveImportDiffValue()==null ? 0.0 :siv.getPowerActiveImportDiffValue().equalsIgnoreCase("null") ? 0.0 : Double.parseDouble(String.valueOf(siv.getPowerActiveImportDiffValue())) );
			meterValuesPojo.setCurrentImportDiffValue(siv.getCurrentImportDiffValue()==null ? 0.0 :siv.getCurrentImportDiffValue().equalsIgnoreCase("null") ? 0.0 : Double.parseDouble(String.valueOf(siv.getCurrentImportDiffValue())) );
			meterValuesPojo.setSoCDiffValue(siv.getSoCDiffValue()==null ? 0.0 :siv.getSoCDiffValue().equalsIgnoreCase("null") ? 0.0 : Double.parseDouble(String.valueOf(siv.getSoCDiffValue())) );
			try {
				esLoggerUtil.createOcppMeterLogsIndex(meterValuesPojo,"Charger",finalData.getSecondValue(),siv.getStartTransTimeStamp());
			}catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}
	@Override
	public void sendPushNotification(SessionImportedValues siv, String NoftyType) {
		if (!siv.getStTxnObj().isOfflineFlag()) {
			try {
				Thread thread = new Thread() {
					@SuppressWarnings("unchecked")
					public void run() {
						try {
							JSONObject extra = new JSONObject();
							List<String> deviceTokens = new ArrayList();
							if (siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser")) {
								List<DeviceDetails> deviceDetails = OCPPDeviceDetailsService.getDeviceByUser(Long.valueOf(String.valueOf(siv.getStTxnObj().getUserId())));
								if (deviceDetails != null) {
									deviceDetails.forEach(device -> {
										try {
											if (device.getDeviceType().equalsIgnoreCase("Android")) {
												if (!String.valueOf(device.getDeviceToken()).equalsIgnoreCase("")) {
													deviceTokens.add(device.getDeviceToken());
												}
											}
											if (device.getDeviceType().equalsIgnoreCase("ios")) {
//											iOSRecipients.add(device.getDeviceToken());	
											}

										} catch (Exception e) {
											e.printStackTrace();
										}
									});
								}
							} else if (siv.getTxnData().getUserType().equalsIgnoreCase("PAYG")) {
								String user_obj = siv.getTxnData().getUser_obj();
								if (user_obj != null && !user_obj.equalsIgnoreCase("null")) {
									JsonNode tariff = objectMapper.readTree(user_obj);
									if (tariff.size() > 0) {
										if (String.valueOf(tariff.get("deviceType").asText()).equalsIgnoreCase("Android")) {
											if (!String.valueOf(tariff.get("deviceToken").asText()).equalsIgnoreCase("")) {
												deviceTokens.add(String.valueOf(tariff.get("deviceToken").asText()));
											}
										} else if (String.valueOf(tariff.get("deviceType").asText()).equalsIgnoreCase("iOS")) {
//										iOSRecipients.add(String.valueOf(tariff.get("deviceToken").asText()));	
										}
									}
								}
							}
							Map<String, Object> orgData = ocppUserService.getOrgData(siv.getSite_orgId(), String.valueOf(siv.getStnObj().get("referNo").asText()));
							JSONObject info = new JSONObject();
							extra.put("stationId", String.valueOf(siv.getStnObj().get("stnId").asLong()));
							extra.put("stationName", String.valueOf(siv.getStnObj().get("referNo").asText()));
							extra.put("portId", String.valueOf(siv.getStnObj().get("portId").asLong()));
							extra.put("sessionId", siv.getChargeSessUniqId());
							info.put("action", NoftyType);
							info.put("notificationId", utils.getIntRandomNumber(9));
							info.put("extra", String.valueOf(extra));
							info.put("title", String.valueOf(orgData.get("orgName")));
							info.put("body", "");
							info.put("userId", String.valueOf(siv.getStTxnObj().getUserId()));

							if (deviceTokens.size() > 0) {
								pushNotification.sendMulticastMessage(info, deviceTokens, null, 0);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				thread.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void txnAlerts(Double revenue, Double kWh, Double sessionMins, boolean revenueAlertFlag, boolean kWhAlertFlag, boolean durationAlertFlag, String stationRefNum, Long sessId, long orgId, String randomSessionId, String currencyHexCode,long stationId) {
		try {

			Map<String, Object> stopMailDetails = new HashMap<String, Object>();
			stopMailDetails.put("curDate", String.valueOf(new Date()));
			stopMailDetails.put("EnergyUsage", kWh + "kWh");
			stopMailDetails.put("Duration", Utils.getTimeFormate(sessionMins) + "(HH:MM:SS)");
			stopMailDetails.put("revenue", currencyHexCode + "" + revenue);
			stopMailDetails.put("StationId", stationRefNum);
			stopMailDetails.put("mailType", "txnAlert");
			stopMailDetails.put("sessionId", String.valueOf(sessId));
			stopMailDetails.put("randomSession", randomSessionId);
			stopMailDetails.put("orgId", orgId);

			if (revenue >= revenueAlert && revenueAlertFlag == false) {
				customLogger.info(stationRefNum,
						"inside revenue Alert : " + revenueAlert + " , revenue value : " + revenue);
				stopMailDetails.put("heading", "Max Revenue Alert");
				String mailSubject = "Session " + sessId + " Revenue is increased more than " + revenueAlert;
				String mails = propertiesServiceImpl.getPropety("internalWorkMails");
				revenueAlertFlag = true;
			}
			if (kWh >= kWhAlert && kWhAlertFlag == false) {
				customLogger.info(stationRefNum, "inside kWh Alert : " + kWhAlert + " , kWh value : " + kWh);
				stopMailDetails.put("heading", "Max kWh Alert");
				String mailSubject = "Session " + sessId + " energy used more than " + kWhAlert + "kWh";
				String mails = propertiesServiceImpl.getPropety("internalWorkMails");
				kWhAlertFlag = true;
			}
			if (sessionMins >= durationAlertInMins && durationAlertFlag == false) {
				customLogger.info(stationRefNum, " inside duration Alert : " + durationAlertInMins + " , duration value : " + sessionMins);
				stopMailDetails.put("heading", "Max Duration Alert");
				String mailSubject = "Session " + sessId + " duration is increased more than " + durationAlertInMins / 60 + " hours";
				String mails = propertiesServiceImpl.getPropety("internalWorkMails");
				durationAlertFlag = true;
			}
			String updateQuery = "update ocpp_sessionData set kWhAlert='" + kWhAlertFlag + "',revenueAlert='" + revenueAlertFlag + "',durationAlert='" + durationAlertFlag + "' where sessionId = '" + randomSessionId + "'";
			int update = executeRepository.update(updateQuery);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public List<Map<String, Object>> previousMeterValuesDataInStop(String sessionId, Date startTransTimeStamp,
																   double startTransactionMeterValue, Double stopTransactionMeterValue, String stnRefNo) {
		List<Map<String, Object>> listMapData = new ArrayList<>();
		try {
			String hqlQuery = "select top 1 s.id as sessionId,os.endTime as DataRcvdTimeStamp,endTimeStamp as previousMeterEndTimeStamp,wattsecondsused as Wattsecondsused,os.energyActiveImportRegisterUnit as units,isnull(atx.amtDebit,0) as amtDebit,"
					+ " isnull(powerActiveImport_value,0) as powerActiveImportValue,os.kWhAlert,os.revenueAlert,avg_power,os.durationAlert,'false' as isFirstMeterReading,s.socStartVal,"
					+ "s.socEndVal,s.kilowattHoursUsed as previousMeterWattsecondsused,sessionElapsedInMin,s.paymentMode as paymentType,os.promoCodeUsedTime,isnull(0.00,0) as userProcessingFee,isnull(s.creationDate,GETUTCDATE()) as creationDate,s.accountTransaction_id from ocpp_sessionData os inner join session s on s.sessionId=os.sessionId left join account_transaction atx on s.accountTransaction_id = "
					+ "atx.id where s.sessionid='" + sessionId + "' order by os.id desc";
			listMapData = executeRepository.findAll(hqlQuery);

			Double totalKwUsed = 0.0;

			if (listMapData.isEmpty()) {
				HashMap<String, Object> mapData = new HashMap<>();
				mapData.put("DataRcvdTimeStamp", startTransTimeStamp);
				mapData.put("sessionElapsedInMin", "0");
				mapData.put("sessionId", "0");

				// if(stopTransactionMeterValue==0)
				// stopTransactionMeterValue=getMaximumMeterValue(sessionId);

				totalKwUsed = ((stopTransactionMeterValue - startTransactionMeterValue) / 1000);
				mapData.put("totalKwUsed", totalKwUsed);
				mapData.put("previousMeterWattsecondsused", "0");
				mapData.put("amtDebit", 0.0);
				mapData.put("powerActiveImportValue", 0);
				mapData.put("kWhAlert", 0);
				mapData.put("revenueAlert", 0);
				mapData.put("durationAlert", 0);
				mapData.put("socEndVal", 0);
				mapData.put("isFirstMeterReading", "true");
				mapData.put("socStartVal", 0);
				mapData.put("socEndVal", 0);
				mapData.put("paymentType", "Wallet");
				mapData.put("userProcessingFee", 0);
				mapData.put("accountTransaction_id", 0);

				listMapData.add(mapData);
			} else {
				Map<String, Object> mapData = listMapData.get(0);
				if (mapData.get("units") == null)
					mapData.put("units", "kWh");
				String currentImportUnit = String.valueOf(mapData.get("units"));
				Double previouswattUsed = Double.parseDouble(String.valueOf(mapData.get("Wattsecondsused")));

				if (currentImportUnit.equalsIgnoreCase("Wh")) {
					totalKwUsed = ((stopTransactionMeterValue - previouswattUsed) / 1000);
				} else if (currentImportUnit.equalsIgnoreCase("kWh")) {
					totalKwUsed = ((stopTransactionMeterValue / 1000) - previouswattUsed);
				} else if (currentImportUnit.equalsIgnoreCase("W")) {
					totalKwUsed = ((stopTransactionMeterValue - startTransactionMeterValue) / 3600000);
				} else {
					totalKwUsed = ((stopTransactionMeterValue - startTransactionMeterValue) / 1000);
				}
				mapData.put("totalKwUsed", totalKwUsed);
			}
			customLogger.info(stnRefNo, "previous stop value data : " + listMapData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listMapData;
	}
	public Double getMaximumMeterValue(String randomSessionId) {
		double parseDouble = 0.00;
		try {
			String hqlQuery = "select isNull(convert(varchar,MAX(wattsecondsused)),0) from ocpp_sessionData se inner join session s on s.sessionId=se.sessionId where s.sessionid='"
					+ randomSessionId + "'";
			String recordBySql = generalDao.getRecordBySql(hqlQuery);

			if (recordBySql == null) {
				recordBySql = "0";
			}
			parseDouble = Double.parseDouble(recordBySql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parseDouble;
	}
	@Override
	public SessionImportedValues insertIntoAccountTransaction(SessionImportedValues siv) {
		try {
			double accountBalance = OCPPAccountAndCredentialService.getNormalUserBalance(Long.valueOf(String.valueOf(siv.getUserObj().get("UserId").asLong())), siv.getStnRefNum());
//			double remainingBalance = accountBalance - siv.getUser_crncy_revenue();
			double remainingBalance =new BigDecimal(String.valueOf(accountBalance)).subtract(new BigDecimal(String.valueOf(siv.getUser_crncy_revenue()))).setScale(2, RoundingMode.HALF_UP).doubleValue();
			logger.info(Thread.currentThread().getId()+"accountBalance : "+accountBalance);
			logger.info(Thread.currentThread().getId()+"user revenue : "+siv.getUser_crncy_revenue());


			AccountTransactions accountTransactionObj = new AccountTransactions();
			accountTransactionObj.setAmtCredit(0.00);
			accountTransactionObj.setAmtDebit(siv.getUser_crncy_revenue());
			accountTransactionObj.setComment("Vehicle charging");
			accountTransactionObj.setCreateTimeStamp(utils.getUTCDate());
			accountTransactionObj.setLastUpdatedTime(utils.getUTCDate());
			accountTransactionObj.setCurrentBalance(remainingBalance);
			accountTransactionObj.setStatus("SUCCESS");
			Accounts acc = new Accounts();
			acc.setId(Long.valueOf(String.valueOf(siv.getUserObj().get("accid").asLong())));
			accountTransactionObj.setAccount(acc);
			accountTransactionObj.setSessionId(siv.getChargeSessUniqId());
			accountTransactionObj.setCurrencyType(String.valueOf(siv.getUserObj().get("crncy_Code").asText()));
			if(siv.getTxnData().getPaymentMode() != null && (siv.getTxnData().getPaymentMode().contains("card") || siv.getTxnData().getPaymentMode().equalsIgnoreCase("card"))) {
				accountTransactionObj.setPaymentMode("Credit Card");
			}else {
				accountTransactionObj.setPaymentMode(siv.getTxnData().getPaymentMode());
			}
			accountTransactionObj.setTransactionType("session");
			accountTransactionObj.setTax1_amount(0.00);
			accountTransactionObj.setTax2_amount(0.00);
			accountTransactionObj.setTax1_pct(0.00);
			accountTransactionObj.setTax3_amount(0.00);
			accountTransactionObj.setTax2_pct(0.00);
			accountTransactionObj.setTax3_pct(0.00);
			accountTransactionObj = generalDao.save(accountTransactionObj);
			logger.info(Thread.currentThread().getId()+"remainingBalance : "+remainingBalance);
			if(siv.getTxnData().getPaymentMode() != null && !siv.getTxnData().getPaymentMode().equalsIgnoreCase("Credit Card") && !siv.getTxnData().getPaymentMode().equalsIgnoreCase("Card")) {
				String queryForAccountBalanceUpdate = "update Accounts set accountBalance=" + remainingBalance + " where id=" + siv.getUserObj().get("accid");
				generalDao.updateHqlQuiries(queryForAccountBalanceUpdate);
			}
			siv.setAccTxns(accountTransactionObj);
//			siv.setSettlementTimeStamp(utils.getUTCDate());
			callingNegativeBalanceApi(String.valueOf(siv.getUserObj().get("uuid").asText()),Long.valueOf(String.valueOf(siv.getUserObj().get("UserId").asLong())),remainingBalance, siv.getStnRefNum());

			insertNotificationTracker(siv);
		} catch (Exception e) {
			logger.error(""+e);
		}
		return siv;

	}

	@Override
	public void insertNotificationTracker(SessionImportedValues siv) {
		try {
			NotificationTracker notificationTracker=new NotificationTracker();

			notificationTracker.setUserId(siv.getUserObj().get("UserId").asLong());
			notificationTracker.setAccount_transactionId(siv.getAccTxns().getId());
			notificationTracker.setSessionId(siv.getChargeSessUniqId());
			notificationTracker.setEmailflag(false);
			notificationTracker.setSmsflag(false);
			notificationTracker.setPushNotificationFlag(false);
			notificationTracker.setModifiedDate(utils.getUTCDate());
			notificationTracker.setResend(false);
			notificationTracker.setResendCount(0);
			generalDao.save(notificationTracker);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean cardChecking(long userId) {
		boolean flag=false;
		try {
			String query="select * from UserCards where userId='"+userId+"'";
			List<Map<String,Object>> data=executeRepository.findAll(query);
			if(data!=null && data.size()>0) {
				flag=true;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}
	public void callingNegativeBalanceApi(String uuid,long userId,double remaingingBalance,String stnRefNum) {
		try {
			boolean cardChecking = cardChecking(userId);
			if(remaingingBalance <= -0.50 && cardChecking) {
				try {
					Thread th = new Thread() {
						public void run() {
							try {
								String urlToRead = mobileServerUrl + "api/v3/payment/paymentIntent/payDueAmount";
								customLogger.info(stnRefNum,"payDueAmount url : " + urlToRead);
								URL url = null;
								StringBuilder result = new StringBuilder();
								url = new URL(urlToRead);
								HttpURLConnection conn = null;
								conn = (HttpURLConnection) url.openConnection();
								conn.setRequestMethod("POST");
								conn.setConnectTimeout(5000);
								conn.setDoOutput(true);
								conn.setDoInput(true);
								conn.setRequestProperty("Content-Type", "application/json");
								conn.setRequestProperty("EVG-Correlation-ID", mobileAuthKey);
								conn.setRequestProperty("Accept", "application/json");
								JSONObject requestBody   = new JSONObject();
								requestBody.put("uid",uuid);

								customLogger.info(stnRefNum,"payDueAmount url requestBody : " + requestBody);

								OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
								wr.write(String.valueOf(requestBody));
								wr.flush();
								BufferedReader rd = null;
								rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
								String line;
								while ((line = rd.readLine()) != null) {
									result.append(line);
								}
								customLogger.info(stnRefNum,"negative balance url response : " + result);
								rd.close();
							}catch (Exception e) {
								e.printStackTrace();
							}
						}
					};
					th.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Boolean getUserNotificationflag(Long userId) {
		Boolean singleRecord = false;
		try {
			singleRecord = generalDao
					.getFlag("select sessionEnds from PreferredNotification where userid=" + userId + "");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return singleRecord;
	}
	public void deletePushNotifications(Long userId) {
		try {
			String deleteNotiQuery = "delete from pushNotification where userId=" + userId + "";

			executeRepository.update(deleteNotiQuery);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void deleteSessionCharging_activeTransactions(SessionImportedValues siv) {
		try {
			String deleteActiveTrans = "delete from ocpp_activeTransaction where stationid='" + siv.getStTxnObj().getStationId()+ "' and connectorId = '" + siv.getStTxnObj().getConnectorId() + "'";
			logger.info(Thread.currentThread().getId()+"deleteSessionCharging_activeTransactions : " + deleteActiveTrans);
			executeRepository.update(deleteActiveTrans);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void insertStopTransactionData(Long connectorId, String rfidTag, Double stopTransactionMeterValue,
										  String reasonForTermination, String randomSessionId, Date stopTransTimeStamp) {
		try {

			OCPPStopTransaction stopTransaction = new OCPPStopTransaction();

			stopTransaction.setConnectorId(connectorId);
			stopTransaction.setIdTag(rfidTag);
			stopTransaction.setReason(reasonForTermination);
			stopTransaction.setTimeStamp(stopTransTimeStamp);
			stopTransaction.setMeterStop(stopTransactionMeterValue);
			stopTransaction.setSessionId(randomSessionId);

			generalDao.save(stopTransaction);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void connectedTimeBillingNotification(long userId, String randomSessionId, long orgId, String stationRefNum,
												 Map<String, Object> newComponent) {
		try {
			List<DeviceDetails> deviceDetails = OCPPDeviceDetailsService.getDeviceByUser(userId);
			JSONArray iOSRecipients = new JSONArray();
			JSONArray androidRecipients = new JSONArray();
			Map<String, Object> orgData = ocppUserService.getOrgData(orgId, stationRefNum);
			if (deviceDetails != null) {
				deviceDetails.forEach(device -> {
					try {
						if (device.getOrgId() != null && device.getOrgId().equals(orgId)) {
							if (device.getDeviceType().equalsIgnoreCase("Android")) {
								androidRecipients.add(device.getDeviceToken());
							} else if (device.getDeviceType().equalsIgnoreCase("iOS")) {
								iOSRecipients.add(device.getDeviceToken());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				});

				if (!androidRecipients.isEmpty()) {
					JSONObject info = new JSONObject();
					info.put("title", String.valueOf(orgData.get("orgName")));
					info.put("sound", "default");
					info.put("action", "Idle Billing");
					info.put("notificationId", "123123123");
					info.put("extra", String.valueOf(newComponent));
					info.put("body", "");
					info.put("userId", String.valueOf(userId));
					for(int i=0;i<androidRecipients.size();i++) {
						String receipt=String.valueOf(androidRecipients.get(i));
						pushNotification.pushNotificationForIosAndroid(receipt, "Android", info, stationRefNum, String.valueOf(orgData.get("token")),"Idle Billing",null,0);
					}
				}
				if(!iOSRecipients.isEmpty()) {
					JSONObject info = new JSONObject();
					info.put("title",String.valueOf(orgData.get("orgName")));
					info.put("type","Idle Billing");
					info.put("sound", "default");
					info.put("categoryIdentifier", "notification");
					info.put("content-available", "1");
					info.put("mutable_content", "true");
					info.putAll(newComponent);
					for(int i=0;i<iOSRecipients.size();i++) {
						String receipt=String.valueOf(iOSRecipients.get(i));
						pushNotification.pushNotificationForIosAndroid(receipt,"iOS",info,stationRefNum,String.valueOf(orgData.get("token")),"Idle Billing",null,0);
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void chargingSessionInvoice(SessionImportedValues siv,String userCurrencyCheck,String siteCurrencyCheck,BigDecimal finalCostWithoutSalesTax,BigDecimal salesTaxVal,
									   BigDecimal salesTaxPer,boolean additionalPricing,Map<String,Object> stationObj,String stationRefNum,Map<String,Object> portObj,
									   Map<String,Object> siteObj,String heading,double socStartVal,double socEndVal,String userCurrencySymbol,String duration,long orgId,String orgName,Date stopTransTimeStamp,
									   long sessId,boolean offlineFlag,String reasonForTermination,String sendMailId,String mailSubject,String stationGreetingMail,long userId,long stationId,String userTime,String accountName) {
		/*
		 * try { PreferredNotification preferedNofy =
		 * preferedNotifyService.getPreferedNofy(userId); if((preferedNofy != null &&
		 * preferedNofy.isEmailChargingCompleted() && userId > 0) || (userId == 0)) {
		 * BigDecimal
		 * processingFee=currencyConversion.convertCurrency(userCurrencyCheck,
		 * siteCurrencyCheck, new BigDecimal(String.valueOf(siv.getProcessingFee())));
		 * BigDecimal rate=currencyConversion.convertCurrency(userCurrencyCheck,
		 * siteCurrencyCheck, new BigDecimal(String.valueOf(siv.getPortPrice())));
		 * BigDecimal rate2=currencyConversion.convertCurrency(userCurrencyCheck,
		 * siteCurrencyCheck, new
		 * BigDecimal(String.valueOf(siv.getAdditionalVendingPricePerUnit())));
		 * BigDecimal cost=currencyConversion.convertCurrency(userCurrencyCheck,
		 * siteCurrencyCheck, finalCostWithoutSalesTax); BigDecimal
		 * txnFee=currencyConversion.convertCurrency(userCurrencyCheck,
		 * siteCurrencyCheck, new BigDecimal(String.valueOf(0.00))); BigDecimal
		 * connectedTimePrice=currencyConversion.convertCurrency(userCurrencyCheck,
		 * siteCurrencyCheck, new
		 * BigDecimal(String.valueOf(siv.getCostOfSameEnergy()))); BigDecimal
		 * connectorPrice1=currencyConversion.convertCurrency(userCurrencyCheck,
		 * siteCurrencyCheck, new BigDecimal(String.valueOf(siv.getConnectorPrice())));
		 * BigDecimal salesTax=currencyConversion.convertCurrency(userCurrencyCheck,
		 * siteCurrencyCheck, salesTaxVal); BigDecimal
		 * revenueForMail=utils.decimalwithtwodecimals(new
		 * BigDecimal(siv.getFinalCostInslcCurrency()));
		 * cost=utils.decimalwithtwodecimals(cost);
		 * processingFee=utils.decimalwithtwodecimals(processingFee);
		 * rate=utils.decimalwithtwodecimals(rate);
		 * txnFee=utils.decimalwithtwodecimals(txnFee);
		 * connectedTimePrice=utils.decimalwithtwodecimals(connectedTimePrice);
		 * connectorPrice1=utils.decimalwithtwodecimals(connectorPrice1);
		 * rate2=utils.decimalwithtwodecimals(rate2);
		 *
		 * String
		 * salesTaxPerForMail=utils.decimalwithtwoZeros(utils.decimalwithtwodecimals(
		 * salesTaxPer)); String
		 * SalesTaxForMail=utils.decimalwithtwoZeros(utils.decimalwithtwodecimals(
		 * salesTax)); String costForMail=utils.decimalwithtwoZeros(cost); String
		 * processingFeeForMail=utils.decimalwithtwoZeros(processingFee); String
		 * rateForMail=utils.decimalwithtwoZeros(rate); String
		 * txnFeeForMail=utils.decimalwithtwoZeros(txnFee); String
		 * connectedTimePriceForMail=utils.decimalwithtwoZeros(connectedTimePrice);
		 * String connectorPriceForMail=utils.decimalwithtwoZeros(connectorPrice1);
		 * String totalCostForMail=utils.decimalwithtwoZeros(revenueForMail); String
		 * portPriceForMail=utils.decimalwithtwoZeros(new
		 * BigDecimal(String.valueOf(siv.getPortPrice()))); String
		 * VendingPricePerUnit2ForMail=utils.decimalwithtwoZeros(rate2);
		 *
		 * Map<String,Object> stopMailDetails = new HashMap<String,Object>();
		 * if(additionalPricing) { stopMailDetails.put("Rate",
		 * "["+userCurrencySymbol+""+rateForMail+"/"+
		 * siv.getPortPriceUnit()+"]["+userCurrencySymbol+""+VendingPricePerUnit2ForMail
		 * +"/"+siv.getAdditionalVendingPriceUnit()+"]"); }else {
		 * stopMailDetails.put("Rate", userCurrencySymbol+""+rateForMail+"/"+
		 * siv.getPortPriceUnit()); }DecimalFormat df = new DecimalFormat("0.0000");
		 *
		 * Map<String, Object> dealerOrg =
		 * userService.getDealerOrgName(siv.getStationId());
		 * stopMailDetails.put("heading", heading); stopMailDetails.put("curDate",
		 * userTime); stopMailDetails.put("EnergyUsage",
		 * df.format(siv.getTotalKwUsed())+"kWh"); stopMailDetails.put("Duration",
		 * duration+"(HH:MM:SS)"); stopMailDetails.put("TotalCost",
		 * userCurrencySymbol+""+totalCostForMail); stopMailDetails.put("StationId",
		 * stationRefNum); stopMailDetails.put("Location",
		 * String.valueOf(stationObj.get("stationAddress")));
		 * stopMailDetails.put("ConnectorId", portObj.get("displayName"));
		 * stopMailDetails.put("mailType", "stopTxn"); stopMailDetails.put("orgId",
		 * orgId); stopMailDetails.put("StartTime",
		 * utils.stringToDate(siv.getStartTransTimeStamp()).replace("T",
		 * " ").replace("Z", " ") +" UTC"); stopMailDetails.put("EndTime",
		 * utils.stringToDate(stopTransTimeStamp).replace("T", " ").replace("Z", " ")
		 * +" UTC"); stopMailDetails.put("ChargerType",
		 * String.valueOf(stationObj.get("chagerType")));
		 * stopMailDetails.put("MaxPower",
		 * String.valueOf(portObj.get("capacity"))+"kW");
		 * stopMailDetails.put("ConnectorType",
		 * String.valueOf(portObj.get("connectorType")));
		 * stopMailDetails.put("OrganizationName",
		 * String.valueOf(dealerOrg.get("orgName")));
		 * stopMailDetails.put("whiteLabelName", orgName);
		 * stopMailDetails.put("socStartVal",
		 * String.valueOf(stationObj.get("chagerType")).equalsIgnoreCase("AC") ? "NA" :
		 * socStartVal < 0 ? "0%" : socStartVal > 100.0 ? "100.0%" :
		 * String.valueOf(socStartVal)+"%"); stopMailDetails.put("socEndVal",
		 * String.valueOf(stationObj.get("chagerType")).equalsIgnoreCase("AC") ? "NA" :
		 * socEndVal < 0 ? "0%" : socEndVal > 100.0 ? "100.0%" :
		 * String.valueOf(socEndVal)+"%"); stopMailDetails.put("TxnCost",
		 * userCurrencySymbol+""+costForMail); stopMailDetails.put("sessionId",
		 * String.valueOf(sessId)); String
		 * userCurrencySymbol1=Jsoup.parse(userCurrencySymbol).text(); if(userId > 0) {
		 * stopMailDetails.put("accName", accountName);
		 * stopMailDetails.put("processingFee",
		 * userCurrencySymbol+""+siv.getUserProcessingFee());
		 * stopMailDetails.put("processingFee1",
		 * userCurrencySymbol1+""+siv.getUserProcessingFee()); }else {
		 * stopMailDetails.put("accName", "Customer");
		 * stopMailDetails.put("processingFee",
		 * userCurrencySymbol+""+processingFeeForMail);
		 * stopMailDetails.put("processingFee1",
		 * userCurrencySymbol1+""+processingFeeForMail); } stopMailDetails.put("txnFee",
		 * userCurrencySymbol+""+txnFeeForMail); stopMailDetails.put("graceTime",
		 * Utils.getTimeFormate(siv.getUsedGraceTime())+"(HH:MM:SS)");
		 * stopMailDetails.put("connectedTime",Utils.getTimeFormate(siv.
		 * getDurationMinsOfSameEngy())+"(HH:MM:SS)");
		 * stopMailDetails.put("connectedTimePrice",
		 * userCurrencySymbol+""+connectedTimePriceForMail+"("+userCurrencySymbol+""+
		 * connectorPriceForMail+"/"+String.valueOf(stationObj.get("connectedTimeUnits")
		 * )+")"); stopMailDetails.put("contactUsNo", "949-945-2000");
		 * stopMailDetails.put("stnAddress",String.valueOf(stationObj.get(
		 * "stationAddress"))); stopMailDetails.put("salesTax",
		 * userCurrencySymbol+""+SalesTaxForMail); stopMailDetails.put("salesTaxPerc",
		 * salesTaxPerForMail+"%");
		 * stopMailDetails.put("StationName",String.valueOf(stationObj.get("stationName"
		 * )));//new stopMailDetails.put("stationAddress",String.valueOf(stationObj.get(
		 * "stationAddress")));
		 * stopMailDetails.put("chargerCapacity",String.valueOf(portObj.get("capacity"))
		 * ); stopMailDetails.put("PortId",String.valueOf(portObj.get("connector_id")));
		 * stopMailDetails.put("TotalEnergy",String.valueOf(siv.getTotalKwUsed()));
		 * stopMailDetails.put("ReasonForTermination",offlineFlag==true ?
		 * "offlineTransaction" : reasonForTermination +
		 * "("+utils.stringToDate(stopTransTimeStamp).replace("T", " ").replace("Z",
		 * " ") +" UTC" + ")");
		 * stopMailDetails.put("StartSoC",String.valueOf(socStartVal));
		 * stopMailDetails.put("EndSoC",String.valueOf(socEndVal));
		 * stopMailDetails.put("siteName",String.valueOf(siteObj.get("siteName")));
		 * stopMailDetails.put("hourlyParkingCost","$0.00");
		 * stopMailDetails.put("totalParkingCost","$0.00");
		 * stopMailDetails.put("paymentMethod",siv.getPaymentMode());
		 * stopMailDetails.put("randomSession", siv.getRandomSessionId());
		 *
		 *
		 * if(additionalPricing) { stopMailDetails.put("Rate1",
		 * "["+userCurrencySymbol1+""+rateForMail+"/"+
		 * siv.getPortPriceUnit()+"]["+userCurrencySymbol1+""+
		 * VendingPricePerUnit2ForMail+"/"+siv.getAdditionalVendingPriceUnit()+"]");
		 * }else { stopMailDetails.put("Rate1", userCurrencySymbol1+""+rateForMail+"/"+
		 * siv.getPortPriceUnit()); } stopMailDetails.put("TxnCost1",
		 * userCurrencySymbol1+""+costForMail);
		 * stopMailDetails.put("connectedTimePrice1",
		 * userCurrencySymbol1+""+connectedTimePriceForMail+"("+userCurrencySymbol1+""+
		 * connectorPriceForMail+"/"+String.valueOf(stationObj.get("connectedTimeUnits")
		 * )+")"); stopMailDetails.put("txnFee1", userCurrencySymbol1+""+txnFeeForMail);
		 * stopMailDetails.put("TotalCost1", userCurrencySymbol1+""+totalCostForMail);
		 *
		 * stopMailDetails.put("salesTax1", userCurrencySymbol1+""+SalesTaxForMail);
		 * stopMailDetails.put("processingFee1", "$"+""+processingFeeForMail);
		 * stopMailDetails.put("socValue1", "$"+""+SalesTaxForMail); Map<String, Object>
		 * logoData =
		 * userService.logoDeatils(Long.valueOf(String.valueOf(stopMailDetails.get(
		 * "orgId"))), String.valueOf(stopMailDetails.get("StationId")));
		 * stopMailDetails.put("imagePath", String.valueOf(logoData.get("url")));
		 * boolean flag=false; List<Map<String,Object>>
		 * listdata=dynamicTariffService.getSessionData(sessId);
		 * List<Map<String,Object>> list=new ArrayList(); for(Map<String,Object> data :
		 * listdata){ flag=true; data.put("duration",
		 * Utils.getTimeFormate(Double.parseDouble(String.valueOf(data.get(
		 * "sessionDurationInMin"))))+"(HH:MM:SS)");
		 * data.put("totalKwUsed",String.valueOf(data.get("totalKwUsed"))+"kWh");
		 * list.add(data); } stopMailDetails.put("tou", flag);
		 * stopMailDetails.put("orders", list);
		 *
		 * customLogger.info(stationRefNum,
		 * "Stop Txn Mail is Calling ....."+stopMailDetails);
		 * pdfGenerator.generatePDF(stopMailDetails, orgId,userId);
		 * emailServiceImpl.sendEmail(new MailForm(sendMailId, mailSubject,
		 * stationGreetingMail),stopMailDetails,orgId,stationRefNum,stationId); }else {
		 * logger.info(Thread.currentThread().getId()+"user disabled the charging session completed mail alert"); }
		 *
		 * }catch (Exception e) { e.printStackTrace(); }
		 */
	}
	public void deleteOCPPSessionData(String sessionId) {
		try {
			Thread th = new Thread(){
				public void run() {
					String deleteActiveTrans1 = "delete from ocpp_sessionData where sessionId='" + sessionId + "'";
					executeRepository.update(deleteActiveTrans1);
				}
			};
			th.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public Map<String,Object> getSessionData(Long transactionId){
		Map<String,Object> map=new HashMap<String,Object>();
		try {
			String query="select isnull(s.cost,'0.0') as finalEnergyCost1,isnull(s.cost2,'0.0') as finalEnergyCost2,isnull(s.finalCostInSlcCurrency,'0.0') as finalCost,isnull(s.portPrice,'0.0') as portPrice,"
					+ " isnull(s.portPriceUnit,'kWh') as portPriceUnit,isnull(s.kilowattHoursUsed,'0.0') as kilowattHoursUsed,"
					+ " ROUND(isnull(s.sessionElapsedInMin,'0.0'), 2) as sessionElapsedInMin,port_id as portId from session s inner join ocpp_startTransaction os on s.sessionId=os.sessionId where os.transactionId='"+transactionId+"'";
			List list=executeRepository.findAll(query);
			if(list.size()>0&&list!=null) {
				map=executeRepository.findAll(query).get(0);
			}else {
				map.put("finalCost", "0.0");
				map.put("portPrice", "0.0");
				map.put("portPriceUnit", "kWh");
				map.put("kilowattHoursUsed", "0.0");
				map.put("sessionElapsedInMin", "0.0");
				map.put("portId", "0");
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		return map;
	}
	@Override
	public SessionImportedValues insertIntoSession(SessionImportedValues siv) {
		try {
			Session session = generalDao.findOne("FROM Session where sessionId='"+siv.getChargeSessUniqId()+"'", new Session());
			if(session != null) {
				long meterValueCount=0;
				Double socStart=0.00;
				Double socEnd=Double.valueOf(String.valueOf(siv.getSoCValue()).equalsIgnoreCase("null") == true ? "0.00" : String.valueOf(siv.getSoCValue()));
				session_pricings sessionpricings=siv.getSesspricings();
				if(sessionpricings!=null) {
					meterValueCount=sessionpricings.getMetervaluesCount();
				}
				String power=siv.getPowerActiveImportValueES()==null ? "0" : siv.getPowerActiveImportValueES();
				String avgPower=power;
				String peakPower=power;
				double minPower=Double.valueOf(power);
				if(!power.equalsIgnoreCase("null") && session.getAvg_power() != null && !session.getAvg_power().equalsIgnoreCase("null") ) {
					peakPower=String.valueOf(Double.parseDouble(power)>session.getPowerActiveImport_value() ? power : session.getPowerActiveImport_value());
					minPower=Double.parseDouble(power)<session.getMin_power() ? Double.valueOf(avgPower) : session.getMin_power();
					avgPower=String.valueOf((Double.parseDouble(String.valueOf(avgPower))+Double.parseDouble(String.valueOf(session.getAvg_power())))/2);
				}
				logger.info(Thread.currentThread().getId()+"meterValueCount : "+meterValueCount);
				if(meterValueCount==0) {
					socStart=Double.valueOf(String.valueOf(siv.getSoCValue()).equalsIgnoreCase("null") == true ? "0.00" : String.valueOf(siv.getSoCValue()));
				}else {
					socStart=session.getSocStartVal();
					socEnd=socEnd>session.getSocEndVal()?socEnd:session.getSocEndVal();
				}
				socStart = socStart > 100 ? 100 : socStart;
				socEnd = socEnd > 100 ? 100 : socEnd;
				session.setFinalCostInSlcCurrency(siv.getFinalCostInslcCurrency());
				session.setCost(siv.getFinalCosttostore());
				session.setSessionElapsedInMin(siv.getSessionDuration().doubleValue());
				session.setEndTimeStamp(siv.getMeterValueTimeStatmp());
				session.setKilowattHoursUsed(siv.getTotalKwUsed().doubleValue());
				session.setStartTxnProgress(false);
				session.setTransactionStatus("going on");
				session.setSocStartVal(socStart);
				session.setSocEndVal(socEnd);
				session.setAvg_power(avgPower);
				session.setMin_power(minPower);
				session.setStartTimeStamp(siv.getStartTransTimeStamp());
				session.setPowerActiveImport_value(Double.valueOf(peakPower));
				session.setSuccessFlag(Double.valueOf(String.valueOf(siv.getTotalKwUsed().doubleValue())) >= 0.25 && Double.valueOf(String.valueOf(siv.getSessionDuration().doubleValue())) >= 5);
				if(siv.getTxnData().getReward() != null) {
					JsonNode rewardPrices = objectMapper.readTree(siv.getTxnData().getReward());
					session.setRewardValue(session.getRewardType().equalsIgnoreCase("Amount") ? rewardPrices.get(0).get("usedAmount").asDouble() : session.getRewardType().equalsIgnoreCase("kWh") ? rewardPrices.get(0).get("usedkWh").asDouble() : 0);
				}
				session=generalDao.update(session);
				siv.setChargeSessId(session.getId());
				siv.setSession(session);
			}else {
				logger.info(Thread.currentThread().getId()+"session obj is not there");
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return siv;
	}
	@Override
	public SessionImportedValues 	insertIntoStopSessionData(SessionImportedValues siv) {
		try {
			Session session = generalDao.findOne("FROM Session where sessionId='"+siv.getChargeSessUniqId()+"'", new Session());
			if(session != null) {
				String reasonForTer=siv.getStTxnObj().isOfflineFlag() ? "OfflineTransaction" : siv.getReasonForTermination();
				logger.info(Thread.currentThread().getId()+"reasonForTer : "+reasonForTer);
				siv.setReasonForTermination(reasonForTer);
				String settlement="settled";
				if(siv.isIdleBilling() && !siv.getStTxnObj().isOfflineFlag()) {
					if(reasonForTer.equalsIgnoreCase("Remote")||reasonForTer.equalsIgnoreCase("EVDisconnected")||reasonForTer.equalsIgnoreCase("Local")) {
						reasonForTer=session.getReasonForTer();
						settlement=session.getSettlement();
					}else {
						siv.setIdleBilling(false);
						if(siv.getTxnData() != null && (siv.getTxnData().getBillingCases().equalsIgnoreCase("TOU+Rewards") || siv.getTxnData().getBillingCases().equalsIgnoreCase("TOU+Free+Rewards"))) {
							siv = RewardAmountForNormalUser(siv);
						}
						siv = amountDeduction(siv);
					}
				}

				session.setFinalCostInSlcCurrency(siv.getFinalCostInslcCurrency());
				session.setCost(siv.getFinalCosttostore());
				session.setSessionElapsedInMin(siv.getSessionDuration().doubleValue());
				session.setEndTimeStamp(siv.getMeterValueTimeStatmp());
				session.setKilowattHoursUsed(siv.getTotalKwUsed().doubleValue());
				session.setSessionStatus("Completed");
				session.setSettlement(settlement);
				session.setReasonForTer(reasonForTer);
				session.setTransactionStatus("completed");
				session.setSuccessFlag(Double.valueOf(String.valueOf(siv.getTotalKwUsed().doubleValue())) >= 0.25 && Double.valueOf(String.valueOf(siv.getSessionDuration().doubleValue())) >= 5);
//				session.setSettlementTimeStamp(siv.getSettlementTimeStamp());

				if(siv.getAccTxns() != null) {
					AccountTransactions acc = new AccountTransactions();
					acc.setId(siv.getAccTxns() != null ? siv.getAccTxns().getId():null);
					session.setAccountTransaction(acc);
				}

				if(siv.getTxnData().getReward() != null) {
					JsonNode rewardPrices = objectMapper.readTree(siv.getTxnData().getReward());
					session.setRewardValue(session.getRewardType().equalsIgnoreCase("Amount") ? rewardPrices.get(0).get("usedAmount").asDouble() : session.getRewardType().equalsIgnoreCase("kWh") ? rewardPrices.get(0).get("usedkWh").asDouble() : 0);
				}
				session=generalDao.update(session);
				siv.setChargeSessId(session.getId());
				siv.setSession(session);
			}else {
				logger.info(Thread.currentThread().getId()+"session obj is not there");
			}
		}catch (Exception e) {
			logger.error(""+e);
		}
		return siv;
	}
	@Override
	public void insertIntoSessionPricings(SessionImportedValues siv) {
		try {
			session_pricings sessionPricings = siv.getSesspricings();
			if(sessionPricings != null) {
				sessionPricings.setCost_info(siv.getCost_pricings());
				sessionPricings.setMetervaluesCount(sessionPricings.getMetervaluesCount() + 1);
				generalDao.update(sessionPricings);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public SessionImportedValues amountDeduction(SessionImportedValues siv) {
		try {
			if(!siv.getTxnData().getUserType().equalsIgnoreCase("Unknown") && !siv.isIdleBilling()) {
				float currncyRate = Float.parseFloat(String.valueOf(crncyConversionService.currencyRate(String.valueOf(siv.getUserObj().get("crncy_Code").asText()),String.valueOf(siv.getSiteObj().get("crncy_Code").asText()))));
				siv.setCurrencyRate(currncyRate);
				if (!String.valueOf(siv.getUserObj().get("crncy_Code").asText()).equalsIgnoreCase(String.valueOf(siv.getSiteObj().get("crncy_Code").asText()))) {
					siv.setUser_crncy_revenue(currencyConversion.convertCurrency(String.valueOf(siv.getUserObj().get("crncy_Code").asText()), String.valueOf(siv.getSiteObj().get("crncy_Code").asText()), new BigDecimal(siv.getNeedToDebit())).doubleValue());
				}else if(String.valueOf(siv.getUserObj().get("crncy_Code").asText()).equalsIgnoreCase(String.valueOf(siv.getSiteObj().get("crncy_Code").asText()))){
					siv.setUser_crncy_revenue(siv.getNeedToDebit());
				}

				if(siv.getTxnData().getUserType() != null && siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser") ) {
					if(siv.getTxnData().getPaymentMode().equalsIgnoreCase("Wallet")) {
						siv = insertIntoAccountTransaction(siv);
					}else if(siv.getTxnData().getPaymentMode().equalsIgnoreCase("Reward")) {
						siv = insertIntoAccountTransaction(siv);
					}else if(siv.getTxnData().getPaymentMode().equalsIgnoreCase("Card")) {
						siv = insertIntoAccountTransaction(siv);
					}
				}else if(siv.getTxnData().getUserType() != null && siv.getTxnData().getUserType().equalsIgnoreCase("PAYG")&& !siv.isIdleBilling()) {
					siv = insertIntoAccountTransactionPayG(siv);
					paymentService.capture(siv.getChargeSessUniqId(),siv.getFinalCostInslcCurrency(),siv.getPaygAccTxns().getId(),siv.getStnRefNum());
				}
			}
		}catch (Exception e) {
			logger.error(""+e);
		}
		return siv;
	}
	private SessionImportedValues insertIntoAccountTransactionPayG(SessionImportedValues siv) {
		try {
			AccountTransactionForGuestUser guestUserTransaction = null;
			try {
				guestUserTransaction = generalDao.findOne("From AccountTransactionForGuestUser where sessionId='" + siv.getChargeSessUniqId() + "'",new AccountTransactionForGuestUser());

				if (guestUserTransaction == null)
					guestUserTransaction = new AccountTransactionForGuestUser();
				else
					guestUserTransaction.setId(guestUserTransaction.getId());

				guestUserTransaction.setRevenue(siv.getFinalCostInslcCurrency());
				guestUserTransaction.setSessionId(siv.getChargeSessUniqId());
				guestUserTransaction.setTime(utils.getUTCDate());
				guestUserTransaction.setPhone(siv.getIdTag());
				guestUserTransaction.setFlag(true);
				guestUserTransaction.setStationId(siv.getStnId());

				siv.setPaygAccTxns(generalDao.saveOrupdate(guestUserTransaction));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return siv;
	}
	@Override
	public SessionImportedValues deleteTxnData(SessionImportedValues siv) {
		try {
			boolean dataDelete = false;
			OCPPTransactionData txnData = siv.getTxnData();
			logger.info(Thread.currentThread().getId()+"!siv.isIdleBilling() : "+!siv.isIdleBilling());
			if(txnData != null && (Double.parseDouble(String.valueOf(siv.getTotalKwUsed())) < txnData.getMinkWhEnergy() || !siv.isIdleBilling())) {
				dataDelete = true;
			}else if(siv.isIdleBilling()){
				dataDelete = false;
			}
			if(dataDelete) {
				String query="delete from ocpp_TransactionData where sessionId='"+siv.getChargeSessUniqId()+"'";
				executeRepository.update(query);
				siv.setCurrentScreen(true);
				deleteSessionCharging_activeTransactions(siv);
			}else {
				double maximumRevenue=txnData.getMaximumRevenue()-siv.getFinalCostInslcCurrency();
				if(maximumRevenue<0) {
					maximumRevenue=0;
				}
				String query="update ocpp_TransactionData set reasonForTer='"+siv.getReasonForTermination()+"',stop=1,maximumRevenue='"+maximumRevenue+"',reward='"+siv.getTxnData().getReward()+"' where sessionId='"+siv.getChargeSessUniqId()+"' ";
				executeRepository.update(query);
			}
			try {
				String delete="delete from  session_energy where sessionId='"+siv.getChargeSessUniqId()+"'";
				executeRepository.update(delete);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return siv;
	}

	@Override
	public Map<String, Object> getPayGBySessionId(String sessionId) {
		Map<String, Object> findAll=new HashMap<>();
		try {
			findAll = executeRepository.findMap("select id as preAuthId,phone,userType,authorizeAmount from userPayment where sessionId='"+sessionId+"' and count < 3 order by id desc");
		}catch (Exception e) {
			e.printStackTrace();
		}
		return findAll;
	}
//	@Override
//	public void updatePayGSession(SessionImportedValues siv) {
//		try {
//			if(siv.getTxnData().getUserType().equalsIgnoreCase("PAYG")) {
//				Map<String, Object> paygData = getPayGBySessionId(siv.getChargeSessUniqId());
//				customLogger.info(siv.getStnRefNum(), "userPaymentDetailsBySessionId : "+paygData);
//				if(paygData != null && !paygData.isEmpty()) {
//					String userPaymentId = String.valueOf(paygData.get("preAuthId"));
//					String urlToRead = mobileServerUrl+"api/v3/payment/paymentIntent/capture";
//					customLogger.info(siv.getStnRefNum(), "urlToRead : "+urlToRead);
//					Map<String, Object> params = new HashMap<String, Object>(); 
//					params.put("userPaymentId", userPaymentId);
//					params.put("userType", "GuestUser");
//					params.put("captureAmount", siv.getFinalCostInslcCurrency());
//					params.put("accTxnId", siv.getPaygAccTxns().getId());
//					HttpHeaders headers = new HttpHeaders();
//					headers.set("Content-Type", "application/json");
//					headers.set("EVG-Correlation-ID", mobileAuthKey);
//					HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(params, headers);
//					customLogger.info(siv.getStnRefNum(), "payment Type stripe capture request body : "+params);
//					utils.apicallingPOST(urlToRead, requestEntity);
//				}
//			}
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	@Override
	public void ocpiMeterReadingCall(String requestMessage, String stnRefNum,String sessionId) {
		try {
			Thread newThread = new Thread() {
				public void run() {
					try {
						String url= ocpiUrl + "ocpi/ocpp/meterreading";
						TransactionForm transactionForm=new TransactionForm();
						transactionForm.setClientId(stnRefNum);
						transactionForm.setMessage(requestMessage);
						transactionForm.setSessionId(sessionId);
						HttpHeaders headers = new HttpHeaders();
						headers.set("Content-Type", "application/json");
						HttpEntity<TransactionForm> requestEntity = new HttpEntity<TransactionForm>(transactionForm, headers);
						restTemplate.setRequestFactory(utils.httpConfig(500, 500));
						restTemplate.postForEntity(url, requestEntity, TransactionForm.class);
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			};
			newThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void ocpiMeterStopCall(String requestMessage, String stnRefNum,String sessionId,long stnId) {
		try {
			Thread newThread = new Thread() {
				public void run() {
					try {
						String url= ocpiUrl + "ocpi/ocpp/stop";
						TransactionForm transactionForm=new TransactionForm();
						transactionForm.setClientId(stnRefNum);
						transactionForm.setMessage(requestMessage);
						transactionForm.setSessionId(sessionId);
						transactionForm.setStnId(stnId);
						HttpHeaders headers = new HttpHeaders();
						headers.set("Content-Type", "application/json");
						HttpEntity<TransactionForm> requestEntity = new HttpEntity<TransactionForm>(transactionForm, headers);
						restTemplate.setRequestFactory(utils.httpConfig(500, 500));
						restTemplate.postForEntity(url, requestEntity, TransactionForm.class);
					}catch(Exception e) {
						//e.printStackTrace();
					}
				}
			};
			newThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void updateTxnDataRSTPFlag(String sessionId,int val) {
		try {
			String str = "update ocpp_TransactionData set rstp="+val+" where sessionId='"+sessionId+"'";
			executeRepository.update(str);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public SessionImportedValues lowBalanceValidation(SessionImportedValues siv) {
		try {
			if (!siv.getStTxnObj().isOfflineFlag()) {
				if (siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser") && !siv.getTxnData().isRstp() && !siv.getTxnData().isDgNoZeroBal()) {
					Double accBal = OCPPAccountAndCredentialService.getNormalUserBalance(siv.getStTxnObj().getUserId(), siv.getStnRefNum());
					logger.info(Thread.currentThread().getId() + "accBal : " + accBal);
					boolean cardChecking = cardChecking(Long.valueOf(String.valueOf(siv.getUserObj().get("UserId").asLong())));
					if (siv.getNeedToDebit() > 0 && siv.getNeedToDebit() > accBal && !siv.getTxnData().isRstp() && !cardChecking) {
						siv.getTxnData().setRstp(true);
						OCPPForm of = new OCPPForm();
						of.setStationId(Long.valueOf(String.valueOf(siv.getStnObj().get("stnId").asLong())));
						of.setConnectorId(Long.valueOf(String.valueOf(siv.getStnObj().get("portId").asLong())));
						of.setRequestType("RemoteStop");
						utils.ocppStopCalling(of);
						alertsService.lowBalancePushNotiAlert(siv);
						alertsService.lowBalanceMailAlert(siv, utils.decimalwithtwodecimals(new BigDecimal((accBal - siv.getNeedToDebit()))).doubleValue());
						alertsService.smsLowBalanceAlert(siv);
					}
				} else if (siv.getTxnData().getUserType().equalsIgnoreCase("PAYG") && !siv.getTxnData().isRstp() && !siv.getTxnData().getBillingCases().contains("Free")) {
					JsonNode userJson = objectMapper.readTree(siv.getTxnData().getUser_obj());
					if (userJson.size() > 0) {
						Double authorizeAmount = userJson.get("authorizeAmount").asDouble();
						logger.info(Thread.currentThread().getId() + "authorizeAmount : " + authorizeAmount);
						if (authorizeAmount != 0 && siv.getFinalCostInslcCurrency() > (authorizeAmount - 2) && !siv.getTxnData().isRstp()) {
							siv.getTxnData().setRstp(true);
							OCPPForm of = new OCPPForm();
							of.setStationId(Long.valueOf(String.valueOf(siv.getStnObj().get("stnId").asLong())));
							of.setConnectorId(Long.valueOf(String.valueOf(siv.getStnObj().get("portId").asLong())));
							of.setRequestType("RemoteStop");
							utils.ocppStopCalling(of);
						}
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return siv;
	}

	@Override
	public void updateTxnData(OCPPTransactionData TXN) {
		try {
			generalDao.update(TXN);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sessionInterruptAlert(SessionImportedValues siv, StopTransaction stopTxnObj) {
		try {
			String reason = stopTxnObj.getReason();
			if(reason != null && (reason.equalsIgnoreCase("EmergencyStop") || reason.equalsIgnoreCase("EvDisconnected") || reason.equalsIgnoreCase("HardReset")  || reason.equalsIgnoreCase("UnlockCommand") ||
					reason.equalsIgnoreCase("PowerLoss") || reason.equalsIgnoreCase("Reboot") || reason.equalsIgnoreCase("SoftReset") || reason.equalsIgnoreCase("TimeExpired"))) {
				alertsService.sessionInterruptedPushNotifyAlert(siv);
				alertsService.sessionInterruptedMailAlert(siv);
				alertsService.smsSessionInterruptedAlert(siv);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void idleBilling(String sessionId,String time) {
		try {
			OCPPTransactionData txnData = generalDao.findOne("FROM OCPPTransactionData where sessionId='"+sessionId+"' and stop=1 order by id desc", new OCPPTransactionData());
			if(txnData!=null && !txnData.isOfflineTransaction()) {
				Date statsTime=utils.stringToDate(time);
				JsonNode tariff = objectMapper.readTree(txnData.getTariff_prices());
				if(tariff.size() > 0) {
					JsonNode prices = objectMapper.readTree(String.valueOf(tariff.get(0).get("cost_info")));
					if(prices.size() > 0) {
						JsonNode aditional = objectMapper.readTree(String.valueOf(prices.get(0).get("aditional")));
						if(aditional.size() > 0) {
							JsonNode idleCharge = objectMapper.readTree(String.valueOf(aditional.get("idleCharge")));
							logger.info(Thread.currentThread().getId()+"idleCharge : "+idleCharge);
							if(idleCharge.size() > 0) {
								BigDecimal idleChargePrice=new BigDecimal(idleCharge.get("price").asText());
								BigDecimal idleBillCapCost = new BigDecimal(0.00);
								BigDecimal gracePeriod=new BigDecimal(idleCharge.get("gracePeriod").asText());
								double step = idleCharge.get("step").asDouble();
								Session ses = generalDao.findOne("FROM Session where sessionId='"+txnData.getSessionId()+"' order by id desc", new Session());
								session_pricings sesPrices = generalDao.findOne("FROM session_pricings where sessionId='"+txnData.getSessionId()+"' order by id desc", new session_pricings());
								BigDecimal costWithTax=new BigDecimal("0.0");
								BigDecimal needToDebit=new BigDecimal("0.0");
								if(ses!=null) {
									BigDecimal finalCost =new BigDecimal(String.valueOf(ses.getFinalCostInSlcCurrency()));
									Map<String, BigDecimal> idleTime = utils.getTimeDifferenceInMiliSec(ses.getEndTimeStamp(), statsTime);
									BigDecimal idleTimeMins = new BigDecimal(String.valueOf(idleTime.get("Minutes")));
									BigDecimal idleBillCap = new BigDecimal(idleCharge.get("idleBillCap").asText());
									BigDecimal maximumRevenue=new BigDecimal(String.valueOf(txnData.getMaximumRevenue()));
									if(idleBillCap.doubleValue()>maximumRevenue.doubleValue()) {
										idleBillCap=maximumRevenue;
									}
									logger.info(Thread.currentThread().getId()+"idleTimeMins : "+idleTimeMins);
									BigDecimal cost = new BigDecimal("0.00");
									BigDecimal tempIdleMins =new BigDecimal("0.00");
									Map<String,Object> idlePrice = new HashMap<String,Object>();
									if(ses.getCost()>0 && idleTimeMins.doubleValue() > 0 && idleTimeMins.doubleValue() > gracePeriod.doubleValue() && !txnData.getIdleStatus().equalsIgnoreCase("faulted") &&
											!txnData.getIdleStatus().equalsIgnoreCase("Preparing") && !txnData.getIdleStatus().equalsIgnoreCase("SuspendedEVSE") && !txnData.getIdleStatus().equalsIgnoreCase("SuspendedEV") && !txnData.getIdleStatus().equalsIgnoreCase("Charging")&& !txnData.getIdleStatus().equalsIgnoreCase("Available")) {

										tempIdleMins = idleTimeMins.subtract(gracePeriod);

										if(step == 3600) {
											cost = cost.add(tempIdleMins.multiply(new BigDecimal(60)).multiply(idleChargePrice.divide(new BigDecimal(3600), 15, RoundingMode.HALF_UP))).setScale(2,RoundingMode.HALF_UP);
										}
										if(step == 60) {
											cost = cost.add(tempIdleMins.multiply(new BigDecimal(60)).multiply(idleChargePrice.divide(new BigDecimal(60), 15, RoundingMode.HALF_UP))).setScale(2,RoundingMode.HALF_UP);
										}
										logger.info(Thread.currentThread().getId()+"idleTimeMins cost : "+cost);

										idleBillCapCost = cost;
										if(cost.doubleValue() >= idleBillCap.doubleValue() ) {
											cost = idleBillCap;
										}
										cost = cost.setScale(2,RoundingMode.HALF_UP);
										idlePrice.put("idleStartTime", utils.stringToDate(utils.addDateSec(ses.getEndTimeStamp(),(int) gracePeriod.doubleValue()*60)));
										idlePrice.put("idleEndTime", utils.stringToDate(statsTime));

									}
									JsonNode costInfoJSON = objectMapper.readTree(sesPrices.getCost_info());
									logger.info(Thread.currentThread().getId()+"costInfoJSON : "+costInfoJSON);
									if(costInfoJSON.size() > 0) {
										List<Map<String,Object>> tariffLs = new ArrayList<>();
										List<Map<String,Object>> pricesLs = new ArrayList<>();
										Map<String,Object> tariffMap = new HashMap<>();
										Map<String,Object> costInfoMap = new HashMap<>();
										tariffMap.put("tariffId", costInfoJSON.get(0).get("tariffId"));
										tariffMap.put("max_price_id", costInfoJSON.get(0).get("max_price_id"));
										tariffMap.put("min_price_id", costInfoJSON.get(0).get("min_price_id"));
										tariffMap.put("tariffName", costInfoJSON.get(0).get("tariffName"));
										tariffMap.put("startTime", costInfoJSON.get(0).get("startTime"));
										tariffMap.put("endTime", costInfoJSON.get(0).get("endTime"));


										Map<String,Object> aditionalSubMap = new HashMap<>();
										List<Map<String,Object>> taxls=new ArrayList();
										JsonNode taxes=objectMapper.readTree(String.valueOf(costInfoJSON.get(0).get("cost_info").get(0).get("aditional").get("tax")));
										JsonNode rateRider=objectMapper.readTree(String.valueOf(costInfoJSON.get(0).get("cost_info").get(0).get("aditional").get("rateRider")));
										String rateRiderType = "";
										BigDecimal rateRiderAmount=new BigDecimal("0");
										if(rateRider.size()>0) {
											rateRiderType = rateRider.get("type").asText();
											rateRiderAmount=new BigDecimal(rateRider.get("amount").asText()).setScale(2, RoundingMode.HALF_UP);

										}
										BigDecimal totalTaxPer=new BigDecimal("0");
										if(taxes!=null && taxes.size()>0) {
											for(int i=0;i < taxes.size();i++) {
												JsonNode taxJsonMap = objectMapper.readTree(String.valueOf(taxes.get(i)));
												if(taxJsonMap.size() > 0) {
													totalTaxPer=totalTaxPer.add(new BigDecimal(taxJsonMap.get("percnt").asText()));
												}
											}
										}
										if(totalTaxPer.doubleValue()>0 && cost.doubleValue()>0) {
											BigDecimal multiply = (cost.divide(new BigDecimal(100),9, RoundingMode.HALF_UP).multiply(totalTaxPer)).setScale(2, RoundingMode.HALF_UP);
											BigDecimal finalCostStore=multiply.add(cost);
											if(finalCostStore.doubleValue()>maximumRevenue.doubleValue()) {
												BigDecimal costWithoutTax=(new BigDecimal(String.valueOf("100")).multiply(maximumRevenue)).divide(new BigDecimal(String.valueOf("100")).add(totalTaxPer), 2, RoundingMode.HALF_UP);
												cost=costWithoutTax;
											}
										}
										cost = cost.setScale(2,RoundingMode.HALF_UP);
										costWithTax=cost;
										BigDecimal totalTax=new BigDecimal("0.0");
										BigDecimal connectedTimeBill=cost;
										if(taxes!=null && taxes.size()>0) {
											for(int i=0;i < taxes.size();i++) {
												JsonNode taxJsonMap = objectMapper.readTree(String.valueOf(taxes.get(i)));
												if(taxJsonMap.size() > 0) {
													Map<String,Object> taxSubMap = new HashMap<>();
													String name = taxJsonMap.get("name").asText();
													BigDecimal taxAmount = new BigDecimal(taxJsonMap.get("amount").asText());
													BigDecimal taxPercent = new BigDecimal(taxJsonMap.get("percnt").asText());
													BigDecimal multiply = (cost.divide(new BigDecimal(100),9, RoundingMode.HALF_UP).multiply(taxPercent)).setScale(2, RoundingMode.HALF_UP);
													costWithTax=costWithTax.add(multiply);
													BigDecimal totaltaxAmount=taxAmount.add(multiply).setScale(2,RoundingMode.HALF_UP);

													taxSubMap.put("name", name);
													taxSubMap.put("percnt", taxPercent);
													taxSubMap.put("restrictionType", "TAX");
													taxSubMap.put("amount", totaltaxAmount);
													taxSubMap.put("idleAmount", multiply);
													taxSubMap.put("chargingAmount", taxAmount);
													taxls.add(taxSubMap);
													totalTax=totalTax.add(totaltaxAmount).setScale(2,RoundingMode.HALF_UP);
												}
											}
										}
										finalCost =new BigDecimal(String.valueOf(ses.getCost())).add(totalTax).add(connectedTimeBill).setScale(2,RoundingMode.HALF_UP);
										if(rateRiderType.equalsIgnoreCase("-ve")) {
											finalCost =new BigDecimal(String.valueOf(ses.getCost())).add(totalTax).add(connectedTimeBill).subtract(rateRiderAmount).setScale(2,RoundingMode.HALF_UP);
										}else if(rateRiderType.equalsIgnoreCase("+ve")) {
											finalCost =new BigDecimal(String.valueOf(ses.getCost())).add(totalTax).add(connectedTimeBill).add(rateRiderAmount).setScale(2,RoundingMode.HALF_UP);
										}
//										finalCost = finalCost.add(costWithTax).setScale(2,RoundingMode.DOWN);
										costWithTax=costWithTax.setScale(2,RoundingMode.HALF_UP);
										needToDebit=finalCost.subtract(new BigDecimal(String.valueOf(ses.getFinalCostInSlcCurrency())));
										cost=cost.setScale(2,RoundingMode.HALF_UP);
										idlePrice.put("inActiveCost", cost);
										idlePrice.put("idleBillCap", idleBillCapCost);
										idlePrice.put("price", idleChargePrice);
										idlePrice.put("step", step);
										idlePrice.put("restrictionType", "Idle Charge");
										idlePrice.put("gracePeriod", gracePeriod.doubleValue());
										idlePrice.put("inActiveduration", tempIdleMins);
										idlePrice.put("tax_incl", costWithTax.setScale(2,RoundingMode.HALF_UP));
										idlePrice.put("tax_excl", cost.setScale(2,RoundingMode.HALF_UP));
										idlePrice.put("gracePeriodStartTime",utils.stringToDate(ses.getEndTimeStamp()));
										idlePrice.put("gracePeriodEndTime", utils.stringToDate(utils.addDateSec(ses.getEndTimeStamp(),(int) gracePeriod.doubleValue()*60)));


										aditionalSubMap.put("tax", taxls);
										aditionalSubMap.put("rateRider", objectMapper.readTree(String.valueOf(costInfoJSON.get(0).get("cost_info").get(0).get("aditional").get("rateRider"))));
										aditionalSubMap.put("idle", idlePrice);
										costInfoMap.put("standard", objectMapper.readTree(String.valueOf(costInfoJSON.get(0).get("cost_info").get(0).get("standard"))));
										costInfoMap.put("aditional", aditionalSubMap);
										pricesLs.add(costInfoMap);
										tariffMap.put("cost_info", pricesLs);
										tariffLs.add(tariffMap);
										String json = objectMapper.writeValueAsString(tariffLs);
										logger.info(Thread.currentThread().getId()+"json : "+json);

										//aditionalInfoLs.add(idlePriceMap);
										//costInfoLs.addAll(aditionalInfoLs);
										sesPrices.setCost_info(json);
										generalDao.update(sesPrices);

										logger.info(Thread.currentThread().getId()+"finalCost at idle cost : "+finalCost);
									}else {
										logger.info(Thread.currentThread().getId()+"Idle time is less than grace time : "+sessionId);
									}
									JsonNode stationObj=objectMapper.readTree(String.valueOf(txnData.getStn_obj()));
									SessionImportedValues siv=new SessionImportedValues();
									siv.setChargeSessUniqId(txnData.getSessionId());
									siv.setFinalCostInslcCurrency(finalCost.doubleValue());
									siv.setNeedToDebit(finalCost.doubleValue());
									siv.setIdTag(ses.getCustomerId());
									siv.setStnId(stationObj.get("stnId").asLong());
									siv.setStnRefNum(stationObj.get("referNo").asText());
									siv.setTxnData(txnData);
									siv.setSiteObj(objectMapper.readTree(String.valueOf(txnData.getSite_obj())));
									siv.setUserObj(objectMapper.readTree(String.valueOf(txnData.getUser_obj())));
									siv.setTransactionId(txnData.getTransactionId());
									siv.setMeterValueTimeStatmp(ses.getEndTimeStamp());
									if(txnData.getBillingCases().equalsIgnoreCase("TOU+Rewards") || txnData.getBillingCases().equalsIgnoreCase("TOU+Free+Rewards")) {
										siv.setStTxnObj(stationService.getStartTransactionWithRfid(siv));
										siv=RewardAmountForNormalUser(siv);
									}
									siv=amountDeduction(siv);
									Long accountTransactionId=null;
									if(siv.getAccTxns() != null) {
										AccountTransactions acc = new AccountTransactions();
										acc.setId(siv.getAccTxns() != null ? siv.getAccTxns().getId():null);
										accountTransactionId=acc.getId();
									}
									String sessUpdate = "update session set finalCostInSlcCurrency = "+siv.getFinalCostInslcCurrency()+",reasonForTer='"+txnData.getReasonForTer()+"',accountTransaction_id="+accountTransactionId+",settlement='settled', settlementTimeStamp=GETUTCDATE() where sessionId = '"+sessionId+"'";
									logger.info(Thread.currentThread().getId()+"update sess query : "+sessUpdate);
									executeRepository.update(sessUpdate);
//									if(ses.getUserId()!=null && ses.getUserId()!=0) {
//										siv.setIdleBilling(false);
//										siv=RewardAmountForNormalUser(siv);
//										amountDeduction(siv);
//										String accUpdate = "update accounts set accountBalance = accountBalance - "+needToDebit+" where user_id = '"+ses.getUserId()+"'";
//										logger.info(Thread.currentThread().getId()+"update accounts query : "+accUpdate);
//										executeRepository.update(accUpdate);
//										
//										String update="update account_transaction set amtDebit='"+finalCost+"' where id='"+ses.getAccountTransaction().getId()+"'";
//										executeRepository.update(update);
//										
//									}else if(txnData.getUserType().equalsIgnoreCase("PAYG")){
//										siv = insertIntoAccountTransactionPayG(siv);
//										paymentService.capture(siv.getChargeSessUniqId(),siv.getFinalCostInslcCurrency(),siv.getPaygAccTxns().getId(),siv.getStnRefNum());
//									}
									boolean notification=alertsService.chargingActivityNotification(txnData, ses.getUserId(), ses.getKilowattHoursUsed(),finalCost.doubleValue() , ses.getSessionElapsedInMin());
									String mail=alertsService.mailChargingSessionSummaryData(sessionId,true);
									siv.setNotification(notification);
									siv.setMail(mail.equalsIgnoreCase("success"));
									JsonNode userJson = objectMapper.readTree(txnData.getUser_obj());
									if(userJson.size() > 0) {
										boolean smsIdleBilling=false;
										if(siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser")) {
											PreferredNotification pn = alertsService.preferredNotify(siv.getUserObj().get("UserId").asLong());
											if(pn != null && pn.isSmsIdleBilling()) {
												smsIdleBilling = true;
											}
										}else if(siv.getTxnData().getUserType().equalsIgnoreCase("PAYG")){
											smsIdleBilling = true;
										}
										if(smsIdleBilling) {
											String phoneNumber = String.valueOf(userJson.get("phoneNumber").asText());
											boolean sms=smsIntegrationImpl.sendSMSUsingBootConfg(stationObj.get("referNo").asText(),String.valueOf(ses.getId()),"stop",0.0,phoneNumber,step==360?"hour":"minute",String.valueOf(0),sessionId,0);
											siv.setSms(sms);
										}
									}
									ocppDeviceDetailsService.deleteDeviceDetails(stationObj.get("stnId").asLong(),ses.getUserId());
								}
							}else {
								logger.info(Thread.currentThread().getId()+"Idle charge billing is not selected for this session : "+txnData.getSessionId());
							}
						}
					}
				}
			}
		}catch (Exception e) {
			logger.error(""+e);
		}
	}

}

//class SessionBillableValues {
//	BigDecimal lastBillableKwh;
//	BigDecimal lastBillableDuration;
//
//	public SessionBillableValues() {
//		this.lastBillableKwh = null;
//		this.lastBillableDuration = null;
//	}
//}