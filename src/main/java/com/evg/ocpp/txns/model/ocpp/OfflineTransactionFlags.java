package com.evg.ocpp.txns.model.ocpp;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "ocpp_offlineTransactionFlags")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfflineTransactionFlags extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;

    @Column(name = "start_time_set", nullable = false)
    private boolean startTimeSet = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date();

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isStartTimeSet() {
        return startTimeSet;
    }

    public void setStartTimeSet(boolean startTimeSet) {
        this.startTimeSet = startTimeSet;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "OfflineTransactionFlags [sessionId=" + sessionId + ", startTimeSet=" + startTimeSet + ", createdAt=" + createdAt + "]";
    }
}