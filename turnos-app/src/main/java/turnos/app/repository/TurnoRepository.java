package turnos.app.repository;

import turnos.app.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TurnoRepository {

    public void guardar(Turno t) {
        String sql = """
            INSERT INTO turnos(nombre_cliente, dni, telefono, fecha, hora, estado)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getCliente().getNombre());
            ps.setInt(2, t.getCliente().getDni());
            ps.setString(3, t.getCliente().getTelefono());
            ps.setString(4, t.getFecha().toString());
            ps.setString(5, t.getHora().toString());
            ps.setString(6, t.getEstado().name());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Turno> listar() {
        List<Turno> lista = new ArrayList<>();

        String sql = "SELECT * FROM turnos";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Cliente c = new Cliente(
                        rs.getString("nombre_cliente"),
                        rs.getInt("dni"),
                        rs.getString("telefono")
                );

                Turno t = new Turno(
                        rs.getInt("id"),
                        c,
                        LocalDate.parse(rs.getString("fecha")),
                        LocalTime.parse(rs.getString("hora")),
                        EstadoTurno.valueOf(rs.getString("estado"))
                );

                lista.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public void cancelarTurno(int idTurno) {
        String sql = "UPDATE turnos SET estado = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, EstadoTurno.CANCELADO.name());
            stmt.setInt(2, idTurno);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al cancelar turno", e);
        }
    }



}
