package com.iemr.common.repository.beneficiary;


import com.iemr.common.model.beneficiary.RMNCHBeneficiaryDetailsRmnch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepo extends JpaRepository<RMNCHBeneficiaryDetailsRmnch, Long> {

    @Query(nativeQuery = true, value = " select UserName from db_iemr.m_user where  UserID = :userId and deleted is false ")
    String getUserName(@Param("userId") Integer userId);

    Optional<RMNCHBeneficiaryDetailsRmnch> findById(Long benID);


}
