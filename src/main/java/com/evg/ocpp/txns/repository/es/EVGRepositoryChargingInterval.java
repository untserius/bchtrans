package com.evg.ocpp.txns.repository.es;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.evg.ocpp.txns.model.es.OCPPChargingIntervalData;

public interface EVGRepositoryChargingInterval extends ElasticsearchRepository<OCPPChargingIntervalData, String> {
	
	@Query("{ \"query_string\" : { \"query\": \"(?0)\", \"fields\": [\"sessionId\"] } }")
	Optional<OCPPChargingIntervalData> findById(String sessionId);
	
	@Query("{ \"query_string\" : { \"query\": \"(?0)\", \"fields\": [\"sessionId\"] } }")
	Page<OCPPChargingIntervalData> findBySessionId(String sessionId,Pageable pageable);
	
	@Query("{ \"query_string\" : { \"query\": \"(?0)\", \"fields\": [\"sessionId\"] } }")
	boolean existsById(String sessionId);

}
