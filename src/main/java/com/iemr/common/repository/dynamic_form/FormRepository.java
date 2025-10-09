package com.iemr.common.repository.dynamic_form;

import com.iemr.common.data.dynamic_from.FormDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormRepository extends JpaRepository<FormDefinition, Long> {
    Optional<FormDefinition> findByFormId(String formId);
    List<FormDefinition> findByModule_Id(Long moduleId);
}