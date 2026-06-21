package com.grupo8.turnos_app.modules.users.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grupo8.turnos_app.modules.users.entities.User;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByPublicId(UUID publicId);
    boolean existsByEmailAndActiveTrue(String email);

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