package com.grupo8.turnos_app.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.grupo8.turnos_app.common.enums.AppointmentStatus;
import com.grupo8.turnos_app.common.enums.DayOfWeek;
import com.grupo8.turnos_app.common.enums.DepositStatus;
import com.grupo8.turnos_app.common.enums.RoleName;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// ddl-auto=update adds new tables and columns but never changes the ones that already exist,
// so databases created with an older version of the entities drift from the code:
// - enum columns keep the values they had when they were created (deposits.status without
//   CANCELED makes expiring or cancelling an unpaid booking fail with "Data truncated").
// - columns added later to a table with rows leave those rows with NULL (deleted = NULL hides
//   them from findAllByDeletedFalse) or with an all-zero public_id shared by every row.
// every step is idempotent and only touches what is out of date, so it is safe to run on each start.
// it runs right after hibernate updates the schema (depends on the entityManagerFactory) and before
// the context finishes starting, so the scheduled jobs never query a table while it is being altered.
@Component
@DependsOn("entityManagerFactory")
@RequiredArgsConstructor
@Slf4j
public class LegacySchemaRepair {

    private static final String ZERO_UUID = "UNHEX(REPEAT('0', 32))";

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void run() {
        repairEnumColumn("deposits", "status", DepositStatus.values());
        repairEnumColumn("appointments", "status", AppointmentStatus.values());
        repairEnumColumn("day_schedules", "day", DayOfWeek.values());
        repairEnumColumn("roles", "name", RoleName.values());

        for (String table : List.of("business_types", "businesses", "services")) {
            update("UPDATE " + table + " SET deleted = 0 WHERE deleted IS NULL", table + ".deleted NULL -> false");
        }

        // UUID() is evaluated once per row, so each row gets its own id
        update("UPDATE business_types SET public_id = UUID_TO_BIN(UUID()) WHERE public_id = " + ZERO_UUID,
                "business_types.public_id all zeros -> new uuid");
    }

    // widens a MySQL ENUM column so it accepts every value of the Java enum, keeping its nullability
    private void repairEnumColumn(String table, String column, Enum<?>[] values) {
        try {
            List<String> rows = jdbcTemplate.queryForList(
                    "SELECT CONCAT(column_type, '|', is_nullable) FROM information_schema.columns "
                            + "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                    String.class, table, column);
            if (rows.isEmpty())
                return;

            String[] parts = rows.get(0).split("\\|");
            String columnType = parts[0];
            if (!columnType.toLowerCase().startsWith("enum("))
                return;

            boolean missingValue = Arrays.stream(values)
                    .anyMatch(value -> !columnType.contains("'" + value.name() + "'"));
            if (!missingValue)
                return;

            String enumValues = Arrays.stream(values)
                    .map(value -> "'" + value.name() + "'")
                    .collect(Collectors.joining(","));
            String nullability = "YES".equalsIgnoreCase(parts[1]) ? "NULL" : "NOT NULL";
            jdbcTemplate.execute("ALTER TABLE " + table + " MODIFY COLUMN " + column
                    + " ENUM(" + enumValues + ") " + nullability);
            log.info("[SchemaRepair] {}.{} now accepts {}", table, column, enumValues);
        } catch (Exception e) {
            log.warn("[SchemaRepair] could not check {}.{}: {}", table, column, e.getMessage());
        }
    }

    private void update(String sql, String description) {
        try {
            int changed = jdbcTemplate.update(sql);
            if (changed > 0)
                log.info("[SchemaRepair] {}: {} row(s)", description, changed);
        } catch (Exception e) {
            log.warn("[SchemaRepair] could not apply '{}': {}", description, e.getMessage());
        }
    }
}
