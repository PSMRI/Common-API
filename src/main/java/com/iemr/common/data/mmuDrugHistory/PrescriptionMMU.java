package com.iemr.common.data.mmuDrugHistory;

import com.google.gson.annotations.Expose;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "t_prescription")
public class PrescriptionMMU {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Expose
    @Column(name = "PrescriptionID", insertable = false, updatable = false)
    private Long prescriptionID;

    @Expose
    @Column(name = "BenVisitID")
    private Long benVisitID;

    @Expose
    @Column(name = "ProviderServiceMapID")
    private Integer providerServiceMapID;

    @Expose
    @Column(name = "DiagnosisProvided")
    private String diagnosisProvided;

    @Expose
    @Column(name = "Remarks")
    private String remarks;

    @Expose
    @Column(name = "Deleted", insertable = false, updatable = true)
    private Boolean deleted;

}
