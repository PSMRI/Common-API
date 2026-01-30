package com.iemr.common.repository.users;

import com.iemr.common.data.users.UserServiceRole;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
=======
>>>>>>> c6e42d94 (FLW-713 Remove All File Upload Options (#350))
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
<<<<<<< HEAD
public interface UserServiceRoleRepo extends JpaRepository<UserServiceRole, Integer> {
    UserServiceRole findByUserName(String userName);
    UserServiceRole findByUserId(Integer userId);



=======
public interface UserServiceRoleRepo extends JpaRepository<UserServiceRole,Integer> {
    List<UserServiceRole> findByUserName(String userName);
>>>>>>> c6e42d94 (FLW-713 Remove All File Upload Options (#350))
}
