package com.ecommerce.notificationservice.controller;

import com.ecommerce.notificationservice.dto.NotificationRequest;
import com.ecommerce.notificationservice.dto.NotificationResponse;
import com.ecommerce.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.send(request));
    }

    @GetMapping("/reference/{referenceId}")
    public ResponseEntity<List<NotificationResponse>> getByReferenceId(@PathVariable Long referenceId) {
        return ResponseEntity.ok(notificationService.getByReferenceId(referenceId));
    }

    @GetMapping("/recipient")
    public ResponseEntity<List<NotificationResponse>> getByRecipient(@RequestParam String email) {
        return ResponseEntity.ok(notificationService.getByRecipient(email));
    }
}
