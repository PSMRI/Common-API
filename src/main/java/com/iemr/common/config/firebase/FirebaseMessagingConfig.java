package com.iemr.common.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
public class FirebaseMessagingConfig {

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    @Value("${firebase.credential-file:}")
    private String firebaseCredentialFile;

    @Value("${firebase.credential-base64:}")
    private String firebaseCredentialBase64;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        if (!firebaseEnabled) {
            throw new IllegalStateException("Firebase is disabled");
        }

        GoogleCredentials credentials;

        if (!firebaseCredentialBase64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(firebaseCredentialBase64);
            credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decoded));
        } else if (!firebaseCredentialFile.isBlank()) {
            credentials = GoogleCredentials.fromStream(
                new ClassPathResource(firebaseCredentialFile).getInputStream()
            );
        } else {
            throw new IllegalStateException("No Firebase credentials provided");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        FirebaseApp firebaseApp = FirebaseApp.getApps().isEmpty()
                ? FirebaseApp.initializeApp(options)
                : FirebaseApp.getInstance();

        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
