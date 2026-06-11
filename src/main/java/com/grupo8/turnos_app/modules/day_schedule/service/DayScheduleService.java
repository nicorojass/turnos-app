package com.grupo8.turnos_app.modules.day_schedule.service;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.common.enums.DayOfWeek;
import com.grupo8.turnos_app.modules.business.entities.Business;
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
import com.grupo8.turnos_app.modules.day_schedule.exception.BusinessNotFoundException;

@Service
@RequiredArgsConstructor
public class DayScheduleService {

  private final DayScheduleRepository dayScheduleRepository;
  private final BusinessRepository businessRepository;

  // Called by businessService when creating a new business
  // Creates each week's day_schedule with null values to be set later.
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

  public List<DayScheduleResponse> getScheduleByBusiness(Long businessId) {
    checkBusinessExists(businessId);
    return dayScheduleRepository.findAllByBusinessId(businessId)
        .stream()
        .map(DayScheduleMapper::toResponse)
        .toList();
  }

  // Replaces the full weekly schedule. Each item in the list is upserted by day.
  // If a day is missing from the list it stays untouched in the DB.
  @Transactional
  public List<DayScheduleResponse> updateFullSchedule(Long businessId,
      List<DayScheduleUpdateRequest> requests) {
    checkBusinessExists(businessId);

    return requests.stream()
        .map(req -> updateDay(businessId, req))
        .toList();
  }

  // Update day_schedule settings
  private DayScheduleResponse updateDay(Long businessId, DayScheduleUpdateRequest request) {
    DaySchedule daySchedule = dayScheduleRepository
        .findByBusinessIdAndDay(businessId, request.getDay())
        .orElseThrow(() -> new DayScheduleNotFoundException(
            "Schedule not found for day " + request.getDay()));

    LocalTime effectiveStart = request.getDayStart() != null
        ? request.getDayStart()
        : daySchedule.getDayStart();
    LocalTime effectiveEnd = request.getDayEnd() != null
        ? request.getDayEnd()
        : daySchedule.getDayEnd();

    if (effectiveStart != null && effectiveEnd != null && !effectiveEnd.isAfter(effectiveStart)) {
      throw new InvalidScheduleTimeException("End time must be after start time for day " + request.getDay());
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

  // Helper
  
  // throws if the business does not exist. used to validate existence before any
  // operation.
  private void checkBusinessExists(Long businessId) {
    if (!businessRepository.existsById(businessId))
      throw new BusinessNotFoundException("Business not found");
  }
}