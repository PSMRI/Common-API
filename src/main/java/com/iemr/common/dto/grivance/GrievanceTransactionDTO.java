package com.iemr.common.dto.grivance;

import java.io.Serializable;
import java.sql.Timestamp;


import lombok.Data;

@Data
public class GrievanceTransactionDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	
		private String actionTakenBy;
		private String status;
	    private String fileName;
	    private String fileType;
	    private String redressed;
		private Timestamp createdAt;
	    private Timestamp updatedAt;
	    private String comment;

	    // Constructor, Getters, and Setters
	
	    public GrievanceTransactionDTO(
	    		String actionTakenBy, String status,
	    		String fileName, String fileType,
					String redressed, Timestamp createdAt, Timestamp updatedAt, String comment) {
				super();
				this.actionTakenBy = actionTakenBy;
				this.status = status;
				this.fileName = fileName;
				this.fileType = fileType;
				this.redressed = redressed;
				this.createdAt = createdAt;
				this.updatedAt = updatedAt;
				this.comment = comment;
			}
}
