package com.grupo8.turnos_app.modules.business.controllers;

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
import com.grupo8.turnos_app.modules.business.dto.BusinessRequest;
import com.grupo8.turnos_app.modules.business.dto.BusinessResponse;
import com.grupo8.turnos_app.modules.business.services.BusinessService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/businesses")
@RequiredArgsConstructor
public class BusinessController {
    
    private final BusinessService businessService;


    @PostMapping
    public ResponseEntity<BusinessResponse> createBusiness(
        Authentication authentication,
        @Valid @RequestBody BusinessRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
        .body(businessService.createBusiness(authentication.getName(), request));
}

    @GetMapping("/{id}")
    public ResponseEntity<BusinessResponse> getBusiness(@PathVariable UUID id) {
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

    @GetMapping("/mine")
    public ResponseEntity<BusinessResponse> getMyBusiness(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(businessService.getMyBusiness(userDetails.getUsername()));
}

    @PutMapping("/{id}")
    public ResponseEntity<BusinessResponse> updateBusiness(@PathVariable UUID id, @RequestBody @Valid BusinessRequest request) {
        return ResponseEntity.ok(businessService.updateBusiness(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusiness(@PathVariable UUID id) {
        businessService.deleteBusiness(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/types/{typeId}")
    public ResponseEntity<BusinessResponse> addType(@PathVariable UUID id, @PathVariable UUID typeId) {
        return ResponseEntity.ok(businessService.addTypeToBusiness(id, typeId));
    }

    @DeleteMapping("/{id}/types/{typeId}")
    public ResponseEntity<BusinessResponse> removeType(@PathVariable UUID id, @PathVariable UUID typeId) {
        return ResponseEntity.ok(businessService.removeTypeFromBusiness(id, typeId));
    }

}

