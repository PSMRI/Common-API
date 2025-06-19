package com.iemr.common.controller.firebaseNotification;

import com.iemr.common.model.notification.NotificationMessage;
import com.iemr.common.service.firebaseNotification.FirebaseNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value= "/firebaseNotification",headers = "Authorization")
public class FirebaseNotificationController {
    final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    FirebaseNotificationService firebaseNotificationService;

    @RequestMapping(value = "sendNotification",method = RequestMethod.POST)
    public String sendNotificationByToken(@RequestBody NotificationMessage notificationMessage){
        return firebaseNotificationService.sendNotification(notificationMessage);
    }


}
