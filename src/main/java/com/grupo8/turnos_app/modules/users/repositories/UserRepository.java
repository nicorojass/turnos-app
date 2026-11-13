package com.grupo8.turnos_app.modules.users.repositories;

import com.grupo8.turnos_app.common.enums.RoleName;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grupo8.turnos_app.modules.users.entities.User;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // used to serialize the bookings of the same client
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithLock(@Param("id") Long id);
    boolean existsByEmail(String email);
    Optional<User> findByPublicId(UUID publicId);
    boolean existsByEmailAndActiveTrue(String email);

    long countByActiveTrue();
    long countByActiveTrueAndRoles_Name(RoleName name);

    // count new users registered in  date range
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :from AND u.createdAt < :to")
    long countNewUsersInRange(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    // monthly user growth: [year, month, count] for the last n months
    @Query("SELECT YEAR(u.createdAt), MONTH(u.createdAt), COUNT(u) FROM User u " +
        "WHERE u.createdAt >= :since " +
        "GROUP BY YEAR(u.createdAt), MONTH(u.createdAt) " +
        "ORDER BY YEAR(u.createdAt), MONTH(u.createdAt)")
    List<Object[]> monthlyUserGrowth(@Param("since") LocalDateTime since);
}