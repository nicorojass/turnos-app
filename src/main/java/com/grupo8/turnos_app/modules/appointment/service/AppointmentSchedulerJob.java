package com.grupo8.turnos_app.modules.appointment.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppointmentSchedulerJob {

    private final BusinessRepository businessRepository;
    private final AppointmentGeneratorService appointmentGeneratorService;

    @Scheduled(cron = "0 0 1 * * *") // todos los días a la 01:00
    public void generateDailySlots() {
        List<Business> businesses = businessRepository.findAll();
        for (Business business : businesses) {
            appointmentGeneratorService.generateSlotsForBusiness(business.getPublicId());
        }
    }
}
