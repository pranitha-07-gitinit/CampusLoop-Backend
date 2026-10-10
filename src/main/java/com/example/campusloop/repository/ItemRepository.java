package com.example.campusloop.repository;

import com.example.campusloop.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByItemNameContainingIgnoreCase(String itemName);

    List<Item> findByTypeIgnoreCase(String type);
}