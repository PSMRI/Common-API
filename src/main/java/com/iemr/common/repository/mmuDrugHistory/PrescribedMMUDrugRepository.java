package com.iemr.common.repository.mmuDrugHistory;

import com.iemr.common.data.mmuDrugHistory.PrescribedMMUDrugDetail;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescribedMMUDrugRepository extends CrudRepository<PrescribedMMUDrugDetail, Long> {
    PrescribedMMUDrugDetail findByPrescribedDrugID(Long prescribedDrugID);

}
