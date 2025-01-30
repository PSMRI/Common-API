package com.iemr.common.repository.grievance;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.iemr.common.data.grievance.GrievanceTransaction;

@Repository
public interface GrievanceTransactionRepo extends CrudRepository<GrievanceTransaction, Long> {

}
