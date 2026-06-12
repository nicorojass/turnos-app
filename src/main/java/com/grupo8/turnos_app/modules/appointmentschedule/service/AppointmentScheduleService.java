package com.grupo8.turnos_app.modules.appointmentschedule.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.common.exception.NotFoundException;
import com.grupo8.turnos_app.modules.appointmentschedule.dto.AppointmentScheduleRequest;
import com.grupo8.turnos_app.modules.appointmentschedule.dto.AppointmentScheduleResponse;
import com.grupo8.turnos_app.modules.appointmentschedule.entity.AppointmentSchedule;
import com.grupo8.turnos_app.modules.appointmentschedule.exception.AppointmentScheduleAlreadyExistsException;
import com.grupo8.turnos_app.modules.appointmentschedule.mapper.AppointmentScheduleMapper;
import com.grupo8.turnos_app.modules.appointmentschedule.repository.AppointmentScheduleRepository;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;
import com.grupo8.turnos_app.modules.service.entity.Serv;
import com.grupo8.turnos_app.modules.service.repository.ServRepository;
import com.grupo8.turnos_app.modules.users.entities.User;
import com.grupo8.turnos_app.modules.users.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentScheduleService {

    private final AppointmentScheduleRepository appointmentScheduleRepository;
    private final BusinessRepository businessRepository;
    private final ServRepository servRepository;
    private final UserRepository userRepository;

    public List<AppointmentScheduleResponse> getByBusiness(Long businessId) {
        return appointmentScheduleRepository.findByBusinessId(businessId)
                .stream()
                .map(AppointmentScheduleMapper::toResponse)
                .toList();
    }

    public AppointmentScheduleResponse create(Long businessId, AppointmentScheduleRequest request) {
    Business business = businessRepository.findById(businessId)
            .orElseThrow(() -> new NotFoundException("Business not found"));

        Serv service = servRepository.findById(request.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));

        User employee = null;
        if (request.getEmployeeId() != null) {
            employee = userRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new NotFoundException("Employee not found"));
        }
        
        if (appointmentScheduleRepository.existsByBusinessIdAndDayNumberAndStartTimeAndEndTime(
                businessId, request.getDayNumber(), request.getStartTime(), request.getEndTime())) {
        throw new AppointmentScheduleAlreadyExistsException("A schedule already exists for this day and time range");
        }

        AppointmentSchedule schedule = AppointmentScheduleMapper.toEntity(request);
        schedule.setBusiness(business);
        schedule.setService(service);
        schedule.setEmployee(employee);

        return AppointmentScheduleMapper.toResponse(appointmentScheduleRepository.save(schedule));
    }

    public void delete(Long id) {
        AppointmentSchedule schedule = appointmentScheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Appointment schedule not found"));
        appointmentScheduleRepository.delete(schedule);
    }

    public AppointmentScheduleResponse update(Long businessId, Long id, AppointmentScheduleRequest request) {
    AppointmentSchedule schedule = appointmentScheduleRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Appointment schedule not found"));

    Serv service = servRepository.findById(request.getServiceId())
            .orElseThrow(() -> new NotFoundException("Service not found"));

    User employee = null;
    if (request.getEmployeeId() != null) {
        employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new NotFoundException("Employee not found"));
    }

    if (appointmentScheduleRepository.existsByBusinessIdAndDayNumberAndStartTimeAndEndTimeAndIdNot(
        businessId, request.getDayNumber(), request.getStartTime(), request.getEndTime(), id)) {
    throw new AppointmentScheduleAlreadyExistsException("A schedule already exists for this day and time range");
        }
    schedule.setDayNumber(request.getDayNumber());
    schedule.setStartTime(request.getStartTime());
    schedule.setEndTime(request.getEndTime());
    schedule.setPrice(request.getPrice());
    schedule.setService(service);
    schedule.setEmployee(employee);

    return AppointmentScheduleMapper.toResponse(appointmentScheduleRepository.save(schedule));
    }
}