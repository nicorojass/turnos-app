package com.grupo8.turnos_app.modules.agenda.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AgendaSchedulerJob {

    private final BusinessRepository businessRepository;
    private final AgendaGeneratorService agendaGeneratorService;

    @Scheduled(cron = "0 0 2 * * *") // daily triggered at 02am
    public void generateDailySlots() {
        List<Business> businesses = businessRepository.findAllByAutomaticScheduleTrueAndDeletedFalse();
        for (Business business : businesses) {
            agendaGeneratorService.runAutomation(business);
        }
    }
}
