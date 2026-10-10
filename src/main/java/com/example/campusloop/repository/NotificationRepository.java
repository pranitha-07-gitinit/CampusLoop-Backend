
package com.example.campusloop.repository;

import com.example.campusloop.entity.Notification;
import com.example.campusloop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    long countByUserAndReadStatusFalse(User user);
}