package com.example.campusloop.repository;

import com.example.campusloop.entity.Request;
import com.example.campusloop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequester(User requester);
}