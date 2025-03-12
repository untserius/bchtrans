<!DOCTYPE html>
<html>
   <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <meta http-equiv="X-UA-Compatible" content="IE=edge">
      <title>Charging activity</title>
   </head>
   <body bgcolor="#ebebeb" style="margin: 0;background-color: #ebebeb;padding: 0;font-family: Arial, Helvetica, sans-serif;font-size: 13px;line-height: 1.5;color: #3e3935;">
      <table width="100%" cellspacing="0" cellpadding="0" border="0" style="border-collapse: collapse;">
         <tr>
            <td align="center" bgcolor="#ebebeb" style="border-width: 0;font-family: Arial, Helvetica, sans-serif;">
               <table class="container" cellspacing="0" cellpadding="0" border="0" style="border-collapse: collapse;max-width: 500px;width: 100%;">
                  <tr>
                     <td class="superheader" bgcolor="#ffffff" align="left" style="border-width: 0;font-family: Arial, Helvetica, sans-serif;background-color: #ffffff;padding: 25px 20px;text-align: left;">
                        <table cellspacing="0" cellpadding="0" border="0" style="border-collapse: collapse;max-width: 440px;width: 100%;">
                           <tr>
                              <td class="logo" rowspan="2" valign="middle" align="left" style="border-width: 0;font-family: Arial, Helvetica, sans-serif;text-align: left;padding: 0 20px 0 0;"><a href="https://www.bchydro.com/?utm_source=transactional&utm_medium=email&utm_content=header" style="text-decoration: underline;color: #0074a4;"><img src="https://www.bchydro.com/content/dam/BCHydro/email/images/global/bch-logo.jpg" width="150" height="45" border="0" alt="BC Hydro: Power Smart" style="display: block;border: none;"></a></td>
                           </tr>
                        </table>
                     </td>
                  </tr>
                  <tr>
                     <td class="header" bgcolor="#004F6C" align="left" style="border-width: 0;font-family: Arial, Helvetica, sans-serif;background-color: #004F6C;text-align: left;">
                        <table cellspacing="0" cellpadding="0" border="0" style="border-collapse: collapse;width: 100%;">
                           <tr>
                              <td class="title" align="left" style="border-width: 0;font-family: Arial, Helvetica, sans-serif;text-align: left;font-size: 42px;font-weight: bold;color: #ffffff;">
                                 <img src="http://barrie.jumphost.ca/bchydro/evgateway/img/charging-activity-title.png" alt="Charging activity" style="display: block;border: none;width:100%;height:auto;">
                              </td>
                           </tr>
                        </table>
                     </td>
                  </tr>
                  <tr>
                     <td class="content" bgcolor="#ffffff" align="left" style="border-width: 0;font-family: Arial, Helvetica, sans-serif;background-color: #ffffff;padding: 40px 20px 20px 20px;text-align: left;">
                        <table cellspacing="0" cellpadding="0" border="0" style="border-collapse: collapse;max-width: 440px;width: 100%;">
                           <tr>
                              <td align="left" style="border-width: 0;font-family: Arial, Helvetica, sans-serif;text-align: left;">
                                 <p style="margin: 0 0 15px 0;font-size: 16px;line-height: 1.5;font-weight: 700;">Hi ${accName},</p>
                                 <p style="margin: 0 0 15px 0;">Thank you for charging with BC Hydro. Here's a summary of your session:</p>
                                 <p style="margin: 0 0 15px 0;">${currDate}<br/>
                                    ${stnAddress}
                                 </p>
                                 <p style="margin: 0 0 15px 0;"><strong>Charger ID:</strong> ${stnRefNum}<br/>
                                    <strong>Session ID:</strong> ${sessionId}
                                 </p>
                                 <table cellspacing="0" cellpadding="0" border="0" style="width:100%;background:#05A1C6;">
                                    <tbody>
                                       <tr>
                                          <td style="font-family: Arial, Helvetica, sans-serif;background: #05A1C6;padding:10px;box-sizing:border-box;">
                                             <h2 style="margin:0; color: white;font-size: 16px;font-weight: 700;">Session Details</h2>
                                          </td>
                                       </tr>
                                    </tbody>
                                 </table>
                                 <table cellspacing="0" cellpadding="0" border="0" bgcolor="#f5f3f2" style="background:#f5f3f2;width:100%;">
                                    <tbody>
                                       <tr>
                                          <td bgcolor="#f5f3f2" style="font-family: Arial, Helvetica, sans-serif;width: 160px;box-sizing: border-box;padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">Account identification:</td>
                                          <td bgcolor="#f5f3f2" style="font-family: Arial, Helvetica, sans-serif;font-weight: 700; padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${digitalId}</td>
                                       </tr>
                                       <tr>
                                          <td bgcolor="#f5f3f2" style="font-family: Arial, Helvetica, sans-serif;width: 160px;box-sizing: border-box;padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">Site name:</td>
                                          <td bgcolor="#f5f3f2" style="font-family: Arial, Helvetica, sans-serif;font-weight: 700; padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${site_name}</td>
                                       </tr>
                                       <tr>
                                          <td bgcolor="#f5f3f2" style="font-family: Arial, Helvetica, sans-serif;width: 160px;box-sizing: border-box;padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">Charger max power:</td>
                                          <td bgcolor="#f5f3f2" style="font-family: Arial, Helvetica, sans-serif;font-weight: 700; padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${capacity_ChargerType}</td>
                                       </tr>
                                       <tr>
                                          <td bgcolor="#f5f3f2" style="font-family: Arial, Helvetica, sans-serif;width: 160px;box-sizing: border-box;padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">Session start date:</td>
                                          <td bgcolor="#f5f3f2" style="font-family: Arial, Helvetica, sans-serif;font-weight: 700; padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${start_time}</td>
                                       </tr>
                                       <tr>
                                          <td bgcolor="#f5f3f2" style="font-family: Arial, Helvetica, sans-serif;width: 160px;box-sizing: border-box;padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">Session end date:</td>
                                          <td bgcolor="#f5f3f2" style="font-family: Arial, Helvetica, sans-serif;font-weight: 700; padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${end_time}</td>
                                       </tr>
                                       <tr>
                                          <td bgcolor="#f5f3f2" style="font-family: Arial, Helvetica, sans-serif;width: 160px;box-sizing: border-box;padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">Rate:</td>
                                          <td bgcolor="#f5f3f2" style="font-family: Arial, Helvetica, sans-serif;font-weight: 700; padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${prices}</td>
                                       </tr>
                                       <tr>
                                          <td bgcolor="#f5f3f2" style="font-family: Arial, Helvetica, sans-serif;width: 160px;box-sizing: border-box;padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">Charging time duration:</td>
                                          <td bgcolor="#f5f3f2" style="font-family: Arial, Helvetica, sans-serif;font-weight: 700; padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${sessionDuration}</td>
                                       </tr>
                                    </tbody>
                                 </table>
                                 <p style="margin: 0 0 15px 0;">&nbsp;</p>
                                 <table cellspacing="0" cellpadding="0" border="0" style="width:100%;background:#05A1C6;">
                                    <tbody>
                                       <tr>
                                          <td style="font-family: Arial, Helvetica, sans-serif;background: #05A1C6;padding:10px;box-sizing:border-box;">
                                             <h2 style="margin:0; color: white;font-size: 16px;font-weight: 700;">Energy delivered</h2>
                                          </td>
                                       </tr>
                                    </tbody>
                                 </table>
                                 <table cellspacing="0" cellpadding="0" border="0" style="width:100%;">
                                    <tbody>
                                       <tr>
                                          <td style="font-family: Arial, Helvetica, sans-serif;width: 160px;box-sizing: border-box;padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">Starting battery %:</td>
                                          <td style="font-family: Arial, Helvetica, sans-serif;font-weight: 700; padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${startSoc}</td>
                                       </tr>
                                       <tr>
                                          <td style="font-family: Arial, Helvetica, sans-serif;width: 160px;box-sizing: border-box;padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">Final battery %:</td>
                                          <td style="font-family: Arial, Helvetica, sans-serif;font-weight: 700; padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${endSoc}</td>
                                       </tr>
									   <tr>
                                          <td style="font-family: Arial, Helvetica, sans-serif;width: 160px;box-sizing: border-box;padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">Total energy delivered:</td>
                                          <td style="font-family: Arial, Helvetica, sans-serif;font-weight: 700; padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${totalkWh} kWh</td>
                                       </tr>
                                    </tbody>
                                 </table>
								 
								 <p style="margin: 0 0 15px 0;">&nbsp;</p>
                                 <table cellspacing="0" cellpadding="0" border="0" style="width:100%;background:#05A1C6;">
                                    <tbody>
                                       <tr>
                                          <td style="font-family: Arial, Helvetica, sans-serif;background: #05A1C6;padding:10px;box-sizing:border-box;">
                                             <h2 style="margin:0; color: white;font-size: 16px;font-weight: 700;">Idle Charging Details</h2>
                                          </td>
                                       </tr>
                                    </tbody>
                                 </table>
                                 <table cellspacing="0" cellpadding="0" border="0" style="width:100%;">
                                    <tbody>
                                       <#if idleCharging?has_content>
                                          <#list idleCharging as item>
                                          <tr>
                                          <td style="font-family: Arial, Helvetica, sans-serif;width: 160px;box-sizing: border-box;padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${item.name}:</td>
                                          <td style="font-family: Arial, Helvetica, sans-serif;font-weight: 700; padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${item.value}</td>
                                          </tr>                                          
                                          </#list>
                                       </#if>
                                       
                                    </tbody>
                                 </table>
								 
                                 <p style="margin: 0 0 15px 0;">&nbsp;</p>
                                 <table cellspacing="0" cellpadding="0" border="0" style="width:100%;background:#05A1C6;">
                                    <tbody>
                                       <tr>
                                          <td style="font-family: Arial, Helvetica, sans-serif;background: #05A1C6;padding:10px;box-sizing:border-box;">
                                             <h2 style="margin:0; color: white;font-size: 16px;font-weight: 700;">Cost Details (CAD)</h2>
                                          </td>
                                       </tr>
                                    </tbody>
                                 </table>
                                 <table cellspacing="0" cellpadding="0" border="0" style="width:100%;">
                                    <tbody>
									    <tr>
                                          <td style="font-family: Arial, Helvetica, sans-serif;width: 160px;box-sizing: border-box;padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">Charging cost:</td>
                                          <td style="font-family: Arial, Helvetica, sans-serif;font-weight: 700; padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${charging_cost}</td>
                                        </tr>
                                       <#if taxes?has_content>
                                          <#list taxes as item>
                                          <tr>
                                          <td style="font-family: Arial, Helvetica, sans-serif;width: 160px;box-sizing: border-box;padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${item.tax_name_per}:</td>
                                          <td style="font-family: Arial, Helvetica, sans-serif;font-weight: 700; padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${item.taxAmount}</td>
                                          </tr>
                                          </#list>
                                       </#if>
                                       <tr>
                                          <td style="font-family: Arial, Helvetica, sans-serif;width: 160px;box-sizing: border-box;padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">Total cost:</td>
                                          <td style="font-family: Arial, Helvetica, sans-serif;font-weight: 700; padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${finalAmount}</td>
                                       </tr>
                                    </tbody>
                                 </table>
								 <#if user_type=="RegisteredUser">
									 <p style="margin: 0 0 15px 0;">&nbsp;</p>
									 <table cellspacing="0" cellpadding="0" border="0" style="width:100%;background:#05A1C6;">
										<tbody>
										   <tr>
											  <td style="font-family: Arial, Helvetica, sans-serif;background: #05A1C6;padding:10px;box-sizing:border-box;">
												 <h2 style="margin:0; color: white;font-size: 16px;font-weight: 700;">New balance (CAD)</h2>
											  </td>
										   </tr>
										</tbody>
									 </table>
									 <table cellspacing="0" cellpadding="0" border="0" style="width:100%;">
										<tbody>
											<tr>
											  <td style="font-family: Arial, Helvetica, sans-serif;width: 160px;box-sizing: border-box;padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${txn_by}:</td>
											  <td style="font-family: Arial, Helvetica, sans-serif;font-weight: 700; padding: 5px 10px;gap: 4px;border-right: 1px solid #FFF; border-bottom: 1px solid #FFF; background: #f5f3f2;">${user_accountBalance}</td>
										   </tr>
										</tbody>
									 </table>
								 </#if>
                                 <p style="margin: 0 0 15px 0;">&nbsp;</p>
                                 <p style="margin: 0 0 15px 0;">Questions? Reach us 24 hours a day at ${support_phone} or by email at <a href="evsupport@bchydro.com" style="text-decoration:underline;color: #0074a4;font-weight: bold;">${support_mail}</a></p>
                              </td>
                           </tr>
                        </table>
                     </td>
                  </tr>
                  <tr>
                     <td class="links" bgcolor="#F5F5F5" align="left" style="border-width: 0;font-family: Arial, Helvetica, sans-serif;background-color: #F5F5F5;padding: 20px 20px 10px 20px;text-align: left;">
                        <table cellspacing="0" cellpadding="0" border="0" style="border-collapse: collapse;max-width: 440px;width: 100%;">
                           <tr>
                              <td align="left" style="border-width: 0;font-family: Arial, Helvetica, sans-serif;text-align: left;">
                                 <table align="left" cellspacing="0" cellpadding="0" border="0" style="border-collapse: collapse;min-width: 0;width: auto;">
                                    <tr>
                                       <td align="left" style="border-width: 0;font-family: Arial, Helvetica, sans-serif;text-align: left;padding: 0 30px 10px 0;">
                                          <a href="https://electricvehicles.bchydro.com/charge/public-charging?utm_source=ev&utm_medium=email&utm_content=footer" style="text-decoration: underline;color: #000000;font-weight: bold;">BC Hydro EV</a>
                                       </td>
                                    </tr>
                                 </table>
                                 <table align="left" cellspacing="0" cellpadding="0" border="0" style="border-collapse: collapse;min-width: 0;width: auto;">
                                    <tr>
                                       <td align="left" style="border-width: 0;font-family: Arial, Helvetica, sans-serif;text-align: left;padding: 0 30px 10px 0;">
                                          <a href="https://electricvehicles.bchydro.com/charge/public-charging/how-use-our-fast-chargers?utm_source=ev&utm_medium=email&utm_content=footer" style="text-decoration: underline;color: #000000;font-weight: bold;">Charging support</a>
                                       </td>
                                    </tr>
                                 </table>
                                 <table align="left" cellspacing="0" cellpadding="0" border="0" style="border-collapse: collapse;min-width: 0;width: auto;">
                                    <tr>
                                       <td align="left" style="border-width: 0;font-family: Arial, Helvetica, sans-serif;text-align: left;padding: 0 30px 10px 0;">
                                          <a href="https://electricvehicles.bchydro.com/contact-us?utm_source=ev&utm_medium=email&utm_content=footer" style="text-decoration: underline;color: #000000;font-weight: bold;">Contact us</a>
                                       </td>
                                    </tr>
                                 </table>
                              </td>
                           </tr>
                        </table>
                     </td>
                  </tr>
                  <tr>
                     <td class="footer" bgcolor="#ffffff" align="left" style="border-width: 0;font-family: Arial, Helvetica, sans-serif;background-color: #F5F5F5;padding: 0 20px;text-align: left;">
                        <table cellspacing="0" cellpadding="0" border="0" style="border-collapse: collapse;max-width: 440px;width: 100%;">
                           <tr>
                              <td align="left" style="border-width: 0;font-family: Arial, Helvetica, sans-serif;text-align: left;border-top: 1px solid #000000;font-size: 11px;color: #000000;padding: 20px 0 20px 0;">
                                 <p style="margin: 0 0 15px 0;">You've received this email at <a href="mailto:[email]" style="text-decoration: underline;color: #000000;font-weight: bold;display: inline-block;">${to_mail}</a> because
                                    you've registered with the BC Hydro EV network.
                                 </p>
                                 <p style="margin: 0 0 15px 0;">© BC Hydro, <span class="address">333 Dunsmuir&zwj; Street, Vancouver, B.C.
                                    V6B 5R3</span> | <a href="https://www.bchydro.com/siteinfo/privacy.html?utm_source=transactional&utm_medium=email&utm_content=privacy" style="text-decoration: underline;color: #000000;font-weight: bold;display: inline-block;">Privacy
                                    statement</a>
                                 </p>
                                 <table class="social" style="border-collapse: collapse;min-width: unset;width: 130px;">
                                    <tbody>
                                       <tr>
                                          <td style="border-width: 0;font-family: Arial, Helvetica, sans-serif;text-align: left;border-top: none;font-size: 11px;color: #000000;padding: 0;">
                                             <a href="https://www.facebook.com/bchydro" style="text-decoration: underline;color: #000000;font-weight: bold;display: inline-block;"><img src="https://www.bchydro.com/content/dam/BCHydro/email/images/content/facebook-blk.png" alt="Facebook" width="20" height="20" style="display: block;border: none;margin-right: 10px;"></a>
                                          </td>
                                          <td style="border-width: 0;font-family: Arial, Helvetica, sans-serif;text-align: left;border-top: none;font-size: 11px;color: #000000;padding: 0;">
                                             <a href="https://www.instagram.com/bchydro/" style="text-decoration: underline;color: #000000;font-weight: bold;display: inline-block;"><img src="https://www.bchydro.com/content/dam/BCHydro/email/images/content/instagram-blk.png" alt="Instagram" width="20" height="20" style="display: block;border: none;margin-right: 10px;"></a>
                                          </td>
                                          <td style="border-width: 0;font-family: Arial, Helvetica, sans-serif;text-align: left;border-top: none;font-size: 11px;color: #000000;padding: 0;">
                                             <a href="https://www.tiktok.com/@bchydro?lang=en" style="text-decoration: underline;color: #000000;font-weight: bold;display: inline-block;"><img src="https://www.bchydro.com/content/dam/BCHydro/email/images/content/tiktok-blk.png" alt="YouTube" width="20" height="20" style="display: block;border: none;margin-right: 10px;"></a>
                                          </td>
                                          <td style="border-width: 0;font-family: Arial, Helvetica, sans-serif;text-align: left;border-top: none;font-size: 11px;color: #000000;padding: 0;">
                                             <a href="https://twitter.com/bchydro" style="text-decoration: underline;color: #000000;font-weight: bold;display: inline-block;"><img src="https://www.bchydro.com/content/dam/BCHydro/email/images/content/twitter-blk.png" alt="Twitter" width="20" height="20" style="display: block;border: none;margin-right: 10px;"></a>
                                          </td>
                                          <td style="border-width: 0;font-family: Arial, Helvetica, sans-serif;text-align: left;border-top: none;font-size: 11px;color: #000000;padding: 0;">
                                             <a href="https://www.youtube.com/user/BChydro" style="text-decoration: underline;color: #000000;font-weight: bold;display: inline-block;"><img src="https://www.bchydro.com/content/dam/BCHydro/email/images/content/youtube-blk.png" alt="YouTube" width="20" height="20" style="display: block;border: none;margin-right: 10px;"></a>
                                          </td>
                                       </tr>
                                    </tbody>
                                 </table>
                              </td>
                           </tr>
                        </table>
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
      </table>
   </body>
</html>