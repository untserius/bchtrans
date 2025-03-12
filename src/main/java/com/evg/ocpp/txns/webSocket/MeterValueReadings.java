package com.evg.ocpp.txns.webSocket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.forms.FinalData;
import com.evg.ocpp.txns.forms.MeterValue;
import com.evg.ocpp.txns.forms.SampledValue;
import com.evg.ocpp.txns.forms.SessionImportedValues;
import com.evg.ocpp.txns.model.ocpp.OCPPStartTransaction;
import com.evg.ocpp.txns.repository.ExecuteRepository;
import com.evg.ocpp.txns.utils.LoggerUtil;
import com.evg.ocpp.txns.utils.Utils;

@Service 
public class MeterValueReadings {
	private final static Logger logger = LoggerFactory.getLogger(MeterValueReadings.class);
	
	@Autowired
	private Utils utils;
	
	@Autowired
	private LoggerUtil customLogger;
	
	@Autowired
	private ExecuteRepository executeRepository;
	
	public SessionImportedValues energyConsumptionCalc(SessionImportedValues siv) {
		try {
			logger.info(Thread.currentThread().getId()+"start value : "+siv.getTxnData().getMeterStart()+" , unit : "+siv.getEnergyActiveImportRegisterUnit()+" , curr value : "+siv.getEnergyActiveImportRegisterValue());
			long meterValueCount=0;
			double maxkWh=siv.getTxnData().getMaxkWh();
			if(siv.getSesspricings()!=null) {
				meterValueCount=siv.getSesspricings().getMetervaluesCount();
			}
			BigDecimal value = new BigDecimal(siv.getEnergyActiveImportRegisterValue() == null ? "0.0" : siv.getEnergyActiveImportRegisterValue());
			if(meterValueCount==0 && siv.getTxnData().getMeterStart() < 0) {
				BigDecimal meterStart=new BigDecimal(String.valueOf(siv.getTxnData().getMeterStart()));
				if(siv.getEnergyActiveImportRegisterUnit().equalsIgnoreCase("Wh")) {
					meterStart=value.setScale(4, RoundingMode.HALF_UP);
				}else if(siv.getEnergyActiveImportRegisterUnit().equalsIgnoreCase("kWh")) {
					meterStart=value.multiply(new BigDecimal("1000")).setScale(4, RoundingMode.HALF_UP);
				}else if(siv.getEnergyActiveImportRegisterUnit().equalsIgnoreCase("W")) {
					meterStart=value.divide(new BigDecimal("3600"), 4, RoundingMode.HALF_UP);
				}
				siv.getTxnData().setMeterStart(meterStart.doubleValue());
				siv.getTxnData().setkWhStatus("Updated");
			}
			if(siv.getTxnData().getMeterStart() < 0) {
				siv.getTxnData().setMeterStart(0.0);
			}
			if(siv.isStop() && siv.getTxnData().getkWhStatus().equalsIgnoreCase("Updated")) {
				siv.setTotalKwUsed(new BigDecimal(String.valueOf(siv.getPreviousSessionData().get("kilowattHoursUsed"))));
			}else {
				if (siv.getEnergyActiveImportRegisterUnit().equalsIgnoreCase("Wh")) {
					BigDecimal val = value.subtract(new BigDecimal(String.valueOf(siv.getTxnData().getMeterStart())));
					siv.setTotalKwUsed(new BigDecimal(String.valueOf(utils.decimalwithFourdecimals(val))).divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP));
				} else if (siv.getEnergyActiveImportRegisterUnit().equalsIgnoreCase("kWh")) {
					siv.setTotalKwUsed(value.subtract(new BigDecimal(String.valueOf(siv.getTxnData().getMeterStart())).divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP)));
				} else if (siv.getEnergyActiveImportRegisterUnit().equalsIgnoreCase("W")) {
					BigDecimal val = value.subtract((new BigDecimal(String.valueOf(siv.getTxnData().getMeterStart())).divide(new BigDecimal("3600"), 4, RoundingMode.HALF_UP)));
					siv.setTotalKwUsed(new BigDecimal(String.valueOf(utils.decimalwithFourdecimals(val))).divide(new BigDecimal("3600000"), 4, RoundingMode.HALF_UP));
				} else {
					BigDecimal val = value.subtract(new BigDecimal(String.valueOf(siv.getTxnData().getMeterStart())));
					siv.setTotalKwUsed(new BigDecimal(String.valueOf(utils.decimalwithFourdecimals(val))).divide(new BigDecimal("1000"),4, RoundingMode.HALF_UP));
				}
			}
			if(meterValueCount==0) {
				if(siv.getTotalKwUsed().doubleValue()>maxkWh || siv.getTotalKwUsed().doubleValue()<0) {
					BigDecimal meterStart=new BigDecimal(String.valueOf(siv.getTxnData().getMeterStart()));
					if(siv.getEnergyActiveImportRegisterUnit().equalsIgnoreCase("Wh")) {
						meterStart=value.setScale(4, RoundingMode.HALF_UP);
					}else if(siv.getEnergyActiveImportRegisterUnit().equalsIgnoreCase("kWh")) {
						meterStart=value.multiply(new BigDecimal("1000")).setScale(4, RoundingMode.HALF_UP);
					}else if(siv.getEnergyActiveImportRegisterUnit().equalsIgnoreCase("W")) {
						meterStart=value.divide(new BigDecimal("3600"), 4, RoundingMode.HALF_UP);
					}
					siv.getTxnData().setMeterStart(meterStart.doubleValue());
					siv.setTotalKwUsed(new BigDecimal("0"));
					siv.getTxnData().setkWhStatus("Updated");
				}
			}
			siv.setActualEnergy(siv.getTotalKwUsed());

			BigDecimal energyDelivered=siv.getTotalKwUsed();

			BigDecimal duration=siv.getSessionDuration();

			BigDecimal portCapacityForMin=new BigDecimal(siv.getStnObj().get("capacity").asText()).divide(new BigDecimal("60"),4,RoundingMode.HALF_UP);

			if(energyDelivered.doubleValue()>0 && duration.doubleValue()>0 && portCapacityForMin.doubleValue()>0) {

				BigDecimal portCapacity=portCapacityForMin.multiply(duration).setScale(4, RoundingMode.HALF_UP);

				BigDecimal appr_portCapacity=portCapacity.add((portCapacity.multiply(new BigDecimal(siv.getTxnData().getMaxCapacityPer()))).divide(new BigDecimal("100"), 4,RoundingMode.HALF_UP)).setScale(4,RoundingMode.HALF_UP);

				if(appr_portCapacity.doubleValue()>0 && energyDelivered.doubleValue()>=appr_portCapacity.doubleValue()) {

					siv.setTotalKwUsed(portCapacity);
					siv.setEnergyModify(true);
//					siv.getTxnData().setEnergyModifyFlag(true);

				}

			}

			if(siv.getTotalKwUsed().doubleValue()<0) {
				siv.setTotalKwUsed(new BigDecimal("0"));
			}
			
			if(siv.getTotalKwUsed().doubleValue()<Double.parseDouble(String.valueOf(siv.getPreviousSessionData().get("kilowattHoursUsed")))) {
				siv.setTotalKwUsed(new BigDecimal(String.valueOf(siv.getPreviousSessionData().get("kilowattHoursUsed"))));
			}
			if(siv.getSessionDuration().doubleValue()<Double.parseDouble(String.valueOf(siv.getPreviousSessionData().get("sessionElapsedInMin")))) {
				siv.setSessionDuration(new BigDecimal(String.valueOf(siv.getPreviousSessionData().get("sessionElapsedInMin"))));
				siv.setMeterValueTimeStatmp(utils.stringToDate(String.valueOf(siv.getPreviousSessionData().get("endTimeStamp"))));
			}
			siv.setTotalKwUsed(siv.getTotalKwUsed().setScale(4, RoundingMode.HALF_UP));
			siv.setBillTotalKwUsed(siv.getTotalKwUsed());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return siv;
	}
	public BigDecimal getkwhValue(SessionImportedValues siv,boolean flag,double appr_portCapacity) {
		BigDecimal energy=new BigDecimal("0");
		try {
			if(flag) {
				String query="select isNull(power,0) as power,isNull(kwh,0) as kwh from session_energy where sessionId='"+siv.getChargeSessUniqId()+"' order by id desc";
				List<Map<String,Object>> list=executeRepository.findAll(query);
				if(list.size()>0) {
					BigDecimal kwh=new BigDecimal(String.valueOf(list.get(0).get("kwh")));
					kwh=siv.getTotalKwUsed().subtract(kwh);
					if(kwh.doubleValue()>=0 && kwh.doubleValue()<appr_portCapacity) {
						energy=kwh;
					}else {
						BigDecimal power=new BigDecimal(String.valueOf(list.get(0).get("power")));
						BigDecimal duration=siv.getSessionDuration().subtract(new BigDecimal(String.valueOf(siv.getPreviousSessionData().get("sessionElapsedInMin"))));
						energy=((power.add(new BigDecimal(String.valueOf(siv.getPowerActiveImportSessionValue())))).divide(new BigDecimal("2"),4,RoundingMode.HALF_UP)).multiply(duration.divide(new BigDecimal("60"), 15,RoundingMode.HALF_UP)).setScale(4, RoundingMode.HALF_UP);
					}
					String update="update session_energy set power="+siv.getPowerActiveImportSessionValue()+",kwh="+siv.getTotalKwUsed()+" where sessionId='"+siv.getChargeSessUniqId()+"'";
					executeRepository.update(update);
				}else {
					String insert="insert into session_energy (kwh,power,sessionId) values ("+siv.getTotalKwUsed()+","+siv.getPowerActiveImportSessionValue()+",'"+siv.getChargeSessUniqId()+"')";
					executeRepository.update(insert);
				}
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return energy;
	}
	
	
	public SessionImportedValues MeterValueReadings(FinalData finalData,String stationRefNum,SessionImportedValues siv,MeterValue mValue,OCPPStartTransaction startTransactionObj) {
		try {
			 Double powerVal = 0.00;
			 Double voltage = 0.00;
			 Double ampere = 0.00;
			 BigDecimal powerValue=new BigDecimal("0");
			 BigDecimal powerActiveImportValue=new BigDecimal("0");
			 boolean powerActive=false;
				if (mValue.getSampledValue().size() == 1) {
					siv.setEnergyActiveImportRegisterUnit(mValue.getSampledValue().get(0).getUnit() == null ? "Wh" : mValue.getSampledValue().get(0).getUnit());
					siv.setEnergyActiveImportRegisterValue(mValue.getSampledValue().get(0).getValue() == null ? "0" : mValue.getSampledValue().get(0).getValue());
				}
				for (SampledValue simpledVal : mValue.getSampledValue()) {
					if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Energy.Active.Import.Register")) {
						siv.setEnergyActiveImportRegisterContext(simpledVal.getContext());
						siv.setEnergyActiveImportRegisterFormat(simpledVal.getFormat());
						siv.setEnergyActiveImportRegisterLocation(simpledVal.getLocation());
						siv.setEnergyActiveImportRegisterMeasurand(simpledVal.getMeasurand());
						siv.setEnergyActiveImportRegisterPhase(simpledVal.getPhase());
						siv.setEnergyActiveImportRegisterUnit(simpledVal.getUnit()==null ? "Wh" : simpledVal.getUnit());
						siv.setEnergyActiveImportRegisterValue(String.valueOf(simpledVal.getValue()==null ? "0" : simpledVal.getValue()));
						
						String powerUnits = simpledVal.getUnit()==null ? "Wh" : simpledVal.getUnit();
						 powerValue = new BigDecimal(String.valueOf(simpledVal.getValue()== null ? "0" : simpledVal.getValue()));
						if (powerUnits.equalsIgnoreCase("Wh")) {
							powerValue = (powerValue.divide(new BigDecimal("1000"),9, RoundingMode.HALF_UP));
						} else if (powerUnits.equalsIgnoreCase("kWh")) {
							powerValue = (powerValue);
						} else if (powerUnits.equalsIgnoreCase("W")) {
							powerValue = new BigDecimal(String.valueOf(powerValue)).divide(new BigDecimal("3600000"),9, RoundingMode.HALF_UP);
						} else {
							powerValue = (powerValue);
						}
						siv.setEnergyActiveImportRegisterUnitES("kWh");
						siv.setEnergyActiveImportRegisterValueES(String.valueOf(powerValue));
					} else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Current.Import")) {
						siv.setCurrentImportContext(simpledVal.getContext());
						siv.setCurrentImportFormat(simpledVal.getFormat());
						siv.setCurrentImportLocation(simpledVal.getLocation());
						siv.setCurrentImportMeasurand(simpledVal.getMeasurand());
						siv.setCurrentImportPhase(simpledVal.getPhase());
						siv.setCurrentImportUnit(simpledVal.getUnit()==null ? "A" : simpledVal.getUnit());
						siv.setCurrentImportValue(simpledVal.getValue()== null ? "0.0" : simpledVal.getValue());
						
						ampere=simpledVal.getValue()== null ? 0.00 : Double.valueOf(simpledVal.getValue());
						
						if(simpledVal.getPhase()!=null) {
							if(simpledVal.getPhase().equalsIgnoreCase("L1")) {
								siv.setCurrentImportPhase1Value(simpledVal.getValue()== null ? "0.0" : simpledVal.getValue());
							}else if(simpledVal.getPhase().equalsIgnoreCase("L2")) {
								siv.setCurrentImportPhase2Value(simpledVal.getValue()== null ? "0.0" : simpledVal.getValue());
							}else if(simpledVal.getPhase().equalsIgnoreCase("L3")) {
								siv.setCurrentImportPhase3Value(simpledVal.getValue()== null ? "0.0" : simpledVal.getValue());
							}
						} else {
							siv.setCurrentImportPhase1Value("0.0");
							siv.setCurrentImportPhase2Value("0.0");
							siv.setCurrentImportPhase3Value("0.0");
						}
	
					} else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Current.Offered")) {
						siv.setCurrentOfferedUnit(simpledVal.getUnit()==null ? "A" : simpledVal.getUnit());
						siv.setCurrentOfferedValue(simpledVal.getValue()== null ? "0.0" : simpledVal.getValue());
					} else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Power.Active.Import")) {
						siv.setPowerActiveImportContext(simpledVal.getContext());
						siv.setPowerActiveImportFormat(simpledVal.getFormat());
						siv.setPowerActiveImportLocation(simpledVal.getLocation());
						siv.setPowerActiveImportMeasurand(simpledVal.getMeasurand());
						siv.setPowerActiveImportPhase(simpledVal.getPhase());
						siv.setPowerActiveImportUnit(simpledVal.getUnit()==null ? "W" : simpledVal.getUnit());
						siv.setPowerActiveImportValue(simpledVal.getValue()== null ? "0.0" : simpledVal.getValue());
						String powerUnits = simpledVal.getUnit()==null ? "W" : simpledVal.getUnit();
						 powerValue = new BigDecimal(String.valueOf(simpledVal.getValue()==null ? "0" : simpledVal.getValue().equalsIgnoreCase("null") ? "0" : simpledVal.getValue()));
						if (powerUnits.equalsIgnoreCase("W")) {
							powerValue = new BigDecimal(String.valueOf(powerValue)).divide(new BigDecimal("1000"), 5, RoundingMode.HALF_UP);
							powerUnits = "kW";
						} else {
							powerValue = (powerValue);
						}
						powerActive=true;
						powerActiveImportValue=powerValue;
					} else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Power.Offered")) {
						
						String powerUnits = simpledVal.getUnit()==null ? "W" : simpledVal.getUnit();
						 powerValue = new BigDecimal(String.valueOf(simpledVal.getValue()==null ? "0" : simpledVal.getValue()));
						if (powerUnits.equalsIgnoreCase("Wh")) {
							powerValue = (powerValue.divide(new BigDecimal("1000"),9,RoundingMode.HALF_UP));
						} else if (powerUnits.equalsIgnoreCase("kWh")) {
							powerValue = (powerValue);
						} else if (powerUnits.equalsIgnoreCase("W")) {
							powerValue = new BigDecimal(String.valueOf(powerValue.divide(new BigDecimal("3600000"), 9, RoundingMode.HALF_UP)));
						} else {
							powerValue = (powerValue);
						}
						siv.setPowerOfferedUnit("kW");
						siv.setPowerOfferedValue(String.valueOf(powerValue));
					} else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("SoC")) {
				
						siv.setSoCContext(simpledVal.getContext());
						siv.setSoCformat(simpledVal.getFormat());
						siv.setSoClocation(simpledVal.getLocation());
						siv.setSoCMeasurand(simpledVal.getMeasurand());
						siv.setSoCPhase(simpledVal.getPhase());
						siv.setSoCUnit(simpledVal.getUnit());
						siv.setSoCValue(simpledVal.getValue()==null ? "0" : simpledVal.getValue().equalsIgnoreCase("NaN") ? "0" : simpledVal.getValue());
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Energy.Active.Export.Register")) {
						
						String powerUnits =simpledVal.getUnit()==null ? "W": simpledVal.getUnit();
						 powerValue = new BigDecimal(String.valueOf(simpledVal.getValue()==null ? "0":simpledVal.getValue()));
						if (powerUnits.equalsIgnoreCase("Wh")) {
							powerValue = (powerValue.divide(new BigDecimal("1000"),9, RoundingMode.HALF_UP));
						} else if (powerUnits.equalsIgnoreCase("kWh")) {
							powerValue = (powerValue);
						} else if (powerUnits.equalsIgnoreCase("W")) {
							powerValue = new BigDecimal(String.valueOf(powerValue)).divide(new BigDecimal("3600000"), 5, RoundingMode.HALF_UP);
						} else {
							powerValue = (powerValue);
						}
						siv.setEnergyActiveExportRegisterValue(String.valueOf(powerValue));
						siv.setEnergyActiveExportRegisterUnit(simpledVal.getUnit()==null ? "W":simpledVal.getUnit().equalsIgnoreCase("W") ? "kWh" :simpledVal.getUnit().equalsIgnoreCase("Wh") ? "kWh" :(simpledVal.getUnit()));
						
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Energy.Active.Export.Interval")) {
						
						String powerUnits = simpledVal.getUnit()==null ? "W" :simpledVal.getUnit();
						 powerValue = new BigDecimal(String.valueOf(simpledVal.getValue()==null ? "0":simpledVal.getValue()));
						if (powerUnits.equalsIgnoreCase("Wh")) {
							powerValue = (powerValue.divide(new BigDecimal("1000"),9, RoundingMode.HALF_UP));
						} else if (powerUnits.equalsIgnoreCase("kWh")) {
							powerValue = (powerValue);
						} else if (powerUnits.equalsIgnoreCase("W")) {
							powerValue = new BigDecimal(String.valueOf(powerValue)).divide(new BigDecimal("3600000"), 5, RoundingMode.HALF_UP);
						} else {
							powerValue = (powerValue);
						}
						
						siv.setEnergyActiveExportIntervalUnit(simpledVal.getUnit()==null ? "W":simpledVal.getUnit().equalsIgnoreCase("W") ? "kWh" :simpledVal.getUnit().equalsIgnoreCase("Wh") ? "kWh" :(simpledVal.getUnit()));
						siv.setEnergyActiveExportIntervalValue(String.valueOf(powerValue));
						
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Energy.Active.Import.Interval")) {
						
						String powerUnits =simpledVal.getUnit()==null ? "W" : simpledVal.getUnit();
						 powerValue = new BigDecimal(String.valueOf(simpledVal.getValue()==null ? "0":simpledVal.getValue()));
						if (powerUnits.equalsIgnoreCase("Wh")) {
							powerValue = (powerValue.divide(new BigDecimal("1000"),9, RoundingMode.HALF_UP));
						} else if (powerUnits.equalsIgnoreCase("kWh")) {
							powerValue = (powerValue);
						} else if (powerUnits.equalsIgnoreCase("W")) {
							powerValue = new BigDecimal(String.valueOf(powerValue)).divide(new BigDecimal("3600000"), 5, RoundingMode.HALF_UP);
						} else {
							powerValue = (powerValue);
						}
						
						siv.setEnergyActiveImportIntervalUnit(simpledVal.getUnit()==null ? "W":simpledVal.getUnit().equalsIgnoreCase("W") ? "kWh" :simpledVal.getUnit().equalsIgnoreCase("Wh") ? "kWh" :(simpledVal.getUnit()));
						siv.setEnergyActiveImportIntervalValue(String.valueOf(powerValue));
						
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Energy.Reactive.Export.Register")) {
						
						String powerUnits = simpledVal.getUnit()==null ? "W" :simpledVal.getUnit();
						 powerValue = new BigDecimal(String.valueOf(simpledVal.getValue()==null ? "0":simpledVal.getValue()));
						if (powerUnits.equalsIgnoreCase("Wh")) {
							powerValue = (powerValue.divide(new BigDecimal("1000"),9, RoundingMode.HALF_UP));
						} else if (powerUnits.equalsIgnoreCase("kWh")) {
							powerValue = (powerValue);
						} else if (powerUnits.equalsIgnoreCase("W")) {
							powerValue = new BigDecimal(String.valueOf(powerValue)).divide(new BigDecimal("3600000"), 5, RoundingMode.HALF_UP);
						} else {
							powerValue = (powerValue);
						}
						
						siv.setEnergyReactiveExportRegisterUnit(simpledVal.getUnit()==null ? "W":simpledVal.getUnit().equalsIgnoreCase("W") ? "kWh" :simpledVal.getUnit().equalsIgnoreCase("Wh") ? "kWh" :(simpledVal.getUnit()));
						siv.setEnergyReactiveExportRegisterValue(String.valueOf(powerValue));
						
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Energy.Reactive.Import.Register")) {
						
						String powerUnits =simpledVal.getUnit()==null ? "W" : simpledVal.getUnit();
						 powerValue = new BigDecimal(String.valueOf(simpledVal.getValue()==null ? "0":simpledVal.getValue()));
						if (powerUnits.equalsIgnoreCase("Wh")) {
							powerValue = (powerValue.divide(new BigDecimal("1000"),9, RoundingMode.HALF_UP));
						} else if (powerUnits.equalsIgnoreCase("kWh")) {
							powerValue = (powerValue);
						} else if (powerUnits.equalsIgnoreCase("W")) {
							powerValue = new BigDecimal(String.valueOf(powerValue)).divide(new BigDecimal("3600000"), 5, RoundingMode.HALF_UP);
						} else {
							powerValue = (powerValue);
						}
						
						siv.setEnergyReactiveImportRegisterUnit(simpledVal.getUnit().equalsIgnoreCase("W") ? "kWh" :simpledVal.getUnit().equalsIgnoreCase("Wh") ? "kWh" :(simpledVal.getUnit()));
						siv.setEnergyReactiveImportRegisterValue(String.valueOf(powerValue));
						
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Energy.Reactive.Import.Interval")) {
						
						String powerUnits = simpledVal.getUnit()==null ? "W" :simpledVal.getUnit();
						 powerValue = new BigDecimal(String.valueOf(simpledVal.getValue()==null ? "0":simpledVal.getValue()));
						if (powerUnits.equalsIgnoreCase("Wh")) {
							powerValue = (powerValue.divide(new BigDecimal("1000"),9, RoundingMode.HALF_UP));
						} else if (powerUnits.equalsIgnoreCase("kWh")) {
							powerValue = (powerValue);
						} else if (powerUnits.equalsIgnoreCase("W")) {
							powerValue = new BigDecimal(String.valueOf(powerValue)).divide(new BigDecimal("3600000"), 5, RoundingMode.HALF_UP);
						} else {
							powerValue = (powerValue);
						}
						
						siv.setEnergyReactiveImportIntervalUnit(simpledVal.getUnit()==null ? "W":simpledVal.getUnit().equalsIgnoreCase("W") ? "kWh" :simpledVal.getUnit().equalsIgnoreCase("Wh") ? "kWh" :(simpledVal.getUnit()));
						siv.setEnergyReactiveImportIntervalValue(String.valueOf(powerValue));
						
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Energy.Reactive.Export.Interval")) {
						
						String powerUnits =simpledVal.getUnit()==null ? "W" : simpledVal.getUnit();
						 powerValue = new BigDecimal(String.valueOf(simpledVal.getValue()==null ? "0":simpledVal.getValue()));
						if (powerUnits.equalsIgnoreCase("Wh")) {
							powerValue = (powerValue.divide(new BigDecimal("1000"),9, RoundingMode.HALF_UP));
						} else if (powerUnits.equalsIgnoreCase("kWh")) {
							powerValue = (powerValue);
						} else if (powerUnits.equalsIgnoreCase("W")) {
							powerValue = new BigDecimal(String.valueOf(powerValue)).divide(new BigDecimal("3600000"), 5, RoundingMode.HALF_UP);
						} else {
							powerValue = (powerValue);
						}
						
						siv.setEnergyReactiveExportIntervalUnit(simpledVal.getUnit()==null ? "W":simpledVal.getUnit().equalsIgnoreCase("W") ? "kWh" :simpledVal.getUnit().equalsIgnoreCase("Wh") ? "kWh" :(simpledVal.getUnit()));
						siv.setEnergyReactiveExportIntervalValue(String.valueOf(powerValue));
						
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Power.Active.Export")) {
						
						String powerUnits = simpledVal.getUnit()==null ? "W" :simpledVal.getUnit();
						 powerValue = new BigDecimal(String.valueOf(simpledVal.getValue()==null ? "0":simpledVal.getValue()));
						if (powerUnits.equalsIgnoreCase("Wh")) {
							powerValue = (powerValue.divide(new BigDecimal("1000"),9, RoundingMode.HALF_UP));
						} else if (powerUnits.equalsIgnoreCase("kWh")) {
							powerValue = (powerValue);
						} else if (powerUnits.equalsIgnoreCase("W")) {
							powerValue = new BigDecimal(String.valueOf(powerValue)).divide(new BigDecimal("3600000"), 5, RoundingMode.HALF_UP);
						} else {
							powerValue = (powerValue);
						}
						
						siv.setPowerActiveExportUnit(simpledVal.getUnit()==null ? "W":simpledVal.getUnit().equalsIgnoreCase("W") ? "kWh" :simpledVal.getUnit().equalsIgnoreCase("Wh") ? "kWh" :(simpledVal.getUnit()));
						siv.setPowerActiveExportValue(String.valueOf(powerValue));
	
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Power.Reactive.Export")) {
						
						String powerUnits = simpledVal.getUnit()==null ? "W" :simpledVal.getUnit();
						 powerValue = new BigDecimal(String.valueOf(simpledVal.getValue()==null ? "0":simpledVal.getValue()));
						if (powerUnits.equalsIgnoreCase("Wh")) {
							powerValue = (powerValue.divide(new BigDecimal("1000"),9, RoundingMode.HALF_UP));
						} else if (powerUnits.equalsIgnoreCase("kWh")) {
							powerValue = (powerValue);
						} else if (powerUnits.equalsIgnoreCase("W")) {
							powerValue = new BigDecimal(String.valueOf(powerValue)).divide(new BigDecimal("3600000"), 5, RoundingMode.HALF_UP);
						} else {
							powerValue = (powerValue);
						}
						
						siv.setPowerReactiveExportUnit(simpledVal.getUnit()==null ? "W":simpledVal.getUnit().equalsIgnoreCase("W") ? "kWh" :simpledVal.getUnit().equalsIgnoreCase("Wh") ? "kWh" :(simpledVal.getUnit()));
						siv.setPowerReactiveExportValue(String.valueOf(powerValue));
	
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Power.Reactive.Import")) {
						
						String powerUnits =simpledVal.getUnit()==null ? "W" : simpledVal.getUnit();
						 powerValue = new BigDecimal(String.valueOf(simpledVal.getValue()==null ? "0":simpledVal.getValue()));
						if (powerUnits.equalsIgnoreCase("Wh")) {
							powerValue = (powerValue.divide(new BigDecimal("1000"),9, RoundingMode.HALF_UP));
						} else if (powerUnits.equalsIgnoreCase("kWh")) {
							powerValue = (powerValue);
						} else if (powerUnits.equalsIgnoreCase("W")) {
							powerValue = new BigDecimal(String.valueOf(powerValue)).divide(new BigDecimal("3600000"), 5, RoundingMode.HALF_UP);
						} else {
							powerValue = (powerValue);
						}
						
						siv.setPowerReactiveImportUnit(simpledVal.getUnit()==null ? "W":simpledVal.getUnit().equalsIgnoreCase("W") ? "kWh" :simpledVal.getUnit().equalsIgnoreCase("Wh") ? "kWh" :(simpledVal.getUnit()));
						siv.setPowerReactiveImportValue(String.valueOf(powerValue));
	
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Power.Factor")) {
						
						String powerUnits =simpledVal.getUnit()==null ? "W" : simpledVal.getUnit();
						 powerValue = new BigDecimal(String.valueOf(simpledVal.getValue()==null ? "0":simpledVal.getValue()));
						if (powerUnits.equalsIgnoreCase("Wh")) {
							powerValue = (powerValue.divide(new BigDecimal("1000"),9, RoundingMode.HALF_UP));
						} else if (powerUnits.equalsIgnoreCase("kWh")) {
							powerValue = (powerValue);
						} else if (powerUnits.equalsIgnoreCase("W")) {
							powerValue = new BigDecimal(String.valueOf(powerValue)).divide(new BigDecimal("3600000"), 5, RoundingMode.HALF_UP);
						} else {
							powerValue = (powerValue);
						}
						
						siv.setPowerFactorUnit(simpledVal.getUnit()==null ? "W":simpledVal.getUnit().equalsIgnoreCase("W") ? "kWh" :simpledVal.getUnit().equalsIgnoreCase("Wh") ? "kWh" :(simpledVal.getUnit()));
						siv.setPowerFactorValue(String.valueOf(powerValue));
	
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Current.Export")) {//
						siv.setCurrentExportUnit(simpledVal.getUnit());
						siv.setCurrentExportValue(simpledVal.getValue());
	
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Voltage")) {///
						siv.setVoltageUnit(simpledVal.getUnit());
						siv.setVoltageValue(simpledVal.getValue());
						voltage=simpledVal.getValue()== null ? 0.00 : Double.valueOf(simpledVal.getValue());
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Voltage") && simpledVal.getPhase()!=null &&simpledVal.getPhase().equalsIgnoreCase("L1-N") ) {
						siv.setVoltagePhase1Value(simpledVal.getValue()== null ? "0.0" : simpledVal.getValue());
					} else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Voltage") && simpledVal.getPhase()!=null &&simpledVal.getPhase().equalsIgnoreCase("L2-N") ) {
						siv.setVoltagePhase2Value(simpledVal.getValue()== null ? "0.0" : simpledVal.getValue());
					} else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Voltage") && simpledVal.getPhase()!=null &&simpledVal.getPhase().equalsIgnoreCase("L3-N") ) {
						siv.setVoltagePhase3Value(simpledVal.getValue()== null ? "0.0" : simpledVal.getValue());
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Frequency")) {///
						siv.setFrequencyUnit(simpledVal.getUnit());
						siv.setFrequencyValue(simpledVal.getValue());
	
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("Temperature")) {///
						siv.setTemperatureUnit(simpledVal.getUnit());
						siv.setTemperatureValue(simpledVal.getValue());
	
					}else if (simpledVal.getMeasurand() != null && simpledVal.getMeasurand().equalsIgnoreCase("RPM")) {
						
						String powerUnits = simpledVal.getUnit()==null ? "W" :simpledVal.getUnit();
						 powerValue = new BigDecimal(String.valueOf(simpledVal.getValue()==null?"0":simpledVal.getValue()));
						if (powerUnits.equalsIgnoreCase("Wh")) {
							powerValue = (powerValue.divide(new BigDecimal("1000"),9, RoundingMode.HALF_UP));
						} else if (powerUnits.equalsIgnoreCase("kWh")) {
							powerValue = (powerValue);
						} else if (powerUnits.equalsIgnoreCase("W")) {
							powerValue = new BigDecimal(String.valueOf(powerValue)).divide(new BigDecimal("3600000"), 5, RoundingMode.HALF_UP);
						} else {
							powerValue = (powerValue);
						}
						
						siv.setRPMUnit(simpledVal.getUnit()==null ? "W" :simpledVal.getUnit().equalsIgnoreCase("W") ? "kWh" :simpledVal.getUnit().equalsIgnoreCase("Wh") ? "kWh" :(simpledVal.getUnit()));
						siv.setRPMValue(String.valueOf(powerValue));
					}
				}
				powerVal = voltage * ampere;
				if(!powerActive && powerVal > 0) {
					powerVal = powerVal/1000;
				}else {
					powerVal=powerActiveImportValue.doubleValue();
				}
				customLogger.info(stationRefNum, "voltage : "+voltage+" , ampere : "+ampere+" , powerVal : "+powerVal);
				siv.setPowerOfferedUnit("kW");
				siv.setPowerOfferedValue(String.valueOf(powerVal));
				
				siv.setPowerActiveImportSessionValue(Double.parseDouble(String.valueOf(powerVal)));
				
				siv.setPowerActiveImportUnitES("kW");
				siv.setPowerActiveImportValueES(String.valueOf(powerVal));
		}catch(Exception e) {
			e.printStackTrace();
		}
		return siv;
    }

}
