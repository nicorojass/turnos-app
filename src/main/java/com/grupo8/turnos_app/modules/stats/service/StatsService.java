package com.grupo8.turnos_app.modules.stats.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.grupo8.turnos_app.common.enums.AppointmentStatus;
import com.grupo8.turnos_app.common.enums.DepositStatus;
import com.grupo8.turnos_app.common.exception.ForbiddenOperationException;
import com.grupo8.turnos_app.modules.appointment.repository.AppointmentRepository;
import com.grupo8.turnos_app.modules.business.entities.Business;
import com.grupo8.turnos_app.modules.business.exceptions.BusinessNotFoundException;
import com.grupo8.turnos_app.modules.business.repositories.BusinessRepository;
import com.grupo8.turnos_app.modules.deposit.repository.DepositRepository;
import com.grupo8.turnos_app.modules.stats.dto.AdminAppointmentStatsDto;
import com.grupo8.turnos_app.modules.stats.dto.AdminStatsResponse;
import com.grupo8.turnos_app.modules.stats.dto.AppointmentStatsDto;
import com.grupo8.turnos_app.modules.stats.dto.BusinessStatsResponse;
import com.grupo8.turnos_app.modules.stats.dto.BusinessSummaryDto;
import com.grupo8.turnos_app.modules.stats.dto.DayStatsDto;
import com.grupo8.turnos_app.modules.stats.dto.EmployeeAppointmentStatsDto;
import com.grupo8.turnos_app.modules.stats.dto.EmployeeStatsResponse;
import com.grupo8.turnos_app.modules.stats.dto.EmployeeSummaryDto;
import com.grupo8.turnos_app.modules.stats.dto.GrowthPointDto;
import com.grupo8.turnos_app.modules.stats.dto.PeriodResponse;
import com.grupo8.turnos_app.modules.stats.dto.RevenueStatsDto;
import com.grupo8.turnos_app.modules.stats.dto.ServiceStatsDto;
import com.grupo8.turnos_app.modules.users.entities.User;
import com.grupo8.turnos_app.modules.users.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// service that contains business logic for building the stats responses for
// business, employee and admin endpoints.
public class StatsService {

    private final AppointmentRepository appointmentRepository;
    private final DepositRepository depositRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    // mysql dayofweek: 1=sun, 2=mon, ..., 7=sat
    private static final Map<Integer, String> MYSQL_DOW = Map.of(
            1, "SUNDAY", 2, "MONDAY", 3, "TUESDAY", 4, "WEDNESDAY",
            5, "THURSDAY", 6, "FRIDAY", 7, "SATURDAY");

    // business stats builder method. contains period, appointment stats, revenue
    // stats, service-level stats, employee-level stats, peak days and unique
    // clients count
    public BusinessStatsResponse getBusinessStats(UUID businessPublicId, String period,
            LocalDate dateFrom, LocalDate dateTo, User currentUser) {

        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));

        // only the owner of this business can see global stats
        if (!business.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("No tenes permiso para ver estas estadísticas");
        }

        LocalDateTime[] range = resolveDateRange(period, dateFrom, dateTo);
        LocalDateTime from = range[0];
        LocalDateTime to = range[1];
        LocalDateTime now = LocalDateTime.now();
        Long businessId = business.getId();

        long finished = appointmentRepository.countFinishedByBusiness(businessId, from, to, now);
        long upcoming = appointmentRepository.countUpcomingByBusiness(businessId, from, to, now);
        long cancelled = appointmentRepository.countByBusinessAndStatus(businessId, AppointmentStatus.CANCELLED, from,
                to);
        long suspended = appointmentRepository.countByBusinessAndStatus(businessId, AppointmentStatus.SUSPENDED, from,
                to);
        long total = finished + upcoming + cancelled + suspended;

        double occupancy = total > 0 ? round((double) (finished + upcoming) / total * 100) : 0.0;
        double cancellationRate = (finished + upcoming + cancelled) > 0
                ? round((double) cancelled / (finished + upcoming + cancelled) * 100)
                : 0.0;

        BigDecimal revenue = appointmentRepository.sumRevenueFinishedByBusiness(businessId, from, to, now);

        List<ServiceStatsDto> byService = appointmentRepository
                .statsGroupedByService(businessId, from, to, now)
                .stream()
                .map(r -> ServiceStatsDto.builder()
                        .serviceName((String) r[0])
                        .appointmentCount(((Number) r[1]).longValue())
                        .revenue((BigDecimal) r[2])
                        .build())
                .toList();

        List<EmployeeSummaryDto> byEmployee = appointmentRepository
                .statsGroupedByEmployee(businessId, from, to, now)
                .stream()
                .map(r -> EmployeeSummaryDto.builder()
                        .employeeId((UUID) r[0])
                        .employeeName((String) r[1])
                        .appointmentCount(((Number) r[2]).longValue())
                        .revenue((BigDecimal) r[3])
                        .build())
                .toList();

        List<DayStatsDto> peakDays = appointmentRepository
                .statsGroupedByDayOfWeek(businessId, from, to)
                .stream()
                .map(r -> DayStatsDto.builder()
                        .dayOfWeek(MYSQL_DOW.getOrDefault(((Number) r[0]).intValue(), "UNKNOWN"))
                        .appointmentCount(((Number) r[1]).longValue())
                        .build())
                .toList();

        long uniqueClients = appointmentRepository.countUniqueRegisteredClients(businessId, from, to, now);

        RevenueStatsDto revenueStats = buildRevenueStats(revenue, businessId, from, to);

        return BusinessStatsResponse.builder()
                .period(buildPeriod(from, to))
                .appointments(AppointmentStatsDto.builder()
                        .total(total)
                        .finished(finished)
                        .upcoming(upcoming)
                        .cancelled(cancelled)
                        .suspended(suspended)
                        .occupancyRate(occupancy)
                        .cancellationRate(cancellationRate)
                        .build())
                .revenue(revenueStats)
                .byService(byService)
                .byEmployee(byEmployee)
                .peakDays(peakDays)
                .uniqueClientsCount(uniqueClients)
                .build();
    }

    // employee stats builder method. contains stats for a specific employee in the
    // given period: appointment stats, hours worked, revenue generated and
    // service-level stats
    public EmployeeStatsResponse getEmployeeStats(UUID businessPublicId, UUID employeePublicId,
            String period, LocalDate dateFrom, LocalDate dateTo, User currentUser) {

        Business business = businessRepository.findByPublicId(businessPublicId)
                .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado"));

        User employee = userRepository.findByPublicId(employeePublicId)
                .orElseThrow(
                        () -> new com.grupo8.turnos_app.common.exception.NotFoundException("Empleado no encontrado"));

        boolean isOwner = business.getOwner().getId().equals(currentUser.getId());
        boolean isSelf = currentUser.getId().equals(employee.getId());

        if (!isOwner && !isSelf) {
            throw new ForbiddenOperationException("No tenes permiso para ver estas estadísticas");
        }

        LocalDateTime[] range = resolveDateRange(period, dateFrom, dateTo);
        LocalDateTime from = range[0];
        LocalDateTime to = range[1];
        LocalDateTime now = LocalDateTime.now();
        Long businessId = business.getId();
        Long employeeId = employee.getId();

        long finished = appointmentRepository.countFinishedByEmployee(employeeId, businessId, from, to, now);
        long upcoming = appointmentRepository.countUpcomingByEmployee(employeeId, businessId, from, to, now);
        long cancelled = appointmentRepository.countCancelledByEmployee(employeeId, businessId, from, to);
        long total = appointmentRepository.countByEmployee(employeeId, businessId, from, to);

        double occupancy = total > 0 ? round((double) (finished + upcoming) / total * 100) : 0.0;

        long minutes = appointmentRepository.sumMinutesWorkedByEmployee(employeeId, businessId, from, to, now);
        double hoursWorked = round(minutes / 60.0);

        BigDecimal revenue = appointmentRepository.sumRevenueByEmployee(employeeId, businessId, from, to, now);

        List<ServiceStatsDto> byService = appointmentRepository
                .statsGroupedByServiceForEmployee(employeeId, businessId, from, to, now)
                .stream()
                .map(r -> ServiceStatsDto.builder()
                        .serviceName((String) r[0])
                        .appointmentCount(((Number) r[1]).longValue())
                        .revenue(BigDecimal.ZERO)
                        .build())
                .toList();

        return EmployeeStatsResponse.builder()
                .employeeId(employee.getPublicId())
                .employeeName(employee.getName())
                .period(buildPeriod(from, to))
                .appointments(EmployeeAppointmentStatsDto.builder()
                        .total(total)
                        .finished(finished)
                        .upcoming(upcoming)
                        .cancelled(cancelled)
                        .occupancyRate(occupancy)
                        .build())
                .hoursWorked(hoursWorked)
                .revenueGenerated(revenue)
                .byService(byService)
                .build();
    }

    // admin stats builder method. contains overall stats for the platform in the
    // given period, such as total businesses, active businesses, total users, new
    // users, appointment stats, revenue stats, top businesses and user growth
    // points
    public AdminStatsResponse getAdminStats(String period, LocalDate dateFrom, LocalDate dateTo) {
        LocalDateTime[] range = resolveDateRange(period, dateFrom, dateTo);
        LocalDateTime from = range[0];
        LocalDateTime to = range[1];
        LocalDateTime now = LocalDateTime.now();

        // 6 months back for growth chart
        LocalDateTime growthSince = LocalDateTime.now().minusMonths(6).withDayOfMonth(1).toLocalDate().atStartOfDay();

        long totalBusinesses = businessRepository.count();
        long activeBusinesses = appointmentRepository.countActiveBusinessesInRange(from, to);
        long totalUsers = userRepository.count();
        long newUsers = userRepository.countNewUsersInRange(from, to);

        long totalAppts = appointmentRepository.countAllInRange(from, to);
        long finishedAppts = appointmentRepository.countAllFinishedInRange(from, to, now);
        long cancelledAppts = appointmentRepository.countAllCancelledInRange(from, to);
        long suspendedAppts = appointmentRepository.countAllSuspendedInRange(from, to);

        BigDecimal totalRevenue = appointmentRepository.sumAllRevenueInRange(from, to, now);

        RevenueStatsDto revenueStats = RevenueStatsDto.builder()
                .totalFromFinished(totalRevenue)
                .depositsCollected(depositRepository.sumByStatusGlobal(DepositStatus.PAID, from, to))
                .depositsRefunded(depositRepository.sumByStatusGlobal(DepositStatus.REFUNDED, from, to))
                .depositsForfeited(depositRepository.sumByStatusGlobal(DepositStatus.FORFEITED, from, to))
                .build();

        List<BusinessSummaryDto> topBusinesses = appointmentRepository
                .topBusinessesByAppointments(from, to, now, PageRequest.of(0, 10))
                .stream()
                .map(r -> BusinessSummaryDto.builder()
                        .businessName((String) r[0])
                        .appointmentCount(((Number) r[1]).longValue())
                        .revenue((BigDecimal) r[2])
                        .build())
                .toList();

        List<GrowthPointDto> userGrowth = userRepository.monthlyUserGrowth(growthSince)
                .stream()
                .map(r -> GrowthPointDto.builder()
                        .year(((Number) r[0]).intValue())
                        .month(((Number) r[1]).intValue())
                        .newUsers(((Number) r[2]).longValue())
                        .build())
                .toList();

        return AdminStatsResponse.builder()
                .period(buildPeriod(from, to))
                .totalBusinesses(totalBusinesses)
                .activeBusinessesInPeriod(activeBusinesses)
                .totalUsers(totalUsers)
                .newUsersInPeriod(newUsers)
                .appointments(AdminAppointmentStatsDto.builder()
                        .total(totalAppts)
                        .finished(finishedAppts)
                        .cancelled(cancelledAppts)
                        .suspended(suspendedAppts)
                        .build())
                .revenue(revenueStats)
                .topBusinesses(topBusinesses)
                .userGrowth(userGrowth)
                .build();
    }

    // resolve the date range from a period string (month, week, etc.) or param
    // values dateFrom/dateTo
    private LocalDateTime[] resolveDateRange(String period, LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom != null && dateTo != null) {
            return new LocalDateTime[] { dateFrom.atStartOfDay(), dateTo.plusDays(1).atStartOfDay() };
        }
        LocalDate today = LocalDate.now();
        return switch (period != null ? period.toUpperCase() : "MONTH") {
            case "TODAY" -> new LocalDateTime[] { today.atStartOfDay(), today.plusDays(1).atStartOfDay() };
            case "WEEK" -> new LocalDateTime[] {
                    today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay(),
                    today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1).atStartOfDay()
            };
            case "YEAR" -> new LocalDateTime[] {
                    today.withDayOfYear(1).atStartOfDay(),
                    today.withDayOfYear(1).plusYears(1).atStartOfDay()
            };
            default -> new LocalDateTime[] {
                    today.withDayOfMonth(1).atStartOfDay(),
                    today.withDayOfMonth(1).plusMonths(1).atStartOfDay()
            };
        };
    }

    // revenue stats include total revenue from finished appointments, deposits
    // collected, refunded and forfeited in the period
    private RevenueStatsDto buildRevenueStats(BigDecimal revenue, Long businessId,
            LocalDateTime from, LocalDateTime to) {
        return RevenueStatsDto.builder()
                .totalFromFinished(revenue)
                .depositsCollected(depositRepository.sumByBusinessAndStatus(businessId, DepositStatus.PAID, from, to))
                .depositsRefunded(
                        depositRepository.sumByBusinessAndStatus(businessId, DepositStatus.REFUNDED, from, to))
                .depositsForfeited(
                        depositRepository.sumByBusinessAndStatus(businessId, DepositStatus.FORFEITED, from, to))
                .build();
    }

    // build a periodResponse DTO from two LocalDateTime values
    private PeriodResponse buildPeriod(LocalDateTime from, LocalDateTime to) {
        return PeriodResponse.builder()
                .from(from.toLocalDate())
                .to(to.minusDays(1).toLocalDate())
                .build();
    }

    // helper method to round doubles to 2 decimal digits
    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
