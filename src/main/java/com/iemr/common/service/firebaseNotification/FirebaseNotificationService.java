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
package com.iemr.common.service.firebaseNotification;

import com.google.firebase.FirebaseException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.iemr.common.data.userToken.UserFcmTokenData;
import com.iemr.common.model.notification.NotificationMessage;
import com.iemr.common.model.notification.UserToken;
import com.iemr.common.repo.userToken.UserFcmTokenRepo;
import com.iemr.common.utils.CookieUtil;
import com.iemr.common.utils.JwtUtil;
import com.iemr.common.utils.exception.IEMRException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Service
public class FirebaseNotificationService {
    final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired(required = false)
    FirebaseMessaging firebaseMessaging;

    @Autowired
    private UserFcmTokenRepo userTokenRepo;

    @Autowired
    private CookieUtil cookieUtil;

    @Autowired
    private JwtUtil jwtUtil;

    private Message message;


    public String sendNotification(NotificationMessage notificationMessage) {
        if (firebaseMessaging == null) {
            logger.error("⚠️ Firebase is not configured, skipping notification");
            return null;
        }

        Notification notification = Notification.builder().setTitle(notificationMessage.getTitle()).setBody(notificationMessage.getBody()).build();

        Message message = Message.builder().setToken(notificationMessage.getToken()).setNotification(notification).putAllData(notificationMessage.getData()).build();


        try {
            String response = FirebaseMessaging.getInstance().send(message);

            return response;
        } catch (FirebaseException e) {
            return "Error sending notification";

        }
    }

    public String updateToken(UserToken userToken) {
        Optional<UserFcmTokenData> existingTokenData = userTokenRepo.findByUserId(userToken.getUserId());

        UserFcmTokenData userTokenData;

        if (existingTokenData.isPresent()) {
            userTokenData = existingTokenData.get();
            userTokenData.setToken(userToken.getToken());
        } else {
            userTokenData = new UserFcmTokenData();
            userTokenData.setUserId(userToken.getUserId());
            userTokenData.setToken(userToken.getToken());
        }

        userTokenRepo.save(userTokenData);
        return "Save Successfully";
    }

    public String getUserToken() throws IEMRException {
        HttpServletRequest requestHeader = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        String jwtTokenFromCookie = cookieUtil.getJwtTokenFromCookie(requestHeader);
        return userTokenRepo.findById(Integer.parseInt(jwtUtil.getUserIdFromToken(jwtTokenFromCookie))) // because your userId is Long in DB
                .map(UserFcmTokenData::getToken)
                .orElse(null); //
    }

}
