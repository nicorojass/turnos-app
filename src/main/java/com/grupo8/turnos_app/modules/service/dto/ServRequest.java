package com.grupo8.turnos_app.modules.service.dto;

import java.math.BigDecimal;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.DecimalMax;
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
    
    @NotBlank(message = "Ingresa el nombre del servicio")
    @Length(max = 255, message = "Name must be less than 255 characters")
    private String name;

    
    @Positive(message = "La duración del servicio debe ser positivo")
    @NotNull(message = "Ingresa la duración del servicio")
    private Integer durationMinutes;

    @NotNull(message = "Ingresa el precio del servicio")
    @DecimalMin(value = "0.01", message = "El precio del servicio no puede ser cero")
    private BigDecimal price;
    
    @DecimalMin(value = "0.00", message = "El porcentaje de seña no puede ser negativo")
    @DecimalMax(value = "100.00", message = "El porcentaje de seña no puede exceder el 100%")
    @NotNull(message = "El porcentaje de seña es obligatorio")
    private BigDecimal depositPorcentage;
}
