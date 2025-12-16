package turnos.app;

import turnos.app.model.Cliente;
import turnos.app.model.Turno;
import turnos.app.service.AgendaService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        AgendaService agenda = new AgendaService();

        Cliente c1 = new Cliente(1, "Nico Rojas", 44534570, "2235401196");
        Cliente c2 = new Cliente (2, "Luca Nadotti", 44534571, "223197234");

        LocalDate hoy = LocalDate.now();
        LocalTime hora10 = LocalTime.of(10,0);
        LocalTime hora11 = LocalTime.of(11,0);

        agenda.crearTurno(c1,hoy,hora10);
        agenda.crearTurno(c2,hoy,hora11);

        try {
            agenda.crearTurno(c2, hoy, hora10);
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR esperado: " + e.getMessage());
        }

            agenda.cancelarTurno(1);

        System.out.println(agenda.listarTurnos());


    }
}

