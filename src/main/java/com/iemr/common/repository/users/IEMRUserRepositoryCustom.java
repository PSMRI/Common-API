/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.common.repository.users;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.iemr.common.data.users.User;
import com.iemr.common.data.users.UserSecurityQMapping;

@Repository
public interface IEMRUserRepositoryCustom extends CrudRepository<User, Long> {
	/* Status ID is set as 1 for new and 2 for active */
	@Query("SELECT u FROM User u WHERE LOWER(u.userName) = LOWER(:userName) and "
			+ "u.password = :password and u.deleted=false and u.statusID in (1, 2)")
	List<User> findByUserNamePassword(@Param("userName") String username, @Param("password") String password);

	User findByUserNameAndPassword(String username, String password);

	@Query("SELECT u FROM User u WHERE LOWER(u.userName) = LOWER(:userName) and u.deleted=false and "
			+ "u.statusID in (1, 2)")
	List<User> findByUserName(@Param("userName") String username);

	@Query("SELECT u FROM User u WHERE LOWER(u.userName) = LOWER(:userName) ")
	List<User> findByUserNameNew(@Param("userName") String username);

	@Query("SELECT usqm FROM UserSecurityQMapping usqm WHERE usqm.UserID = :userId AND (usqm.Deleted=false OR usqm.Deleted is null)")
	List<UserSecurityQMapping> getUserSecurityQues(@Param("userId") Long userid);

	@Transactional
	@Modifying(clearAutomatically = true)
	@Query("UPDATE User u set u.password = :password where u.userID = :userId")
	int updateSetForgetPassword(@Param("userId") Long userid, @Param("password") String password);

	@Query("SELECT u FROM User u WHERE LOWER(u.userName) = LOWER(:userName) and u.password = :password "
			+ "and u.deleted=false and u.statusID in (1, 2)")
	User findUserForChangePass(@Param("userName") String username, @Param("password") String password);

	@Transactional
	@Modifying(clearAutomatically = true)
	@Query("UPDATE User u set u.statusID = 2 where u.userID = :userId")
	int updateSetUserStatusActive(@Param("userId") Long userId);

	@Query("SELECT u FROM User u WHERE u.userID = :userID and u.deleted=false and u.statusID in (1, 2)")
	User findUserByUserID(@Param("userID") Long userID);

	@Query("SELECT u FROM UserSecurityQMapping u WHERE u.UserID=:UserID AND u.QuestionID=:QuestionID AND u.Answers=:Answers")
	UserSecurityQMapping verifySecurityQuestionAnswers(@Param("UserID") Long UserID,
			@Param("QuestionID") String QuestionID, @Param("Answers") String Answers);
	
	@Query("SELECT u FROM User u WHERE u.userID=5718")
	User getAllExistingUsers();
	
	User findByUserID(Long userID);

}
