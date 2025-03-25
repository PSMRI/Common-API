package com.iemr.common.repository.grievance;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.common.data.grievance.GrievanceTransaction;

@Repository
public interface GrievanceTransactionRepo extends CrudRepository<GrievanceTransaction, Long> {
	@Query(value = "select actionTakenBy,status,FileName,FileType,Redressed,createdAt,updatedAt,Comments from t_grievancetransaction t where t.grievanceId = :grievanceId",nativeQuery = true)
	List<Object[]> getGrievanceTransaction(@Param("grievanceId") Long grievanceId);

}
