package com.iemr.common.repository.translation;

import com.iemr.common.data.translation.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface TranslationRepo extends JpaRepository<Translation, Long> {

    Optional<Translation> findByLabelKeyAndIsActive(String labelKey, boolean isActive);

}
