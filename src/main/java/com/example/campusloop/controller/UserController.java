
package com.example.campusloop.controller;

import com.example.campusloop.entity.User;
import com.example.campusloop.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::hidePassword)
                .toList();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {

        if (user.getName() == null ||
                user.getName().isBlank() ||
                user.getEmail() == null ||
                user.getEmail().isBlank() ||
                user.getPassword() == null ||
                user.getPassword().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Name, email and password are required");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already registered");
        }

        user.setRole("STUDENT");
        user.setPassword(
                passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        return hidePassword(savedUser);
    }

    @PostMapping("/login")
    public User login(@RequestBody User user) {

        User existingUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"));

        String storedPassword = existingUser.getPassword();
        String enteredPassword = user.getPassword();

        if (storedPassword == null || enteredPassword == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        boolean passwordMatches;

        if (storedPassword.startsWith("$2a$") ||
                storedPassword.startsWith("$2b$") ||
                storedPassword.startsWith("$2y$")) {

            passwordMatches = passwordEncoder.matches(
                    enteredPassword, storedPassword);

        } else {
            // Support existing accounts with plain-text passwords.
            passwordMatches = storedPassword.equals(enteredPassword);

            if (passwordMatches) {
                existingUser.setPassword(
                        passwordEncoder.encode(enteredPassword));
                existingUser = userRepository.save(existingUser);
            }
        }

        if (!passwordMatches) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        return hidePassword(existingUser);
    }

    private User hidePassword(User user) {
        user.setPassword(null);
        return user;
    }
}