package turnos.app.service;

import turnos.app.model.Cliente;
import turnos.app.model.EstadoTurno;
import turnos.app.model.Turno;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AgendaService {

    private List<Turno> turnos;
    private int contadorTurnos = 1;

    public AgendaService() {
        this.turnos = new ArrayList<>();
    }

    private boolean existeTurno(LocalDate fecha, LocalTime hora) {
        return turnos.stream()
                .anyMatch(t ->
                        t.getFecha().equals(fecha) &&
                                t.getHora().equals(hora) &&
                                t.getEstado() == EstadoTurno.ACTIVO
                );
    }

    public Turno crearTurno(Cliente c, LocalDate fecha, LocalTime hora) {
        if (existeTurno(fecha, hora)) {
            throw new IllegalArgumentException("Ya existe un turno en ese horario");
        }

        Turno t = new Turno(
                contadorTurnos++,
                c,
                fecha,
                hora,
                EstadoTurno.ACTIVO
        );

        turnos.add(t);
        return t;
    }

    private Turno buscarTurno(int id) {
        return turnos.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("No se encontró turno con ese ID")
                );
    }

    public void cancelarTurno(int idTurno) {
        Turno t = buscarTurno(idTurno);
        t.setEstado(EstadoTurno.CANCELADO);
    }

    public List<Turno> listarTurnos() {
        return turnos;
    }
}
