package com.iemr.common.service.firebaseNotification;

import com.google.firebase.FirebaseException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.iemr.common.model.notification.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FirebaseNotificationService {
    final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    FirebaseMessaging firebaseMessaging;

    public String sendNotification(NotificationMessage notificationMessage) {

        Notification notification = Notification.builder().setTitle(notificationMessage.getTitle()).setBody(notificationMessage.getBody()).build();

        Message message = Message.builder().setTopic(notificationMessage.getTopic()).setNotification(notification).putAllData(notificationMessage.getData()).build();


        try {
            String response = FirebaseMessaging.getInstance().send(message);

            return response;
        } catch (FirebaseException e) {
            return "Error sending notification";

        }
    }

}
