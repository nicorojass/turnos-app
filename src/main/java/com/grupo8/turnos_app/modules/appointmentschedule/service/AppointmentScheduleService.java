package com.grupo8.turnos_app.modules.appointmentschedule.service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.common.exception.NotFoundException;
import com.grupo8.turnos_app.common.security.BusinessAccessGuard;
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
    private final BusinessAccessGuard businessAccessGuard;
    private final BusinessRepository businessRepository;
    private final ServRepository servRepository;
    private final UserRepository userRepository;
    private final AgendaGeneratorService agendaGeneratorService;

    public List<AppointmentScheduleResponse> getByBusiness(UUID businessId) {
        Business business = businessRepository.findByPublicId(businessId)
                .orElseThrow(() -> new NotFoundException("Business not found"));
        businessAccessGuard.checkCanManage(business);
        return appointmentScheduleRepository.findByBusinessId(business.getId())
                .stream()
                .map(AppointmentScheduleMapper::toResponse)
                .toList();
    }

    public AppointmentScheduleResponse create(UUID businessId, AppointmentScheduleRequest request) {
        Business business = businessRepository.findByPublicId(businessId)
                .orElseThrow(() -> new NotFoundException("Negocio no encontrado"));
        businessAccessGuard.checkCanManage(business);

        Serv service = servRepository.findByPublicId(request.getServiceId())
                .filter(found -> found.getBusiness().getId().equals(business.getId()))
                .orElseThrow(() -> new NotFoundException("Servicio no encontrado"));

        validateRange(request, service);

        User employee = null;
        if (request.getEmployeeId() != null) {
            employee = userRepository.findByPublicId(request.getEmployeeId())
                    .filter(found -> worksFor(found, business))
                    .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));
        }

        Long employeeId = employee != null ? employee.getId() : null;

        if (appointmentScheduleRepository.existsConflictingSchedule(
                business.getId(), request.getDayNumber(), request.getStartTime(), request.getEndTime(),
                employeeId, null)) {
            throw new AppointmentScheduleAlreadyExistsException("Ya existe un horario para este día, hora y empleado");
        }

        AppointmentSchedule schedule = AppointmentScheduleMapper.toEntity(request);
        schedule.setPrice(service.getPrice());
        schedule.setBusiness(business);
        schedule.setService(service);
        schedule.setEmployee(employee);

        AppointmentSchedule saved = appointmentScheduleRepository.save(schedule);

        // with the automatic agenda off this does nothing and the entry only stays in the template
        agendaGeneratorService.regenerate(business);

        return AppointmentScheduleMapper.toResponse(saved);
    }

    // a template can only use employees of its own business
    private boolean worksFor(User employee, Business business) {
        return business.getEmployees().stream().anyMatch(e -> e.getId().equals(employee.getId()));
    }

    // each entry is one appointment, so it has to fit the service at least once.
    // for example a 45 min haircut doesn't fit between 12:00 and 12:30, but it does between 12:00 and 12:45.
    // used by create and update, before only create checked it.
    private void validateRange(AppointmentScheduleRequest request, Serv service) {
        long scheduleMinutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (scheduleMinutes < service.getDurationMinutes()) {
            throw new InvalidScheduleRangeException("El rango de tiempo debe ser al menos tan largo como la duración del servicio");
        }
    }

    public void delete(UUID publicId) {
        AppointmentSchedule schedule = appointmentScheduleRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException("Turno programado no encontrado"));
        businessAccessGuard.checkCanManage(schedule.getBusiness());
        Business business = schedule.getBusiness();
        appointmentScheduleRepository.delete(schedule);

        // the free slots this entry already generated go away too
        agendaGeneratorService.regenerate(business);
    }

    public AppointmentScheduleResponse update(UUID businessId, UUID publicId, AppointmentScheduleRequest request) {
        Business business = businessRepository.findByPublicId(businessId)
                .orElseThrow(() -> new NotFoundException("Negocio no encontrado"));
        businessAccessGuard.checkCanManage(business);
        AppointmentSchedule schedule = appointmentScheduleRepository.findByPublicId(publicId)
                .filter(found -> found.getBusiness().getId().equals(business.getId()))
                .orElseThrow(() -> new NotFoundException("Turno programado no encontrado"));

        Serv service = servRepository.findByPublicId(request.getServiceId())
                .filter(found -> found.getBusiness().getId().equals(business.getId()))
                .orElseThrow(() -> new NotFoundException("Servicio no encontrado"));

        validateRange(request, service);

        User employee = null;
        if (request.getEmployeeId() != null) {
            employee = userRepository.findByPublicId(request.getEmployeeId())
                    .filter(found -> worksFor(found, business))
                    .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));
        }

        Long employeeId = employee != null ? employee.getId() : null;

        if (appointmentScheduleRepository.existsConflictingSchedule(
                business.getId(), request.getDayNumber(), request.getStartTime(), request.getEndTime(),
                employeeId, schedule.getId())) {
            throw new AppointmentScheduleAlreadyExistsException("Ya existe un horario para este día, hora y empleado");
        }

        schedule.setDayNumber(request.getDayNumber());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setPrice(service.getPrice());
        schedule.setService(service);
        schedule.setEmployee(employee);

        AppointmentSchedule saved = appointmentScheduleRepository.save(schedule);

        // the free slots with the old times are deleted and created again with the new ones.
        // the ones somebody already booked keep the old time, those are never touched
        agendaGeneratorService.regenerate(business);

        return AppointmentScheduleMapper.toResponse(saved);
    }
}