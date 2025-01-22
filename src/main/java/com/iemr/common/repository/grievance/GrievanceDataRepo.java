package com.iemr.common.repository.grievance;




import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.iemr.common.data.grievance.GrievanceDetails;

@Repository
public interface GrievanceDataRepo  extends CrudRepository<GrievanceDetails, Long>{

	@Query("SELECT COUNT(g) > 0 FROM GrievanceDetails g WHERE g.complaintId = :complaintId")
    boolean existsByComplaintId(@Param("complaintId") String complaintId);
	
	@Query("select count(request) "
			+ "from GrievanceDetails request where request.isAllocated = false")
	public Long fetchUnallocatedGrievanceCount();

}
