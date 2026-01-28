package com.iemr.common.repository.dynamic_form;

import com.iemr.common.data.dynamic_from.FormField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<FormField, Long> {
    List<FormField> findByForm_FormIdOrderBySequenceAsc(String formId);
}
