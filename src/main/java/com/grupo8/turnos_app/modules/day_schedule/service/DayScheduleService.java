package com.grupo8.turnos_app.modules.day_schedule.service;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.common.enums.DayOfWeek;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessNotFoundException;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;
import com.grupo8.turnos_app.modules.day_schedule.dto.DayScheduleResponse;
import com.grupo8.turnos_app.modules.day_schedule.dto.DayScheduleUpdateRequest;
import com.grupo8.turnos_app.modules.day_schedule.entity.DaySchedule;
import com.grupo8.turnos_app.modules.day_schedule.mapper.DayScheduleMapper;
import com.grupo8.turnos_app.modules.day_schedule.repository.DayScheduleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.grupo8.turnos_app.modules.day_schedule.exception.DayScheduleNotFoundException;
import com.grupo8.turnos_app.modules.day_schedule.exception.InvalidScheduleTimeException;

@Service
@RequiredArgsConstructor
public class DayScheduleService {

  private final DayScheduleRepository dayScheduleRepository;
  private final BusinessRepository businessRepository;

  // Called by businessService when creating a new business
  // Creates each week's day_schedule with null values to be set later
  @Transactional
  public void initializeScheduleForBusiness(Business business) {
    List<DaySchedule> schedules = Arrays.stream(DayOfWeek.values())
        .map(day -> DaySchedule.builder()
            .day(day)
            .dayStart(null)
            .dayEnd(null)
            .appointmentDuration(null)
            .enabled(false)
            .business(business)
            .build())
        .toList();

    dayScheduleRepository.saveAll(schedules);
  }

  // get schedule by businessId
  public List<DayScheduleResponse> getScheduleByBusiness(UUID businessId) {
    Business business = businessRepository.findByPublicId(businessId)
        .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));
    return dayScheduleRepository.findAllByBusinessId(business.getId())
        .stream()
        .map(DayScheduleMapper::toResponse)
        .toList();
  }

  // Replaces the full weekly schedule. Each item in the list is updated by day.
  // If a day is missing from the list it stays untouched in the DB.
  @Transactional
  public List<DayScheduleResponse> updateFullSchedule(UUID businessId,
      List<DayScheduleUpdateRequest> requests) {
    Business business = businessRepository.findByPublicId(businessId)
        .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));

    return requests.stream()
        .map(req -> updateDay(business.getId(), req))
        .toList();
  }

  // Update day_schedule settings
  private DayScheduleResponse updateDay(Long businessId, DayScheduleUpdateRequest request) {
    DaySchedule daySchedule = dayScheduleRepository
        .findByBusinessIdAndDay(businessId, request.getDay())
        .orElseThrow(() -> new DayScheduleNotFoundException(
            "Dia no encontrado"));

    // new values.
    LocalTime effectiveStart = request.getDayStart() != null
        ? request.getDayStart()
        : daySchedule.getDayStart();
    LocalTime effectiveEnd = request.getDayEnd() != null
        ? request.getDayEnd()
        : daySchedule.getDayEnd();

    // check to avoid time mismatching (ej. start 22hs ; end 14hs)
    if (effectiveStart != null && effectiveEnd != null && !effectiveEnd.isAfter(effectiveStart)) {
      throw new InvalidScheduleTimeException("La hora de finalización del dia debe ser después de la hora de inicio");
    }

    if (request.getDayStart() != null)
      daySchedule.setDayStart(request.getDayStart());
    if (request.getDayEnd() != null)
      daySchedule.setDayEnd(request.getDayEnd());
    if (request.getAppointmentDuration() != null)
      daySchedule.setAppointmentDuration(request.getAppointmentDuration());
    daySchedule.setEnabled(request.getEnabled());

    return DayScheduleMapper.toResponse(dayScheduleRepository.save(daySchedule));
  }

}