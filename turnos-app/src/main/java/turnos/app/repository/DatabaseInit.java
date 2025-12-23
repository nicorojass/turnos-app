package turnos.app.repository;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInit {

    public static void init() {
        String sql = """
            CREATE TABLE IF NOT EXISTS turnos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre_cliente TEXT,
                dni INTEGER,
                telefono TEXT,
                fecha TEXT,
                hora TEXT,
                estado TEXT
            );
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
