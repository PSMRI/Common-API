package com.iemr.common.data.grievance;


	import java.time.LocalDateTime;
	import java.util.List;

	public class GrievanceAllocationRequest {

	    private LocalDateTime startDate;  // Start date for filtering grievances
	    private LocalDateTime endDate;    // End date for filtering grievances
	    private List<Integer> userID;     // List of user IDs (agents) to whom grievances will be allocated
	    private Integer allocateNo;       // Number of grievances to be allocated to each user
	    private String language; 
	    // Getters and Setters

	    public LocalDateTime getStartDate() {
	        return startDate;
	    }

	    public void setStartDate(LocalDateTime startDate) {
	        this.startDate = startDate;
	    }

	    public LocalDateTime getEndDate() {
	        return endDate;
	    }

	    public void setEndDate(LocalDateTime endDate) {
	        this.endDate = endDate;
	    }

	    public List<Integer> getUserID() {
	        return userID;
	    }

	    public void setUserID(List<Integer> userID) {
	        this.userID = userID;
	    }

	    public Integer getAllocateNo() {
	        return allocateNo;
	    }

	    public void setAllocateNo(Integer allocateNo) {
	        this.allocateNo = allocateNo;
	    }
	    
	    public String getLanguage() {
	    	return language;
	    }
	    
	    public void setLanguage(String language) {
	    	this.language = language;
	    }

	    @Override
	    public String toString() {
	        return "GrievanceAllocationRequest{" +
	                "startDate=" + startDate +
	                ", endDate=" + endDate +
	                ", userID=" + userID +
	                ", allocateNo=" + allocateNo +
	                ", language=" + language +
	                '}';
	    }
	

}
