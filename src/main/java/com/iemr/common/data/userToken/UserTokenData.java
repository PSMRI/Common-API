package com.iemr.common.data.userToken;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "user_tokens", schema = "db_iemr")
@Data
public class UserTokenData {
    @Id
    @Column(name = "user_id")
    Integer userId;
    @Column(name = "token")
    String token;
    @Column(name = "updated_at")
    Timestamp updatedAt;
}
