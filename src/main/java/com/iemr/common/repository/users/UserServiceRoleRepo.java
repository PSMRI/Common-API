package com.iemr.common.repository.users;

import com.iemr.common.data.users.UserServiceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserServiceRoleRepo extends JpaRepository<UserServiceRole, Integer> {
    List<UserServiceRole> findByUserName(String userName);

}
