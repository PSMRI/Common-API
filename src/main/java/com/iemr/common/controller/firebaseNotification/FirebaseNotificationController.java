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
*
/*
* AMRIT – Accessible Medical Records via Integrated Technology
*/
package com.iemr.common.controller.firebaseNotification;

import com.iemr.common.model.notification.NotificationMessage;
import com.iemr.common.model.notification.UserToken;
import com.iemr.common.service.firebaseNotification.FirebaseNotificationService;
import com.iemr.common.utils.exception.IEMRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value= "/firebaseNotification")
public class FirebaseNotificationController {
    final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    FirebaseNotificationService firebaseNotificationService;

    @RequestMapping(value = "sendNotification",method = RequestMethod.POST)
    public String sendNotificationByToken(@RequestBody NotificationMessage notificationMessage){
        return firebaseNotificationService.sendNotification(notificationMessage);
    }

    @RequestMapping(value = "updateToken",method = RequestMethod.POST)
    public String  updateToken(@RequestBody UserToken userToken){
        return firebaseNotificationService.updateToken(userToken);
    }

    @RequestMapping(value = "getToken",method = RequestMethod.GET,headers = "Authorization")
    public String  getUserToken() throws IEMRException {

        return  firebaseNotificationService.getUserToken();
    }


}
