package com.shopsphere.notificationservice;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Notification create(@RequestBody NotificationRequest request) {
        if (request.orderId() == null || request.orderId() <= 0
                || request.recipient() == null || request.recipient().isBlank()
                || request.type() == null || request.type().isBlank()
                || request.message() == null || request.message().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "All fields are required; orderId must be positive");
        }

        Notification notification = new Notification(
                request.orderId(),
                request.recipient().trim(),
                request.type().trim(),
                request.message().trim()
        );

        return repository.save(notification);
    }

    @GetMapping
    public List<Notification> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Notification getById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found"));
    }

    public record NotificationRequest(
            Long orderId,
            String recipient,
            String type,
            String message
    ) {}
}
