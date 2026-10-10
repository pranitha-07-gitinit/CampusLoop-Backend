
package com.example.campusloop.controller;

import com.example.campusloop.entity.Item;
import com.example.campusloop.entity.Notification;
import com.example.campusloop.entity.Request;
import com.example.campusloop.entity.User;
import com.example.campusloop.repository.ItemRepository;
import com.example.campusloop.repository.NotificationRepository;
import com.example.campusloop.repository.RequestRepository;
import com.example.campusloop.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin
public class RequestController {

    private final RequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public RequestController(
            RequestRepository requestRepository,
            ItemRepository itemRepository,
            UserRepository userRepository,
            NotificationRepository notificationRepository) {
        this.requestRepository = requestRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<Request> getUserRequests(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return requestRepository.findByRequester(user);
    }

    @PostMapping
    @Transactional
    public Request createRequest(@RequestBody Request request) {

        if (request.getRequester() == null ||
                request.getRequester().getId() == null) {
            throw new RuntimeException("User is required");
        }

        User user = userRepository.findById(
                request.getRequester().getId()
        ).orElseThrow(() ->
                new RuntimeException("User not found"));

        if (request.getItem() == null) {
            throw new RuntimeException("Item is required");
        }

        Item item = request.getItem();

        item.setUser(user);
        item.setType("LOST");
        item.setStatus("REPORTED");

        Item savedItem = itemRepository.save(item);

        request.setItem(savedItem);
        request.setRequester(user);
        request.setStatus("PENDING");

        Request savedRequest = requestRepository.save(request);

        List<User> users = userRepository.findAll();

        List<Notification> notifications = users.stream()
                .filter(otherUser ->
                        !otherUser.getId().equals(user.getId()))
                .map(otherUser -> new Notification(
                        otherUser,
                        "New item reported: " + savedItem.getItemName()
                                + " — " + savedItem.getLocation(),
                        savedItem.getId()
                ))
                .toList();

        notificationRepository.saveAll(notifications);

        return savedRequest;
    }

    @PutMapping("/{id}/user/{userId}")
    public Request editReport(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestBody Item updatedItem) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Report not found"));

        if (request.getRequester() == null ||
                !request.getRequester().getId().equals(userId)) {
            throw new RuntimeException(
                    "You cannot edit another student's report");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException(
                    "Only pending reports can be edited");
        }

        Item item = request.getItem();

        if (item == null) {
            throw new RuntimeException("Report item not found");
        }

        item.setItemName(updatedItem.getItemName());
        item.setDescription(updatedItem.getDescription());
        item.setLocation(updatedItem.getLocation());
        item.setDate(updatedItem.getDate());

        if (updatedItem.getImage() != null) {
            item.setImage(updatedItem.getImage());
        }

        itemRepository.save(item);

        return requestRepository.save(request);
    }

    @DeleteMapping("/{id}/user/{userId}")
    @Transactional
    public String cancelReport(
            @PathVariable Long id,
            @PathVariable Long userId) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Report not found"));

        if (request.getRequester() == null ||
                !request.getRequester().getId().equals(userId)) {
            throw new RuntimeException(
                    "You cannot cancel another student's report");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException(
                    "Only pending reports can be cancelled");
        }

        Item item = request.getItem();

        requestRepository.delete(request);

        if (item != null) {
            itemRepository.delete(item);
        }

        return "Report cancelled successfully";
    }

    @PutMapping("/{id}/status")
    public Request updateRequestStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Request not found"));

        request.setStatus(status);

        return requestRepository.save(request);
    }
}