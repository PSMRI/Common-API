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
import com.iemr.common.utils.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value= "/firebaseNotification",headers = "Authorization")
public class FirebaseNotificationController {
    final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    FirebaseNotificationService firebaseNotificationService;


    @PostMapping("/sendNotification")
    public ResponseEntity<String> sendNotificationByToken(@RequestBody NotificationMessage notificationMessage) {

        logger.info("Received notification request. Token={}, Title={}, ReceiverId={}, Type={}",
                notificationMessage.getToken(),
                notificationMessage.getTitle(),
                notificationMessage.getData() != null ? notificationMessage.getData().get("receiver_user_id") : null,
                notificationMessage.getData() != null ? notificationMessage.getData().get("notification_type") : null);

        logger.debug("Notification payload: {}", notificationMessage);

        try {
            String response = firebaseNotificationService.sendNotification(notificationMessage);

            logger.info("Notification processed successfully. Response={}", response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            logger.error("Failed to process notification request. Token={}, ReceiverId={}, Error={}",
                    notificationMessage.getToken(),
                    notificationMessage.getData() != null ? notificationMessage.getData().get("receiver_user_id") : null,
                    e.getMessage(),
                    e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send notification");
        }
    }

    @PostMapping("/updateToken")
    public ResponseEntity<?> updateToken(@RequestBody UserToken userToken) {
        try {

            Object result = firebaseNotificationService.updateToken(userToken);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {

            logger.error("Error while updating Firebase token", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update Firebase token.");
        }
    }

    @PostMapping(value = "getToken")
    public String  getUserToken() throws IEMRException {

        return  firebaseNotificationService.getUserToken();
    }


}
