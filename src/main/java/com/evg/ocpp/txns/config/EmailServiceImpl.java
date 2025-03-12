package com.evg.ocpp.txns.config;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.evg.ocpp.txns.Service.ocppUserService;
import com.evg.ocpp.txns.ServiceImpl.propertiesServiceImpl;
import com.evg.ocpp.txns.forms.MailForm;
import com.evg.ocpp.txns.utils.LoggerUtil;
import com.evg.ocpp.txns.utils.Utils;;

@Component
public class EmailServiceImpl {

	static Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

	@Autowired
	JavaMailSender mailSender;

	@Autowired
	private LoggerUtil customeLogger;

	@Autowired
	private Utils utils;

	@Autowired
	private ocppUserService userService;
	
	@Value("${customer.Instance}")
	String instance;

	@Value("${file.sessionlogsLocation}")
	private String sessionlogsLocation;

	public boolean userEmail(Map<String,Object> data,long resend,String sessionId) {
		try {
			FreeMarkerConfigurationFactoryBean bean = new FreeMarkerConfigurationFactoryBean();
			bean.setTemplateLoaderPath("/templates/");
			freemarker.template.Template template = bean.createConfiguration().getTemplate(String.valueOf(data.get("template_name")));
			String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, data.get("template_data"));
			Thread emailThread = new Thread() {
				public void run() {
					JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
					mailSender.setHost(String.valueOf(data.get("from_mail_host")));
					mailSender.setPort(Integer.valueOf(String.valueOf(data.get("from_mail_port"))));
					mailSender.setUsername(String.valueOf(data.get("from_mail_auth")));
					mailSender.setPassword(String.valueOf(data.get("from_mail_password")));
					Properties javaMailProperties = new Properties();
//					javaMailProperties.put("mail.smtp.starttls.enable", "true");
					javaMailProperties.put("mail.smtp.ssl.enable", "true");
					javaMailProperties.put("mail.smtp.auth", "true");
					javaMailProperties.put("mail.transport.protocol", String.valueOf(data.get("from_mail_protocol")));
					javaMailProperties.put("mail.debug", "false");
					mailSender.setJavaMailProperties(javaMailProperties);

					MimeMessage mimeMessage = mailSender.createMimeMessage();

					try {
						MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
						helper.addInline("logo.png", new ClassPathResource("evg.png"));
						helper.setTo(String.valueOf(data.get("to_mail")));
						helper.setText(html, true);
						helper.setSubject(String.valueOf(data.get("subject")));
						helper.setFrom(new InternetAddress(String.valueOf(data.get("from_mail"))));
						if (!String.valueOf(String.valueOf(data.get("to_mail_cc"))).equalsIgnoreCase("") && !String.valueOf(String.valueOf(data.get("to_mail_cc"))).equalsIgnoreCase("null")) {
							helper.setCc(String.valueOf(String.valueOf(data.get("to_mail_cc"))).split(","));
						}
						boolean pathValid = utils.isPathValid(String.valueOf(data.get("logo")));
						MimeMultipart multipart = new MimeMultipart("related");
						BodyPart messageBodyPart = new MimeBodyPart();
						messageBodyPart.setContent(html, "text/html");
						multipart.addBodyPart(messageBodyPart);
						if (pathValid) {
							messageBodyPart = new MimeBodyPart();
							DataSource fds = new FileDataSource(String.valueOf(data.get("logo")));
							messageBodyPart.setDataHandler(new DataHandler(fds));
							messageBodyPart.setHeader("Content-ID", "<logo>");
							multipart.addBodyPart(messageBodyPart);

							mimeMessage.setContent(multipart);
						}
						mailSender.send(mimeMessage);
						LOGGER.info("EmailServiceImpl.sendEmail() -mailTo [" + String.valueOf(data.get("to_mail")) + "] - Successfully Sent !.");
						if(sessionId!=null && !sessionId.equalsIgnoreCase("null")) {
							userService.updateNotification("emailflag",true,sessionId,resend);
						}
						helper = null;
					} catch (MessagingException e3) {
						LOGGER.error("",e3);
					}
				}
			};
			emailThread.start();
		} catch (Exception e) {
			LOGGER.error("",e);
			return false;
		}
		return true;
	}

	public boolean bchInternalAlertEmail(Map<String,Object> data,long resend,String sessionId) {
		try {
			FreeMarkerConfigurationFactoryBean bean = new FreeMarkerConfigurationFactoryBean();
			bean.setTemplateLoaderPath("/templates/");
			freemarker.template.Template template = bean.createConfiguration().getTemplate(String.valueOf(data.get("template_name")));
			String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, data.get("template_data"));
			Thread emailThread = new Thread() {
				public void run() {
					JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
					mailSender.setHost(String.valueOf(data.get("from_mail_host")));
					mailSender.setPort(Integer.valueOf(String.valueOf(data.get("from_mail_port"))));
					mailSender.setUsername(String.valueOf(data.get("from_mail_auth")));
					mailSender.setPassword(String.valueOf(data.get("from_mail_password")));
					Properties javaMailProperties = new Properties();
//					javaMailProperties.put("mail.smtp.starttls.enable", "true");
					javaMailProperties.put("mail.smtp.ssl.enable", "true");
					javaMailProperties.put("mail.smtp.auth", "true");
					javaMailProperties.put("mail.transport.protocol", String.valueOf(data.get("from_mail_protocol")));
					javaMailProperties.put("mail.debug", "false");
					mailSender.setJavaMailProperties(javaMailProperties);

					MimeMessage mimeMessage = mailSender.createMimeMessage();

					try {
						MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
						helper.addInline("logo.png", new ClassPathResource("evg.png"));
						helper.setTo(String.valueOf(data.get("to_mail")));
						helper.setText(html, true);
						helper.setSubject(String.valueOf(data.get("subject")));
						helper.setFrom(new InternetAddress(String.valueOf(data.get("from_mail"))));
						if (!String.valueOf(String.valueOf(data.get("to_mail_cc"))).equalsIgnoreCase("") && !String.valueOf(String.valueOf(data.get("to_mail_cc"))).equalsIgnoreCase("null")) {
							helper.setCc(String.valueOf(String.valueOf(data.get("to_mail_cc"))).split(","));
						}
						boolean pathValid = utils.isPathValid(String.valueOf(data.get("logo")));
						MimeMultipart multipart = new MimeMultipart("related");
						BodyPart messageBodyPart = new MimeBodyPart();
						messageBodyPart.setContent(html, "text/html");
						multipart.addBodyPart(messageBodyPart);
						if (pathValid) {
							messageBodyPart = new MimeBodyPart();
							DataSource fds = new FileDataSource(String.valueOf(data.get("logo")));
							messageBodyPart.setDataHandler(new DataHandler(fds));
							messageBodyPart.setHeader("Content-ID", "<logo>");
							multipart.addBodyPart(messageBodyPart);

							mimeMessage.setContent(multipart);
						}
						mailSender.send(mimeMessage);
						LOGGER.info("EmailServiceImpl.sendEmail() -mailTo [" + String.valueOf(data.get("to_mail")) + "] - Successfully Sent !.");
					} catch (MessagingException e3) {
						LOGGER.error("",e3);
					}
				}
			};
			emailThread.start();
		} catch (Exception e) {
			LOGGER.error("",e);
			return false;
		}
		return true;
	}

	public void customerSupportMailService(MailForm mail) {
		Map<String, Object> orgData = userService.getOrgData(1, "");
		try {

			MimeMessage mimeMessage = mailSender.createMimeMessage();

			mail.setMailFrom(String.valueOf(orgData.get("email")));
			mimeMessage.addHeader("Content-type", "text/HTML; charset=UTF-8");
			mimeMessage.addHeader("format", "flowed");
			mimeMessage.addHeader("Content-Transfer-Encoding", "8bit");

			mimeMessage.setFrom(new InternetAddress(String.valueOf(orgData.get("email"))));

			mimeMessage.setReplyTo(InternetAddress.parse(String.valueOf(orgData.get("email"))));

			mimeMessage.setSubject(mail.getMailSubject(), "UTF-8");

			mimeMessage.setContent(mail.getMailContent(), "text/html");
			mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.getMailTo(), false));// env.getProperty("customer.To")
			mimeMessage.setSentDate(new Date());
			mailSender.send(mimeMessage);
		} catch (Exception e) {
			customeLogger.info("Error", "Mail Not Send Due to Below Exception : " + e.getMessage() + " , from mailId : "
					+ String.valueOf(orgData.get("email")));
			e.printStackTrace();
		}
	}
}