package com.grupo8.turnos_app.modules.appointment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grupo8.turnos_app.common.enums.AppointmentStatus;
import com.grupo8.turnos_app.common.enums.DepositStatus;
import com.grupo8.turnos_app.common.security.BusinessAccessGuard;
import com.grupo8.turnos_app.modules.appointment.dto.AppointmentResponse;
import com.grupo8.turnos_app.modules.appointment.entity.Appointment;
import com.grupo8.turnos_app.modules.appointment.repository.AppointmentRepository;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;
import com.grupo8.turnos_app.modules.deposit.entity.Deposit;
import com.grupo8.turnos_app.modules.deposit.repository.DepositRepository;
import com.grupo8.turnos_app.modules.service.entity.Serv;
import com.grupo8.turnos_app.modules.service.repository.ServRepository;
import com.grupo8.turnos_app.modules.users.entities.User;
import com.grupo8.turnos_app.modules.users.repositories.UserRepository;

// the client cancels a booked appointment: the deposit is REFUNDED when there are at least the
// deposit's cancellation notice hours left, and FORFEITED when there are fewer
@ExtendWith(MockitoExtension.class)
class CancellationPolicyTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private BusinessAccessGuard businessAccessGuard;
    @Mock private BusinessRepository businessRepository;
    @Mock private DepositRepository depositRepository;
    @Mock private UserRepository userRepository;
    @Mock private ServRepository serviceRepository;

    @InjectMocks private AppointmentService appointmentService;

    private final User client = User.builder().id(1L).publicId(UUID.randomUUID()).name("Cliente").build();

    @Test
    void refundsWhenThereAreMoreHoursThanThePolicy() {
        DepositStatus status = cancel(48, 48, LocalDateTime.now().plusHours(72));

        assertThat(status).isEqualTo(DepositStatus.REFUNDED);
    }

    @Test
    void refundsWhenThereAreExactlyThePolicyHours() {
        // a few seconds of margin so the time the test takes doesn't leave it at 47h59m
        DepositStatus status = cancel(48, 48, LocalDateTime.now().plusHours(48).plusSeconds(5));

        assertThat(status).isEqualTo(DepositStatus.REFUNDED);
    }

    @Test
    void forfeitsWhenThereAreFewerHoursThanThePolicy() {
        DepositStatus status = cancel(48, 48, LocalDateTime.now().plusHours(47));

        assertThat(status).isEqualTo(DepositStatus.FORFEITED);
    }

    @Test
    void usesTheHoursOfTheDepositAndNotTheCurrentOnesOfTheBusiness() {
        // booked with 48 hours, then the owner lowered it to 24: 30 hours left is still a late cancellation
        DepositStatus status = cancel(24, 48, LocalDateTime.now().plusHours(30));

        assertThat(status).isEqualTo(DepositStatus.FORFEITED);
    }

    @Test
    void aBookingMadeBeforeRaisingThePolicyKeepsItsOldRule() {
        // booked with 24 hours, then the owner raised it to 96: 30 hours left still gets the deposit back
        DepositStatus status = cancel(96, 24, LocalDateTime.now().plusHours(30));

        assertThat(status).isEqualTo(DepositStatus.REFUNDED);
    }

    @Test
    void aDepositWithoutHoursUsesTheOld24HourRule() {
        assertThat(cancel(96, null, LocalDateTime.now().plusHours(25))).isEqualTo(DepositStatus.REFUNDED);
        assertThat(cancel(96, null, LocalDateTime.now().plusHours(23))).isEqualTo(DepositStatus.FORFEITED);
    }

    // cancels a BOOKED appointment of `client` and returns the resulting deposit status
    private DepositStatus cancel(Integer businessHours, Integer depositHours, LocalDateTime start) {
        Business business = Business.builder()
                .id(10L)
                .publicId(UUID.randomUUID())
                .name("Estudio Malbec")
                .slug("estudio-malbec")
                .cancellationNoticeHours(businessHours)
                .build();
        Serv service = Serv.builder().id(20L).publicId(UUID.randomUUID()).name("Corte").build();
        Appointment appointment = Appointment.builder()
                .id(30L)
                .publicId(UUID.randomUUID())
                .status(AppointmentStatus.BOOKED)
                .startDatetime(start)
                .endDatetime(start.plusMinutes(30))
                .price(new BigDecimal("10000"))
                .business(business)
                .service(service)
                .clientUser(client)
                .build();
        Deposit deposit = Deposit.builder()
                .id(40L)
                .publicId(UUID.randomUUID())
                .amount(new BigDecimal("3000"))
                .status(DepositStatus.PAID)
                .cancellationNoticeHours(depositHours)
                .appointment(appointment)
                .build();

        when(appointmentRepository.findByPublicId(appointment.getPublicId())).thenReturn(Optional.of(appointment));
        when(depositRepository.findByAppointmentId(appointment.getId())).thenReturn(Optional.of(deposit));

        AppointmentResponse response = appointmentService.cancelAppointment(appointment.getPublicId(), client);

        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(response.getDeposit().getCancellationNoticeHours()).isEqualTo(depositHours);
        return response.getDeposit().getStatus();
    }
}
