package com.evg.ocpp.txns.ServiceImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.Service.DynamicTariffService;
import com.evg.ocpp.txns.dao.GeneralDao;
import com.evg.ocpp.txns.model.ocpp.SessionPricing;
import com.evg.ocpp.txns.utils.Utils;

@Service
public class DynamicTariffServiceImpl implements DynamicTariffService{
	
	@Autowired
	private  GeneralDao<?, ?> generalDao;
	
	@Autowired
	private Utils Utils;
	
	private final static Logger LOGGER = LoggerFactory.getLogger(DynamicTariffServiceImpl.class);
	
	@Override
	public List<Map<String,Object>> getDriverGroupProfileDetails(Date startTime, Date currentTime,long driverGroupId,long monthId,long currentDayId,String chargerType){
		LOGGER.info("start getDriverGroupProfileDetails");
		List<Map<String,Object>> profilesList=new ArrayList();
		List<Map<String,Object>> newprofilesList=new ArrayList();
		Map<String,Object> profileDetails=new HashMap();
		boolean flag=true;
		long negativePriceId=0;
		if(chargerType.contains("DC")) {
			chargerType="DC";
		}else if(chargerType.contains("AC")) {
			chargerType="AC";
		}
		try {
			//Profiles Check in normal Condition from start to current meter Value
			String profileQuery="select  dtp.chargerType, dt.id as profileId,dtp.id as priceId,dm.monthId,dtd.dayId,dt.profileName,dtp.price,dtp.priceType,dtp.endTime,dtp.startTime,dtp.cost,DATEADD(SS,(-DATEDIFF(SS,convert(Time,CONCAT(t.hour, ':',t.min,':',t.sec)),convert(Time,'"+Utils.stringToDate(currentTime)+"'))),'"+Utils.stringToDate(currentTime)+"') as profileStartTime,DATEADD(SS,(-DATEDIFF(SS,convert(Time,(select CONCAT(tt.hour, ':',tt.min,':',tt.sec) from time tt where tt.id=dtp.endTime)),convert(Time,'"+Utils.stringToDate(currentTime)+"'))),'"+Utils.stringToDate(currentTime)+"') as profileEndTime, DATEDIFF(SS,convert(Time,CONCAT(t.hour, ':',t.min,':',t.sec)),convert(Time,(select CONCAT(tt.hour, ':',tt.min,':',tt.sec) from time tt where tt.id=dtp.endTime))) as timeDiff from dynamicTariff_in_drivergroup dtdg inner join dynamic_Tariff dt on dt.id=dtdg.dynamicProfileId inner join dynamicTariff_Month dm on dm.dynamicProfileId=dt.id inner join dynamicTarrif_dynamicPrice dp on dp.dynamicProfileId=dt.id inner join dynaminTariffPrice dtp on dtp.id=dp.dynamicPriceId inner join  dynamicTarrifPrice_Day dtd on dtd.dynamicPriceId=dtp.id inner join time t on t.id=dtp.startTime where (dtp.chargerType like '%"+chargerType+"%' or dtp.chargerType='both') and dtdg.driverGroupId='"+driverGroupId+"' and dm.monthId='"+monthId+"' and ((convert(datetime,'"+Utils.stringToDate(startTime)+"') between DATEADD(SS,(-DATEDIFF(SS,convert(Time,CONCAT(t.hour, ':',t.min,':',t.sec)),convert(Time,'"+Utils.stringToDate(currentTime)+"'))),'"+Utils.stringToDate(currentTime)+"') and DATEADD(SS,(-DATEDIFF(SS,convert(Time,(select CONCAT(tt.hour, ':',tt.min,':',tt.sec) from time tt where tt.id=dtp.endTime)),convert(Time,'"+Utils.stringToDate(currentTime)+"'))),'"+Utils.stringToDate(currentTime)+"')) or (convert(datetime,'"+Utils.stringToDate(currentTime)+"') between DATEADD(SS,(-DATEDIFF(SS,convert(Time,CONCAT(t.hour, ':',t.min,':',t.sec)),convert(Time,'"+Utils.stringToDate(currentTime)+"'))),'"+Utils.stringToDate(currentTime)+"') and DATEADD(SS,(-DATEDIFF(SS,convert(Time,(select CONCAT(tt.hour, ':',tt.min,':',tt.sec) from time tt where tt.id=dtp.endTime)),convert(Time,'"+Utils.stringToDate(currentTime)+"'))),'"+Utils.stringToDate(currentTime)+"')) or (DATEADD(SS,(-DATEDIFF(SS,convert(Time,CONCAT(t.hour, ':',t.min,':',t.sec)),convert(Time,'"+Utils.stringToDate(currentTime)+"'))),'"+Utils.stringToDate(currentTime)+"') between convert(datetime,'"+Utils.stringToDate(startTime)+"') and convert(datetime,'"+Utils.stringToDate(currentTime)+"')) or (DATEADD(SS,(-DATEDIFF(SS,convert(Time,(select CONCAT(tt.hour, ':',tt.min,':',tt.sec) from time tt where tt.id=dtp.endTime)),convert(Time,'"+Utils.stringToDate(currentTime)+"'))),'"+Utils.stringToDate(currentTime)+"') between convert(datetime,'"+Utils.stringToDate(startTime)+"') and convert(datetime,'"+Utils.stringToDate(currentTime)+"')))";
			profilesList=generalDao.getMapData(profileQuery);
			//Check previous day profiles for negative time difference
			String query="select dt.chargerType, dtf.id as profileId,dp.dayId,dtf.profileName,dt.price,dt.priceType,dt.id as dynamicPriceId,startTime,endTime,dt.cost,DATEDIFF(SS,convert(Time,(select CONCAT(hour, ':',min,':',sec ) as profileEndTime from time t where t.id=dt.startTime )),convert(Time,(select CONCAT(hour, ':',min,':',sec ) as profileEndTime from time t where t.id=dt.endTime ))) as timeInSec from dynaminTariffPrice dt inner join dynamicTarrifPrice_Day dp on dt.id=dp.dynamicPriceId inner join dynamicTarrif_dynamicPrice dd on dt.id=dd.dynamicPriceId  inner join dynamic_Tariff dtf on dtf.id=dd.dynamicProfileId inner join dynamicTariff_Month dtm on dtm.dynamicProfileId=dd.dynamicProfileId inner join dynamicTariff_in_drivergroup dtdg on dtm.dynamicProfileId=dtdg.dynamicProfileId where (dt.chargerType like '%"+chargerType+"%' or dt.chargerType='both') and dtdg.driverGroupId= '"+driverGroupId+"' and dtm.monthId='"+monthId+"'";
			List<Map<String, Object>> listData=generalDao.getMapData(query);
			if(listData!=null && listData.size()>0) {
				for(Map<String, Object> data :listData) {
					Double time=Double.parseDouble(String.valueOf(data.get("timeInSec")));
					negativePriceId=Long.parseLong(String.valueOf(data.get("dynamicPriceId")));
					long dayId=Long.parseLong(String.valueOf(data.get("dayId")));
					long negativeDayId=startTime.getDay()==0 ? 7 :startTime.getDay();
					if(time<0) {
							String negativeQuery="select  dtp.chargerType, dt.id as profileId,dtp.id as priceId,dm.monthId,dtd.dayId,dt.profileName,dtp.price,dtp.priceType,dtp.endTime,dtp.startTime,dtp.cost,DATEADD(SS,(-DATEDIFF(SS,convert(Time,CONCAT(t.hour, ':',t.min,':',t.sec)),convert(Time,'"+Utils.stringToDate(startTime)+"'))),'"+Utils.stringToDate(startTime)+"') as profileStartTime,DATEDIFF(SS,convert(Time,CONCAT(t.hour, ':',t.min,':',t.sec)),convert(Time,(select CONCAT('23', ':','59',':','59')))) as beforeTime ,DATEDIFF(SS,convert(Time,CONCAT('00', ':','00',':','00')),convert(Time,(select CONCAT(tt.hour, ':',tt.min,':',tt.sec) from time tt where tt.id=dtp.endTime))) as afterTime from dynamicTariff_in_drivergroup dtdg inner join dynamic_Tariff dt on dt.id=dtdg.dynamicProfileId inner join dynamicTariff_Month dm on dm.dynamicProfileId=dt.id inner join dynamicTarrif_dynamicPrice dp on dp.dynamicProfileId=dt.id inner join dynaminTariffPrice dtp on dtp.id=dp.dynamicPriceId inner join  dynamicTarrifPrice_Day dtd on dtd.dynamicPriceId=dtp.id inner join time t on t.id=dtp.startTime where (dtp.chargerType like '%"+chargerType+"%' or dtp.chargerType='both') and dtdg.driverGroupId='"+driverGroupId+"' and dm.monthId='"+monthId+"' and dtd.dayId='"+currentDayId+"' and dtp.id='"+negativePriceId+"' and (convert(datetime,'"+Utils.stringToDate(startTime)+"') between DATEADD(SS,(-DATEDIFF(SS,convert(Time,CONCAT(t.hour, ':',t.min,':',t.sec)),convert(Time,'"+Utils.stringToDate(startTime)+"'))),'"+Utils.stringToDate(startTime)+"') and DATEADD(SS,(-DATEDIFF(SS,convert(Time,(CONCAT('23', ':','59',':','59'))),convert(Time,'"+Utils.stringToDate(startTime)+"'))),'"+Utils.stringToDate(startTime)+"') or DATEADD(SS,(-DATEDIFF(SS,convert(Time,CONCAT(t.hour, ':',t.min,':',t.sec)),convert(Time,'"+Utils.stringToDate(startTime)+"'))),'"+Utils.stringToDate(startTime)+"') between convert(datetime,'"+Utils.stringToDate(startTime)+"') and convert(datetime,'"+Utils.stringToDate(currentTime)+"'))";
							List<Map<String, Object>> list=generalDao.getMapData(negativeQuery);
							if(list.size()>0) {
							profileDetails=list.get(0);
							int beforeTimeDiff=Integer.parseInt(String.valueOf(profileDetails.get("beforeTime")));
							int afterTimeDiff=Integer.parseInt(String.valueOf(profileDetails.get("afterTime")));
							Date profileStartTime=Utils.stringToDate(String.valueOf(profileDetails.get("profileStartTime")));
							Date profileEndTime=Utils.addDateSec(profileStartTime, beforeTimeDiff+afterTimeDiff);
							profileDetails.put("profileStartTime", Utils.stringToDate(profileStartTime));
							profileDetails.put("profileEndTime", Utils.stringToDate(profileEndTime));
							profileDetails.put("timeDiff", beforeTimeDiff+afterTimeDiff);
							}else{
							String negativeQuery1="select  dtp.chargerType, dt.id as profileId,dtp.id as priceId,dm.monthId,dtd.dayId,dt.profileName,dtp.price,dtp.priceType,dtp.endTime,dtp.startTime,dtp.cost,DATEADD(SS,(-DATEDIFF(SS,convert(Time,(select CONCAT(tt.hour, ':',tt.min,':',tt.sec) from time tt where tt.id=dtp.endTime)),convert(Time,'"+Utils.stringToDate(startTime)+"'))),'"+Utils.stringToDate(startTime)+"') as profileEndTime, DATEDIFF(SS,convert(Time,CONCAT(t.hour, ':',t.min,':',t.sec)),convert(Time,(select CONCAT('23', ':','59',':','59')))) as beforeTime ,DATEDIFF(SS,convert(Time,CONCAT('00', ':','00',':','00')),convert(Time,(select CONCAT(tt.hour, ':',tt.min,':',tt.sec) from time tt where tt.id=dtp.endTime))) as afterTime from dynamicTariff_in_drivergroup dtdg inner join dynamic_Tariff dt on dt.id=dtdg.dynamicProfileId inner join dynamicTariff_Month dm on dm.dynamicProfileId=dt.id inner join dynamicTarrif_dynamicPrice dp on dp.dynamicProfileId=dt.id inner join dynaminTariffPrice dtp on dtp.id=dp.dynamicPriceId inner join  dynamicTarrifPrice_Day dtd on dtd.dynamicPriceId=dtp.id inner join time t on t.id=dtp.startTime where (dtp.chargerType like '%"+chargerType+"%' or dtp.chargerType='both') and dtdg.driverGroupId='"+driverGroupId+"' and dm.monthId='"+monthId+"' and dtd.dayId='"+negativeDayId+"' and dtp.id='"+negativePriceId+"' and (convert(datetime,'"+Utils.stringToDate(startTime)+"') between DATEADD(SS,(-DATEDIFF(SS,convert(Time,CONCAT('00', ':','00',':','00')),convert(Time,'"+Utils.stringToDate(startTime)+"'))),'"+Utils.stringToDate(startTime)+"') and DATEADD(SS,(-DATEDIFF(SS,convert(Time,(select CONCAT(tt.hour, ':',tt.min,':',tt.sec) from time tt where tt.id=dtp.endTime)),convert(Time,'"+Utils.stringToDate(startTime)+"'))),'"+Utils.stringToDate(startTime)+"'))";
							List<Map<String, Object>> list1=generalDao.getMapData(negativeQuery1);
							if(list1.size()>0) {
							profileDetails=list1.get(0);
							int beforeTimeDiff=Integer.parseInt(String.valueOf(profileDetails.get("beforeTime")));
							int afterTimeDiff=Integer.parseInt(String.valueOf(profileDetails.get("afterTime")));
							Date profileEndTime=Utils.stringToDate(String.valueOf(profileDetails.get("profileEndTime")));
							Date profileStartTime=Utils.addDateSec(profileEndTime, -(beforeTimeDiff+afterTimeDiff));
//							profileEndTime=Utils.addDate(profileEndTime, -(24));
//							profileStartTime=Utils.addDate(profileStartTime, -(24));
							profileDetails.put("profileStartTime", Utils.stringToDate(profileStartTime));
							profileDetails.put("profileEndTime", Utils.stringToDate(profileEndTime));
							profileDetails.put("timeDiff", beforeTimeDiff+afterTimeDiff);
							}
						}
					}
				}
			}
			for(Map<String,Object> data :profilesList) {
//				long priceId=Long.parseLong(String.valueOf(data.get("priceId")));
//				if(priceId==negativePriceId) {
//					flag=false;
//					System.out.println("90>>");
//				}
				if(Double.parseDouble(String.valueOf(data.get("timeDiff")))>0 && (Long.parseLong(String.valueOf(data.get("dayId")))==(startTime.getDay()+1) || Long.parseLong(String.valueOf(data.get("dayId")))==(currentTime.getDay()+1))) {
					if(Long.parseLong(String.valueOf(data.get("dayId")))==(Utils.stringToDate(String.valueOf(data.get("profileStartTime"))).getDay()+1)) {
						newprofilesList.add(data);
					}
				}
			}
			
			if(flag && profileDetails!=null && !profileDetails.isEmpty()) {
				newprofilesList.add(profileDetails);
			}
			if(newprofilesList.size()>0) {
				newprofilesList.sort(Comparator.comparing(o -> Utils.stringToDate(String.valueOf(o.get("profileStartTime")))));
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("76>>profilesList :"+newprofilesList);
		LOGGER.info("end getDriverGroupProfileDetails");
		return newprofilesList;
	}
	
	@Override
	public List<Map<String,Object>> getBillingDetails(List<Map<String,Object>> profileDetailsList, Date startTransactionTime, Date currentTime,String randomSessionId,Map<String,Object> portObj,double kwhUsed,double duration,double vendingPrice,String vendingUnit){
		LOGGER.info("start getBillingDetails");
		List<Map<String,Object>> billingDetailsList=new ArrayList(); 
		try {
			portObj.put("vendingPricePerUnit", vendingPrice);
			portObj.put("vendingPriceUnit", vendingUnit);
			Map<String,Object> lastSessionDetails=getLastSessionDetails(randomSessionId);
			Date lastSessionEndDate=startTransactionTime;
			Date lastSessionEndDate1=startTransactionTime;
			Date lastSessionStartDate=startTransactionTime;
			Date lastSessionEndDate2=startTransactionTime;
			long lastSessionPriceId=0;
			Date startTime=startTransactionTime;
			//Double capacity=Double.parseDouble(String.valueOf(portObj.get("capacity")));
			double kwhUsedInSession=0.0;
			double totalkwhUsedInSession=0.0;
			double sessionMin=0.0;
			double totalMinUsedInSession=0.0;
			boolean noprofile=true;
			boolean lastsessionFlag=true;
			long priceId=0;
			long lastPriceId=0;
			if(lastSessionDetails!=null) {
				lastSessionStartDate=Utils.stringToDate(String.valueOf(lastSessionDetails.get("startTimeStamp")));
				lastSessionEndDate=Utils.stringToDate(String.valueOf(lastSessionDetails.get("endTimeStamp")));
				lastSessionEndDate1=Utils.stringToDate(String.valueOf(lastSessionDetails.get("endTimeStamp")));
				lastSessionEndDate2=Utils.stringToDate(String.valueOf(lastSessionDetails.get("endTimeStamp")));
				lastSessionPriceId=Long.parseLong(String.valueOf(lastSessionDetails.get("dynamicPriceId")));
				lastPriceId=Long.parseLong(String.valueOf(lastSessionDetails.get("sesssionPriceId")));
				totalkwhUsedInSession=Double.parseDouble(String.valueOf(lastSessionDetails.get("kwhUsedInSession")));
				totalMinUsedInSession=Double.parseDouble(String.valueOf(lastSessionDetails.get("duration")));
				kwhUsedInSession=Double.parseDouble(String.valueOf(lastSessionDetails.get("totalKwUsed")));
				sessionMin=Double.parseDouble(String.valueOf(lastSessionDetails.get("sessionDurationInMin")));
				kwhUsed=kwhUsed-totalkwhUsedInSession;
				duration=duration-totalMinUsedInSession;
			}
			
			if(duration!=0) {
			double kwhdivide=kwhUsed/duration;
			for(Map<String,Object> profileDetails :profileDetailsList) {
				Date profileStartTime=Utils.stringToDate(String.valueOf(profileDetails.get("profileStartTime")));
				Date profileEndTime=Utils.stringToDate(String.valueOf(profileDetails.get("profileEndTime")));
				if(profileStartTime.compareTo(currentTime)<0) {
				double totalKwhUsed=0.0;
				priceId=Long.parseLong(String.valueOf(profileDetails.get("priceId")));
				if(lastSessionEndDate2.compareTo(profileEndTime)<0) {
				startTime=lastSessionEndDate;
				Map<String,Object> profileObject=new HashMap();
				Map<String,Object> profileObject1=new HashMap();
				//startTime first
				if(startTime.compareTo(profileStartTime)<=0) {
					Double sessionDurationInMin=Utils.timeDifference(lastSessionEndDate,profileStartTime);
					if(lastSessionStartDate.compareTo(profileStartTime)<0 && lastsessionFlag && lastSessionStartDate.compareTo(startTransactionTime)!=0 && lastSessionEndDate.compareTo(profileStartTime)!=0) {
					 sessionDurationInMin=Utils.timeDifference(lastSessionEndDate2,profileStartTime);
					 lastsessionFlag=false;
					}else {
					 sessionDurationInMin=Utils.timeDifference(lastSessionEndDate,profileStartTime);
					}
//					sessionDurationInMin=Utils.round(sessionDurationInMin);
					totalKwhUsed=kwhdivide*sessionDurationInMin;
					if(lastSessionPriceId==0) {
						totalKwhUsed=totalKwhUsed+kwhUsedInSession;
						sessionDurationInMin=sessionDurationInMin+sessionMin;
					}
					totalKwhUsed=Utils.round(totalKwhUsed);
					double sessionDurationInSec=sessionDurationInMin*60;
					if(sessionDurationInSec>0.010) {
						profileObject.put("dynamicProfileId", "0");
						profileObject.put("dynamicPriceId", "0");
						profileObject.put("vendingPricePerUnit", String.valueOf(portObj.get("vendingPricePerUnit")));
						profileObject.put("vendingPriceUnit", String.valueOf(portObj.get("vendingPriceUnit")));
						profileObject.put("minUsed", sessionDurationInMin);
						profileObject.put("kwhUsed", totalKwhUsed);
						profileObject.put("startTimeStamp", Utils.stringToDate(startTime));
						profileObject.put("endTimeStamp", Utils.stringToDate(profileStartTime));
						profileObject.put("startTimeId", "0");
						profileObject.put("endTimeId", "0");
						profileObject.put("cost", "0");
						profileObject.put("dynamicProfileName", "Standard");
						System.out.println("280>>");
						billingDetailsList.add(profileObject);
					}
					if(profileEndTime.compareTo(currentTime)<0) {
						Double time=Utils.timeDifference(profileStartTime,profileEndTime);
//						time=Utils.round(time);
						double kwh=time*kwhdivide;
						kwh=Utils.round(kwh);
						profileObject1.put("dynamicProfileId", String.valueOf(profileDetails.get("profileId")));
						profileObject1.put("dynamicPriceId",String.valueOf(profileDetails.get("priceId")));
						profileObject1.put("vendingPricePerUnit", String.valueOf(profileDetails.get("price")));
						profileObject1.put("vendingPriceUnit", String.valueOf(profileDetails.get("priceType")));
						profileObject1.put("minUsed", time);
						profileObject1.put("kwhUsed", kwh);
						profileObject1.put("startTimeStamp", Utils.stringToDate(profileStartTime));
						profileObject1.put("endTimeStamp", Utils.stringToDate(profileEndTime));
						profileObject1.put("startTimeId", String.valueOf(profileDetails.get("startTime")));
						profileObject1.put("endTimeId", String.valueOf(profileDetails.get("endTime")));
						profileObject1.put("cost", String.valueOf(profileDetails.get("cost")));
						profileObject1.put("dynamicProfileName",String.valueOf(profileDetails.get("profileName")));
						System.out.println("300>>");
						billingDetailsList.add(profileObject1);
					}else {
						Double time=Utils.timeDifference(profileStartTime,currentTime);
//						time=Utils.round(time);
						double kwh=time*kwhdivide;
						kwh=Utils.round(kwh);
						profileObject1.put("dynamicProfileId", String.valueOf(profileDetails.get("profileId")));
						profileObject1.put("dynamicPriceId",String.valueOf(profileDetails.get("priceId")));
						profileObject1.put("vendingPricePerUnit", String.valueOf(profileDetails.get("price")));
						profileObject1.put("vendingPriceUnit", String.valueOf(profileDetails.get("priceType")));
						profileObject1.put("minUsed", time);
						profileObject1.put("kwhUsed", kwh);
						profileObject1.put("startTimeStamp", Utils.stringToDate(profileStartTime));
						profileObject1.put("endTimeStamp", Utils.stringToDate(currentTime));
						profileObject1.put("startTimeId", String.valueOf(profileDetails.get("startTime")));
						profileObject1.put("endTimeId", String.valueOf(profileDetails.get("endTime")));
						profileObject1.put("cost", String.valueOf(profileDetails.get("cost")));
						profileObject1.put("dynamicProfileName",String.valueOf(profileDetails.get("profileName")));
						System.out.println("319>>");
						billingDetailsList.add(profileObject1);
					}
					
				}else {
					Date startProfile=lastSessionEndDate;
					if(lastSessionDetails!=null) {
						startProfile=lastSessionEndDate2;
					}else {
						startProfile=startTransactionTime;
						profileStartTime=startProfile;
					}
					if(profileEndTime.compareTo(currentTime)<0) {
						
						Double time=Utils.timeDifference(startProfile,profileEndTime);
//						time=Utils.round(time);
						totalKwhUsed=kwhdivide*time;
						if(priceId==lastSessionPriceId) {
							totalKwhUsed=totalKwhUsed+kwhUsedInSession;
							time=time+sessionMin;
						}
						totalKwhUsed=Utils.round(totalKwhUsed);
						profileObject1.put("dynamicProfileId", String.valueOf(profileDetails.get("profileId")));
						profileObject1.put("dynamicPriceId",String.valueOf(profileDetails.get("priceId")));
						profileObject1.put("totalEnergyCost", "");
						profileObject1.put("vendingPricePerUnit", String.valueOf(profileDetails.get("price")));
						profileObject1.put("vendingPriceUnit", String.valueOf(profileDetails.get("priceType")));
						profileObject1.put("minUsed", time);
						profileObject1.put("kwhUsed", totalKwhUsed);
						profileObject1.put("startTimeStamp", Utils.stringToDate(startTransactionTime));
						profileObject1.put("endTimeStamp", Utils.stringToDate(profileEndTime));
						profileObject1.put("startTimeId", String.valueOf(profileDetails.get("startTime")));
						profileObject1.put("endTimeId", String.valueOf(profileDetails.get("endTime")));
						profileObject1.put("cost", String.valueOf(profileDetails.get("cost")));
						profileObject1.put("dynamicProfileName",String.valueOf(profileDetails.get("profileName")));
						System.out.println("354>>");
						billingDetailsList.add(profileObject1);
					}else {
						
						Double time=Utils.timeDifference(startProfile,currentTime);
//						time=Utils.round(time);
						totalKwhUsed=kwhdivide*time;
						if(priceId==lastSessionPriceId) {
							totalKwhUsed=totalKwhUsed+kwhUsedInSession;
							time=time+sessionMin;
						}
						totalKwhUsed=Utils.round(totalKwhUsed);
						profileObject1.put("dynamicProfileId", String.valueOf(profileDetails.get("profileId")));
						profileObject1.put("dynamicPriceId",String.valueOf(profileDetails.get("priceId")));
						profileObject1.put("totalEnergyCost", "");
						profileObject1.put("vendingPricePerUnit", String.valueOf(profileDetails.get("price")));
						profileObject1.put("vendingPriceUnit", String.valueOf(profileDetails.get("priceType")));
						profileObject1.put("minUsed", time);
						profileObject1.put("kwhUsed", totalKwhUsed);
						profileObject1.put("startTimeStamp", Utils.stringToDate(startTransactionTime));
						profileObject1.put("endTimeStamp", Utils.stringToDate(currentTime));
						profileObject1.put("startTimeId", String.valueOf(profileDetails.get("startTime")));
						profileObject1.put("endTimeId", String.valueOf(profileDetails.get("endTime")));
						profileObject1.put("cost", String.valueOf(profileDetails.get("cost")));
						profileObject1.put("dynamicProfileName",String.valueOf(profileDetails.get("profileName")));
						System.out.println("379>>");
						billingDetailsList.add(profileObject1);
					}
				}
				lastSessionEndDate1=profileEndTime;
				noprofile=false;
				lastSessionEndDate=profileEndTime;
			   }
				startTime=lastSessionEndDate1;
				
			}
			}
			if(startTime.compareTo(currentTime) <0) {
				Double time=Utils.timeDifference(startTime,currentTime);
//				if(lastPriceId!=0&&lastPriceId==priceId) {
//				 time=Utils.timeDifference(lastSessionEndDate2,currentTime);
//				 noprofile=true;
//				}
//				time=Utils.round(time);
				double totalKwhUsed=kwhdivide*time;
				if(lastSessionPriceId==0 && noprofile) {
					totalKwhUsed=totalKwhUsed+kwhUsedInSession;
					time=time+sessionMin;
				}
				totalKwhUsed=Utils.round(totalKwhUsed);
				double timeInSec=time*60;
				if(timeInSec>0.010) {
				Map<String,Object> profileObject=new HashMap();
				profileObject.put("dynamicProfileId", "0");
				profileObject.put("dynamicPriceId", "0");
				profileObject.put("vendingPricePerUnit", String.valueOf(portObj.get("vendingPricePerUnit")));
				profileObject.put("vendingPriceUnit", String.valueOf(portObj.get("vendingPriceUnit")));
				profileObject.put("minUsed", time);
				profileObject.put("kwhUsed", totalKwhUsed);
				profileObject.put("startTimeStamp", Utils.stringToDate(startTime));
				profileObject.put("endTimeStamp", Utils.stringToDate(currentTime));
				profileObject.put("startTimeId", "0");
				profileObject.put("endTimeId", "0");
				profileObject.put("cost", "0");
				profileObject.put("dynamicProfileName", "Standard");
				System.out.println("419>>");
				billingDetailsList.add(profileObject);
				}
			}
		}
			if(billingDetailsList.size()==0 && lastSessionDetails!=null && Utils.stringToDate(String.valueOf(lastSessionDetails.get("endTimeStamp"))).compareTo(currentTime)==0) {
				long startTimeId=0;
				long endTImeId=0;
				String timeQuery="select startTime,endTime from dynaminTariffPrice where id='"+String.valueOf(lastSessionDetails.get("dynamicPriceId"))+"'";
				List<Map<String,Object>> timeList=generalDao.getMapData(timeQuery);
				if(timeList.size()>0) {
					startTimeId=Long.parseLong(String.valueOf(timeList.get(0).get("startTime")));
					endTImeId=Long.parseLong(String.valueOf(timeList.get(0).get("endTime")));
				}
				Map<String,Object> profileObject=new HashMap();
				profileObject.put("dynamicProfileId", String.valueOf(lastSessionDetails.get("dynamicProfileId")));
				profileObject.put("dynamicPriceId", String.valueOf(lastSessionDetails.get("dynamicPriceId")));
				profileObject.put("vendingPricePerUnit", String.valueOf(lastSessionDetails.get("portPrice")));
				profileObject.put("vendingPriceUnit", String.valueOf(lastSessionDetails.get("portPriceUnit")));
				profileObject.put("minUsed", String.valueOf(lastSessionDetails.get("sessionDurationInMin")));
				profileObject.put("kwhUsed", kwhUsed+Double.parseDouble(String.valueOf(lastSessionDetails.get("totalKwUsed"))));
				profileObject.put("startTimeStamp", String.valueOf(lastSessionDetails.get("startTimeStamp")));
				profileObject.put("endTimeStamp", Utils.stringToDate(currentTime));
				profileObject.put("startTimeId", startTimeId);
				profileObject.put("endTimeId", endTImeId);
				profileObject.put("cost", String.valueOf(lastSessionDetails.get("cost")));
				profileObject.put("dynamicProfileName",String.valueOf(lastSessionDetails.get("dynamicProfileName")));
				System.out.println("446>>");
				billingDetailsList.add(profileObject);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("333>> billingDetailsList: "+billingDetailsList);
		LOGGER.info("end getBillingDetails");
		return billingDetailsList;
	}
	
	@Override
	public Map<String,Object> getLastSessionDetails(String randomSessionId){
		LOGGER.info("start getLastSessionDetails");
		Map<String,Object> lastSessionDetails=null;
		double kwhUsed=0.0;
		double duration=0.0;
		try {
			String sessionQuery="select dynamicProfileName,id,sessionUniqueId,dynamicProfileId,dynamicPriceId,sessionDurationInMin,totalKwUsed,startTimeStamp,endTimeStamp,portPriceUnit,portPrice,cost from session_Pricing where randomSessionId= '"+randomSessionId+"' order by id desc";
			List<Map<String, Object>> list=generalDao.getMapData(sessionQuery);
			if(list.size()>0) {
				lastSessionDetails=new HashMap();
				lastSessionDetails=list.get(0);
				long priceId=Long.parseLong(String.valueOf(lastSessionDetails.get("dynamicPriceId")));
				if(list.size()>1) {
				priceId=priceId==0? Long.parseLong(String.valueOf(list.get(1).get("dynamicPriceId"))) : priceId;
				}
				for(Map<String, Object> data :list) {
					kwhUsed=Double.parseDouble(String.valueOf(data.get("totalKwUsed")))+kwhUsed;
					duration=Double.parseDouble(String.valueOf(data.get("sessionDurationInMin")))+duration;
				}
				lastSessionDetails.put("kwhUsedInSession", kwhUsed);
				lastSessionDetails.put("duration", duration);
				lastSessionDetails.put("sesssionPriceId", priceId);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("end getLastSessionDetails");
		return lastSessionDetails;
	}
	
	@Override
	public void insertSessionPricing(long sessionId, long dynamicPriceId, long dymanicProfileId,double totalKwUsed,
			double sessionElapsedInMinutes,double dynamicCost, String startTransTimeStamp, String endTimeStamp,
			String randomSessionId,double portPrice,String portPriceUnit,double cost,String dynamicProfileName,Map<String,Object> data) {
		LOGGER.info("start insertSessionPricing");
		SessionPricing sessionPricing=new SessionPricing();
		long id=0;
		String startTime=startTransTimeStamp;
		try {
			String query="select top 1 isnull(sd.id,'0') as id,isnull(sd.dynamicProfileId,'0') as dynamicProfileId,"
					+ " isnull(sd.dynamicPriceId,'0') as dynamicPriceId, endTimeStamp, startTimeStamp from session_Pricing sd where "
					+ " sessionUniqueId='"+sessionId+"' order by id desc";
			List<Map<String, Object>> listData=generalDao.getMapData(query);
			if(listData!=null && listData.size()>0) {
				long profileId=Long.parseLong(String.valueOf(listData.get(0).get("dynamicProfileId")));
				long priceId=Long.parseLong(String.valueOf(listData.get(0).get("dynamicPriceId")));
				if(profileId==dymanicProfileId && priceId==dynamicPriceId) {
					id=Long.parseLong(String.valueOf(listData.get(0).get("id")));
					startTime=String.valueOf(listData.get(0).get("startTimeStamp"));
				}else {
					startTime=String.valueOf(listData.get(0).get("endTimeStamp"));
				}
			}
			String queryForFreeDuration = "from SessionPricing where sessionUniqueId='"+sessionId+"' and dynamicPriceId = "+dynamicPriceId+" and dynamicProfileId = "+dymanicProfileId ;
			sessionPricing = generalDao.findOne(queryForFreeDuration, new SessionPricing());
			if (id == 0) {
				sessionPricing = new SessionPricing();
			} else {
				sessionPricing.setId(id);
			}
			sessionPricing.setDynamicPriceId(dynamicPriceId);
			sessionPricing.setDynamicProfileId(dymanicProfileId);
			sessionPricing.setSessionUniqueId(sessionId);
			sessionPricing.setPortPrice(portPrice);
			sessionPricing.setPortPriceUnit(portPriceUnit);
			sessionPricing.setTotalEnergyCost(dynamicCost);
			sessionPricing.setSessionDurationInMin(sessionElapsedInMinutes);
			sessionPricing.setTotalKwUsed(totalKwUsed);
			sessionPricing.setRandomSessionId(randomSessionId);
			sessionPricing.setStartTimeStamp(Utils.stringToDate(startTime));
			sessionPricing.setEndTimeStamp(Utils.stringToDate(endTimeStamp));
			sessionPricing.setCost(cost);
			sessionPricing.setDynamicProfileName(dynamicProfileName);
			sessionPricing.setTariffStartTime(getTimeById(Long.parseLong(String.valueOf(data.get("tariffStartTimeId"))==null ? "0" : String.valueOf(data.get("tariffStartTimeId")))));
			sessionPricing.setTariffEndTime(getTimeById(Long.parseLong(String.valueOf(data.get("tariffEndTimeId"))==null ?"0":String.valueOf(data.get("tariffEndTimeId")))));
			generalDao.saveOrupdate(sessionPricing);
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("end insertSessionPricing");
	}
	@Override
	public String getTimeById(long Id) {
		LOGGER.info("start getTimeById");
		String time = "00:00";
		try {
			String query = "SELECT CONVERT(varchar(5),  DATEADD(minute,((t.hour*60)+t.min),0), 114) from Time t where id="+Id;
			LOGGER.info("getTimeById : "+query);
			time = generalDao.getRecordBySql(query);
			if(time == null || time.equalsIgnoreCase("null")) {
				time = null;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("start getTimeById");
		return time;
	}
	@Override
	public double getPreviousCost(String randomSessionId , long dynamicPriceId,boolean flag,boolean flag1) {
		LOGGER.info("start getPreviousCost");
		 double energyCost=0.0;
		try {
			String query= "select id,isnull(s.totalEnergyCost,'0') as totalEnergyCost,isnull(s.dynamicPriceId,'0') as dynamicPriceId  from session_Pricing s where randomSessionId ='"+randomSessionId+"' order by s.id desc";
			List<Map<String, Object>> listData=generalDao.getMapData(query);
			if(listData!=null && listData.size()>0) {
				long id=0;
				if(Double.parseDouble(String.valueOf(listData.get(0).get("dynamicPriceId")))==dynamicPriceId && flag1) {
					id=Long.parseLong(String.valueOf(listData.get(0).get("id")));
				}
				for (Map<String, Object> data : listData) {
					if(id!=Long.parseLong(String.valueOf(data.get("id"))) || flag) {
						double cost = Double.parseDouble(String.valueOf((data.get("totalEnergyCost"))));
						energyCost = energyCost + cost;
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("end getPreviousCost");
		return energyCost;
	}

	@Override
	public List<Map<String,Object>> getSessionData(long sessionId){
		LOGGER.info("start getSessionData");
		List<Map<String,Object>> listData=new ArrayList();
		try {
			String query="select s.dynamicProfileName,s.portPrice,s.portPriceUnit,s.totalEnergyCost,s.startTimeStamp,s.endTimeStamp,s.sessionDurationInMin,s.totalKwUsed from session_pricing s where s.sessionUniqueId='"+sessionId+"'";
			listData=generalDao.getMapData(query);
			if(listData.size()>0) {
				listData.sort(Comparator.comparing(o -> Utils.stringToDate(String.valueOf(o.get("startTimeStamp")))));
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("end getSessionData");
		return listData;
	}
}
