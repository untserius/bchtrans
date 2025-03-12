package com.evg.ocpp.txns.model.ocpp;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "ocpp_sessionBillableData")
public class OCPPSessionBillableData extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String chargeSessionId;
    private BigDecimal lastBillableKwh;
    private BigDecimal lastBillableDuration;
    private BigDecimal originalFreeKwh;
    private BigDecimal originalFreeMins;
    private BigDecimal additionalFreeKwh;
    private BigDecimal additionalFreeMins;
    private BigDecimal usedAdditionalKwh;
    private BigDecimal usedAdditionalMins;
    private BigDecimal usedFreeMins;
    private BigDecimal usedFreeKwh;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    public OCPPSessionBillableData() {
        super();
    }

    public OCPPSessionBillableData(String chargeSessionId, BigDecimal lastBillableKwh, BigDecimal lastBillableDuration,
                                   BigDecimal originalFreeKwh, BigDecimal originalFreeMins, BigDecimal additionalFreeKwh,
                                   BigDecimal additionalFreeMins, BigDecimal usedAdditionalKwh, BigDecimal usedAdditionalMins,
                                   BigDecimal usedFreeMins, BigDecimal usedFreeKwh) {
        super();
        this.chargeSessionId = chargeSessionId;
        this.lastBillableKwh = lastBillableKwh;
        this.lastBillableDuration = lastBillableDuration;
        this.createdDate = new Date();
        this.updatedDate = new Date();
        this.originalFreeKwh = originalFreeKwh;
        this.originalFreeMins = originalFreeMins;
        this.additionalFreeKwh = additionalFreeKwh;
        this.additionalFreeMins = additionalFreeMins;
        this.usedAdditionalKwh = usedAdditionalKwh;
        this.usedAdditionalMins = usedAdditionalMins;
        this.usedFreeMins = usedFreeMins;
        this.usedFreeKwh = usedFreeKwh;
    }

    @Column(name = "charge_session_id", nullable = false, unique = true)
    public String getChargeSessionId() {
        return chargeSessionId;
    }

    public void setChargeSessionId(String chargeSessionId) {
        this.chargeSessionId = chargeSessionId;
    }

    @Column(name = "last_billable_kwh", precision = 10, scale = 4)
    public BigDecimal getLastBillableKwh() {
        return lastBillableKwh;
    }

    public void setLastBillableKwh(BigDecimal lastBillableKwh) {
        this.lastBillableKwh = lastBillableKwh;
    }

    @Column(name = "last_billable_duration", precision = 10, scale = 4)
    public BigDecimal getLastBillableDuration() {
        return lastBillableDuration;
    }

    public void setLastBillableDuration(BigDecimal lastBillableDuration) {
        this.lastBillableDuration = lastBillableDuration;
    }

    @Column(name = "created_date")
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Column(name = "updated_date")
    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Column(name = "original_free_kwh")
    public BigDecimal getOriginalFreeKwh() {
        return originalFreeKwh;
    }

    public void setOriginalFreeKwh(BigDecimal originalFreeKwh) {
        this.originalFreeKwh = originalFreeKwh;
    }

    @Column(name = "original_free_mins")
    public BigDecimal getOriginalFreeMins() {
        return originalFreeMins;
    }

    public void setOriginalFreeMins(BigDecimal originalFreeMins) {
        this.originalFreeMins = originalFreeMins;
    }

    @Column(name = "additional_free_kwh")
    public BigDecimal getAdditionalFreeKwh() {
        return additionalFreeKwh;
    }

    public void setAdditionalFreeKwh(BigDecimal additionalFreeKwh) {
        this.additionalFreeKwh = additionalFreeKwh;
    }

    @Column(name = "additional_free_mins")
    public BigDecimal getAdditionalFreeMins() {
        return additionalFreeMins;
    }

    public void setAdditionalFreeMins(BigDecimal additionalFreeMins) {
        this.additionalFreeMins = additionalFreeMins;
    }

    @Column(name = "used_additional_kwh", precision = 10, scale = 4)
    public BigDecimal getUsedAdditionalKwh() {
        return usedAdditionalKwh;
    }

    public void setUsedAdditionalKwh(BigDecimal usedAdditionalKwh) {
        this.usedAdditionalKwh = usedAdditionalKwh;
    }

    @Column(name = "used_additional_mins", precision = 10, scale = 4)
    public BigDecimal getUsedAdditionalMins() {
        return usedAdditionalMins;
    }

    public void setUsedAdditionalMins(BigDecimal usedAdditionalMins) {
        this.usedAdditionalMins = usedAdditionalMins;
    }

    @Column(name = "used_free_mins", precision = 10, scale = 4)
    public BigDecimal getUsedFreeMins() {return usedFreeMins;}

    public void setUsedFreeMins(BigDecimal usedFreeMins) {this.usedFreeMins = usedFreeMins;}

    @Column(name = "used_free_kwh", precision = 10, scale = 4)
    public BigDecimal getUsedFreeKwh() {return usedFreeKwh;}

    public void setUsedFreeKwh(BigDecimal usedFreeKwh) {this.usedFreeKwh = usedFreeKwh;}
}
