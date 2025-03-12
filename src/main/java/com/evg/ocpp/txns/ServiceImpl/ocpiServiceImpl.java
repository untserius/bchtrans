package com.evg.ocpp.txns.ServiceImpl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.Service.StationService;
import com.evg.ocpp.txns.Service.ocpiService;
import com.evg.ocpp.txns.dao.GeneralDao;
import com.evg.ocpp.txns.repository.ExecuteRepository;

@Service
public class ocpiServiceImpl implements ocpiService {

	private final static Logger logger = LoggerFactory.getLogger(ocpiServiceImpl.class);

	@Value("${app.partyId}")
	private String partyId;

	@Value("${app.countryCode}")
	private String ocpiCountryCode;

	@Value("${ocpi.url}")
	private String ocpiUrl;

	@Override
	public void postlastupdated(long id, boolean ocpiFlag) {
		try {
			if (ocpiFlag) {
				Thread th = new Thread() {
					public void run() {
						try {
							String urlToRead = ocpiUrl + "ocpi/ocpp/update?id=" + id;
							logger.info("urlToRead" + urlToRead);
							StringBuilder result = new StringBuilder();
							URL url = null;
							url = new URL(urlToRead);
							HttpURLConnection conn = (HttpURLConnection) url.openConnection();
							conn.setConnectTimeout(5000);
							conn.setReadTimeout(5000);
							conn.setRequestMethod("GET");
							BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
							String line;
							while ((line = rd.readLine()) != null) {
								result.append(line);
							}
							rd.close();
						} catch (Exception e) {
							// e.printStackTrace();
						}
					}
				};
				th.start();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
