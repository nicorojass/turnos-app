package com.grupo8.turnos_app.modules.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessTypeResponse {

    private Long id;
    private String name;
    private Boolean active;
}