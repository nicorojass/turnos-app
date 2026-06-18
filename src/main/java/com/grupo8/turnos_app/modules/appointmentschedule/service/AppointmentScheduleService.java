package com.grupo8.turnos_app.modules.appointmentschedule.service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.common.exception.NotFoundException;
import com.grupo8.turnos_app.modules.agenda.service.AgendaGeneratorService;
import com.grupo8.turnos_app.modules.appointmentschedule.dto.AppointmentScheduleRequest;
import com.grupo8.turnos_app.modules.appointmentschedule.dto.AppointmentScheduleResponse;
import com.grupo8.turnos_app.modules.appointmentschedule.entity.AppointmentSchedule;
import com.grupo8.turnos_app.modules.appointmentschedule.exception.AppointmentScheduleAlreadyExistsException;
import com.grupo8.turnos_app.modules.appointmentschedule.exception.InvalidScheduleRangeException;
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
    private final AgendaGeneratorService agendaGeneratorService;

    public List<AppointmentScheduleResponse> getByBusiness(UUID businessId) {
        Business business = businessRepository.findByPublicId(businessId)
                .orElseThrow(() -> new NotFoundException("Business not found"));
        return appointmentScheduleRepository.findByBusinessId(business.getId())
                .stream()
                .map(AppointmentScheduleMapper::toResponse)
                .toList();
    }

    public AppointmentScheduleResponse create(UUID businessId, AppointmentScheduleRequest request) {
        Business business = businessRepository.findByPublicId(businessId)
                .orElseThrow(() -> new NotFoundException("Business not found"));

        Serv service = servRepository.findByPublicId(request.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));

        long scheduleMinutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (scheduleMinutes < service.getDurationMinutes()) {
            throw new InvalidScheduleRangeException("The time range must be at least as long as the service duration");
        }

        User employee = null;
        if (request.getEmployeeId() != null) {
            employee = userRepository.findByPublicId(request.getEmployeeId())
                    .orElseThrow(() -> new NotFoundException("Employee not found"));
        }

        Long employeeId = employee != null ? employee.getId() : null;

        if (appointmentScheduleRepository.existsConflictingSchedule(
                business.getId(), request.getDayNumber(), request.getStartTime(), request.getEndTime(),
                employeeId, null)) {
            throw new AppointmentScheduleAlreadyExistsException("A schedule already exists for this day, time and employee");
        }

        AppointmentSchedule schedule = AppointmentScheduleMapper.toEntity(request);
        schedule.setPrice(service.getPrice());
        schedule.setBusiness(business);
        schedule.setService(service);
        schedule.setEmployee(employee);

        AppointmentSchedule saved = appointmentScheduleRepository.save(schedule);

        agendaGeneratorService.generateFromToday(business);

        return AppointmentScheduleMapper.toResponse(saved);
    }

    public void delete(UUID publicId) {
        AppointmentSchedule schedule = appointmentScheduleRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException("Appointment schedule not found"));
        appointmentScheduleRepository.delete(schedule);
    }

    public AppointmentScheduleResponse update(UUID businessId, UUID publicId, AppointmentScheduleRequest request) {
        Business business = businessRepository.findByPublicId(businessId)
                .orElseThrow(() -> new NotFoundException("Business not found"));
        AppointmentSchedule schedule = appointmentScheduleRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException("Appointment schedule not found"));

        Serv service = servRepository.findByPublicId(request.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));

        User employee = null;
        if (request.getEmployeeId() != null) {
            employee = userRepository.findByPublicId(request.getEmployeeId())
                    .orElseThrow(() -> new NotFoundException("Employee not found"));
        }

        Long employeeId = employee != null ? employee.getId() : null;

        if (appointmentScheduleRepository.existsConflictingSchedule(
                business.getId(), request.getDayNumber(), request.getStartTime(), request.getEndTime(),
                employeeId, schedule.getId())) {
            throw new AppointmentScheduleAlreadyExistsException("A schedule already exists for this day, time and employee");
        }

        schedule.setDayNumber(request.getDayNumber());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setPrice(service.getPrice());
        schedule.setService(service);
        schedule.setEmployee(employee);

        return AppointmentScheduleMapper.toResponse(appointmentScheduleRepository.save(schedule));
    }
}