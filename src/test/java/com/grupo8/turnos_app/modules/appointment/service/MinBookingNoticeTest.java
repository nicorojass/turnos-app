package com.grupo8.turnos_app.modules.appointment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grupo8.turnos_app.common.enums.AppointmentStatus;
import com.grupo8.turnos_app.common.security.BusinessAccessGuard;
import com.grupo8.turnos_app.modules.appointment.dto.AppointmentResponse;
import com.grupo8.turnos_app.modules.appointment.dto.BookAppointmentRequest;
import com.grupo8.turnos_app.modules.appointment.dto.CreateAppointmentRequest;
import com.grupo8.turnos_app.modules.appointment.entity.Appointment;
import com.grupo8.turnos_app.modules.appointment.exceptions.AppointmentNotAvailableException;
import com.grupo8.turnos_app.modules.appointment.repository.AppointmentRepository;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;
import com.grupo8.turnos_app.modules.deposit.repository.DepositRepository;
import com.grupo8.turnos_app.modules.service.entity.Serv;
import com.grupo8.turnos_app.modules.service.repository.ServRepository;
import com.grupo8.turnos_app.modules.users.repositories.UserRepository;

// the business's minimum booking notice: the public list hides the slots that start before now plus
// those hours, booking one of them answers 409, and the appointments the owner adds by hand don't
// have the rule
@ExtendWith(MockitoExtension.class)
class MinBookingNoticeTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private BusinessAccessGuard businessAccessGuard;
    @Mock private BusinessRepository businessRepository;
    @Mock private DepositRepository depositRepository;
    @Mock private UserRepository userRepository;
    @Mock private ServRepository serviceRepository;

    @InjectMocks private AppointmentService appointmentService;

    // ---------- public list ----------

    @Test
    void thePublicListOnlyAsksForSlotsAfterNowPlusTheNotice() {
        LocalDateTime earliest = earliestStartAskedToTheRepository(2);

        // a slot at 16:00 looked at 15:00 is before 17:00, so the query leaves it out
        assertThat(earliest).isBetween(LocalDateTime.now().plusHours(2).minusSeconds(5),
                LocalDateTime.now().plusHours(2));
    }

    @Test
    void withZeroHoursThePublicListWorksLikeBefore() {
        assertThat(earliestStartAskedToTheRepository(0))
                .isBetween(LocalDateTime.now().minusSeconds(5), LocalDateTime.now());
    }

    @Test
    void aBusinessWithoutTheFieldIsTakenAsZero() {
        assertThat(earliestStartAskedToTheRepository(null))
                .isBetween(LocalDateTime.now().minusSeconds(5), LocalDateTime.now());
    }

    // ---------- booking ----------

    @Test
    void bookingASlotInsideTheNoticeAnswersNotAvailableWithTheHours() {
        Appointment appointment = freeSlot(business(2), LocalDateTime.now().plusHours(1));

        assertThatThrownBy(() -> book(appointment))
                .isInstanceOf(AppointmentNotAvailableException.class)
                .hasMessage("El turno ya no está disponible: este negocio pide reservar con al menos "
                        + "2 horas de anticipación.");
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.UNBOOKED);
    }

    @Test
    void withOneHourTheMessageIsInSingular() {
        Appointment appointment = freeSlot(business(1), LocalDateTime.now().plusMinutes(30));

        assertThatThrownBy(() -> book(appointment))
                .hasMessageContaining("al menos 1 hora de anticipación");
    }

    @Test
    void bookingASlotOutsideTheNoticeWorks() {
        Appointment appointment = freeSlot(business(2), LocalDateTime.now().plusHours(3));

        assertThat(book(appointment).getStatus()).isEqualTo(AppointmentStatus.AWAITING_PAYMENT);
    }

    @Test
    void withZeroHoursASlotInTenMinutesCanStillBeBooked() {
        Appointment appointment = freeSlot(business(0), LocalDateTime.now().plusMinutes(10));

        assertThat(book(appointment).getStatus()).isEqualTo(AppointmentStatus.AWAITING_PAYMENT);
    }

    @Test
    void aBusinessWithoutTheFieldLetsBookLikeBefore() {
        Appointment appointment = freeSlot(business(null), LocalDateTime.now().plusMinutes(10));

        assertThat(book(appointment).getStatus()).isEqualTo(AppointmentStatus.AWAITING_PAYMENT);
    }

    // ---------- appointments added by the owner ----------

    @Test
    void theOwnerCanAddAnAppointmentInTenMinutesEvenWith24Hours() {
        Business business = business(24);
        Serv service = service(business);
        when(businessRepository.findByPublicId(business.getPublicId())).thenReturn(Optional.of(business));
        when(serviceRepository.findByPublicId(service.getPublicId())).thenReturn(Optional.of(service));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(call -> call.getArgument(0));

        AppointmentResponse response = appointmentService.createAppointment(business.getPublicId(),
                CreateAppointmentRequest.builder()
                        .startDatetime(LocalDateTime.now().plusMinutes(10))
                        .serviceId(service.getPublicId())
                        .clientName("Federico Paz")
                        .build());

        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.BOOKED);
    }

    // ---------- helpers ----------

    private LocalDateTime earliestStartAskedToTheRepository(Integer hours) {
        Business business = business(hours);
        when(businessRepository.findByPublicId(business.getPublicId())).thenReturn(Optional.of(business));
        ArgumentCaptor<LocalDateTime> earliest = ArgumentCaptor.forClass(LocalDateTime.class);
        when(appointmentRepository.findAvailableSlots(eq(business.getId()), earliest.capture(), isNull(), isNull()))
                .thenReturn(List.of());

        appointmentService.getAvailableSlots(business.getPublicId(), null, null);

        verify(appointmentRepository).findAvailableSlots(eq(business.getId()), any(), isNull(), isNull());
        return earliest.getValue();
    }

    // books as an anonymous client
    private AppointmentResponse book(Appointment appointment) {
        when(appointmentRepository.findIdByPublicId(appointment.getPublicId()))
                .thenReturn(Optional.of(appointment.getId()));
        when(appointmentRepository.findByIdWithLock(appointment.getId())).thenReturn(Optional.of(appointment));

        return appointmentService.bookAppointment(appointment.getPublicId(),
                BookAppointmentRequest.builder().clientName("Federico Paz").clientEmail("fede@mail.com").build(),
                null);
    }

    private Business business(Integer minBookingNoticeHours) {
        return Business.builder()
                .id(10L)
                .publicId(UUID.randomUUID())
                .name("Estudio Malbec")
                .slug("estudio-malbec")
                .minBookingNoticeHours(minBookingNoticeHours)
                .build();
    }

    private Serv service(Business business) {
        return Serv.builder()
                .id(20L)
                .publicId(UUID.randomUUID())
                .name("Corte")
                .durationMinutes(30)
                .price(new BigDecimal("10000"))
                .depositPorcentage(new BigDecimal("30"))
                .business(business)
                .build();
    }

    private Appointment freeSlot(Business business, LocalDateTime start) {
        return Appointment.builder()
                .id(30L)
                .publicId(UUID.randomUUID())
                .status(AppointmentStatus.UNBOOKED)
                .startDatetime(start)
                .endDatetime(start.plusMinutes(30))
                .price(new BigDecimal("10000"))
                .business(business)
                .service(service(business))
                .build();
    }
}
