package com.iemr.common.data.mmuDrugHistory;

import java.sql.Date;
import java.sql.Timestamp;

import com.google.gson.annotations.Expose;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Data
@Table(name = "t_prescribeddrug")
public class PrescribedMMUDrugDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Expose
    @Column(name = "PrescribedDrugID")
    private Long prescribedDrugID;

    @Expose
    @Column(name = "BeneficiaryRegID")
    private Long beneficiaryRegID;

    @Expose
    @Column(name = "BenVisitID")
    private Long benVisitID;

    @Expose
    @Column(name = "ProviderServiceMapID")
    private Integer providerServiceMapID;

    @Expose
    @Column(name = "VisitCode")
    private Long visitCode;

    @Expose
    @Column(name = "PrescriptionID")
    private Long prescriptionID;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PrescriptionID", referencedColumnName = "PrescriptionID", insertable = false, updatable = false)
    private PrescriptionMMU prescription;

    @Expose
    @Column(name = "DrugForm")
    private String formName;

    @Expose
    @Column(name = "DrugTradeOrBrandName")
    private String drugTradeOrBrandName;

    @Expose
    @Column(name = "DrugID")
    private Integer drugID;

    @Expose
    @Column(name = "GenericDrugName")
    private String drugName;

    @Expose
    @Column(name = "DrugStrength")
    private String drugStrength;

    @Expose
    @Column(name = "Dose")
    private String dose;

    @Expose
    @Column(name = "Route")
    private String route;

    @Expose
    @Column(name = "Frequency")
    private String frequency;

    @Expose
    @Column(name = "Duration")
    private String duration;

    @Expose
    @Column(name = "DuartionUnit")
    private String unit;

    @Expose
    @Column(name = "RelationToFood")
    private String relationToFood;

    @Expose
    @Column(name = "SpecialInstruction")
    private String instructions;

    @Expose
    @Column(name = "QtyPrescribed")
    private Integer qtyPrescribed;

    @Expose
    @Column(name = "Deleted", insertable = false, updatable = true)
    private Boolean deleted;

    @Expose
    @Column(name = "Processed", insertable = false, updatable = true)
    private String processed;

    @Expose
    @Column(name = "CreatedBy")
    private String createdBy;

}