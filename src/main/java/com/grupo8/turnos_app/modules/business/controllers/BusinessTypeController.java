package com.grupo8.turnos_app.modules.business.controllers;

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

import com.grupo8.turnos_app.modules.business.dto.BusinessTypeRequest;
import com.grupo8.turnos_app.modules.business.dto.BusinessTypeResponse;
import com.grupo8.turnos_app.modules.business.services.BusinessTypeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/business-types")
@RequiredArgsConstructor
public class BusinessTypeController {

    private final BusinessTypeService businessTypeService;

    @PostMapping
    public ResponseEntity<BusinessTypeResponse> createBusinessType(@RequestBody @Valid BusinessTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(businessTypeService.createBusinessType(request));
    }

    @GetMapping
    public ResponseEntity<List<BusinessTypeResponse>> getAllBusinessTypes() {
        return ResponseEntity.ok(businessTypeService.getAllBusinessTypes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessTypeResponse> updateBusinessType(@PathVariable Long id, @RequestBody @Valid BusinessTypeRequest request) {
        return ResponseEntity.ok(businessTypeService.updateBusinessType(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> toggleBusinessTypeDeleted(@PathVariable Long id) {
        businessTypeService.toggleBusinessTypeDeleted(id);
        return ResponseEntity.noContent().build();
    }
}