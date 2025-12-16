package turnos.app.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Turno {
    private int id;
    private Cliente cliente;
    private LocalDate fecha;
    private LocalTime hora;
    private EstadoTurno estado;

    public Turno() {
    }

    public Turno(int id, Cliente cliente, LocalDate fecha, LocalTime hora, EstadoTurno estado) {
        this.id = id;
        this.cliente = cliente;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public void setEstado(EstadoTurno estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Turno ID: " + getId() +
                " | Cliente: " + getCliente().getNombre() +
                " | Fecha: " + getFecha() +
                " | Hora: " + getHora() +
                " | Estado: " + getEstado();
    }
}
