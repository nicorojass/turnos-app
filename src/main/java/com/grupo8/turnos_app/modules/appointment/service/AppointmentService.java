package com.grupo8.turnos_app.modules.appointment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo8.turnos_app.common.enums.AppointmentStatus;
import com.grupo8.turnos_app.common.enums.DepositStatus;
import com.grupo8.turnos_app.common.exception.NotFoundException;
import com.grupo8.turnos_app.modules.appointment.dto.AppointmentResponse;
import com.grupo8.turnos_app.modules.appointment.dto.BookAppointmentRequest;
import com.grupo8.turnos_app.modules.appointment.entity.Appointment;
import com.grupo8.turnos_app.modules.appointment.exceptions.AppointmentNotAvailableException;
import com.grupo8.turnos_app.modules.appointment.exceptions.InvalidPriceException;
import com.grupo8.turnos_app.modules.appointment.exceptions.InvalidStatusException;
import com.grupo8.turnos_app.modules.appointment.exceptions.NoServiceException;
import com.grupo8.turnos_app.modules.appointment.mapper.AppointmentMapper;
import com.grupo8.turnos_app.modules.appointment.repository.AppointmentRepository;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;
import com.grupo8.turnos_app.modules.deposit.entity.Deposit;
import com.grupo8.turnos_app.modules.deposit.repository.DepositRepository;
import com.grupo8.turnos_app.modules.users.entities.User;
import com.grupo8.turnos_app.modules.users.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

  private final AppointmentRepository appointmentRepository;
  private final BusinessRepository businessRepository;
  private final DepositRepository depositRepository;
  private final UserRepository userRepository;

  // -------- QUERY SERVICES ------------

  // returns all appointments for a business, paginated and filterable by status
  public Page<AppointmentResponse> getAppointmentsByBusiness(
      UUID businessId,
      AppointmentStatus status,
      Pageable pageable) {

    Business business = businessRepository.findByPublicId(businessId)
        .orElseThrow(() -> new NotFoundException("Business not found"));

    Page<Appointment> page = (status != null)
        ? appointmentRepository.findByBusinessIdAndStatus(business.getId(), status, pageable)
        : appointmentRepository.findByBusinessId(business.getId(), pageable);

    return page.map(appointment -> AppointmentMapper.toResponse(appointment));
  }

  // returns today's appointments for the owner dashboard
  public List<AppointmentResponse> getTodayAppointments(UUID businessId) {
    Business business = businessRepository.findByPublicId(businessId)
        .orElseThrow(() -> new NotFoundException("Business not found"));
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);

    return appointmentRepository
        .findTodayAppointments(business.getId(), startOfDay, endOfDay)
        .stream()
        .map(appointment -> AppointmentMapper.toResponse(appointment))
        .collect(java.util.stream.Collectors.toList());
  }

  // returns future UNBOOKED slots for the public endpoint with optional filters
  public List<AppointmentResponse> getAvailableSlots(
      UUID businessId,
      Long serviceId,
      Long employeeId) {

    Business business = businessRepository.findByPublicId(businessId)
        .orElseThrow(() -> new NotFoundException("Business not found"));

    return appointmentRepository
        .findAvailableSlots(business.getId(), LocalDateTime.now(), serviceId, employeeId)
        .stream()
        .map(appointment -> AppointmentMapper.toResponse(appointment))
        .collect(java.util.stream.Collectors.toList());
  }

  // returns all appointments for authed client
  public List<AppointmentResponse> getMyAppointments(Long clientUserId) {
    return appointmentRepository
        .findByClientUserIdOrderByStartDatetimeDesc(clientUserId)
        .stream()
        .map(appointment -> AppointmentMapper.toResponse(appointment))
        .collect(java.util.stream.Collectors.toList());
  }

  // -------- SERVICES (BOOK, CANCEL, SUSPEND, DELETE) ------------
  // BOOK APPOINTMENT | /book
  // uses pessimistic lock to avoid double booking on concurrent requests

  @Transactional
  public AppointmentResponse bookAppointment(UUID publicId, BookAppointmentRequest request) {

    // step 1: resolver UUID al Long interno (sin lock)
    Appointment ref = appointmentRepository.findByPublicId(publicId)
        .orElseThrow(() -> new NotFoundException("Error al reservar: turno no encontrado."));

    // step 2: re-fetch con lock pesimista usando el PK interno
    Appointment appointment = appointmentRepository.findByIdWithLock(ref.getId())
        .orElseThrow(() -> new NotFoundException("Error al reservar: turno no encontrado."));

    // cjeck if slot is still available
    if (appointment.getStatus() != AppointmentStatus.UNBOOKED) {
      throw new AppointmentNotAvailableException(
          "El turno ya no está disponible.");
    }

    // load the registered client if id was provided
    User clientUser = null;
    if (request.getClientUserId() != null) {
      clientUser = userRepository.findById(request.getClientUserId())
          .orElseThrow(() -> new NotFoundException("Usuario no encontrado."));
    }

    // set appointment's client data
    appointment.setClientName(request.getClientName());
    appointment.setClientEmail(request.getClientEmail());
    appointment.setClientPhone(request.getClientPhone());
    appointment.setClientUser(clientUser);
    appointment.setStatus(AppointmentStatus.AWAITING_PAYMENT);

    // validate that appointment has price and service set
    if (appointment.getPrice() == null || appointment.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidPriceException("Error al reservar: El precio del turno es inválido.");
    }

    if (appointment.getService() == null) {
      throw new NoServiceException("Error al reservar: El turno no tiene un servicio asociado.");
    }

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

    // create deposit with pending status
    Deposit deposit = Deposit.builder()
        .appointment(appointment)
        .amount(depositAmount)
        .status(DepositStatus.PENDING)
        .build();

    appointmentRepository.save(appointment);
    depositRepository.save(deposit);

    // link deposit to appointment so it shows up in the response
    appointment.setDeposit(deposit);

    return AppointmentMapper.toResponse(appointment);
  }
  // COMPLETE APPOINTMENT | /complete
  
  public AppointmentResponse completeAppointment(UUID publicId) {
    Appointment appointment = appointmentRepository.findByPublicId(publicId)
            .orElseThrow(() -> new NotFoundException("Appointment not found"));

    if (appointment.getStatus() != AppointmentStatus.BOOKED)
        throw new InvalidStatusException("Only BOOKED appointments can be marked as completed");

    appointment.setStatus(AppointmentStatus.COMPLETED);
    return AppointmentMapper.toResponse(appointmentRepository.save(appointment));
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
  public AppointmentResponse cancelAppointment(UUID publicId) {

    Appointment appointment = appointmentRepository.findByPublicId(publicId)
        .orElseThrow(() -> new NotFoundException("Error al cancelar el turno: turno no encontrado."));

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

  // (requested by business owner)
  // SUSPEND APPOINTMENT | /suspend
  // business owner cancells booked appt: deposit is always refunded

  @Transactional
  public AppointmentResponse suspendAppointment(UUID publicId) {

    Appointment appointment = appointmentRepository.findByPublicId(publicId)
        .orElseThrow(() -> new NotFoundException("Error al suspender el turno: turno no encontrado."));

    if (appointment.getStatus() != AppointmentStatus.BOOKED) {
      throw new InvalidStatusException(
          "Error al suspender el turno: solo los turnos reservados pueden ser suspendidos.");
    }

    Deposit deposit = depositRepository.findByAppointmentId(appointment.getId())
        .orElseThrow(() -> new NotFoundException("Error al suspender el turno: seña no encontrada para este turno."));

    deposit.setStatus(DepositStatus.REFUNDED);
    appointment.setStatus(AppointmentStatus.SUSPENDED);

    depositRepository.save(deposit);
    appointmentRepository.save(appointment);
    appointment.setDeposit(deposit);

    return AppointmentMapper.toResponse(appointment);
  }

  // (requested by business owner)
  // DELETE APPOINTMENT
  // IMPORTANT || only UNBOOKED appts with no deposit can be deleted

  @Transactional
  public void deleteAppointment(UUID publicId) {

    Appointment appointment = appointmentRepository.findByPublicId(publicId)
        .orElseThrow(() -> new NotFoundException("Error al eliminar el turno: turno no encontrado."));

    // check status so only unbooked appts are able to be hard deleted
    if (appointment.getStatus() != AppointmentStatus.UNBOOKED) {
      throw new InvalidStatusException(
          "Error al eliminar el turno: solo los turnos sin reservar pueden ser eliminados.");
    }

    appointmentRepository.delete(appointment);
  }
}