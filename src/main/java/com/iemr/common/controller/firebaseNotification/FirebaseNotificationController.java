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
@RequestMapping(value= "/firebaseNotification",headers = "Authorization")
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
