package com.whatsappclone.services;


import com.whatsappclone.events.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SimpMessagingTemplate template;

    public void sendNotification(String userId, Notification notification) {
        log.info("Sending websocket notification to user {} with payload {}", userId, notification);
        template.convertAndSendToUser(userId, "/chat", notification);
    }
}
