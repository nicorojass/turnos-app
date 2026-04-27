package com.grupo8.turnos_app.modules.service.dto;

import java.math.BigDecimal;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ServRequest {
    
    @NotBlank(message = "Name is required")
    @Length(max = 255, message = "Name must be less than 255 characters")
    private String name;

    
    @Positive(message = "Duration must be a positive integer")
    @NotNull(message = "Duration is required")
    private Integer durationMinutes;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be a positive number")
    private BigDecimal price;
    
    @NotNull(message = "Business ID is required")
    private Long businessId;
}
