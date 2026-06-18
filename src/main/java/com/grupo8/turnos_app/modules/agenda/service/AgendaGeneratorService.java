package com.grupo8.turnos_app.modules.agenda.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo8.turnos_app.common.enums.AppointmentStatus;
import com.grupo8.turnos_app.common.enums.DayOfWeek;
import com.grupo8.turnos_app.modules.appointment.entity.Appointment;
import com.grupo8.turnos_app.modules.appointment.repository.AppointmentRepository;
import com.grupo8.turnos_app.modules.appointmentschedule.entity.AppointmentSchedule;
import com.grupo8.turnos_app.modules.appointmentschedule.repository.AppointmentScheduleRepository;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessNotFoundException;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;
import com.grupo8.turnos_app.modules.day_schedule.entity.DaySchedule;
import com.grupo8.turnos_app.modules.day_schedule.repository.DayScheduleRepository;

import lombok.RequiredArgsConstructor;

// agenda: automatic appointment generation based on business and schedule configuration. generates UNBOOKED appointments.
@Service
@RequiredArgsConstructor
public class AgendaGeneratorService {

  private final AppointmentScheduleRepository appointmentScheduleRepository;
  private final AppointmentRepository appointmentRepository;
  private final BusinessRepository businessRepository;
  private final DayScheduleRepository dayScheduleRepository;

  // called by cron job. checks the anticipation gate and generates slots
  // (appointments with UNBOOKED status) for the business from scheduleEnd onward.
  // updates scheduleEnd after generation.
  @Transactional
  public void runAutomation(Business business) {
    if (!Boolean.TRUE.equals(business.getAutomaticSchedule()))
      return;

    Integer daysToCreate = business.getScheduleDaysToCreate();
    if (daysToCreate == null || daysToCreate <= 0)
      return;

    LocalDate today = LocalDate.now();
    LocalDate fromDate;

    // if scheduleEnd is set and is after today, use it as the fromDate. otherwise, use today 
    if (business.getScheduleEnd() != null) {
      LocalDate scheduleEndDate = business.getScheduleEnd().toLocalDate();
      long dayDifference = ChronoUnit.DAYS.between(today, scheduleEndDate);
      Integer anticipation = business.getScheduleAnticipation();
      if (anticipation != null && dayDifference > anticipation)
        return;
      fromDate = scheduleEndDate;
    } else {
      fromDate = today;
    }

    generateDaySlots(business, fromDate);

    // update business' schedule end to the last generated day
    LocalDate baseEnd = business.getScheduleEnd() != null ? business.getScheduleEnd().toLocalDate() : today;
    business.setScheduleEnd(baseEnd.plusDays(daysToCreate).atStartOfDay());
    businessRepository.save(business);
  }

  // called manually when activating automatic scheduling or when an
  // appointmentSchedule is created.
  // generates from today without updating scheduleEnd.
  @Transactional
  public void generateFromToday(UUID businessId) {
    Business business = businessRepository.findByPublicId(businessId)
        .orElseThrow(() -> new BusinessNotFoundException("Business not found"));
    generateFromToday(business);
  }

  @Transactional
  public void generateFromToday(Business business) {
    Integer daysToCreate = business.getScheduleDaysToCreate();
    if (daysToCreate == null || daysToCreate <= 0)
      return;
    generateDaySlots(business, LocalDate.now());
  }

  private void generateDaySlots(Business business, LocalDate fromDate) {
    Integer daysToCreate = business.getScheduleDaysToCreate();

    Map<DayOfWeek, DaySchedule> dayScheduleMap = dayScheduleRepository
        .findAllByBusinessId(business.getId()).stream()
        .collect(Collectors.toMap(DaySchedule::getDay, ds -> ds));

    Map<Integer, List<AppointmentSchedule>> schedulesByDayNumber = appointmentScheduleRepository
        .findByBusinessId(business.getId()).stream()
        .collect(Collectors.groupingBy(AppointmentSchedule::getDayNumber));

    // iterate over the days to create and generate appointments based on the
    // daySchedule's appointmentSchedules
    for (int day = 1; day <= daysToCreate; day++) {
      LocalDate date = fromDate.plusDays(day);
      // ordinal(): MONDAY=0 to SUNDAY=6, matching DayOfWeek enum (LUN=0 to DOM=6) and
      // dayNumber convention
      int dayNumber = date.getDayOfWeek().ordinal();
      DayOfWeek customDay = DayOfWeek.values()[dayNumber];

      DaySchedule daySchedule = dayScheduleMap.get(customDay);
      if (daySchedule == null || !Boolean.TRUE.equals(daySchedule.getEnabled()))
        continue;

      List<AppointmentSchedule> appointmentsForDay = schedulesByDayNumber.getOrDefault(dayNumber, List.of());
      for (AppointmentSchedule appointment : appointmentsForDay) {
        generateAppointment(business, appointment, date);
      }
    }
  }

  private void generateAppointment(Business business, AppointmentSchedule appointment, LocalDate date) {
    // set start and end datetime for the appointment based on the date and
    // appointment times
    LocalDateTime startDatetime = LocalDateTime.of(date, appointment.getStartTime());
    LocalDateTime endDatetime = LocalDateTime.of(date, appointment.getEndTime());
    Long employeeId = appointment.getEmployee() != null ? appointment.getEmployee().getId() : null;

    if (startDatetime.isAfter(LocalDateTime.now())
        && !appointmentRepository.existsSlot(business.getId(), appointment.getService().getId(), employeeId,
            startDatetime)) {

      appointmentRepository.save(Appointment.builder()
          .startDatetime(startDatetime)
          .endDatetime(endDatetime)
          .status(AppointmentStatus.UNBOOKED)
          .price(appointment.getService().getPrice())
          .business(business)
          .service(appointment.getService())
          .employee(appointment.getEmployee())
          .build());
    }
  }
}
