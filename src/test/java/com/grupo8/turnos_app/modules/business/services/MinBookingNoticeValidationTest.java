package com.grupo8.turnos_app.modules.business.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grupo8.turnos_app.common.security.BusinessAccessGuard;
import com.grupo8.turnos_app.modules.agenda.service.AgendaGeneratorService;
import com.grupo8.turnos_app.modules.appointment.repository.AppointmentRepository;
import com.grupo8.turnos_app.modules.business.dto.BusinessRequest;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.mapper.BusinessMapper;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;
import com.grupo8.turnos_app.modules.business.repositories.BusinessTypeRepository;
import com.grupo8.turnos_app.modules.day_schedule.service.DayScheduleService;
import com.grupo8.turnos_app.modules.users.entities.User;
import com.grupo8.turnos_app.modules.users.repositories.UserRepository;

// the business only takes 0, 1, 2, 4, 12 or 24 hours of minimum booking notice, and returns it
// in its responses (0 for the businesses created before it existed)
@ExtendWith(MockitoExtension.class)
class MinBookingNoticeValidationTest {

    @Mock private BusinessRepository businessRepository;
    @Mock private BusinessAccessGuard businessAccessGuard;
    @Mock private UserRepository userRepository;
    @Mock private BusinessTypeRepository businessTypeRepository;
    @Mock private DayScheduleService dayScheduleService;
    @Mock private AgendaGeneratorService agendaGeneratorService;
    @Mock private AppointmentRepository appointmentRepository;

    @InjectMocks private BusinessService businessService;

    @ParameterizedTest
    @ValueSource(ints = { -1, 3, 6, 48 })
    void aValueOutOfTheListIsRejectedWithAClearMessage(int hours) {
        BusinessRequest request = BusinessRequest.builder()
                .name("Estudio Malbec")
                .email("hola@malbec.com")
                .slug("estudio-malbec")
                .phone("1145550182")
                .minBookingNoticeHours(hours)
                .build();

        assertThatThrownBy(() -> businessService.createBusiness("owner@mail.com", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La anticipación mínima para reservar tiene que ser de 0, 1, 2, 4, 12 o 24 horas");
    }

    @Test
    void theResponseReturnsTheHoursAndZeroWhenTheyAreEmpty() {
        assertThat(BusinessMapper.toResponse(business(12)).getMinBookingNoticeHours()).isEqualTo(12);
        assertThat(BusinessMapper.toResponse(business(null)).getMinBookingNoticeHours()).isZero();
    }

    @Test
    void aNewBusinessStartsWithZeroWhenTheRequestLeavesItOut() {
        BusinessRequest request = BusinessRequest.builder().name("Estudio Malbec").build();

        assertThat(BusinessMapper.toEntity(request).getMinBookingNoticeHours()).isZero();
    }

    private Business business(Integer minBookingNoticeHours) {
        return Business.builder()
                .id(10L)
                .publicId(UUID.randomUUID())
                .name("Estudio Malbec")
                .slug("estudio-malbec")
                .owner(User.builder().id(1L).publicId(UUID.randomUUID()).build())
                .businessTypes(new ArrayList<>())
                .minBookingNoticeHours(minBookingNoticeHours)
                .build();
    }
}
