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
import com.grupo8.turnos_app.modules.day_schedule.DaySchedules;
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

    // scheduleEnd is the last day already generated, so the next batch starts the day after.
    // first run (scheduleEnd null) starts today, so the slots left for today get created too.
    // if the window already expired (for example the server was off a few days) it also restarts
    // from today, otherwise it would spend the whole batch on past dates that get skipped anyway.
    if (business.getScheduleEnd() != null) {
      LocalDate scheduleEndDate = business.getScheduleEnd().toLocalDate();
      long dayDifference = ChronoUnit.DAYS.between(today, scheduleEndDate);
      Integer anticipation = business.getScheduleAnticipation();
      if (anticipation != null && dayDifference > anticipation)
        return;
      fromDate = scheduleEndDate.isBefore(today) ? today : scheduleEndDate.plusDays(1);
    } else {
      fromDate = today;
    }

    // inclusive range: with daysToCreate 30 starting on november 11th it generates up to
    // december 10th, that is, 30 days counting the first one
    LocalDate toDate = fromDate.plusDays(daysToCreate - 1);
    generateDaySlots(business, fromDate, toDate);

    business.setScheduleEnd(toDate.atStartOfDay());
    businessRepository.save(business);
  }

  // rebuilds the agenda from scratch, the same way sacaturno does when the owner saves changes:
  // deletes the future free slots and generates daysToCreate days again starting today, with the
  // weekly template as it is right now. that way the free slots on the public page always match the
  // template, for example if a template entry moves from 10:00 to 10:30 the 10:00 slots disappear.
  // called when the automatic agenda is turned on, when daysToCreate changes, when a template entry
  // is created, edited or deleted, and from "generate now" in the panel.
  // does nothing while the automatic agenda is off: the template is a draft until it gets turned on.
  // scheduleEnd ends up on the last generated day, so the cron renews from there.
  @Transactional
  public void regenerate(UUID businessId) {
    Business business = businessRepository.findByPublicId(businessId)
        .orElseThrow(() -> new BusinessNotFoundException("Business not found"));
    regenerate(business);
  }

  @Transactional
  public void regenerate(Business business) {
    if (!Boolean.TRUE.equals(business.getAutomaticSchedule()))
      return;

    Integer daysToCreate = business.getScheduleDaysToCreate();
    if (daysToCreate == null || daysToCreate <= 0)
      return;

    appointmentRepository.deleteFutureFreeSlots(business.getId(), LocalDateTime.now());

    // same inclusive range as runAutomation: 30 days counting today
    LocalDate today = LocalDate.now();
    LocalDate toDate = today.plusDays(daysToCreate - 1);
    generateDaySlots(business, today, toDate);

    business.setScheduleEnd(toDate.atStartOfDay());
    businessRepository.save(business);
  }

  // generates the UNBOOKED appointments for every date between fromDate and toDate, both included.
  // it's idempotent: existsSlot skips the ones already created, so running it twice doesn't duplicate.
  private void generateDaySlots(Business business, LocalDate fromDate, LocalDate toDate) {

    Map<DayOfWeek, DaySchedule> dayScheduleMap = dayScheduleRepository
        .findAllByBusinessId(business.getId()).stream()
        .collect(Collectors.toMap(DaySchedule::getDay, ds -> ds));

    Map<Integer, List<AppointmentSchedule>> schedulesByDayNumber = appointmentScheduleRepository
        .findByBusinessId(business.getId()).stream()
        .collect(Collectors.groupingBy(AppointmentSchedule::getDayNumber));

    // iterate over the days to create and generate appointments based on the
    // daySchedule's appointmentSchedules
    // starts on fromDate itself; before it started on the day after, so today never got slots
    for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
      // ordinal(): MONDAY=0 to SUNDAY=6, matching DayOfWeek enum (LUN=0 to DOM=6) and
      // dayNumber convention
      int dayNumber = date.getDayOfWeek().ordinal();
      DayOfWeek customDay = DayOfWeek.values()[dayNumber];

      DaySchedule daySchedule = dayScheduleMap.get(customDay);
      if (daySchedule == null || !Boolean.TRUE.equals(daySchedule.getEnabled()))
        continue;

      List<AppointmentSchedule> appointmentsForDay = schedulesByDayNumber.getOrDefault(dayNumber, List.of());
      for (AppointmentSchedule appointment : appointmentsForDay) {
        // templates created before the day hours changed can fall outside them now: those wait
        // until the hours or the template change again
        if (!DaySchedules.fitsOpeningHours(daySchedule, appointment.getStartTime(), appointment.getEndTime()))
          continue;
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

    // besides the slot already existing, it also skips the ones that would land on top of an
    // appointment the owner created by hand for the same employee. for example if the owner added
    // a manual 10:00 to 10:45 for lucia on monday 16, the template's 10:30 for lucia isn't created
    // that monday, otherwise lucia would have two overlapping appointments
    if (startDatetime.isAfter(LocalDateTime.now())
        && !appointmentRepository.existsSlot(business.getId(), appointment.getService().getId(), employeeId,
            startDatetime)
        && !appointmentRepository.existsOverlappingInBusiness(business.getId(), employeeId, startDatetime,
            endDatetime, true)) {

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
