package turnos.app;

import turnos.app.model.Cliente;
import turnos.app.model.Turno;
import turnos.app.service.AgendaService;
import turnos.app.ui.ConsolaUI;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        new ConsolaUI().iniciar();
    }
}

