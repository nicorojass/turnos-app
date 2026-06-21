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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo8.turnos_app.common.enums.AppointmentStatus;
import com.grupo8.turnos_app.common.enums.DepositStatus;
import com.grupo8.turnos_app.common.exception.NotFoundException;
import com.grupo8.turnos_app.modules.appointment.dto.AppointmentResponse;
import com.grupo8.turnos_app.modules.appointment.dto.BookAppointmentRequest;
import com.grupo8.turnos_app.modules.appointment.entity.Appointment;
import com.grupo8.turnos_app.modules.appointment.exceptions.AppointmentConflictException;
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
        .orElseThrow(() -> new NotFoundException("We couldn't find the business you're looking for."));

    Page<Appointment> page = (status != null)
        ? appointmentRepository.findByBusinessIdAndStatus(business.getId(), status, pageable)
        : appointmentRepository.findByBusinessId(business.getId(), pageable);

    return page.map(appointment -> AppointmentMapper.toResponse(appointment));
  }

  // returns today's appointments for the owner dashboard
  public List<AppointmentResponse> getTodayAppointments(UUID businessId) {
    Business business = businessRepository.findByPublicId(businessId)
        .orElseThrow(() -> new NotFoundException("We couldn't find the business you're looking for."));
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
        .orElseThrow(() -> new NotFoundException("We couldn't find the business you're looking for."));

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
public AppointmentResponse bookAppointment(UUID publicId, BookAppointmentRequest request, Authentication authentication) {

    // step 1: resolver UUID al Long interno (sin lock)
    Appointment ref = appointmentRepository.findByPublicId(publicId)
        .orElseThrow(() -> new NotFoundException("We couldn't find the appointment you're trying to book. It may have been removed."));

    // step 2: re-fetch con lock pesimista usando el PK interno
    Appointment appointment = appointmentRepository.findByIdWithLock(ref.getId())
        .orElseThrow(() -> new NotFoundException("We couldn't find the appointment you're trying to book. It may have been removed."));

    // check if slot is still available
    if (appointment.getStatus() != AppointmentStatus.UNBOOKED) {
        throw new AppointmentNotAvailableException("This appointment slot is no longer available. Please choose a different one.");
    }

    // check if business is active
    if (Boolean.TRUE.equals(appointment.getBusiness().getDeleted())) {
      throw new NotFoundException("This business is currently unavailable. Please try again later.");
    }

    // validate that appointment has price and service set
    if (appointment.getPrice() == null || appointment.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
        throw new InvalidPriceException("This appointment can't be booked because it has an invalid price. Please contact the business for assistance.");
    }

    if (appointment.getService() == null) {
        throw new NoServiceException("This appointment can't be booked because it has no service assigned. Please contact the business for assistance.");
    }

    User clientUser = null;
    if (authentication != null && authentication.isAuthenticated()) {
        clientUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("We couldn't find your account. Please log in again."));
        appointment.setClientName(clientUser.getName());
        appointment.setClientEmail(clientUser.getEmail());
    } else {
        if (request.getClientName() == null || request.getClientEmail() == null) {
            throw new IllegalArgumentException("Please provide your name and email address to complete the booking.");
        }
        appointment.setClientName(request.getClientName());
        appointment.setClientEmail(request.getClientEmail());
        appointment.setClientPhone(request.getClientPhone());
    }
    appointment.setClientUser(clientUser);

    // check for overlapping appointments
    if (clientUser != null && appointmentRepository.hasOverlappingAppointment(
            clientUser.getId(), appointment.getStartDatetime(), appointment.getEndDatetime())) {
        throw new AppointmentConflictException("You already have an appointment booked during this time slot. Please choose a different one.");
    }

    // anti-spam: no se puede reservar si ya tenés un turno pendiente de pago
  if (clientUser != null) {
    if (appointmentRepository.hasUnpaidByUser(clientUser.getId())) {
        throw new AppointmentConflictException("You have an appointment awaiting payment. Please complete that payment before booking a new one.");
    }
  } else {
    if (appointmentRepository.hasUnpaidByEmail(request.getClientEmail())) {
        throw new AppointmentConflictException("You have an appointment awaiting payment. Please complete that payment before booking a new one.");
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

    // create deposit with pending status
    Deposit deposit = Deposit.builder()
            .appointment(appointment)
            .amount(depositAmount)
            .status(DepositStatus.PENDING)
            .build();

    appointmentRepository.save(appointment);
    depositRepository.save(deposit);

    appointment.setDeposit(deposit);

    return AppointmentMapper.toResponse(appointment);
}

  // CONFIRM DEPOSIT PAYMENT | /pay-deposit

  @Transactional
  public AppointmentResponse confirmDepositPayment(UUID publicId) {

    Appointment appointment = appointmentRepository.findByPublicId(publicId)
        .orElseThrow(() -> new NotFoundException("We couldn't find the appointment you're trying to pay for. It may have been removed."));

    // Payment is only allowed when the appointment is waiting for it
    if (appointment.getStatus() != AppointmentStatus.AWAITING_PAYMENT) {
      throw new InvalidStatusException(
          "This appointment isn't waiting for a payment right now. Its current status doesn't allow payment confirmation.");
    }

    Deposit deposit = depositRepository.findByAppointmentId(appointment.getId())
        .orElseThrow(() -> new NotFoundException("We couldn't find the deposit associated with this appointment. Please contact support."));

    if (deposit.getStatus() != DepositStatus.PENDING) {
      throw new InvalidStatusException(
          "This deposit has already been processed and can't be confirmed again.");
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
        .orElseThrow(() -> new NotFoundException("We couldn't find the appointment you're trying to cancel. It may have already been removed."));

    // only BOOKED or AWAITING_PAYMENT appointments can be cancelled
    if (appointment.getStatus() != AppointmentStatus.BOOKED
        && appointment.getStatus() != AppointmentStatus.AWAITING_PAYMENT) {
      throw new InvalidStatusException(
          "This appointment can't be cancelled in its current state. Only booked or pending-payment appointments can be cancelled.");
    }

    Deposit deposit = depositRepository.findByAppointmentId(appointment.getId())
        .orElseThrow(() -> new NotFoundException("We couldn't find the deposit associated with this appointment. Please contact support."));

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

  // (requested by business owner)
  // SUSPEND APPOINTMENT | /suspend
  // business owner cancells booked appt: deposit is always refunded

  @Transactional
  public AppointmentResponse suspendAppointment(UUID publicId) {

    Appointment appointment = appointmentRepository.findByPublicId(publicId)
        .orElseThrow(() -> new NotFoundException("We couldn't find the appointment you're trying to suspend. It may have been removed."));

    if (appointment.getStatus() != AppointmentStatus.BOOKED) {
      throw new InvalidStatusException(
          "Only confirmed (booked) appointments can be suspended. This appointment is not in a bookable state.");
    }

    Deposit deposit = depositRepository.findByAppointmentId(appointment.getId())
        .orElseThrow(() -> new NotFoundException("We couldn't find the deposit associated with this appointment. Please contact support."));

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
        .orElseThrow(() -> new NotFoundException("We couldn't find the appointment you're trying to delete. It may have already been removed."));

    // check status so only unbooked appts are able to be hard deleted
    if (appointment.getStatus() != AppointmentStatus.UNBOOKED) {
      throw new InvalidStatusException(
          "Only unbooked appointment slots can be deleted. This appointment already has a client associated with it.");
    }

    appointmentRepository.delete(appointment);
  }
}