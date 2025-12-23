package turnos.app.ui;

import turnos.app.service.AgendaService;

import java.util.Scanner;

public class ConsolaUI {

    private AgendaService agendaService;
    private Scanner scanner = new Scanner(System.in);

    public ConsolaUI() {
        this.agendaService = agendaService;
        this.scanner = scanner;
    }

    public void iniciar(){
        int opcion;

        do {
            mostrarMenu();
            opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:

            }


        }while (opcion != 0);

    }

    private void mostrarMenu(){
        System.out.println("\n===GESTION DE TURNOS===");
        System.out.println("1. Agendar turno");
        System.out.println("2. Cancelar turno");
        System.out.println("3. Listar turnos");
        System.out.println("0. Salir");
    }

    private void crearTurno(){
        System.out.println("\n===AGENDAR TURNO===");

        


    }




}
