package com.grupo8.turnos_app.modules.appointment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo8.turnos_app.common.enums.AppointmentStatus;
import com.grupo8.turnos_app.common.enums.DepositStatus;
import com.grupo8.turnos_app.common.enums.RoleName;
import com.grupo8.turnos_app.common.exception.ForbiddenOperationException;
import com.grupo8.turnos_app.common.exception.NotFoundException;
import com.grupo8.turnos_app.common.security.BusinessAccessGuard;
import com.grupo8.turnos_app.modules.appointment.dto.AppointmentResponse;
import com.grupo8.turnos_app.modules.appointment.dto.BookAppointmentRequest;
import com.grupo8.turnos_app.modules.appointment.dto.CreateAppointmentRequest;
import com.grupo8.turnos_app.modules.appointment.entity.Appointment;
import com.grupo8.turnos_app.modules.appointment.exceptions.AppointmentConflictException;
import com.grupo8.turnos_app.modules.appointment.exceptions.AppointmentNotAvailableException;
import com.grupo8.turnos_app.modules.appointment.exceptions.InvalidAppointmentException;
import com.grupo8.turnos_app.modules.appointment.exceptions.InvalidPriceException;
import com.grupo8.turnos_app.modules.appointment.exceptions.InvalidStatusException;
import com.grupo8.turnos_app.modules.appointment.exceptions.NoServiceException;
import com.grupo8.turnos_app.modules.appointment.mapper.AppointmentMapper;
import com.grupo8.turnos_app.modules.appointment.repository.AppointmentRepository;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;
import com.grupo8.turnos_app.modules.deposit.entity.Deposit;
import com.grupo8.turnos_app.modules.deposit.repository.DepositRepository;
import com.grupo8.turnos_app.modules.service.entity.Serv;
import com.grupo8.turnos_app.modules.service.repository.ServRepository;
import com.grupo8.turnos_app.modules.users.entities.User;
import com.grupo8.turnos_app.modules.users.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

  private final AppointmentRepository appointmentRepository;
  private final BusinessAccessGuard businessAccessGuard;
  private final BusinessRepository businessRepository;
  private final DepositRepository depositRepository;
  private final UserRepository userRepository;
  private final ServRepository serviceRepository;
  

  // -------- QUERY SERVICES ------------

  // returns all appointments for a business, paginated and filterable by status
  @Transactional(readOnly = true)
  public Page<AppointmentResponse> getAppointmentsByBusiness(
      UUID businessId,
      AppointmentStatus status,
      Pageable pageable) {

    Business business = businessRepository.findByPublicId(businessId)
        .orElseThrow(() -> new NotFoundException("Business not found"));
    businessAccessGuard.checkCanManage(business);

    Page<Appointment> page = (status != null)
        ? appointmentRepository.findByBusinessIdAndStatus(business.getId(), status, pageable)
        : appointmentRepository.findByBusinessId(business.getId(), pageable);

    return page.map(appointment -> AppointmentMapper.toResponse(appointment));
  }

  // returns today's appointments for the owner dashboard
  @Transactional(readOnly = true)
  public List<AppointmentResponse> getTodayAppointments(UUID businessId) {
    Business business = businessRepository.findByPublicId(businessId)
        .orElseThrow(() -> new NotFoundException("Business not found"));
    businessAccessGuard.checkCanManage(business);
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);

    return appointmentRepository
        .findTodayAppointments(business.getId(), startOfDay, endOfDay)
        .stream()
        .map(appointment -> AppointmentMapper.toResponse(appointment))
        .collect(java.util.stream.Collectors.toList());
  }

  // returns future UNBOOKED slots for the public endpoint with optional filters
  @Transactional(readOnly = true)
  public List<AppointmentResponse> getAvailableSlots(
    UUID businessId,
    UUID serviceId,
    UUID employeeId) {

  Business business = businessRepository.findByPublicId(businessId)
      .orElseThrow(() -> new NotFoundException("Business not found"));

  Long serviceInternalId = null;
  if (serviceId != null) {
      serviceInternalId = serviceRepository.findByPublicId(serviceId)
              .orElseThrow(() -> new NotFoundException("Service not found"))
              .getId();
  }

  Long employeeInternalId = null;
  if (employeeId != null) {
      employeeInternalId = userRepository.findByPublicId(employeeId)
              .orElseThrow(() -> new NotFoundException("Employee not found"))
              .getId();
  }

  return appointmentRepository
      .findAvailableSlots(business.getId(), LocalDateTime.now(), serviceInternalId, employeeInternalId)
      .stream()
      .map(AppointmentMapper::toResponse)
      .collect(Collectors.toList());
}

  // returns all appointments for authed client
  @Transactional(readOnly = true)
  public List<AppointmentResponse> getMyAppointments(Long clientUserId) {
    return appointmentRepository
        .findByClientUserIdOrderByStartDatetimeDesc(clientUserId)
        .stream()
        .map(appointment -> AppointmentMapper.toResponse(appointment))
        .collect(java.util.stream.Collectors.toList());
  }

  // -------- SERVICES (CREATE, BOOK, CANCEL, SUSPEND, DELETE) ------------
  // CREATE APPOINTMENT | POST /businesses/{id}/appointments
  // the owner creates one appointment on a specific day and time from the agenda in the panel,
  // outside the weekly template. the end comes from the service duration: a 45 min haircut
  // starting 2026-11-16 10:00 ends at 10:45.
  // without client data it's created free (UNBOOKED) and shows up on the public page like any
  // other slot. with client data it's booked right away (BOOKED) without a deposit, because the
  // owner is loading an appointment somebody asked for in person or over the phone.
  // it's marked as manual so the agenda regeneration doesn't delete it (see deleteFutureFreeSlots)

  @Transactional
  public AppointmentResponse createAppointment(UUID businessId, CreateAppointmentRequest request) {

    Business business = businessRepository.findByPublicId(businessId)
        .orElseThrow(() -> new NotFoundException("Negocio no encontrado"));

    // the service has to be from this business and not deleted, otherwise somebody could create
    // appointments with another business's service by sending its id
    Serv service = serviceRepository.findByPublicId(request.getServiceId())
        .filter(s -> s.getBusiness().getId().equals(business.getId()))
        .filter(s -> !Boolean.TRUE.equals(s.getDeleted()))
        .orElseThrow(() -> new NotFoundException("Servicio no encontrado"));

    // same thing with the employee: it has to be part of this business's team. the team is the
    // business's employees list (user.getBusiness() is the business a user owns, not where they work)
    User employee = null;
    if (request.getEmployeeId() != null) {
      employee = userRepository.findByPublicId(request.getEmployeeId())
          .filter(u -> business.getEmployees().stream().anyMatch(e -> e.getId().equals(u.getId())))
          .orElseThrow(() -> new NotFoundException("Profesional no encontrado"));
    }

    if (service.getDurationMinutes() == null || service.getDurationMinutes() <= 0) {
      throw new InvalidAppointmentException("El servicio no tiene una duración cargada.");
    }

    LocalDateTime start = request.getStartDatetime();
    if (!start.isAfter(LocalDateTime.now())) {
      throw new InvalidAppointmentException("No se puede crear un turno en un horario que ya pasó.");
    }
    LocalDateTime end = start.plusMinutes(service.getDurationMinutes());

    Long employeeId = employee != null ? employee.getId() : null;
    if (appointmentRepository.existsOverlappingInBusiness(business.getId(), employeeId, start, end, false)) {
      throw new AppointmentConflictException(employee != null
          ? "Ese profesional ya tiene un turno que se superpone con este horario."
          : "Ya hay un turno sin profesional asignado que se superpone con este horario.");
    }

    // blank texts count as not sent, so an empty phone field from the form doesn't book anything
    String clientName = blankToNull(request.getClientName());
    String clientEmail = blankToNull(request.getClientEmail());
    String clientPhone = blankToNull(request.getClientPhone());
    boolean forClient = clientName != null || clientEmail != null || clientPhone != null;
    if (forClient && clientName == null) {
      throw new InvalidAppointmentException("Para cargar el turno a nombre de un cliente falta su nombre.");
    }

    Appointment appointment = Appointment.builder()
        .startDatetime(start)
        .endDatetime(end)
        .status(forClient ? AppointmentStatus.BOOKED : AppointmentStatus.UNBOOKED)
        .clientName(clientName)
        .clientEmail(clientEmail)
        .clientPhone(clientPhone)
        .reservedAt(forClient ? LocalDateTime.now() : null)
        .price(service.getPrice())
        .business(business)
        .service(service)
        .employee(employee)
        .manual(true)
        .build();

    return AppointmentMapper.toResponse(appointmentRepository.save(appointment));
  }

  // trims a text and turns it into null if nothing is left: "  " gives null and " Ana " gives "Ana"
  private String blankToNull(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  // BOOK APPOINTMENT | /book
  // uses pessimistic lock to avoid double booking on concurrent requests

  @Transactional
public AppointmentResponse bookAppointment(UUID publicId, BookAppointmentRequest request, Authentication authentication) {

    // step 1: resolve the UUID to the internal id only. loading the whole entity here made step 2 return
    // that same cached instance, so a request that waited for the lock still saw the slot as free and
    // only the unique constraint of the deposit stopped it
    Long appointmentId = appointmentRepository.findIdByPublicId(publicId)
        .orElseThrow(() -> new NotFoundException("Error al reservar: turno no encontrado."));

    // step 2: fetch with a pessimistic lock, so concurrent requests read it one after the other
    Appointment appointment = appointmentRepository.findByIdWithLock(appointmentId)
        .orElseThrow(() -> new NotFoundException("Error al reservar: turno no encontrado."));

    // check if slot is still available
    if (appointment.getStatus() != AppointmentStatus.UNBOOKED) {
        throw new AppointmentNotAvailableException("El turno ya no está disponible.");
    }

    // check if business is active
    if (Boolean.TRUE.equals(appointment.getBusiness().getDeleted())) {
      throw new NotFoundException("El negocio no está disponible.");
    }

    // validate that appointment has price and service set
    if (appointment.getPrice() == null || appointment.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
        throw new InvalidPriceException("Error al reservar: El precio del turno es inválido.");
    }

    if (appointment.getService() == null) {
        throw new NoServiceException("Error al reservar: El turno no tiene un servicio asociado.");
    }

    User clientUser = null;
    if (authentication != null && authentication.isAuthenticated()) {
        clientUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado."));
        appointment.setClientName(clientUser.getName());
        appointment.setClientEmail(clientUser.getEmail());
        appointment.setClientPhone(request.getClientPhone());
    } else {
        if (request.getClientName() == null || request.getClientEmail() == null) {
            throw new IllegalArgumentException("Ingresa tu nombre y email para reservar el turno.");
        }
        appointment.setClientName(request.getClientName());
        appointment.setClientEmail(request.getClientEmail());
        appointment.setClientPhone(request.getClientPhone());
    }
    appointment.setClientUser(clientUser);

    // check for overlapping appointments
    if (clientUser != null && appointmentRepository.hasOverlappingAppointment(
            clientUser.getId(), appointment.getStartDatetime(), appointment.getEndDatetime())) {
        throw new AppointmentConflictException("Ya tenés un turno reservado en este horario.");
    }

    // anti-spam: no se puede reservar si ya tenés un turno pendiente de pago
  if (clientUser != null) {
    if (appointmentRepository.hasUnpaidByUser(clientUser.getId())) {
        throw new AppointmentConflictException("Ya tenés un turno pendiente de pago. Completalo antes de reservar otro.");
    }
  } else {
    if (appointmentRepository.hasUnpaidByEmail(request.getClientEmail())) {
        throw new AppointmentConflictException("Ya tenés un turno pendiente de pago. Completalo antes de reservar otro.");
    }
  }

    appointment.setStatus(AppointmentStatus.AWAITING_PAYMENT);
    appointment.setReservedAt(LocalDateTime.now());

    // validate outofrange / null percentage: default deposit is set to 30%
    BigDecimal depositPercentage = appointment.getService().getDepositPorcentage();
    if (depositPercentage == null
            || depositPercentage.compareTo(BigDecimal.ZERO) <= 0
            || depositPercentage.compareTo(new BigDecimal("100")) > 0) {
        depositPercentage = new BigDecimal("30.00");
    }

    // calculate deposit amount: price * percentage / 100
    BigDecimal depositAmount = appointment.getPrice()
            .multiply(depositPercentage)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

    // reuse existing deposit if slot was previously cancelled, otherwise create new
    Deposit deposit = depositRepository.findByAppointmentId(appointment.getId())
            .map(existing -> {
                existing.setAmount(depositAmount);
                existing.setStatus(DepositStatus.PENDING);
                existing.setPaidAt(null);
                return existing;
            })
            .orElseGet(() -> Deposit.builder()
                    .appointment(appointment)
                    .amount(depositAmount)
                    .status(DepositStatus.PENDING)
                    .build());

    appointmentRepository.save(appointment);
    depositRepository.save(deposit);

    appointment.setDeposit(deposit);

    return AppointmentMapper.toResponse(appointment);
}

  // CONFIRM DEPOSIT PAYMENT | /pay-deposit

  @Transactional
  public AppointmentResponse confirmDepositPayment(UUID publicId) {

    Appointment appointment = appointmentRepository.findByPublicId(publicId)
        .orElseThrow(() -> new NotFoundException("Error al confirmar el pago: turno no encontrado."));

    // Payment is only allowed when the appointment is waiting for it
    if (appointment.getStatus() != AppointmentStatus.AWAITING_PAYMENT) {
      throw new InvalidStatusException(
          "Error al confirmar el pago: el turno no está esperando un pago.");
    }

    Deposit deposit = depositRepository.findByAppointmentId(appointment.getId())
        .orElseThrow(() -> new NotFoundException("Error al confirmar el pago: seña no encontrada para este turno"));

    if (deposit.getStatus() != DepositStatus.PENDING) {
      throw new InvalidStatusException(
          "Error al confirmar el pago: la seña no está en estado pendiente.");
    }

    // confirm payment: appointment status = BOOKED
    deposit.setStatus(DepositStatus.PAID);
    deposit.setPaidAt(LocalDateTime.now());
    appointment.setStatus(AppointmentStatus.BOOKED);

    depositRepository.save(deposit);
    appointmentRepository.save(appointment);
    appointment.setDeposit(deposit);

    return AppointmentMapper.toResponse(appointment);
  }

  // (requested by client user)
  // CANCEL APPOINTMENT | /cancel
  // >= 24hs till appt makes deposit REFUNDED | < 24hs till appt makes deposit
  // FORFEITED

  @Transactional
  public AppointmentResponse cancelAppointment(UUID publicId, User currentUser) {

    Appointment appointment = appointmentRepository.findByPublicId(publicId)
        .orElseThrow(() -> new NotFoundException("Error al cancelar el turno: turno no encontrado."));

    // only the client who booked it can cancel; the owner suspends or deletes instead
    if (appointment.getClientUser() == null || !appointment.getClientUser().getId().equals(currentUser.getId())) {
      throw new ForbiddenOperationException("No tenés permiso para cancelar este turno");
    }

    // only BOOKED or AWAITING_PAYMENT appointments can be cancelled
    if (appointment.getStatus() != AppointmentStatus.BOOKED
        && appointment.getStatus() != AppointmentStatus.AWAITING_PAYMENT) {
      throw new InvalidStatusException(
          "Error al cancelar el turno: el turno no puede ser cancelado en su estado actual.");
    }

    Deposit deposit = depositRepository.findByAppointmentId(appointment.getId())
        .orElseThrow(() -> new NotFoundException("Error al cancelar el turno: seña no encontrada para este turno"));

    // calculate hours remaining until appointment start
    long hoursUntilAppointment = ChronoUnit.HOURS.between(
        LocalDateTime.now(),
        appointment.getStartDatetime());

    // if still awaiting payment, just release the slot — no need to cancel
    if (appointment.getStatus() == AppointmentStatus.AWAITING_PAYMENT) {
      appointment.setStatus(AppointmentStatus.UNBOOKED);
      appointment.setClientName(null);
      appointment.setClientEmail(null);
      appointment.setClientPhone(null);
      appointment.setClientUser(null);
      appointment.setReservedAt(null);
      deposit.setStatus(DepositStatus.CANCELED);
      depositRepository.save(deposit);
      appointmentRepository.save(appointment);
      appointment.setDeposit(deposit);
      return AppointmentMapper.toResponse(appointment);
    }

    if (hoursUntilAppointment >= 24) {
      // early cancelation: deposit is returned to the client
      deposit.setStatus(DepositStatus.REFUNDED);
    } else {
      // late cancellation: business keeps the deposit
      deposit.setStatus(DepositStatus.FORFEITED);
    }

    appointment.setStatus(AppointmentStatus.CANCELLED);

    depositRepository.save(deposit);
    appointmentRepository.save(appointment);
    appointment.setDeposit(deposit);

    return AppointmentMapper.toResponse(appointment);
  }

  // GET APPOINTMENT | /appointments/{id} (public)
  // the payment link (/reserva/:id) is opened without a session, even on another device, so anyone with
  // the id can read the appointment. the client contact data only goes to the client who booked it,
  // the business owner or an admin; everyone else gets it as null.
  @Transactional(readOnly = true)
  public AppointmentResponse getAppointment(UUID publicId, User currentUser) {
    Appointment appointment = appointmentRepository.findByPublicId(publicId)
        .orElseThrow(() -> new NotFoundException("Turno no encontrado"));

    AppointmentResponse response = AppointmentMapper.toResponse(appointment);
    if (!canSeeClientData(appointment, currentUser)) {
      response.setClientName(null);
      response.setClientEmail(null);
      response.setClientPhone(null);
    }
    return response;
  }

  private boolean canSeeClientData(Appointment appointment, User currentUser) {
    if (currentUser == null)
      return false;
    boolean isClient = appointment.getClientUser() != null
        && appointment.getClientUser().getId().equals(currentUser.getId());
    boolean isOwner = appointment.getBusiness().getOwner() != null
        && appointment.getBusiness().getOwner().getId().equals(currentUser.getId());
    boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ADMIN);
    return isClient || isOwner || isAdmin;
  }

  // (requested by business owner)
  // SUSPEND APPOINTMENT | /suspend
  // business owner cancells booked appt: deposit is always refunded

  @Transactional
  public AppointmentResponse suspendAppointment(UUID publicId) {

    Appointment appointment = appointmentRepository.findByPublicId(publicId)
        .orElseThrow(() -> new NotFoundException("Error al suspender el turno: turno no encontrado."));
    businessAccessGuard.checkCanManage(appointment.getBusiness());

    if (appointment.getStatus() != AppointmentStatus.BOOKED) {
      throw new InvalidStatusException(
          "Error al suspender el turno: solo los turnos reservados pueden ser suspendidos.");
    }

    // an appointment the owner booked by hand for a client (from the agenda) has no deposit, so
    // there's nothing to refund and it just gets suspended. before this it answered 404
    depositRepository.findByAppointmentId(appointment.getId()).ifPresent(deposit -> {
      deposit.setStatus(DepositStatus.REFUNDED);
      depositRepository.save(deposit);
      appointment.setDeposit(deposit);
    });

    appointment.setStatus(AppointmentStatus.SUSPENDED);
    appointmentRepository.save(appointment);

    return AppointmentMapper.toResponse(appointment);
  }

  // (requested by business owner)
  // DELETE APPOINTMENT
  // IMPORTANT || only UNBOOKED appts with no deposit can be deleted

  @Transactional
  public void deleteAppointment(UUID publicId) {

    Appointment appointment = appointmentRepository.findByPublicId(publicId)
        .orElseThrow(() -> new NotFoundException("Error al eliminar el turno: turno no encontrado."));
    businessAccessGuard.checkCanManage(appointment.getBusiness());

    // check status so only unbooked appts are able to be hard deleted
    if (appointment.getStatus() != AppointmentStatus.UNBOOKED) {
      throw new InvalidStatusException(
          "Error al eliminar el turno: solo los turnos sin reservar pueden ser eliminados.");
    }

    appointmentRepository.delete(appointment);
  }
}