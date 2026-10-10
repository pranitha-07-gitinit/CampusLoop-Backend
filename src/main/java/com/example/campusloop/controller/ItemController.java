package com.example.campusloop.controller;

import com.example.campusloop.entity.Item;
import com.example.campusloop.entity.Request;
import com.example.campusloop.entity.User;
import com.example.campusloop.repository.ItemRepository;
import com.example.campusloop.repository.RequestRepository;
import com.example.campusloop.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@CrossOrigin
public class ItemController {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    public ItemController(ItemRepository itemRepository,
                          UserRepository userRepository,
                          RequestRepository requestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
    }

    @GetMapping
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @PostMapping
    public Item createItem(@RequestBody Item item) {

        if (item.getUser() != null && item.getUser().getId() != null) {
            User user = userRepository.findById(item.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            item.setUser(user);
        }

        return itemRepository.save(item);
    }

    @PutMapping("/{id}/status")
    public Item updateStatus(@PathVariable Long id,
                             @RequestParam String status) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setStatus(status);

        return itemRepository.save(item);
    }

    @DeleteMapping("/{id}")
    public String deleteItem(@PathVariable Long id) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        List<Request> requests = requestRepository.findAll()
                .stream()
                .filter(request -> request.getItem() != null
                        && request.getItem().getId().equals(id))
                .toList();

        requestRepository.deleteAll(requests);

        itemRepository.delete(item);

        return "Item deleted successfully";
    }

    @GetMapping("/search")
    public List<Item> searchItems(@RequestParam String name) {
        return itemRepository.findByItemNameContainingIgnoreCase(name);
    }

    @GetMapping("/type")
    public List<Item> getItemsByType(@RequestParam String type) {
        return itemRepository.findByTypeIgnoreCase(type);
    }
}