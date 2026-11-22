package com.grupo8.turnos_app.modules.service.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo8.turnos_app.common.exception.NotFoundException;
import com.grupo8.turnos_app.common.security.BusinessAccessGuard;
import com.grupo8.turnos_app.modules.appointment.entity.Appointment;
import com.grupo8.turnos_app.modules.appointment.exceptions.AppointmentConflictException;
import com.grupo8.turnos_app.modules.appointment.repository.AppointmentRepository;
import com.grupo8.turnos_app.modules.appointmentschedule.exception.InvalidScheduleRangeException;
import com.grupo8.turnos_app.modules.appointmentschedule.repository.AppointmentScheduleRepository;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessNotFoundException;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;
import com.grupo8.turnos_app.modules.service.dto.ServRequest;
import com.grupo8.turnos_app.modules.service.dto.ServResponse;
import com.grupo8.turnos_app.modules.service.entity.Serv;
import com.grupo8.turnos_app.modules.service.exception.ServiceAlreadyExistsException;
import com.grupo8.turnos_app.modules.service.mapper.ServMapper;
import com.grupo8.turnos_app.modules.service.repository.ServRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServService {
    
    private final ServRepository serviceRepository;
    private final BusinessRepository businessRepository;
    private final BusinessAccessGuard businessAccessGuard;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentScheduleRepository appointmentScheduleRepository;

    public ServResponse createService(UUID businessId, ServRequest request) {

    Business business = businessRepository.findByPublicId(businessId)
        .orElseThrow(() -> new BusinessNotFoundException("Business not found"));
    businessAccessGuard.checkCanManage(business);

    Serv service = ServMapper.toEntity(request);
    service.setBusiness(business);
    service.setDeleted(false);

    if (serviceRepository.existsByBusinessIdAndNameAndDurationMinutesAndPriceAndDepositPorcentage(
        business.getId(), request.getName(), request.getDurationMinutes(), request.getPrice(), request.getDepositPorcentage())) {
    throw new ServiceAlreadyExistsException("An identical service already exists for this business");
}
    return ServMapper.toResponse(serviceRepository.save(service));
}

    public List<ServResponse> getServicesByBusinessId(UUID businessId) {
        Business business = businessRepository.findByPublicId(businessId)
            .filter(found -> !Boolean.TRUE.equals(found.getDeleted()))
            .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));
        List<Serv> services = serviceRepository.findByBusinessIdAndDeletedFalse(business.getId());
        return services.stream()
                .map(ServMapper::toResponse)
                .toList();
    }

    @Transactional
    public ServResponse editService(UUID publicId, ServRequest request) {
    Serv service = serviceRepository.findByPublicId(publicId)
        .orElseThrow(() -> new NotFoundException("Service not found"));
    businessAccessGuard.checkCanManage(service.getBusiness());

    if (serviceRepository.existsByBusinessIdAndNameAndDurationMinutesAndPriceAndDepositPorcentage(
            service.getBusiness().getId(), request.getName(), request.getDurationMinutes(),
            request.getPrice(), request.getDepositPorcentage())) {
        throw new ServiceAlreadyExistsException("An identical service already exists for this business");
    }

    if (!request.getDurationMinutes().equals(service.getDurationMinutes()))
        applyNewDuration(service, request.getDurationMinutes());

    service.setName(request.getName());
    service.setPrice(request.getPrice());
    service.setDurationMinutes(request.getDurationMinutes());
    service.setDepositPorcentage(request.getDepositPorcentage());
    Serv saved = serviceRepository.save(service);

    // the price is copied to the templates and to every slot when they are created, so a new price
    // has to reach them too. booked appointments keep the price they were booked with.
    appointmentScheduleRepository.updatePriceByServiceId(saved.getId(), saved.getPrice());
    appointmentRepository.updateFutureFreeSlotsPrice(saved.getId(), saved.getPrice(), LocalDateTime.now());

    return ServMapper.toResponse(saved);
}


    // template slots take their times from the template, so a longer service only has to still fit in
    // every template. manual free appointments (created from the agenda) take the end from the
    // duration, so their end moves, as long as it doesn't land on another appointment of the same person
    private void applyNewDuration(Serv service, int newDuration) {
        boolean templateTooShort = appointmentScheduleRepository.findByServiceId(service.getId()).stream()
                .anyMatch(schedule -> Duration.between(schedule.getStartTime(), schedule.getEndTime()).toMinutes() < newDuration);
        if (templateTooShort)
            throw new InvalidScheduleRangeException(
                    "Hay plantillas de este servicio más cortas que la nueva duración. Ajustalas antes de cambiarla");

        for (Appointment appointment : appointmentRepository.findFutureFreeManualByService(service.getId(), LocalDateTime.now())) {
            LocalDateTime newEnd = appointment.getStartDatetime().plusMinutes(newDuration);
            Long employeeId = appointment.getEmployee() != null ? appointment.getEmployee().getId() : null;
            if (appointmentRepository.existsOverlappingExcluding(service.getBusiness().getId(), employeeId,
                    appointment.getStartDatetime(), newEnd, appointment.getId()))
                throw new AppointmentConflictException("Con la nueva duración, el turno suelto del "
                        + appointment.getStartDatetime().toLocalDate() + " a las " + appointment.getStartDatetime().toLocalTime()
                        + " se superpondría con otro turno. Movelo o borralo antes de cambiarla");
            appointment.setEndDatetime(newEnd);
        }
    }

    @Transactional
    public void deleteService(UUID publicId) {
        Serv service = serviceRepository.findByPublicId(publicId).orElseThrow(() -> new NotFoundException("Service not found"));
        businessAccessGuard.checkCanManage(service.getBusiness());
        service.setDeleted(true);
        serviceRepository.save(service);

        // the service stops being offered: its templates go away and so do its future free slots.
        // booked appointments stay, the client still has to be attended.
        appointmentScheduleRepository.deleteByServiceId(service.getId());
        appointmentRepository.deleteFutureFreeSlotsByService(service.getId(), LocalDateTime.now());
    }
}