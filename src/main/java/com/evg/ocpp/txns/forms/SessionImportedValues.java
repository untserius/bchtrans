package com.evg.ocpp.txns.forms;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import com.evg.ocpp.txns.model.ocpp.AccountTransactionForGuestUser;
import com.evg.ocpp.txns.model.ocpp.AccountTransactions;
import com.evg.ocpp.txns.model.ocpp.OCPPStartTransaction;
import com.evg.ocpp.txns.model.ocpp.OCPPTransactionData;
import com.evg.ocpp.txns.model.ocpp.Session;
import com.evg.ocpp.txns.model.ocpp.session_pricings;
import com.fasterxml.jackson.databind.JsonNode;

public class SessionImportedValues {
	
	//MeterValues
	private String currentImportContext;
	private String currentImportFormat;
	private String currentImportLocation;
	private String currentImportPhase;
	private String currentImportUnit;
	private String currentImportValue;
	private String currentImportMeasurand;

	private String currentOfferedContext;
	private String currentOfferedFormat;
	private String currentOfferedLocation;
	private String currentOfferedMeasurand;
	private String currentOfferedPhase;
	private String currentOfferedUnit;
	private String currentOfferedValue;

	private String energyActiveImportRegisterContext;
	private String energyActiveImportRegisterFormat;
	private String energyActiveImportRegisterLocation;
	private String energyActiveImportRegisterMeasurand;
	private String energyActiveImportRegisterPhase;
	private String energyActiveImportRegisterUnit="Wh";
	private String energyActiveImportRegisterValue="0";

	private String powerActiveImportContext;
	private String powerActiveImportFormat;
	private String powerActiveImportLocation;
	private String powerActiveImportMeasurand;
	private String powerActiveImportPhase;
	private String powerActiveImportUnit;
	private String powerActiveImportValue="0";
	private Double powerActiveImportSessionValue=0.0;

	private String powerOfferedContext;
	private String powerOfferedFormat;
	private String powerOfferedLocation;
	private String powerOfferedMeasurand;
	private String powerOfferedPhase;
	private String powerOfferedUnit;
	private String powerOfferedValue;
	
	private String SoCContext;
	private String SoCformat;
	private String SoClocation;
	private String SoCMeasurand;
	private String SoCPhase;
	private String SoCUnit="Percent";
	private String SoCValue="0";
	
	private String EnergyActiveExportRegisterUnit;
	private String EnergyReactiveImportRegisterUnit;
	private String EnergyActiveExportIntervalUnit;
	private String EnergyActiveImportIntervalUnit;
	private String EnergyReactiveImportIntervalUnit;
	private String EnergyReactiveExportRegisterUnit;
	private String EnergyReactiveExportIntervalUnit;
	private String PowerActiveExportUnit;
	private String PowerReactiveExportUnit;
	private String PowerReactiveImportUnit;
	private String PowerFactorUnit;
	private String CurrentExportUnit;
	private String VoltageUnit;
	private String FrequencyUnit;
	private String TemperatureUnit;
	private String RPMUnit;
	
	private String EnergyActiveExportRegisterValue;
	private String EnergyReactiveExportRegisterValue;
	private String EnergyReactiveImportRegisterValue;
	private String EnergyActiveImportRegisterDiffValue;
	private String EnergyActiveExportIntervalValue;
	private String EnergyActiveImportIntervalValue;
	private String EnergyReactiveExportIntervalValue;
	private String EnergyReactiveImportIntervalValue;
	private String PowerActiveExportValue;
	private String PowerReactiveExportValue;
	private String PowerReactiveImportValue;
	private String PowerFactorValue;
	private String CurrentExportValue;
	private String VoltageValue;
	private String FrequencyValue;
	private String TemperatureValue;
	private String RPMValue;
	private String energyActiveImportRegisterValueES;
	private String energyActiveImportRegisterUnitES;
	private String powerActiveImportUnitES;
	private String powerActiveImportValueES;
	private String avg_power;

	private Date meterValueTimeStatmp;
	private Date startTransTimeStamp;
	
	private String CurrentImportDiffValue;
	private String PowerActiveImportDiffValue;
	private String SoCDiffValue;
	
	private String unqReqId;
	private String idTag;
	private float currencyRate;
	private String chargeSessUniqId;
	private long chargeSessId;
	private JsonNode stnObj;
	private JsonNode siteObj;
	private JsonNode userObj;
	private long stnId;
	private long portId;
	private String driverGroupName;
	private int driverGroupdId;
	private String userEmail;
	private String reasonForTermination;
	private String stationMode;
	private BigDecimal sessionDuration=new BigDecimal(0.00);
	private BigDecimal totalKwUsed=new BigDecimal(0.00);
	private BigDecimal billSessionDuration=new BigDecimal(0.00);
	private BigDecimal billTotalKwUsed=new BigDecimal(0.00);
	private double finalCosttostore=0.0;
	private double finalCostInslcCurrency=0.0;
	private boolean masterList;
	private double socStartVal;
	private double socEndVal;
	private boolean startTxnProgress;
	private String preProdSess;
	private Long transactionId;
	private long connectorId;
	private String settlement;
	private String paymentMode;
	private String stnRefNum;
    private String rewardType;
    private double rewardValue;
    private boolean selfCharging;
    private long orgId;
	private String site_crncy_char;
	private long site_orgId=1;
	private OCPPTransactionData txnData;
	private AccountTransactions accTxns;
	private AccountTransactionForGuestUser paygAccTxns;
	private String cost_pricings;
	private Map<String,Object> previousSessionData;
	
	private String user_crncy_HexCode;
	private String user_crncy_Code;
	private String user_crncy_Char;
	private double user_crncy_revenue;
	private long user_orgId=1;
	private OCPPStartTransaction stTxnObj;
	private session_pricings sesspricings;
	
	private String request;
	private String response;
	private boolean idleBilling=false;
	private double needToDebit=0.0;
	private boolean stop=false;
	private String currentImportPhase1Value= "0.0";
	private String currentImportPhase2Value= "0.0";
	private String currentImportPhase3Value= "0.0";
 
	private String voltagePhase1Value = "0.0";
	private String voltagePhase2Value = "0.0";
	private String voltagePhase3Value = "0.0";
	private boolean ampFlag;
	private Session session;
	
	private String vendingUnit="kWh";
	private double vendingPrice=0.0;
	private boolean mail=false;
	private boolean sms=false;
	private boolean notification=false;
	private boolean currentScreen=false;

	private boolean energyModify=false;
	private BigDecimal actualEnergy=new BigDecimal("0.0");

	public BigDecimal getActualEnergy() {
		return actualEnergy;
	}

	public void setActualEnergy(BigDecimal actualEnergy) {
		this.actualEnergy = actualEnergy;
	}

	public boolean isCurrentScreen() {
		return currentScreen;
	}
	public void setCurrentScreen(boolean currentScreen) {
		this.currentScreen = currentScreen;
	}
	public boolean isMail() {
		return mail;
	}
	public void setMail(boolean mail) {
		this.mail = mail;
	}
	public boolean isSms() {
		return sms;
	}
	public void setSms(boolean sms) {
		this.sms = sms;
	}
	public boolean isNotification() {
		return notification;
	}
	public void setNotification(boolean notification) {
		this.notification = notification;
	}
	public String getVendingUnit() {
		return vendingUnit;
	}
	public void setVendingUnit(String vendingUnit) {
		this.vendingUnit = vendingUnit;
	}
	public double getVendingPrice() {
		return vendingPrice;
	}
	public void setVendingPrice(double vendingPrice) {
		this.vendingPrice = vendingPrice;
	}
	public boolean isStop() {
		return stop;
	}
	public void setStop(boolean stop) {
		this.stop = stop;
	}
	public String getCurrentImportContext() {
		return currentImportContext;
	}
	public void setCurrentImportContext(String currentImportContext) {
		this.currentImportContext = currentImportContext;
	}
	public String getCurrentImportFormat() {
		return currentImportFormat;
	}
	public void setCurrentImportFormat(String currentImportFormat) {
		this.currentImportFormat = currentImportFormat;
	}
	public String getCurrentImportLocation() {
		return currentImportLocation;
	}
	public void setCurrentImportLocation(String currentImportLocation) {
		this.currentImportLocation = currentImportLocation;
	}
	public String getCurrentImportPhase() {
		return currentImportPhase;
	}
	public void setCurrentImportPhase(String currentImportPhase) {
		this.currentImportPhase = currentImportPhase;
	}
	public String getCurrentImportUnit() {
		return currentImportUnit;
	}
	public void setCurrentImportUnit(String currentImportUnit) {
		this.currentImportUnit = currentImportUnit;
	}
	public String getCurrentImportValue() {
		return currentImportValue;
	}
	public void setCurrentImportValue(String currentImportValue) {
		this.currentImportValue = currentImportValue;
	}
	public String getCurrentImportMeasurand() {
		return currentImportMeasurand;
	}
	public void setCurrentImportMeasurand(String currentImportMeasurand) {
		this.currentImportMeasurand = currentImportMeasurand;
	}
	public String getCurrentOfferedContext() {
		return currentOfferedContext;
	}
	public void setCurrentOfferedContext(String currentOfferedContext) {
		this.currentOfferedContext = currentOfferedContext;
	}
	public String getCurrentOfferedFormat() {
		return currentOfferedFormat;
	}
	public void setCurrentOfferedFormat(String currentOfferedFormat) {
		this.currentOfferedFormat = currentOfferedFormat;
	}
	public String getCurrentOfferedLocation() {
		return currentOfferedLocation;
	}
	public void setCurrentOfferedLocation(String currentOfferedLocation) {
		this.currentOfferedLocation = currentOfferedLocation;
	}
	public String getCurrentOfferedMeasurand() {
		return currentOfferedMeasurand;
	}
	public void setCurrentOfferedMeasurand(String currentOfferedMeasurand) {
		this.currentOfferedMeasurand = currentOfferedMeasurand;
	}
	public String getCurrentOfferedPhase() {
		return currentOfferedPhase;
	}
	public void setCurrentOfferedPhase(String currentOfferedPhase) {
		this.currentOfferedPhase = currentOfferedPhase;
	}
	public String getCurrentOfferedUnit() {
		return currentOfferedUnit;
	}
	public void setCurrentOfferedUnit(String currentOfferedUnit) {
		this.currentOfferedUnit = currentOfferedUnit;
	}
	public String getCurrentOfferedValue() {
		return currentOfferedValue;
	}
	public void setCurrentOfferedValue(String currentOfferedValue) {
		this.currentOfferedValue = currentOfferedValue;
	}
	public String getEnergyActiveImportRegisterContext() {
		return energyActiveImportRegisterContext;
	}
	public void setEnergyActiveImportRegisterContext(String energyActiveImportRegisterContext) {
		this.energyActiveImportRegisterContext = energyActiveImportRegisterContext;
	}
	public String getEnergyActiveImportRegisterFormat() {
		return energyActiveImportRegisterFormat;
	}
	public void setEnergyActiveImportRegisterFormat(String energyActiveImportRegisterFormat) {
		this.energyActiveImportRegisterFormat = energyActiveImportRegisterFormat;
	}
	public String getEnergyActiveImportRegisterLocation() {
		return energyActiveImportRegisterLocation;
	}
	public void setEnergyActiveImportRegisterLocation(String energyActiveImportRegisterLocation) {
		this.energyActiveImportRegisterLocation = energyActiveImportRegisterLocation;
	}
	public String getEnergyActiveImportRegisterMeasurand() {
		return energyActiveImportRegisterMeasurand;
	}
	public void setEnergyActiveImportRegisterMeasurand(String energyActiveImportRegisterMeasurand) {
		this.energyActiveImportRegisterMeasurand = energyActiveImportRegisterMeasurand;
	}
	public String getEnergyActiveImportRegisterPhase() {
		return energyActiveImportRegisterPhase;
	}
	public void setEnergyActiveImportRegisterPhase(String energyActiveImportRegisterPhase) {
		this.energyActiveImportRegisterPhase = energyActiveImportRegisterPhase;
	}
	public String getEnergyActiveImportRegisterUnit() {
		return energyActiveImportRegisterUnit;
	}
	public void setEnergyActiveImportRegisterUnit(String energyActiveImportRegisterUnit) {
		this.energyActiveImportRegisterUnit = energyActiveImportRegisterUnit;
	}
	public String getEnergyActiveImportRegisterValue() {
		return energyActiveImportRegisterValue;
	}
	public void setEnergyActiveImportRegisterValue(String energyActiveImportRegisterValue) {
		this.energyActiveImportRegisterValue = energyActiveImportRegisterValue;
	}
	public String getPowerActiveImportContext() {
		return powerActiveImportContext;
	}
	public void setPowerActiveImportContext(String powerActiveImportContext) {
		this.powerActiveImportContext = powerActiveImportContext;
	}
	public String getPowerActiveImportFormat() {
		return powerActiveImportFormat;
	}
	public void setPowerActiveImportFormat(String powerActiveImportFormat) {
		this.powerActiveImportFormat = powerActiveImportFormat;
	}
	public String getPowerActiveImportLocation() {
		return powerActiveImportLocation;
	}
	public void setPowerActiveImportLocation(String powerActiveImportLocation) {
		this.powerActiveImportLocation = powerActiveImportLocation;
	}
	public String getPowerActiveImportMeasurand() {
		return powerActiveImportMeasurand;
	}
	public void setPowerActiveImportMeasurand(String powerActiveImportMeasurand) {
		this.powerActiveImportMeasurand = powerActiveImportMeasurand;
	}
	public String getPowerActiveImportPhase() {
		return powerActiveImportPhase;
	}
	public void setPowerActiveImportPhase(String powerActiveImportPhase) {
		this.powerActiveImportPhase = powerActiveImportPhase;
	}
	public String getPowerActiveImportUnit() {
		return powerActiveImportUnit;
	}
	public void setPowerActiveImportUnit(String powerActiveImportUnit) {
		this.powerActiveImportUnit = powerActiveImportUnit;
	}
	public String getPowerActiveImportValue() {
		return powerActiveImportValue;
	}
	public void setPowerActiveImportValue(String powerActiveImportValue) {
		this.powerActiveImportValue = powerActiveImportValue;
	}
	public Double getPowerActiveImportSessionValue() {
		return powerActiveImportSessionValue;
	}
	public void setPowerActiveImportSessionValue(Double powerActiveImportSessionValue) {
		this.powerActiveImportSessionValue = powerActiveImportSessionValue;
	}
	public String getPowerOfferedContext() {
		return powerOfferedContext;
	}
	public void setPowerOfferedContext(String powerOfferedContext) {
		this.powerOfferedContext = powerOfferedContext;
	}
	public String getPowerOfferedFormat() {
		return powerOfferedFormat;
	}
	public void setPowerOfferedFormat(String powerOfferedFormat) {
		this.powerOfferedFormat = powerOfferedFormat;
	}
	public String getPowerOfferedLocation() {
		return powerOfferedLocation;
	}
	public void setPowerOfferedLocation(String powerOfferedLocation) {
		this.powerOfferedLocation = powerOfferedLocation;
	}
	public String getPowerOfferedMeasurand() {
		return powerOfferedMeasurand;
	}
	public void setPowerOfferedMeasurand(String powerOfferedMeasurand) {
		this.powerOfferedMeasurand = powerOfferedMeasurand;
	}
	public String getPowerOfferedPhase() {
		return powerOfferedPhase;
	}
	public void setPowerOfferedPhase(String powerOfferedPhase) {
		this.powerOfferedPhase = powerOfferedPhase;
	}
	public String getPowerOfferedUnit() {
		return powerOfferedUnit;
	}
	public void setPowerOfferedUnit(String powerOfferedUnit) {
		this.powerOfferedUnit = powerOfferedUnit;
	}
	public String getPowerOfferedValue() {
		return powerOfferedValue;
	}
	public void setPowerOfferedValue(String powerOfferedValue) {
		this.powerOfferedValue = powerOfferedValue;
	}
	public String getSoCContext() {
		return SoCContext;
	}
	public void setSoCContext(String soCContext) {
		SoCContext = soCContext;
	}
	public String getSoCformat() {
		return SoCformat;
	}
	public void setSoCformat(String soCformat) {
		SoCformat = soCformat;
	}
	public String getSoClocation() {
		return SoClocation;
	}
	public void setSoClocation(String soClocation) {
		SoClocation = soClocation;
	}
	public String getSoCMeasurand() {
		return SoCMeasurand;
	}
	public void setSoCMeasurand(String soCMeasurand) {
		SoCMeasurand = soCMeasurand;
	}
	public String getSoCPhase() {
		return SoCPhase;
	}
	public void setSoCPhase(String soCPhase) {
		SoCPhase = soCPhase;
	}
	public String getSoCUnit() {
		return SoCUnit;
	}
	public void setSoCUnit(String soCUnit) {
		SoCUnit = soCUnit;
	}
	public String getSoCValue() {
		return SoCValue;
	}
	public void setSoCValue(String soCValue) {
		SoCValue = soCValue;
	}
	public String getEnergyActiveExportRegisterUnit() {
		return EnergyActiveExportRegisterUnit;
	}
	public void setEnergyActiveExportRegisterUnit(String energyActiveExportRegisterUnit) {
		EnergyActiveExportRegisterUnit = energyActiveExportRegisterUnit;
	}
	public String getEnergyReactiveImportRegisterUnit() {
		return EnergyReactiveImportRegisterUnit;
	}
	public void setEnergyReactiveImportRegisterUnit(String energyReactiveImportRegisterUnit) {
		EnergyReactiveImportRegisterUnit = energyReactiveImportRegisterUnit;
	}
	public String getEnergyActiveExportIntervalUnit() {
		return EnergyActiveExportIntervalUnit;
	}
	public void setEnergyActiveExportIntervalUnit(String energyActiveExportIntervalUnit) {
		EnergyActiveExportIntervalUnit = energyActiveExportIntervalUnit;
	}
	public String getEnergyActiveImportIntervalUnit() {
		return EnergyActiveImportIntervalUnit;
	}
	public void setEnergyActiveImportIntervalUnit(String energyActiveImportIntervalUnit) {
		EnergyActiveImportIntervalUnit = energyActiveImportIntervalUnit;
	}
	public String getEnergyReactiveImportIntervalUnit() {
		return EnergyReactiveImportIntervalUnit;
	}
	public void setEnergyReactiveImportIntervalUnit(String energyReactiveImportIntervalUnit) {
		EnergyReactiveImportIntervalUnit = energyReactiveImportIntervalUnit;
	}
	public String getEnergyReactiveExportRegisterUnit() {
		return EnergyReactiveExportRegisterUnit;
	}
	public void setEnergyReactiveExportRegisterUnit(String energyReactiveExportRegisterUnit) {
		EnergyReactiveExportRegisterUnit = energyReactiveExportRegisterUnit;
	}
	public String getEnergyReactiveExportIntervalUnit() {
		return EnergyReactiveExportIntervalUnit;
	}
	public void setEnergyReactiveExportIntervalUnit(String energyReactiveExportIntervalUnit) {
		EnergyReactiveExportIntervalUnit = energyReactiveExportIntervalUnit;
	}
	public String getPowerActiveExportUnit() {
		return PowerActiveExportUnit;
	}
	public void setPowerActiveExportUnit(String powerActiveExportUnit) {
		PowerActiveExportUnit = powerActiveExportUnit;
	}
	public String getPowerReactiveExportUnit() {
		return PowerReactiveExportUnit;
	}
	public void setPowerReactiveExportUnit(String powerReactiveExportUnit) {
		PowerReactiveExportUnit = powerReactiveExportUnit;
	}
	public String getPowerReactiveImportUnit() {
		return PowerReactiveImportUnit;
	}
	public void setPowerReactiveImportUnit(String powerReactiveImportUnit) {
		PowerReactiveImportUnit = powerReactiveImportUnit;
	}
	public String getPowerFactorUnit() {
		return PowerFactorUnit;
	}
	public void setPowerFactorUnit(String powerFactorUnit) {
		PowerFactorUnit = powerFactorUnit;
	}
	public String getCurrentExportUnit() {
		return CurrentExportUnit;
	}
	public void setCurrentExportUnit(String currentExportUnit) {
		CurrentExportUnit = currentExportUnit;
	}
	public String getVoltageUnit() {
		return VoltageUnit;
	}
	public void setVoltageUnit(String voltageUnit) {
		VoltageUnit = voltageUnit;
	}
	public String getFrequencyUnit() {
		return FrequencyUnit;
	}
	public void setFrequencyUnit(String frequencyUnit) {
		FrequencyUnit = frequencyUnit;
	}
	public String getTemperatureUnit() {
		return TemperatureUnit;
	}
	public void setTemperatureUnit(String temperatureUnit) {
		TemperatureUnit = temperatureUnit;
	}
	public String getRPMUnit() {
		return RPMUnit;
	}
	public void setRPMUnit(String rPMUnit) {
		RPMUnit = rPMUnit;
	}
	public String getEnergyActiveExportRegisterValue() {
		return EnergyActiveExportRegisterValue;
	}
	public void setEnergyActiveExportRegisterValue(String energyActiveExportRegisterValue) {
		EnergyActiveExportRegisterValue = energyActiveExportRegisterValue;
	}
	public String getEnergyReactiveExportRegisterValue() {
		return EnergyReactiveExportRegisterValue;
	}
	public void setEnergyReactiveExportRegisterValue(String energyReactiveExportRegisterValue) {
		EnergyReactiveExportRegisterValue = energyReactiveExportRegisterValue;
	}
	public String getEnergyReactiveImportRegisterValue() {
		return EnergyReactiveImportRegisterValue;
	}
	public void setEnergyReactiveImportRegisterValue(String energyReactiveImportRegisterValue) {
		EnergyReactiveImportRegisterValue = energyReactiveImportRegisterValue;
	}
	public String getEnergyActiveImportRegisterDiffValue() {
		return EnergyActiveImportRegisterDiffValue;
	}
	public void setEnergyActiveImportRegisterDiffValue(String energyActiveImportRegisterDiffValue) {
		EnergyActiveImportRegisterDiffValue = energyActiveImportRegisterDiffValue;
	}
	public String getEnergyActiveExportIntervalValue() {
		return EnergyActiveExportIntervalValue;
	}
	public void setEnergyActiveExportIntervalValue(String energyActiveExportIntervalValue) {
		EnergyActiveExportIntervalValue = energyActiveExportIntervalValue;
	}
	public String getEnergyActiveImportIntervalValue() {
		return EnergyActiveImportIntervalValue;
	}
	public void setEnergyActiveImportIntervalValue(String energyActiveImportIntervalValue) {
		EnergyActiveImportIntervalValue = energyActiveImportIntervalValue;
	}
	public String getEnergyReactiveExportIntervalValue() {
		return EnergyReactiveExportIntervalValue;
	}
	public void setEnergyReactiveExportIntervalValue(String energyReactiveExportIntervalValue) {
		EnergyReactiveExportIntervalValue = energyReactiveExportIntervalValue;
	}
	public String getEnergyReactiveImportIntervalValue() {
		return EnergyReactiveImportIntervalValue;
	}
	public void setEnergyReactiveImportIntervalValue(String energyReactiveImportIntervalValue) {
		EnergyReactiveImportIntervalValue = energyReactiveImportIntervalValue;
	}
	public String getPowerActiveExportValue() {
		return PowerActiveExportValue;
	}
	public void setPowerActiveExportValue(String powerActiveExportValue) {
		PowerActiveExportValue = powerActiveExportValue;
	}
	public String getPowerReactiveExportValue() {
		return PowerReactiveExportValue;
	}
	public void setPowerReactiveExportValue(String powerReactiveExportValue) {
		PowerReactiveExportValue = powerReactiveExportValue;
	}
	public String getPowerReactiveImportValue() {
		return PowerReactiveImportValue;
	}
	public void setPowerReactiveImportValue(String powerReactiveImportValue) {
		PowerReactiveImportValue = powerReactiveImportValue;
	}
	public String getPowerFactorValue() {
		return PowerFactorValue;
	}
	public void setPowerFactorValue(String powerFactorValue) {
		PowerFactorValue = powerFactorValue;
	}
	public String getCurrentExportValue() {
		return CurrentExportValue;
	}
	public void setCurrentExportValue(String currentExportValue) {
		CurrentExportValue = currentExportValue;
	}
	public String getVoltageValue() {
		return VoltageValue;
	}
	public void setVoltageValue(String voltageValue) {
		VoltageValue = voltageValue;
	}
	public String getFrequencyValue() {
		return FrequencyValue;
	}
	public void setFrequencyValue(String frequencyValue) {
		FrequencyValue = frequencyValue;
	}
	public String getTemperatureValue() {
		return TemperatureValue;
	}
	public void setTemperatureValue(String temperatureValue) {
		TemperatureValue = temperatureValue;
	}
	public String getRPMValue() {
		return RPMValue;
	}
	public void setRPMValue(String rPMValue) {
		RPMValue = rPMValue;
	}
	public String getEnergyActiveImportRegisterValueES() {
		return energyActiveImportRegisterValueES;
	}
	public void setEnergyActiveImportRegisterValueES(String energyActiveImportRegisterValueES) {
		this.energyActiveImportRegisterValueES = energyActiveImportRegisterValueES;
	}
	public String getEnergyActiveImportRegisterUnitES() {
		return energyActiveImportRegisterUnitES;
	}
	public void setEnergyActiveImportRegisterUnitES(String energyActiveImportRegisterUnitES) {
		this.energyActiveImportRegisterUnitES = energyActiveImportRegisterUnitES;
	}
	public String getPowerActiveImportUnitES() {
		return powerActiveImportUnitES;
	}
	public void setPowerActiveImportUnitES(String powerActiveImportUnitES) {
		this.powerActiveImportUnitES = powerActiveImportUnitES;
	}
	public String getPowerActiveImportValueES() {
		return powerActiveImportValueES;
	}
	public void setPowerActiveImportValueES(String powerActiveImportValueES) {
		this.powerActiveImportValueES = powerActiveImportValueES;
	}
	public String getAvg_power() {
		return avg_power;
	}
	public void setAvg_power(String avg_power) {
		this.avg_power = avg_power;
	}
	public Date getMeterValueTimeStatmp() {
		return meterValueTimeStatmp;
	}
	public void setMeterValueTimeStatmp(Date meterValueTimeStatmp) {
		this.meterValueTimeStatmp = meterValueTimeStatmp;
	}
	public Date getStartTransTimeStamp() {
		return startTransTimeStamp;
	}
	public void setStartTransTimeStamp(Date startTransTimeStamp) {
		this.startTransTimeStamp = startTransTimeStamp;
	}
	public String getUnqReqId() {
		return unqReqId;
	}
	public void setUnqReqId(String unqReqId) {
		this.unqReqId = unqReqId;
	}
	public String getIdTag() {
		return idTag;
	}
	public void setIdTag(String idTag) {
		this.idTag = idTag;
	}
	public String getChargeSessUniqId() {
		return chargeSessUniqId;
	}
	public void setChargeSessUniqId(String chargeSessUniqId) {
		this.chargeSessUniqId = chargeSessUniqId;
	}
	public String getDriverGroupName() {
		return driverGroupName;
	}
	public void setDriverGroupName(String driverGroupName) {
		this.driverGroupName = driverGroupName;
	}
	public int getDriverGroupdId() {
		return driverGroupdId;
	}
	public void setDriverGroupdId(int driverGroupdId) {
		this.driverGroupdId = driverGroupdId;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public String getReasonForTermination() {
		return reasonForTermination;
	}
	public void setReasonForTermination(String reasonForTermination) {
		this.reasonForTermination = reasonForTermination;
	}
	public String getStationMode() {
		return stationMode;
	}
	public void setStationMode(String stationMode) {
		this.stationMode = stationMode;
	}
	public BigDecimal getSessionDuration() {
		return sessionDuration;
	}
	public void setSessionDuration(BigDecimal sessionDuration) {
		this.sessionDuration = sessionDuration;
	}
	public BigDecimal getTotalKwUsed() {
		return totalKwUsed;
	}
	public void setTotalKwUsed(BigDecimal totalKwUsed) {
		this.totalKwUsed = totalKwUsed;
	}
	public double getFinalCosttostore() {
		return finalCosttostore;
	}
	public void setFinalCosttostore(double finalCosttostore) {
		this.finalCosttostore = finalCosttostore;
	}
	public double getFinalCostInslcCurrency() {
		return finalCostInslcCurrency;
	}
	public void setFinalCostInslcCurrency(double finalCostInslcCurrency) {
		this.finalCostInslcCurrency = finalCostInslcCurrency;
	}
	public boolean isMasterList() {
		return masterList;
	}
	public void setMasterList(boolean masterList) {
		this.masterList = masterList;
	}
	public double getSocStartVal() {
		return socStartVal;
	}
	public void setSocStartVal(double socStartVal) {
		this.socStartVal = socStartVal;
	}
	public double getSocEndVal() {
		return socEndVal;
	}
	public void setSocEndVal(double socEndVal) {
		this.socEndVal = socEndVal;
	}
	public boolean isStartTxnProgress() {
		return startTxnProgress;
	}
	public void setStartTxnProgress(boolean startTxnProgress) {
		this.startTxnProgress = startTxnProgress;
	}
	public String getPreProdSess() {
		return preProdSess;
	}
	public void setPreProdSess(String preProdSess) {
		this.preProdSess = preProdSess;
	}
	public Long getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}
	public String getSettlement() {
		return settlement;
	}
	public void setSettlement(String settlement) {
		this.settlement = settlement;
	}
	public String getPaymentMode() {
		return paymentMode;
	}
	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}
	public String getStnRefNum() {
		return stnRefNum;
	}
	public void setStnRefNum(String stnRefNum) {
		this.stnRefNum = stnRefNum;
	}
	public String getRewardType() {
		return rewardType;
	}
	public void setRewardType(String rewardType) {
		this.rewardType = rewardType;
	}
	public double getRewardValue() {
		return rewardValue;
	}
	public void setRewardValue(double rewardValue) {
		this.rewardValue = rewardValue;
	}
	public boolean isSelfCharging() {
		return selfCharging;
	}
	public void setSelfCharging(boolean selfCharging) {
		this.selfCharging = selfCharging;
	}
	public long getOrgId() {
		return orgId;
	}
	public void setOrgId(long orgId) {
		this.orgId = orgId;
	}
	public String getSite_crncy_char() {
		return site_crncy_char;
	}
	public void setSite_crncy_char(String site_crncy_char) {
		this.site_crncy_char = site_crncy_char;
	}
	public long getSite_orgId() {
		return site_orgId;
	}
	public void setSite_orgId(long site_orgId) {
		this.site_orgId = site_orgId;
	}
	public String getUser_crncy_HexCode() {
		return user_crncy_HexCode;
	}
	public void setUser_crncy_HexCode(String user_crncy_HexCode) {
		this.user_crncy_HexCode = user_crncy_HexCode;
	}
	public String getUser_crncy_Code() {
		return user_crncy_Code;
	}
	public void setUser_crncy_Code(String user_crncy_Code) {
		this.user_crncy_Code = user_crncy_Code;
	}
	public String getUser_crncy_Char() {
		return user_crncy_Char;
	}
	public void setUser_crncy_Char(String user_crncy_Char) {
		this.user_crncy_Char = user_crncy_Char;
	}
	public long getUser_orgId() {
		return user_orgId;
	}
	public void setUser_orgId(long user_orgId) {
		this.user_orgId = user_orgId;
	}
	public long getStnId() {
		return stnId;
	}
	public void setStnId(long stnId) {
		this.stnId = stnId;
	}
	public long getPortId() {
		return portId;
	}
	public void setPortId(long portId) {
		this.portId = portId;
	}
	public OCPPStartTransaction getStTxnObj() {
		return stTxnObj;
	}
	public void setStTxnObj(OCPPStartTransaction stTxnObj) {
		this.stTxnObj = stTxnObj;
	}
	public OCPPTransactionData getTxnData() {
		return txnData;
	}
	public void setTxnData(OCPPTransactionData txnData) {
		this.txnData = txnData;
	}
	public String getCost_pricings() {
		return cost_pricings;
	}
	public void setCost_pricings(String cost_pricings) {
		this.cost_pricings = cost_pricings;
	}
	public session_pricings getSesspricings() {
		return sesspricings;
	}
	public void setSesspricings(session_pricings sesspricings) {
		this.sesspricings = sesspricings;
	}
	public double getUser_crncy_revenue() {
		return user_crncy_revenue;
	}
	public void setUser_crncy_revenue(double user_crncy_revenue) {
		this.user_crncy_revenue = user_crncy_revenue;
	}
	public AccountTransactions getAccTxns() {
		return accTxns;
	}
	public void setAccTxns(AccountTransactions accTxns) {
		this.accTxns = accTxns;
	}
	public float getCurrencyRate() {
		return currencyRate;
	}
	public void setCurrencyRate(float currencyRate) {
		this.currencyRate = currencyRate;
	}
	public AccountTransactionForGuestUser getPaygAccTxns() {
		return paygAccTxns;
	}
	public void setPaygAccTxns(AccountTransactionForGuestUser paygAccTxns) {
		this.paygAccTxns = paygAccTxns;
	}
	public String getCurrentImportDiffValue() {
		return CurrentImportDiffValue;
	}
	public void setCurrentImportDiffValue(String currentImportDiffValue) {
		CurrentImportDiffValue = currentImportDiffValue;
	}
	public String getPowerActiveImportDiffValue() {
		return PowerActiveImportDiffValue;
	}
	public void setPowerActiveImportDiffValue(String powerActiveImportDiffValue) {
		PowerActiveImportDiffValue = powerActiveImportDiffValue;
	}
	public String getSoCDiffValue() {
		return SoCDiffValue;
	}
	public void setSoCDiffValue(String soCDiffValue) {
		SoCDiffValue = soCDiffValue;
	}
	public long getChargeSessId() {
		return chargeSessId;
	}
	public void setChargeSessId(long chargeSessId) {
		this.chargeSessId = chargeSessId;
	}
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public JsonNode getStnObj() {
		return stnObj;
	}
	public void setStnObj(JsonNode stnObj) {
		this.stnObj = stnObj;
	}
	public JsonNode getUserObj() {
		return userObj;
	}
	public void setUserObj(JsonNode userObj) {
		this.userObj = userObj;
	}
	public void setSiteObj(JsonNode siteObj) {
		this.siteObj = siteObj;
	}
	public JsonNode getSiteObj() {
		return siteObj;
	}
	public BigDecimal getBillSessionDuration() {
		return billSessionDuration;
	}
	public void setBillSessionDuration(BigDecimal billSessionDuration) {
		this.billSessionDuration = billSessionDuration;
	}
	public BigDecimal getBillTotalKwUsed() {
		return billTotalKwUsed;
	}
	public void setBillTotalKwUsed(BigDecimal billTotalKwUsed) {
		this.billTotalKwUsed = billTotalKwUsed;
	}
	public long getConnectorId() {
		return connectorId;
	}
	public void setConnectorId(long connectorId) {
		this.connectorId = connectorId;
	}
	public Map<String, Object> getPreviousSessionData() {
		return previousSessionData;
	}
	public void setPreviousSessionData(Map<String, Object> previousSessionData) {
		this.previousSessionData = previousSessionData;
	}
	
	public boolean isIdleBilling() {
		return idleBilling;
	}
	public void setIdleBilling(boolean idleBilling) {
		this.idleBilling = idleBilling;
	}
	
	public double getNeedToDebit() {
		return needToDebit;
	}
	public void setNeedToDebit(double needToDebit) {
		this.needToDebit = needToDebit;
	}
	
	public String getCurrentImportPhase1Value() {
		return currentImportPhase1Value;
	}
	public void setCurrentImportPhase1Value(String currentImportPhase1Value) {
		this.currentImportPhase1Value = currentImportPhase1Value;
	}
	public String getCurrentImportPhase2Value() {
		return currentImportPhase2Value;
	}
	public void setCurrentImportPhase2Value(String currentImportPhase2Value) {
		this.currentImportPhase2Value = currentImportPhase2Value;
	}
	public String getCurrentImportPhase3Value() {
		return currentImportPhase3Value;
	}
	public void setCurrentImportPhase3Value(String currentImportPhase3Value) {
		this.currentImportPhase3Value = currentImportPhase3Value;
	}
	public String getVoltagePhase1Value() {
		return voltagePhase1Value;
	}
	public void setVoltagePhase1Value(String voltagePhase1Value) {
		this.voltagePhase1Value = voltagePhase1Value;
	}
	public String getVoltagePhase2Value() {
		return voltagePhase2Value;
	}
	public void setVoltagePhase2Value(String voltagePhase2Value) {
		this.voltagePhase2Value = voltagePhase2Value;
	}
	public String getVoltagePhase3Value() {
		return voltagePhase3Value;
	}
	public void setVoltagePhase3Value(String voltagePhase3Value) {
		this.voltagePhase3Value = voltagePhase3Value;
	}
	public boolean isAmpFlag() {
		return ampFlag;
	}
	public void setAmpFlag(boolean ampFlag) {
		this.ampFlag = ampFlag;
	}
	public Session getSession() {
		return session;
	}
	public void setSession(Session session) {
		this.session = session;
	}

	public boolean isEnergyModify() {
		return energyModify;
	}

	public void setEnergyModify(boolean energyModify) {
		this.energyModify = energyModify;
	}

	@Override
	public String toString() {
		return "SessionImportedValues{" +
				"currentImportContext='" + currentImportContext + '\'' +
				", currentImportFormat='" + currentImportFormat + '\'' +
				", currentImportLocation='" + currentImportLocation + '\'' +
				", currentImportPhase='" + currentImportPhase + '\'' +
				", currentImportUnit='" + currentImportUnit + '\'' +
				", currentImportValue='" + currentImportValue + '\'' +
				", currentImportMeasurand='" + currentImportMeasurand + '\'' +
				", currentOfferedContext='" + currentOfferedContext + '\'' +
				", currentOfferedFormat='" + currentOfferedFormat + '\'' +
				", currentOfferedLocation='" + currentOfferedLocation + '\'' +
				", currentOfferedMeasurand='" + currentOfferedMeasurand + '\'' +
				", currentOfferedPhase='" + currentOfferedPhase + '\'' +
				", currentOfferedUnit='" + currentOfferedUnit + '\'' +
				", currentOfferedValue='" + currentOfferedValue + '\'' +
				", energyActiveImportRegisterContext='" + energyActiveImportRegisterContext + '\'' +
				", energyActiveImportRegisterFormat='" + energyActiveImportRegisterFormat + '\'' +
				", energyActiveImportRegisterLocation='" + energyActiveImportRegisterLocation + '\'' +
				", energyActiveImportRegisterMeasurand='" + energyActiveImportRegisterMeasurand + '\'' +
				", energyActiveImportRegisterPhase='" + energyActiveImportRegisterPhase + '\'' +
				", energyActiveImportRegisterUnit='" + energyActiveImportRegisterUnit + '\'' +
				", energyActiveImportRegisterValue='" + energyActiveImportRegisterValue + '\'' +
				", powerActiveImportContext='" + powerActiveImportContext + '\'' +
				", powerActiveImportFormat='" + powerActiveImportFormat + '\'' +
				", powerActiveImportLocation='" + powerActiveImportLocation + '\'' +
				", powerActiveImportMeasurand='" + powerActiveImportMeasurand + '\'' +
				", powerActiveImportPhase='" + powerActiveImportPhase + '\'' +
				", powerActiveImportUnit='" + powerActiveImportUnit + '\'' +
				", powerActiveImportValue='" + powerActiveImportValue + '\'' +
				", powerActiveImportSessionValue=" + powerActiveImportSessionValue +
				", powerOfferedContext='" + powerOfferedContext + '\'' +
				", powerOfferedFormat='" + powerOfferedFormat + '\'' +
				", powerOfferedLocation='" + powerOfferedLocation + '\'' +
				", powerOfferedMeasurand='" + powerOfferedMeasurand + '\'' +
				", powerOfferedPhase='" + powerOfferedPhase + '\'' +
				", powerOfferedUnit='" + powerOfferedUnit + '\'' +
				", powerOfferedValue='" + powerOfferedValue + '\'' +
				", SoCContext='" + SoCContext + '\'' +
				", SoCformat='" + SoCformat + '\'' +
				", SoClocation='" + SoClocation + '\'' +
				", SoCMeasurand='" + SoCMeasurand + '\'' +
				", SoCPhase='" + SoCPhase + '\'' +
				", SoCUnit='" + SoCUnit + '\'' +
				", SoCValue='" + SoCValue + '\'' +
				", EnergyActiveExportRegisterUnit='" + EnergyActiveExportRegisterUnit + '\'' +
				", EnergyReactiveImportRegisterUnit='" + EnergyReactiveImportRegisterUnit + '\'' +
				", EnergyActiveExportIntervalUnit='" + EnergyActiveExportIntervalUnit + '\'' +
				", EnergyActiveImportIntervalUnit='" + EnergyActiveImportIntervalUnit + '\'' +
				", EnergyReactiveImportIntervalUnit='" + EnergyReactiveImportIntervalUnit + '\'' +
				", EnergyReactiveExportRegisterUnit='" + EnergyReactiveExportRegisterUnit + '\'' +
				", EnergyReactiveExportIntervalUnit='" + EnergyReactiveExportIntervalUnit + '\'' +
				", PowerActiveExportUnit='" + PowerActiveExportUnit + '\'' +
				", PowerReactiveExportUnit='" + PowerReactiveExportUnit + '\'' +
				", PowerReactiveImportUnit='" + PowerReactiveImportUnit + '\'' +
				", PowerFactorUnit='" + PowerFactorUnit + '\'' +
				", CurrentExportUnit='" + CurrentExportUnit + '\'' +
				", VoltageUnit='" + VoltageUnit + '\'' +
				", FrequencyUnit='" + FrequencyUnit + '\'' +
				", TemperatureUnit='" + TemperatureUnit + '\'' +
				", RPMUnit='" + RPMUnit + '\'' +
				", EnergyActiveExportRegisterValue='" + EnergyActiveExportRegisterValue + '\'' +
				", EnergyReactiveExportRegisterValue='" + EnergyReactiveExportRegisterValue + '\'' +
				", EnergyReactiveImportRegisterValue='" + EnergyReactiveImportRegisterValue + '\'' +
				", EnergyActiveImportRegisterDiffValue='" + EnergyActiveImportRegisterDiffValue + '\'' +
				", EnergyActiveExportIntervalValue='" + EnergyActiveExportIntervalValue + '\'' +
				", EnergyActiveImportIntervalValue='" + EnergyActiveImportIntervalValue + '\'' +
				", EnergyReactiveExportIntervalValue='" + EnergyReactiveExportIntervalValue + '\'' +
				", EnergyReactiveImportIntervalValue='" + EnergyReactiveImportIntervalValue + '\'' +
				", PowerActiveExportValue='" + PowerActiveExportValue + '\'' +
				", PowerReactiveExportValue='" + PowerReactiveExportValue + '\'' +
				", PowerReactiveImportValue='" + PowerReactiveImportValue + '\'' +
				", PowerFactorValue='" + PowerFactorValue + '\'' +
				", CurrentExportValue='" + CurrentExportValue + '\'' +
				", VoltageValue='" + VoltageValue + '\'' +
				", FrequencyValue='" + FrequencyValue + '\'' +
				", TemperatureValue='" + TemperatureValue + '\'' +
				", RPMValue='" + RPMValue + '\'' +
				", energyActiveImportRegisterValueES='" + energyActiveImportRegisterValueES + '\'' +
				", energyActiveImportRegisterUnitES='" + energyActiveImportRegisterUnitES + '\'' +
				", powerActiveImportUnitES='" + powerActiveImportUnitES + '\'' +
				", powerActiveImportValueES='" + powerActiveImportValueES + '\'' +
				", avg_power='" + avg_power + '\'' +
				", meterValueTimeStatmp=" + meterValueTimeStatmp +
				", startTransTimeStamp=" + startTransTimeStamp +
				", CurrentImportDiffValue='" + CurrentImportDiffValue + '\'' +
				", PowerActiveImportDiffValue='" + PowerActiveImportDiffValue + '\'' +
				", SoCDiffValue='" + SoCDiffValue + '\'' +
				", unqReqId='" + unqReqId + '\'' +
				", idTag='" + idTag + '\'' +
				", currencyRate=" + currencyRate +
				", chargeSessUniqId='" + chargeSessUniqId + '\'' +
				", chargeSessId=" + chargeSessId +
				", stnObj=" + stnObj +
				", siteObj=" + siteObj +
				", userObj=" + userObj +
				", stnId=" + stnId +
				", portId=" + portId +
				", driverGroupName='" + driverGroupName + '\'' +
				", driverGroupdId=" + driverGroupdId +
				", userEmail='" + userEmail + '\'' +
				", reasonForTermination='" + reasonForTermination + '\'' +
				", stationMode='" + stationMode + '\'' +
				", sessionDuration=" + sessionDuration +
				", totalKwUsed=" + totalKwUsed +
				", billSessionDuration=" + billSessionDuration +
				", billTotalKwUsed=" + billTotalKwUsed +
				", finalCosttostore=" + finalCosttostore +
				", finalCostInslcCurrency=" + finalCostInslcCurrency +
				", masterList=" + masterList +
				", socStartVal=" + socStartVal +
				", socEndVal=" + socEndVal +
				", startTxnProgress=" + startTxnProgress +
				", preProdSess='" + preProdSess + '\'' +
				", transactionId=" + transactionId +
				", connectorId=" + connectorId +
				", settlement='" + settlement + '\'' +
				", paymentMode='" + paymentMode + '\'' +
				", stnRefNum='" + stnRefNum + '\'' +
				", rewardType='" + rewardType + '\'' +
				", rewardValue=" + rewardValue +
				", selfCharging=" + selfCharging +
				", orgId=" + orgId +
				", site_crncy_char='" + site_crncy_char + '\'' +
				", site_orgId=" + site_orgId +
				", txnData=" + txnData +
				", accTxns=" + accTxns +
				", paygAccTxns=" + paygAccTxns +
				", cost_pricings='" + cost_pricings + '\'' +
				", previousSessionData=" + previousSessionData +
				", user_crncy_HexCode='" + user_crncy_HexCode + '\'' +
				", user_crncy_Code='" + user_crncy_Code + '\'' +
				", user_crncy_Char='" + user_crncy_Char + '\'' +
				", user_crncy_revenue=" + user_crncy_revenue +
				", user_orgId=" + user_orgId +
				", stTxnObj=" + stTxnObj +
				", sesspricings=" + sesspricings +
				", request='" + request + '\'' +
				", response='" + response + '\'' +
				", idleBilling=" + idleBilling +
				", needToDebit=" + needToDebit +
				", stop=" + stop +
				", currentImportPhase1Value='" + currentImportPhase1Value + '\'' +
				", currentImportPhase2Value='" + currentImportPhase2Value + '\'' +
				", currentImportPhase3Value='" + currentImportPhase3Value + '\'' +
				", voltagePhase1Value='" + voltagePhase1Value + '\'' +
				", voltagePhase2Value='" + voltagePhase2Value + '\'' +
				", voltagePhase3Value='" + voltagePhase3Value + '\'' +
				", ampFlag=" + ampFlag +
				", session=" + session +
				", vendingUnit='" + vendingUnit + '\'' +
				", vendingPrice=" + vendingPrice +
				", mail=" + mail +
				", sms=" + sms +
				", notification=" + notification +
				", currentScreen=" + currentScreen +
				", energyModify=" + energyModify +
				", actualEnergy=" + actualEnergy +
				'}';
	}
}