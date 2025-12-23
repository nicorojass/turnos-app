package turnos.app.service;

import turnos.app.model.Cliente;
import turnos.app.model.EstadoTurno;
import turnos.app.model.Turno;
import turnos.app.repository.TurnoRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AgendaService {

    private TurnoRepository repo = new TurnoRepository();
    private int contadorTurnos = 1;

    public AgendaService() {
        this.repo = repo;
    }

    private boolean existeTurno(LocalDate fecha, LocalTime hora) {
        return repo.existeTurno(fecha, hora);
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

        repo.guardar(t);
        return t;
    }

    public Turno buscarTurno(int id) {
        Turno t = repo.buscarTurno(id);
        return t;
    }

    public void cancelarTurno(int idTurno) {
        repo.cancelarTurno(idTurno);
    }

    public List<Turno> listarTurnos() {
        return repo.listar();
    }
}
