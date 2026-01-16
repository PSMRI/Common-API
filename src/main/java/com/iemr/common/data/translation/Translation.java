package com.iemr.common.data.translation;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "m_translation")
@Data
public class Translation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "label_key")
    private String labelKey;
    @Column(name = "english")
    private String english;
    @Column(name = "hindi_translation")
    private String hindiTranslation;
    @Column(name = "assamese_translation")
    private String assameseTranslation;
    @Column(name = "is_active")
    private Boolean isActive;

}
