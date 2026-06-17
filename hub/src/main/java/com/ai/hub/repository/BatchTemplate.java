package com.ai.hub.repository;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "USER_BULK_FILE_DATA")
public class BatchTemplate {

    @Id
    @Column(name = "ID")
    private String id;
    
    @Column(name = "CLIENT_NUMBER")
    private String clientNumber;

    @Column(name = "CLASS")
    private String className;

    @Column(name = "SHORT_DESC",length =2000)
    private String shortDesc;

    @Column(name = "LONG_DESC", length = 4000)
    private String longDesc;

    @Column(name = "MATERIAL_TYPE")
    private String materialType;

    @Column(name = "UNSPC")
    private String unspc;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "BATCH_ID")
    private String batchId;
    
    @Column(name = "STATUS")
    private String status;

    public String getClientNumber() {
		return clientNumber;
	}

	public void setClientNumber(String clientNumber) {
		this.clientNumber = clientNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getLongDesc() {
        return longDesc;
    }

    public void setLongDesc(String longDesc) {
        this.longDesc = longDesc;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public String getUnspc() {
        return unspc;
    }

    public void setUnspc(String unspc) {
        this.unspc = unspc;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
}