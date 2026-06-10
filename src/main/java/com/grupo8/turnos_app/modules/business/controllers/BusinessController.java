package com.grupo8.turnos_app.modules.business.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.grupo8.turnos_app.modules.business.dto.BusinessRequest;
import com.grupo8.turnos_app.modules.business.dto.BusinessResponse;
import com.grupo8.turnos_app.modules.business.services.BusinessService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/businesses")
@RequiredArgsConstructor
public class BusinessController {
    
    private final BusinessService businessService;

    @PostMapping
    public ResponseEntity<BusinessResponse> createBusiness(@RequestBody @Valid BusinessRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(businessService.createBusiness(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessResponse> getBusiness(@PathVariable Long id) {
        return ResponseEntity.ok(businessService.getBusinessById(id));
    }

    @GetMapping
    public ResponseEntity<List<BusinessResponse>> getAllBusinesses() {
        return ResponseEntity.ok(businessService.getAllBusinesses());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<BusinessResponse> getBusinessBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(businessService.getBusinessBySlug(slug));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessResponse> updateBusiness(@PathVariable Long id, @RequestBody @Valid BusinessRequest request) {
        return ResponseEntity.ok(businessService.updateBusiness(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> toggleBusinessActive(@PathVariable Long id) {
        businessService.toggleBusinessActive(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusiness(@PathVariable Long id) {
        businessService.deleteBusiness(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/types/{typeId}")
    public ResponseEntity<BusinessResponse> addType(@PathVariable Long id, @PathVariable Long typeId) {
        return ResponseEntity.ok(businessService.addTypeToBusiness(id, typeId));
    }

    @DeleteMapping("/{id}/types/{typeId}")
    public ResponseEntity<BusinessResponse> removeType(@PathVariable Long id, @PathVariable Long typeId) {
        return ResponseEntity.ok(businessService.removeTypeFromBusiness(id, typeId));
    }

}

