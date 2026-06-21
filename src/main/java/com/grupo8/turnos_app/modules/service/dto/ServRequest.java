package com.grupo8.turnos_app.modules.service.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
    @Size(max = 80, message = "El nombre del servicio debe tener menos de 80 caracteres")
    private String name;

    @Positive(message = "La duración del servicio debe ser positivo")
    @Max(value = 480, message = "La duración no puede superar las 8 horas (480 minutos)")
    @NotNull(message = "Ingresa la duración del servicio")
    private Integer durationMinutes;

    @NotNull(message = "Ingresa el precio del servicio")
    @DecimalMin(value = "0.01", message = "El precio del servicio no puede ser cero")
    @DecimalMax(value = "10000000.00", message = "El precio ingresado supera el máximo permitido")
    private BigDecimal price;
    
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00", message = "El porcentaje de seña no puede exceder el 100%")
    @NotNull(message = "El porcentaje de seña es obligatorio")
    private BigDecimal depositPorcentage;
}
