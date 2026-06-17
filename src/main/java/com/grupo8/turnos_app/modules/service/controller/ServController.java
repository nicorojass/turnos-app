package com.grupo8.turnos_app.modules.service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo8.turnos_app.modules.service.dto.ServRequest;
import com.grupo8.turnos_app.modules.service.dto.ServResponse;
import com.grupo8.turnos_app.modules.service.service.ServService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/services")
@RequiredArgsConstructor
public class ServController {

    private final ServService service;

    @PostMapping
    public ResponseEntity<ServResponse> createService(
        @PathVariable UUID businessId,
        @Valid @RequestBody ServRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createService(businessId, request));
    }

    @GetMapping
    public ResponseEntity<List<ServResponse>> getServicesByBusinessId(@PathVariable UUID businessId) {
        return ResponseEntity.ok(service.getServicesByBusinessId(businessId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServResponse> editService(@PathVariable UUID id,@Valid @RequestBody ServRequest request) {
        return ResponseEntity.ok(service.editService(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable UUID id) {
        service.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}