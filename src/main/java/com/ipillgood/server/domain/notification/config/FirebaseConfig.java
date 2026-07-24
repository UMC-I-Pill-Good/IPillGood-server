package com.ipillgood.server.domain.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(NotificationProperties.class)
public class FirebaseConfig {

    @Bean
    @ConditionalOnProperty(prefix = "notification.delivery", name = "enabled", havingValue = "true")
    public FirebaseApp firebaseApp(NotificationProperties notificationProperties) {
        return FirebaseApp.getApps()
                .stream()
                .filter(app -> FirebaseApp.DEFAULT_APP_NAME.equals(app.getName()))
                .findFirst()
                .orElseGet(() -> initializeFirebaseApp(notificationProperties));
    }

    @Bean
    @ConditionalOnProperty(prefix = "notification.delivery", name = "enabled", havingValue = "true")
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    private FirebaseApp initializeFirebaseApp(NotificationProperties notificationProperties) {
        String serviceAccountBase64 = notificationProperties.firebase().serviceAccountBase64();
        if (!StringUtils.hasText(serviceAccountBase64)) {
            throw new IllegalStateException("FIREBASE_SERVICE_ACCOUNT_BASE64 must be set when notification delivery is enabled.");
        }

        try {
            byte[] serviceAccountJson = Base64.getDecoder().decode(serviceAccountBase64);
            GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(serviceAccountJson));
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
            return FirebaseApp.initializeApp(options);
        } catch (IllegalArgumentException | IOException e) {
            throw new IllegalStateException("Firebase service account credentials are invalid.", e);
        }
    }
}
