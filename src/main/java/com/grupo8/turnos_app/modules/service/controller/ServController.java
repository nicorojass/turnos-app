package com.grupo8.turnos_app.modules.service.controller;

import java.util.List;

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
@RequestMapping("/businesses/{businessId}/services")
@RequiredArgsConstructor
public class ServController {

    private final ServService service;

    @PostMapping
    public ResponseEntity<ServResponse> createService(
        @PathVariable Long businessId,
        @Valid @RequestBody ServRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createService(businessId, request));
    }

    @GetMapping
    public ResponseEntity<List<ServResponse>> getServicesByBusinessId(@PathVariable Long businessId) {
        return ResponseEntity.ok(service.getServicesByBusinessId(businessId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServResponse> editService(@PathVariable Long id,@Valid @RequestBody ServRequest request) {
        return ResponseEntity.ok(service.editService(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        service.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}