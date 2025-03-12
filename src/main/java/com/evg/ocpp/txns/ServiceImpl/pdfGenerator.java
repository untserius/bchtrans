package com.evg.ocpp.txns.ServiceImpl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.evg.ocpp.txns.Service.ocppUserService;

@Service
public class pdfGenerator {
	
	@Value("${file.sessionlogsLocation}")
	private String sessionlogsLocation;

	@Autowired
	private ocppUserService userService;
	
	private final static Logger logger = LoggerFactory.getLogger(pdfGenerator.class);
	
	public void generatePDF(Map<String,Object> stopObj ,long orgId,long userId) {
		try {
			
			 Map<String, Object> orgData = userService.getOrgData(orgId,String.valueOf(stopObj.get("referNo")));
			 stopObj.put("contactUsNo", String.valueOf(orgData.get("phoneNumber")));
			 stopObj.put("portalUrl", String.valueOf(orgData.get("portalLink")));
			 stopObj.put("orgAddress", String.valueOf(orgData.get("address")));
			 stopObj.put("whiteLabelName", String.valueOf(orgData.get("orgName")));
			
			File folderPathObj = null;
			folderPathObj = new File(sessionlogsLocation+String.valueOf(stopObj.get("StationId"))+"/"+String.valueOf(stopObj.get("randomSession")));
			if (!folderPathObj.exists()) {
				folderPathObj.mkdirs();
			}
			String filepath = folderPathObj.getAbsolutePath();
			PDDocument document = new PDDocument();
			PDPage page = new PDPage();
			document.addPage(page);
			PDPageContentStream contentStream = new PDPageContentStream(document, page);

			URL url = new URL(String.valueOf(stopObj.get("imagePath")));
			
			BufferedImage img = ImageIO.read(url);
			File file = ResourceUtils.getFile("classpath:evg.png");
			//ImageIO.write(img, "png", file);

			PDImageXObject image = PDImageXObject.createFromFile(ResourceUtils.getFile("classpath:evg.png").getPath(), document);
			//contentStream.drawImage(image, 50, 650);
			
			PDFont font = PDType1Font.HELVETICA;  
			contentStream.beginText();
			contentStream.setFont( font, 12 ); 
			contentStream.newLineAtOffset(100, 600); 
			contentStream.setLeading(14.5f);
			contentStream.showText("Dear "+stopObj.get("accName")+",");
			contentStream.newLine();
            contentStream.showText("Thank you for charging with "+stopObj.get("whiteLabelName"));
            contentStream.newLine();  
			contentStream.showText("Questions? Contact us 24 hours a day at "+stopObj.get("contactUsNo"));
			contentStream.newLine();
			contentStream.newLine();
			contentStream.showText(String.valueOf(stopObj.get("curDate")));
			contentStream.newLine();
			contentStream.newLine(); 
			contentStream.showText("Station Id :                            "+stopObj.get("StationId"));
			contentStream.newLine();
			contentStream.showText("Session Id :                          "+stopObj.get("sessionId"));
			contentStream.newLine();
			contentStream.showText("Station Name :                     "+stopObj.get("StationName"));
			contentStream.newLine();
			contentStream.showText("Site Name :                          "+stopObj.get("siteName"));
			contentStream.newLine();
			contentStream.showText("Site Address :                      "+stopObj.get("stnAddress"));
			contentStream.newLine();  
			contentStream.showText("----------------------------------------------------------------");
			contentStream.newLine();
			contentStream.showText("Unit Cost :                             "+stopObj.get("Rate1"));
			contentStream.newLine();
			contentStream.showText("Port Id :                                 "+stopObj.get("PortId"));
			contentStream.newLine();
			contentStream.showText("Charger Capacity :                "+stopObj.get("chargerCapacity")+"kW");
			contentStream.newLine();
			contentStream.showText("Start SoC :                             "+stopObj.get("StartSoC")+"%");
			contentStream.newLine();
			contentStream.showText("End SoC :                              "+stopObj.get("EndSoC")+"%");
			contentStream.newLine();
			contentStream.showText("Reason For Termination :      "+stopObj.get("ReasonForTermination"));
			contentStream.newLine();
			contentStream.showText("Charging time :                      "+stopObj.get("Duration"));
			contentStream.newLine();
			contentStream.showText("Charging cost :                       "+stopObj.get("TxnCost1"));
			contentStream.newLine();
			contentStream.showText("Idle Fee :                                "+stopObj.get("connectedTimePrice1"));
			contentStream.newLine();
			contentStream.showText("Paid idle time :                       "+stopObj.get("connectedTime"));
			contentStream.newLine();
			contentStream.showText("Grace period :                        "+stopObj.get("graceTime"));
			contentStream.newLine();
			contentStream.showText("Transaction fee :                    "+stopObj.get("txnFee1"));
			contentStream.newLine();
			contentStream.showText("processing fee :                     "+stopObj.get("processingFee1"));
			contentStream.newLine();
			contentStream.showText("Sales tax("+stopObj.get("salesTaxPerc")+") :                  "+stopObj.get("salesTax1"));
			contentStream.newLine();
			contentStream.showText("Hourly parking cost :              $0");
			contentStream.newLine();
			contentStream.showText("Total parking cost :                $0");
			contentStream.newLine();
			contentStream.showText("Payment method  :                 RFID");
			contentStream.newLine();
			contentStream.showText("----------------------------------------------------------------");
			contentStream.newLine();
			contentStream.showText("Total paid :                              "+stopObj.get("TotalCost1"));
			contentStream.newLine();
			contentStream.showText("Total energy delivered :          "+stopObj.get("EnergyUsage"));
			contentStream.newLine();
			contentStream.newLine();
			contentStream.newLine();
			contentStream.newLine();
			contentStream.showText("      © 2021 "+stopObj.get("whiteLabelName")+". All Rights Reserved.");
			contentStream.newLine();
			contentStream.showText(String.valueOf(orgData.get("address"))==null ? "" : String.valueOf(orgData.get("address")));
			contentStream.endText();
			contentStream.close();
			
			document.save(filepath+"/"+"session_"+String.valueOf(stopObj.get("sessionId"))+".pdf");
			document.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
