package com.iemr.common.dto.grivance;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GrievanceWorklistDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	 private String complaintID;
	    private String subjectOfComplaint;
	    private String complaint;
	    private Long beneficiaryRegID;
	    private Integer providerServiceMapID;
	    private String firstName;
	    private String lastName;
	    private String primaryNumber;
	    private List<GrievanceTransactionDTO> transactions = new ArrayList<>(); // transactions list
	    private String severety;
	    private String state;
	    private Integer userId;
	    private Boolean deleted;
	    private String createdBy;
	    private Timestamp createdDate;
	    private Timestamp lastModDate;
	    private Boolean isCompleted;
	    private String gender;
	    private String district;
	    private Long beneficiaryID;
	    private String age;
	    private Boolean retryNeeded;
	    private Integer callCounter;
	 //   private String lastCall; // Add this field if you want
	    
	    
		public GrievanceWorklistDTO(String complaintID, String subjectOfComplaint, String complaint,
				Long beneficiaryRegID, Integer providerServiceMapID, String firstName, String lastName,
				String primaryNumber, List<GrievanceTransactionDTO> transactions, String severety, String state,
				Integer userId, Boolean deleted, String createdBy, Timestamp createdDate, Timestamp lastModDate,
				Boolean isCompleted, String gender, String district, Long beneficiaryID, String age,
				Boolean retryNeeded, Integer callCounter) {
			super();
			this.complaintID = complaintID;
			this.subjectOfComplaint = subjectOfComplaint;
			this.complaint = complaint;
			this.beneficiaryRegID = beneficiaryRegID;
			this.providerServiceMapID = providerServiceMapID;
			this.firstName = firstName;
			this.lastName = lastName;
			this.primaryNumber = primaryNumber;
			this.transactions = transactions;
			this.severety = severety;
			this.state = state;
			this.userId = userId;
			this.deleted = deleted;
			this.createdBy = createdBy;
			this.createdDate = createdDate;
			this.lastModDate = lastModDate;
			this.isCompleted = isCompleted;
			this.gender = gender;
			this.district = district;
			this.beneficiaryID = beneficiaryID;
			this.age = age;
			this.retryNeeded = retryNeeded;
			this.callCounter = callCounter;
		}

	    


}
