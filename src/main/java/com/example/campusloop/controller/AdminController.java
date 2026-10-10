package com.example.campusloop.controller;

import com.example.campusloop.entity.Item;
import com.example.campusloop.entity.Request;
import com.example.campusloop.repository.ItemRepository;
import com.example.campusloop.repository.RequestRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    private final ItemRepository itemRepository;
    private final RequestRepository requestRepository;

    public AdminController(ItemRepository itemRepository,
                           RequestRepository requestRepository) {
        this.itemRepository = itemRepository;
        this.requestRepository = requestRepository;
    }

    @GetMapping("/items")
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @GetMapping("/requests")
    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }

    @PutMapping("/requests/{id}/approve")
    public String approveRequest(@PathVariable Long id) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus("APPROVED");

        requestRepository.save(request);

        return "Request approved successfully";
    }

    @PutMapping("/requests/{id}/reject")
    public String rejectRequest(@PathVariable Long id) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus("REJECTED");

        requestRepository.save(request);

        return "Request rejected successfully";
    }

    @DeleteMapping("/requests/{id}")
    public String deleteRequest(@PathVariable Long id) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        Item item = request.getItem();

        requestRepository.delete(request);

        if (item != null) {
            itemRepository.delete(item);
        }

        return "Request deleted successfully";
    }
}