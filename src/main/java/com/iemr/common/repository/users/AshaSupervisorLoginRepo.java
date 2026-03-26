package com.iemr.common.repository.users;

import java.util.ArrayList;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.iemr.common.data.users.AshaSupervisorMapping;

@Repository
public interface AshaSupervisorLoginRepo extends CrudRepository<AshaSupervisorMapping, Long> {

	ArrayList<AshaSupervisorMapping> findBySupervisorUserIDAndDeletedFalse(Integer supervisorUserID);
}
