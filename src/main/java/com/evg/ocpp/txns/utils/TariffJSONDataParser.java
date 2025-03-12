package com.evg.ocpp.txns.utils;

import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.forms.AdditionalTariffPrices;
import com.evg.ocpp.txns.forms.StandardTariffPrices;
import com.evg.ocpp.txns.forms.TariffData;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TariffJSONDataParser {
	
	public TariffData gettariffData(String jsonData) {
		TariffData tariffData=new TariffData();
		try {
			boolean jsonValid = isJSONValid(jsonData);
			if(jsonValid) {
				JSONParser jsonParser = new JSONParser();
				Object mainObj = jsonParser.parse(jsonData);
				JSONArray jsonArry = (JSONArray) mainObj;
				for (Object o : jsonArry) {
					if (o != null) {
						if (o instanceof JSONObject) {
							Iterator iter = ((JSONObject) o).entrySet().iterator();
							while (iter.hasNext()) {
								Map.Entry me = (Map.Entry) iter.next();
								if(me.getKey().equals("tariffId")) {
									if(me.getValue()==null || me.getValue()=="") {
										tariffData.setTariffId(0);
									}else {
										tariffData.setTariffId(Long.parseLong(String.valueOf(me.getValue())));
									}
								}else if(me.getKey().equals("max_price_id")) {
									if(me.getValue()==null || me.getValue()=="") {
										tariffData.setMaxPriceId(0);
									}else {
										tariffData.setMaxPriceId(Long.parseLong(String.valueOf(me.getValue())));
									}
								}else if(me.getKey().equals("min_price_id")) {
									if(me.getValue()==null || me.getValue()=="") {
										tariffData.setMinPriceId(0);
									}else {
										tariffData.setMinPriceId(Long.parseLong(String.valueOf(me.getValue())));
									}
								}else if(me.getKey().equals("tariffName")) {
									tariffData.setTariffName(String.valueOf(me.getValue()));
								}else if(me.getKey().equals("startTime")) {
									if(me.getValue()==null || me.getValue()=="") {
										tariffData.setStartTime(Utils.getYearUTC());
									}else {
										tariffData.setStartTime(String.valueOf(me.getValue()));
									}
								}else if(me.getKey().equals("endTime")) {
									if(me.getValue()==null || me.getValue()=="") {
										tariffData.setEndTime(Utils.getYearUTC());
									}else {
										tariffData.setEndTime(String.valueOf(me.getValue()));
									}
								}else if(me.getKey().equals("cost_info")) {
									if (me.getValue() instanceof JSONArray) {
										for (Object object : (JSONArray) me.getValue()) {
											if (object instanceof JSONObject) {
												Iterator iter1 = ((JSONObject) object).entrySet().iterator();
												while (iter1.hasNext()) {
													Map.Entry me1 = (Map.Entry) iter1.next();
													if(me1.getKey().equals("standard")) {
														if (me1.getValue() instanceof JSONArray) {
															for (Object object1 : (JSONArray) me1.getValue()) {
																if (object1 instanceof JSONObject) {
																	Iterator iter2 = ((JSONObject) object).entrySet().iterator();
																	while (iter2.hasNext()) {
																		Map.Entry me2 = (Map.Entry) iter2.next();
																		StandardTariffPrices tariffPrices=new StandardTariffPrices();
																		if(me2.getKey().equals("energy")) {
																			if(me2.getValue() instanceof JSONObject) {
																				Iterator iter3 = ((JSONObject) me2.getValue()).entrySet().iterator();
																				while (iter3.hasNext()) {
																					Map.Entry me3 = (Map.Entry) iter3.next();
																					if(me3.getKey().equals("price")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setPrice(0);
																						}else {
																							tariffPrices.setPrice(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("step")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setStep(0);
																						}else {
																							tariffPrices.setStep(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("type")) {
																						tariffPrices.setType(String.valueOf(me3.getValue()));
																					}else if(me3.getKey().equals("tax_excl")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setTax_excl(0);
																						}else {
																							tariffPrices.setTax_excl(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("tax_incl")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setTax_incl(0);
																						}else {
																							tariffPrices.setTax_incl(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}
																				}
																			}
																			tariffData.setEnergyPrice(tariffPrices);
																		}else if(me2.getKey().equals("Time")) {
																			if(me2.getValue() instanceof JSONObject) {
																				Iterator iter3 = ((JSONObject) me2.getValue()).entrySet().iterator();
																				while (iter3.hasNext()) {
																					Map.Entry me3 = (Map.Entry) iter3.next();
																					if(me3.getKey().equals("price")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setPrice(0);
																						}else {
																							tariffPrices.setPrice(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("step")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setStep(0);
																						}else {
																							tariffPrices.setStep(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("type")) {
																						tariffPrices.setType(String.valueOf(me3.getValue()));
																					}else if(me3.getKey().equals("tax_excl")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setTax_excl(0);
																						}else {
																							tariffPrices.setTax_excl(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("tax_incl")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setTax_incl(0);
																						}else {
																							tariffPrices.setTax_incl(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}
																				}
																			}
																			tariffData.setTimePrice(tariffPrices);
																		}else if(me2.getKey().equals("flat")) {
																			if(me2.getValue() instanceof JSONObject) {
																				Iterator iter3 = ((JSONObject) me2.getValue()).entrySet().iterator();
																				while (iter3.hasNext()) {
																					Map.Entry me3 = (Map.Entry) iter3.next();
																					if(me3.getKey().equals("price")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setPrice(0);
																						}else {
																							tariffPrices.setPrice(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("step")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setStep(0);
																						}else {
																							tariffPrices.setStep(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("type")) {
																						tariffPrices.setType(String.valueOf(me3.getValue()));
																					}else if(me3.getKey().equals("tax_excl")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setTax_excl(0);
																						}else {
																							tariffPrices.setTax_excl(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("tax_incl")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setTax_incl(0);
																						}else {
																							tariffPrices.setTax_incl(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}
																				}
																			}
																			tariffData.setFlatPrice(tariffPrices);
																		}else if(me2.getKey().equals("parking")) {
																			if(me2.getValue() instanceof JSONObject) {
																				Iterator iter3 = ((JSONObject) me2.getValue()).entrySet().iterator();
																				while (iter3.hasNext()) {
																					Map.Entry me3 = (Map.Entry) iter3.next();
																					if(me3.getKey().equals("price")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setPrice(0);
																						}else {
																							tariffPrices.setPrice(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("step")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setStep(0);
																						}else {
																							tariffPrices.setStep(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("type")) {
																						tariffPrices.setType(String.valueOf(me3.getValue()));
																					}else if(me3.getKey().equals("tax_excl")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setTax_excl(0);
																						}else {
																							tariffPrices.setTax_excl(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("tax_incl")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setTax_incl(0);
																						}else {
																							tariffPrices.setTax_incl(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}
																				}
																			}
																			tariffData.setParkingPrice(tariffPrices);
																		}
																	}
																}
															}
														}
													}else if(me1.getKey().equals("aditional")) {
														if (me1.getValue() instanceof JSONArray) {
															for (Object object1 : (JSONArray) me1.getValue()) {
																if (object1 instanceof JSONObject) {
																	Iterator iter2 = ((JSONObject) object).entrySet().iterator();
																	while (iter2.hasNext()) {
																		Map.Entry me2 = (Map.Entry) iter2.next();
																		AdditionalTariffPrices tariffPrices=new AdditionalTariffPrices();
																		if(me2.getKey().equals("rateRider")) {
																			if(me2.getValue() instanceof JSONObject) {
																				Iterator iter3 = ((JSONObject) me2.getValue()).entrySet().iterator();
																				while (iter3.hasNext()) {
																					Map.Entry me3 = (Map.Entry) iter3.next();
																					if(me3.getKey().equals("price")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setPrice(0);
																						}else {
																							tariffPrices.setPrice(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("stepSize")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setStepsize(0);
																						}else {
																							tariffPrices.setStepsize(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("type")) {
																						tariffPrices.setType(String.valueOf(me3.getValue()));
																					}else if(me3.getKey().equals("vat")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setVat(0);
																						}else {
																							tariffPrices.setVat(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("restrictionType")) {
																						tariffPrices.setRestrictionType(String.valueOf(me3.getValue()));
																					}
																				}
																			}
																		 tariffData.setRateRider(tariffPrices);
																		}else if(me2.getKey().equals("tax")) {
																			if(me2.getValue() instanceof JSONObject) {
																				Iterator iter3 = ((JSONObject) me2.getValue()).entrySet().iterator();
																				while (iter3.hasNext()) {
																					Map.Entry me3 = (Map.Entry) iter3.next();
																					if(me3.getKey().equals("price")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setPrice(0);
																						}else {
																							tariffPrices.setPrice(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("stepSize")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setStepsize(0);
																						}else {
																							tariffPrices.setStepsize(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("type")) {
																						tariffPrices.setType(String.valueOf(me3.getValue()));
																					}else if(me3.getKey().equals("vat")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setVat(0);
																						}else {
																							tariffPrices.setVat(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("restrictionType")) {
																						tariffPrices.setRestrictionType(String.valueOf(me3.getValue()));
																					}
																				}
																			}
																		 tariffData.setTAX(tariffPrices);
																		}else if(me2.getKey().equals("idleCharge")) {
																			if(me2.getValue() instanceof JSONObject) {
																				Iterator iter3 = ((JSONObject) me2.getValue()).entrySet().iterator();
																				while (iter3.hasNext()) {
																					Map.Entry me3 = (Map.Entry) iter3.next();
																					if(me3.getKey().equals("price")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setPrice(0);
																						}else {
																							tariffPrices.setPrice(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("stepSize")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setStepsize(0);
																						}else {
																							tariffPrices.setStepsize(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("type")) {
																						tariffPrices.setType(String.valueOf(me3.getValue()));
																					}else if(me3.getKey().equals("vat")) {
																						if(me3.getValue()==null || me3.getValue()=="") {
																							tariffPrices.setVat(0);
																						}else {
																							tariffPrices.setVat(Double.parseDouble(String.valueOf(me3.getValue())));
																						}
																					}else if(me3.getKey().equals("restrictionType")) {
																						tariffPrices.setRestrictionType(String.valueOf(me3.getValue()));
																					}
																				}
																			}
																		 tariffData.setIdleCharge(tariffPrices);
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return tariffData;
	}
	public static boolean isJSONValid(String jsonInString ) {
	    try {
	       if(jsonInString != null && !jsonInString.equalsIgnoreCase("null") && !jsonInString.equalsIgnoreCase("")) {
	    	   final ObjectMapper mapper = new ObjectMapper();
		       mapper.readTree(jsonInString);
		       return true;
	       }else {
	    	   return false;
	       }
	    } catch (Exception e) {
	       return false;
	    }
	  }
}
