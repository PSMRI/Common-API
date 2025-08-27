package com.iemr.common.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
public class FirebaseMessagingConfig {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    @Value("${firebase.credential-file:}")
    private String firebaseCredentialFile;


    @Bean
    @ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
    public FirebaseMessaging firebaseMessaging() throws IOException {
        if (!firebaseEnabled) {
            logger.error("⚠️ Firebase disabled by config");
            return null;
        }

        try {
            if (firebaseCredentialFile == null || firebaseCredentialFile.isBlank()) {
                logger.error("⚠️ No Firebase credentials path provided");
                return null; // don't throw, app will still start
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ClassPathResource(firebaseCredentialFile).getInputStream()
            );
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp firebaseApp = FirebaseApp.getApps().isEmpty()
                    ? FirebaseApp.initializeApp(options)
                    : FirebaseApp.getInstance();

            return FirebaseMessaging.getInstance(firebaseApp);

        } catch (Exception e) {
            logger.error("⚠️ Firebase init failed: " + e.getMessage());
            return null; // keep app running
        }

    }

}
