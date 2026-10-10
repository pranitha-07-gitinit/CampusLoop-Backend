
package com.example.campusloop.controller;

import com.example.campusloop.entity.Notification;
import com.example.campusloop.entity.User;
import com.example.campusloop.repository.NotificationRepository;
import com.example.campusloop.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(
            @PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::hideRecipientPassword)
                .toList();
    }

    @GetMapping("/user/{userId}/unread-count")
    public long getUnreadCount(@PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return notificationRepository
                .countByUserAndReadStatusFalse(user);
    }

    @PutMapping("/{notificationId}/read/user/{userId}")
    public Notification markAsRead(
            @PathVariable Long notificationId,
            @PathVariable Long userId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new RuntimeException("Notification not found"));

        if (notification.getUser() == null ||
                !notification.getUser().getId().equals(userId)) {
            throw new RuntimeException(
                    "You cannot access another user's notification");
        }

        notification.setReadStatus(true);

        Notification saved = notificationRepository.save(notification);
        saved.getUser().setPassword(null);

        return saved;
    }

    private Notification hideRecipientPassword(Notification notification) {
        if (notification.getUser() != null) {
            notification.getUser().setPassword(null);
        }
        return notification;
    }
}