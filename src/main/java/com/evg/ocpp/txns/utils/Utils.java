package com.evg.ocpp.txns.utils;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.evg.ocpp.txns.dao.GeneralDao;
import com.evg.ocpp.txns.forms.OCPPForm;

@Service
public class Utils {
	private final static Logger logger = LoggerFactory.getLogger(Utils.class);

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private GeneralDao<?, ?> generalDao;
	
	@Value("${ocpp.url}")
	private String ocppURL;
	
	public static String getTimeFormate(Double timecon){
		String afterConverTion=null;
		try {
			int minsinsec = (int) (timecon*60);
			int p1 = minsinsec % 60;
			int p2 = minsinsec / 60;
			int p3 = p2 % 60;
			p2 = p2 / 60;
			String str1 = String.format("%02d", p1);
			String str2 = String.format("%02d", p2);
			String str3 = String.format("%02d", p3);
			afterConverTion=str2 +":"+str3+":"+str1;
		}catch (Exception e) {
			logger.error("",e);
		}
		return afterConverTion;
	}
	
	
	public static String getOneYearUTC()  {
		String time = "";
		try {
			LocalDateTime actualDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
			actualDateTime=actualDateTime.plusYears(1);
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
			//formatter.withZone(ZoneId.of("UTC"));
			time = actualDateTime.format(formatter);
		} catch (Exception e) {
			logger.error("",e);
		}
	   return time;
	}
	public static String getYearUTC()  {
		String time = "";
		try {
			LocalDateTime actualDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
			time = actualDateTime.format(formatter);
		} catch (Exception e) {
			logger.error("",e);
		}
	   return time;
	}
	
	public static String getUTC()  {
		String finalUtctime = null;
		try {
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			String utctime = dateFormat.format(date);
			finalUtctime = utctime + "Z";
		}catch (Exception e) {
			logger.error("",e);
		}
		return finalUtctime;
	}
	
	public SimpleClientHttpRequestFactory httpConfig(int connTimeOut,int readTimeOut) {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		try {
			factory.setConnectTimeout(connTimeOut);
			factory.setReadTimeout(readTimeOut);
		}catch (Exception e) {
			logger.error("",e);
		}
		return factory;
	}
	
	public static Date getUtcDateFormate(Date date)  {
		Date parse = null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//2020-09-02T07:41:28Z  yyyy-MM-dd'T'HH:mm:ssZ
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			String utctime = dateFormat.format(date);
			parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(utctime);
		}catch (Exception e) {
			logger.error("",e);
		}
	   return parse;
	}

	public static Date getUTCDate()  {
		Date parse = null;
		try {
			Date date = new Date();
			SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			String sysDate = DateFormat.format(date);
			parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sysDate);
		}catch (Exception e) {
			logger.error("",e);
		}
		return parse;
	}
	
	public static String getUTCDateWithTimeZone()  {
		String sysDate = null;
		try {
			Date date = new Date();
			SimpleDateFormat DateFormat = new SimpleDateFormat("MMMM d, yyyy, HH:mm:ss z");
			DateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			sysDate = DateFormat.format(date);
		}catch (Exception e) {
			logger.error("",e);
		}
		return sysDate;
	}
	
	public static String getUTCDateString()  {
		String sysDate = null;
		try {
			Date date = new Date();
			SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			sysDate = DateFormat.format(date);
		}catch (Exception e) {
			logger.error("",e);
		}
		return sysDate;
	}
	public static String getDate(Date date) {
		String sysDate = null;
		try {
			SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			sysDate = DateFormat.format(date);
		}catch (Exception e) {
			logger.error("",e);
		}
		return sysDate;
	}
	
	public static String getUtcTime() {
		String sysDate = null;
		try {
			Date date = new Date();
			SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			sysDate = DateFormat.format(date);
		}catch (Exception e) {
			logger.error("",e);
		}
		return sysDate;
	}
	
	public static Date getDateFormate(Date date)  {	
		Date parse = null;
		try {
			parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
		}catch (Exception e) {
			logger.error("",e);
		}
		return parse;
	}
	public static Date getDateFrmt(Date date)  {
		Date parse = null;
		try {
			parse = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(date));
		}catch (Exception e) {
			logger.error("",e);
		}
		return parse;
	}
	public static String getTimeFrmt(Date date)  {
		return new SimpleDateFormat("HH:mm:ss").format(date);
	}
	
	public static Date browserDateFormateToUtcDateFormat(String date)  {
		Date parse = null;
		try {
			Date formatteris = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss").parse(date.replace("GMT", ""));
			
			String parsingdDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(formatteris);
			
			parse = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(parsingdDate);
		}catch (Exception e) {
			logger.error("",e);
		}
		return parse;
	}
	
	public static String browserDateFormate(String date)  {
		String parsingdDate = null;
		try {
			Date formatteris = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss").parse(date.replace("GMT", ""));
			
			parsingdDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(formatteris);
		}catch (Exception e) {
			logger.error("",e);
		}
		return parsingdDate;
	}
	
	public static String getIntRandomNumber() {
		Random rand1 = new Random();
		String randomValues = String.valueOf(Math.abs(rand1.nextInt()));
		return randomValues;
	}
	
	public static boolean getOfflineFlag(Date startTransTime)  {
		try {
			Date currentUtcTime=getUTCDate();
			//Date currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(currentUtcTime);
			//Date startTransactionTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTransTime);
			long minutes = TimeUnit.MILLISECONDS.toMinutes(currentUtcTime.getTime()-startTransTime.getTime());
			
			if(minutes>10)
				return true;
		} catch (Exception e) {
			logger.error("",e);
		}
		
		return false;
	}
	
	public static Map<String,BigDecimal> getTimeDifferenceInMiliSec(Date startTimeStamp,Date endTimeStamp)  {
		
		Map<String, BigDecimal> timeConvertMap=new HashMap<String, BigDecimal>();
		
		try {
			//Time difference in Miliseconds
			Long timeDifference=endTimeStamp.getTime()-startTimeStamp.getTime();
			
			//converting to miliseconds to seconds
//			double seconds = Math.abs(TimeUnit.MILLISECONDS.toSeconds(timeDifference));
//			//converting to miliseconds to Hours
//			double hours=TimeUnit.MILLISECONDS.toHours(timeDifference);
//			double totalDurationHours= (seconds/3600);
//			double totalDurationInminutes=(seconds/60);
			
			BigDecimal seconds = new BigDecimal(String.valueOf(Math.abs(TimeUnit.MILLISECONDS.toSeconds(timeDifference))));
			//converting to miliseconds to Hours
			BigDecimal hours=new BigDecimal(String.valueOf(TimeUnit.MILLISECONDS.toHours(timeDifference)));
			BigDecimal totalDurationHours=seconds.divide(new BigDecimal("3600"),15,RoundingMode.UP);
			BigDecimal totalDurationInminutes=seconds.divide(new BigDecimal("60"),15,RoundingMode.UP);
			
			timeConvertMap.put("Seconds", seconds);
			timeConvertMap.put("Minutes", totalDurationInminutes);
			timeConvertMap.put("Hours", hours);
			timeConvertMap.put("durationInHours", totalDurationHours);
		}catch (Exception e) {
			logger.error("",e);
		}
		return timeConvertMap;
	}
	
	
	public Map<String, Double> getTimeInHours_duration(String startTime,String EndTime) {
		Map<String, Double> timeConvertMap=new HashMap<String, Double>();
		try {
			Date startTimeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(startTime);
			Date endTimeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(EndTime);
			
			double MinutesForSessionElapsed=0.0;
			
			long diff = endTimeStamp.getTime()-startTimeStamp.getTime();
			
			long diffSeconds = diff / 1000 % 60;
			//int seconds = (int) diffSeconds;
			long diffMinutes = diff / (60 * 1000) % 60;//(diff/(6000)%60)
			
			long diffHours = diff / (60 * 60 * 1000);
			
				double Mins = (double) (diffHours + ((double) diffMinutes / 60) + ((double) diffSeconds / (60 * 60)));
				double MinutesForSessionElapsedForMaxSession = (double) (diffHours * 60 + ((double) diffMinutes) + ((double) diffSeconds / (60)));
			if (diffSeconds >= 30.0) {
				Mins = (double) (diffHours + ((double) diffMinutes / 60) + ((double) diffSeconds / (60 * 60)));
				MinutesForSessionElapsed = (double) (diffHours * 60 + ((double) diffMinutes) + ((double) diffSeconds / (60)));
			} else {
				Mins = (double) (diffHours + ((double) diffMinutes / 60));
				MinutesForSessionElapsed = (double) (diffHours * 60+ ((double) diffMinutes));
			}
			
			timeConvertMap.put("durationInHours", Mins);
			timeConvertMap.put("MinutesForSessionElapsed", MinutesForSessionElapsed);
			timeConvertMap.put("MinutesForSessionElapsedForMaxSession", MinutesForSessionElapsedForMaxSession);
		}catch (Exception e) {
			logger.error("",e);
		}
		return timeConvertMap;
	}
	
	public BigDecimal decimalwithtwodecimals(BigDecimal final_Cost) {
				
		try {
			String finalcostString = String.valueOf(final_Cost);
			String finalcostbeforedecimals = finalcostString.split("\\.")[0];			
			String finalcostafterdecimals = finalcostString.substring(finalcostString.indexOf(".")).substring(1, 3);
			String finalcoststringcombined = finalcostbeforedecimals + "." + finalcostafterdecimals;			
			final_Cost = new BigDecimal(finalcoststringcombined);
			//final_Cost = Double.valueOf(new DecimalFormat("##.##").format(final_Cost));
			
		}catch(Exception e) {
			//logger.error("",e);
		}
		return final_Cost;
	}
	
	public BigDecimal decimalwithFourdecimals(BigDecimal final_Cost) {
		try {
			String finalcostString = String.valueOf(final_Cost);
			String finalcostbeforedecimals = finalcostString.split("\\.")[0];			
			String finalcostafterdecimals = finalcostString.substring(finalcostString.indexOf(".")).substring(1, 5);
			String finalcoststringcombined = finalcostbeforedecimals + "." + finalcostafterdecimals;			
			final_Cost = new BigDecimal(finalcoststringcombined);
			//final_Cost = Double.valueOf(new DecimalFormat("##.##").format(final_Cost));
			
		}catch(Exception e) {
			//logger.error("",e);
		}
		return final_Cost;
	}
	public String decimalwithtwoZeros(BigDecimal final_Cost) {
		
		String finalcostString = String.valueOf(final_Cost);
		try {
			if(!finalcostString.contains(".")) {
				finalcostString=finalcostString+".0";
			}
			if(finalcostString.substring(finalcostString.indexOf(".")).length()==2) {
				finalcostString=finalcostString+"0";
			}
		}catch(Exception e) {
			//logger.error("",e);
		}
		
		return finalcostString;		
	}
	public String decimalwithfourZeros(BigDecimal final_Cost) {
		
		String finalcostString = String.valueOf(final_Cost);
		try {
			if(!finalcostString.contains(".")) {
				finalcostString=finalcostString+".0";
			}
			if(finalcostString.substring(finalcostString.indexOf(".")).length()==2) {
				finalcostString=finalcostString+"000";
			}
		}catch(Exception e) {
			//logger.error("",e);
		}
		
		return finalcostString;		
	}
	
	public static boolean isPathValid(String path) {
		boolean flag = false;
        try {
        	File file = new File(path);
			if(file.exists()){
				flag = true;
			}
        } catch (Exception ex) {
        	flag=false;
        }
        return flag;
    }
	public String strConverter(String val) {
		try {
			val = val.replaceAll(" ", "");
			val = (val == null ? "0" : val.equalsIgnoreCase("null") ? "0" : val.equalsIgnoreCase("") ? "0" : val);
		}catch(Exception e) {
			logger.error("",e);
			 val = "0";
		}
		return val;
	}
	
	public String strValid(String str) {
		try {
			str =  str==null ? "0" 
				: str.equalsIgnoreCase("false") ? "0" 
					: str.equalsIgnoreCase("true") ? "0" 
							:  str.equalsIgnoreCase("null") ? "0" 
									: str.equalsIgnoreCase("") ? "0" : str;
		} catch (Exception e) {
			logger.error("",e);
		}
		return str;
	}
	public String strValidBit(String str) {
		try {
			str =  str==null ? "false" 
				: str.equalsIgnoreCase("false") ? "false" 
					: str.equalsIgnoreCase("true") ? "true" 
							:  str.equalsIgnoreCase("null") ? "false" 
									: str.equalsIgnoreCase("") ? "false"
											: str.equalsIgnoreCase("0") ? "false"
													: str.equalsIgnoreCase("1") ? "true": str;
		} catch (Exception e) {
			logger.error("",e);
		}
		return str;
	}
	public Date stringToDate(String val) {
		Date date = null;
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = dateFormat.parse(val);
		}catch(Exception e) {
			logger.error("",e);
		}
		return date;
	}
	public String stringToDate(Date date) {
		String val = null;
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			val = dateFormat.format(date);
		}catch(Exception e) {
			logger.error("",e);
		}
		return val;
	}
	
	public static String addHour(int  n)  {
		String time = "";
		try {
			LocalDateTime actualDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
			actualDateTime=actualDateTime.plusHours(n);
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
			//formatter.withZone(ZoneId.of("UTC"));
			time = actualDateTime.format(formatter);
		} catch (Exception e) {
			logger.error("",e);
		}
	   return time;
	}
	public Date addSec(int n,Date date)  { 
		Date time = null;
		try {
			//LocalDateTime actualDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
			//actualDateTime=actualDateTime.plusSeconds(n);
			
			//DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			//formatter.withZone(ZoneId.of("UTC"));
			//time = stringToDate(actualDateTime.format(formatter));
			
			
		    Calendar gcal = new GregorianCalendar();
		    gcal.setTime(date);
		    gcal.add(Calendar.SECOND, n);
		    time = gcal.getTime();
		} catch (Exception e) {
			logger.error("",e);
		}
	   return time;
	}
	public Date addSec(int n)  { 
		Date time = null;
		try {
			LocalDateTime actualDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
			actualDateTime=actualDateTime.plusSeconds(n);
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			//formatter.withZone(ZoneId.of("UTC"));
			time = stringToDate(actualDateTime.format(formatter));
		} catch (Exception e) {
			logger.error("",e);
		}
	   return time;
	}
	public static String addMin(int  n)  { 
		String time = "";
		try {
			LocalDateTime actualDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
			actualDateTime=actualDateTime.plusMinutes(n);
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
			//formatter.withZone(ZoneId.of("UTC"));
			time = actualDateTime.format(formatter);
		} catch (Exception e) {
			logger.error("",e);
		}
	   return time;
	}
	public String getIntRandomNumber(int digit) {
		String randomValues = "";
		try {
			Random rand1 = new Random();
			randomValues = String.valueOf(Math.abs(rand1.nextInt(digit)));
		} catch (Exception e) {
			logger.error("",e);
		}
		return randomValues;
	}
	public String getRandomNumber(String type) {
		StringBuilder val = new StringBuilder();
		try {
			if(type.equalsIgnoreCase("transactionId")) {
				val.append(System.currentTimeMillis()).append("01");//TransactionId
			}else if(type.equalsIgnoreCase("txnSessionId")) {
				val.append(System.currentTimeMillis()).append("02");//SessionId
			}else if(type.equalsIgnoreCase("RSTP")) {
				val.append(System.currentTimeMillis()).append("03");//RemoteStopTransaction RequestId
			}else if(type.equalsIgnoreCase("UC")) {
				val.append(System.currentTimeMillis()).append("04");//UnlockConnector RequestId
			}else {
				val.append(System.currentTimeMillis());
			}
		}catch(Exception e) {
			logger.error("",e);
		}
		return String.valueOf(val);
	}
	
	public static String getUTCByOne(int days) {
		String finalUtctime = null;
		try {
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			String utctime = dateFormat.format(date);
			finalUtctime = utctime + "Z";
			Calendar c = Calendar.getInstance();
			c.setTime(dateFormat.parse(finalUtctime));
			c.add(Calendar.DATE, days); // number of days to add
			String utcTime = dateFormat.format(c.getTime());
			finalUtctime = utcTime + "Z";
		} catch (Exception e) {
			logger.error("",e);
		}
		return finalUtctime;
	}
	
	public String getAuthRefNum(String idTag,long stnId,long portId) {
		String recordBySql="";
		try {
			String str = "select portalStation from ocpp_remoteStartTransaction where stationId = '"+stnId+"' and connectorId = '"+portId+"' and idTag = '"+idTag+"' order by id desc;";
			recordBySql = generalDao.getRecordBySql(str);
		} catch (Exception e) {
			logger.error("",e);
		}
		return recordBySql;
	}
	
	public static Date getPDTDate() {
		 Date date = null;
		try {
			Timestamp ts=new Timestamp(System.currentTimeMillis()-25200000);  
	        date=ts; 
	        SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat.setTimeZone(TimeZone.getTimeZone("PDT"));
			String sysDate = DateFormat.format(date);
			date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sysDate);
		} catch (Exception e) {
			logger.error("",e);
		}
		return date;
	}
	
	public double round(double value) {
		try {
	        DecimalFormat df_obj = new DecimalFormat("#.####");
	        String format = df_obj.format(value);
	        value = Double.valueOf(format);
		}catch (Exception e) {
			//logger.error("",e);
		}
		return value;
	}
	public void apicallingPOST(String url, HttpEntity<Map<String, Object>> requestBody) {
		try {
			Thread newThread = new Thread() {
				public void run() {
					try {
						restTemplate.setRequestFactory(httpConfig(500, 500));
						ResponseEntity<String> response = restTemplate.postForEntity(url, requestBody, String.class);
					}catch (Exception e) {
						logger.error("",e);
					}
				}
			};
			newThread.start();
		} catch (Exception e) {
			logger.error("",e);
		}
	}
	
	public String getuuidRandomId(long stationId) {
		String number=String.valueOf(UUID.randomUUID());
		try {
			number=String.valueOf(stationId)+number.replace("-", "");
		}catch (Exception e) {
			logger.error("",e);
		}
		return number;
	}
	
	public String getStationRandomNumber(long stationId) {
		String number=String.valueOf(UUID.randomUUID()).replace("-", "");
		try {
			number=String.valueOf(stationId)+System.currentTimeMillis();
		}catch (Exception e) {
			logger.error("",e);
		}
		return number;
	}	
	public String uuid() {
		String id = getRandomNumber("");
		try {
			id = String.valueOf(UUID.randomUUID());
		}catch (Exception e) {
			logger.error("",e);
		}
		return id;
	}
	public void ocppCalling(String message,String clientId) {
		try {
			String url = ocppURL + "amp/request";
			Map<String, Object> requestBody = new HashMap();
			requestBody.put("clientId", clientId);
			requestBody.put("message", message);
			
			restTemplate.setRequestFactory(httpConfig(500, 500));
			restTemplate.postForEntity(url, requestBody, String.class);
		}catch(Exception e) {
			logger.error("",e);
		}
	}
	public void ocppStopCalling(OCPPForm form) {
		try {
			Thread th = new Thread() {
				public void run() {
					String url = ocppURL + "ocpp/request";
					Map<String, Object> requestBody = new HashMap();
					requestBody.put("requestType", form.getRequestType());
					requestBody.put("stationId", form.getStationId());
					requestBody.put("connectorId", form.getConnectorId());
					
					restTemplate.setRequestFactory(httpConfig(500, 500));
					restTemplate.postForEntity(url, requestBody, String.class);
				}
			};
			th.start();
		}catch(Exception e) {
			logger.error("",e);
		}
	}
	public double timeDifference(Date date1,Date date2) {
		double  difference_In_Minutes=0.0;
		try {
		double difference_In_Time=date2.getTime()-date1.getTime();
		difference_In_Minutes= (difference_In_Time/ (1000 * 60));
//		difference_In_Minutes=round(difference_In_Minutes);
		}catch (Exception e) {
			logger.error("",e);
		}
		return difference_In_Minutes;
	}
	public BigDecimal timeDifferenceBigDc(Date date1,Date date2) {
		BigDecimal  difference_In_Minutes=new BigDecimal("0");
		try {
			BigDecimal difference_In_Time=new BigDecimal(String.valueOf(date2.getTime()-date1.getTime()));
		    difference_In_Minutes= (difference_In_Time.divide((new BigDecimal("1000").multiply(new BigDecimal("60"))), 9, RoundingMode.HALF_UP));
		}catch (Exception e) {
			logger.error("",e);
		}
		return difference_In_Minutes;
	}
	
	public Date addDateSec(Date date,int number) {
		Date newDate=date;
		try {
			newDate = DateUtils.addSeconds(date, number);
		}catch (Exception e) {
			logger.error("",e);
		}
		return newDate;
	}
	public Date addDate(Date date,int number) {
		Date newDate=date;
		try {
			newDate = DateUtils.addHours(date, number);
		}catch (Exception e) {
			logger.error("",e);
		}
		return newDate;
	}
	public BigDecimal roundToTwoDecimalPlaces(BigDecimal number) {
        return number.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
	public String userTimeZone(String dateandTime,long desiredTimeZone) {
		String time="";
		try {
			if(dateandTime == null) {
				dateandTime = getUTCDateString();
			}
			String str = "select convert(varchar,DATEADD(HOUR, CAST(SUBSTRING(replace(z.utc_code,'GMT',''), 1,3) as int),DATEADD(MINUTE,CAST(SUBSTRING(replace(z.utc_code,'GMT',''),5,2) "
						+ " as int),'"+dateandTime+"')), 20) as usertime,isnull((select z.zone_name from zone z where z.zone_id = "+desiredTimeZone+"),'UTC') as userTimeZone from zone z where z.zone_id = "+desiredTimeZone+"";
			List<Map<String, Object>> mapData = generalDao.getMapData(str);
			if(mapData.size() > 0) {
				time = String.valueOf(mapData.get(0).get("usertime"))+" "+String.valueOf(mapData.get(0).get("userTimeZone"));
			}
		}catch (Exception e) {
			logger.error("",e);
			return dateandTime +" UTC";
		}
		return time;
	}
	public static String formatDuration(double sessionDurataion) {
		DecimalFormat formatter = new DecimalFormat("00");

        // Format the duration
		long minutes = (long) sessionDurataion;
        long seconds = Math.round((sessionDurataion - minutes) * 60.0);
        String formattedMinutes = formatter.format(minutes);
        String formattedSeconds = formatter.format(seconds);

        return formattedMinutes + "m " + formattedSeconds + "s";
    }
	public double round2decimals(double value) {
		try {
	        DecimalFormat df_obj = new DecimalFormat("#.##");
	        String format = df_obj.format(value);
	        value = Double.valueOf(format);
		}catch (Exception e) {
			//logger.error("",e);
		}
		return value;
	}
}
