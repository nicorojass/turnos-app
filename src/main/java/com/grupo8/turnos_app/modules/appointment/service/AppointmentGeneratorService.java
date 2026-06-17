package com.grupo8.turnos_app.modules.appointment.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo8.turnos_app.common.enums.AppointmentStatus;
import com.grupo8.turnos_app.modules.appointment.entity.Appointment;
import com.grupo8.turnos_app.modules.appointment.repository.AppointmentRepository;
import com.grupo8.turnos_app.modules.appointmentschedule.entity.AppointmentSchedule;
import com.grupo8.turnos_app.modules.appointmentschedule.repository.AppointmentScheduleRepository;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessNotFoundException;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentGeneratorService {

    private final AppointmentScheduleRepository appointmentScheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final BusinessRepository businessRepository;

    @Transactional
    public void generateSlotsForBusiness(UUID businessId) {
        Business business = businessRepository.findByPublicId(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found"));

        Integer daysToCreate = business.getScheduleDaysToCreate();
        if (daysToCreate == null || daysToCreate <= 0)
            return;

        List<AppointmentSchedule> schedules = appointmentScheduleRepository.findByBusinessId(business.getId());
        if (schedules.isEmpty())
            return;

        LocalDate today = LocalDate.now();

        for (int i = 0; i < daysToCreate; i++) {
            LocalDate date = today.plusDays(i);
            // Java DayOfWeek: MONDAY=1..SUNDAY=7 -> convertimos a SUNDAY=0..SATURDAY=6
            int dayNumber = date.getDayOfWeek().getValue() % 7;

            for (AppointmentSchedule schedule : schedules) {
                if (!schedule.getDayNumber().equals(dayNumber))
                    continue;
                generateSlotsForSchedule(business, schedule, date);
            }
        }
    }

    @Transactional
    public void generateSlotsForSchedule(Business business, AppointmentSchedule schedule, LocalDate date) {
        Integer duration = schedule.getService().getDurationMinutes();
        if (duration == null || duration <= 0)
            return;

        LocalTime slotStart = schedule.getStartTime();
        LocalTime slotEnd = slotStart.plusMinutes(duration);

        // protege contra wrap de medianoche (ej: 23:00 + 90min = 00:30 < 23:00)
        while (slotEnd.isAfter(slotStart) && !slotEnd.isAfter(schedule.getEndTime())) {
            LocalDateTime startDatetime = LocalDateTime.of(date, slotStart);
            LocalDateTime endDatetime = LocalDateTime.of(date, slotEnd);

            Long employeeId = schedule.getEmployee() != null ? schedule.getEmployee().getId() : null;

            if (startDatetime.isAfter(LocalDateTime.now())
                    && !appointmentRepository.existsSlot(business.getId(), schedule.getService().getId(), employeeId, startDatetime)) {

                Appointment appointment = Appointment.builder()
                        .startDatetime(startDatetime)
                        .endDatetime(endDatetime)
                        .status(AppointmentStatus.UNBOOKED)
                        .price(schedule.getPrice())
                        .business(business)
                        .service(schedule.getService())
                        .employee(schedule.getEmployee())
                        .build();

                appointmentRepository.save(appointment);
            }

            slotStart = slotEnd;
            slotEnd = slotStart.plusMinutes(duration);
        }
    }
}