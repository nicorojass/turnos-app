package turnos.app.ui;

import turnos.app.model.Cliente;
import turnos.app.service.AgendaService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

public class ConsolaUI {

    private AgendaService agendaService;
    private Scanner scanner = new Scanner(System.in);

    public ConsolaUI() {
        this.agendaService = new AgendaService();
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
                    crearTurno();
                    break;
                case 2:
                    cancelarTurno();
                    break;
                case 3:
                    listarTurnos();
                    break;
                case 0:
                    System.out.println("Saliendo del sistema...");
                    break;
                default:
                    System.out.println("Opcion incorrecta, ingrese una opcion valida");
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

        System.out.println("Ingrese nombre del Cliente: ");
        String nombre = scanner.nextLine();
        System.out.println("Ingrese DNI: ");
        int dni = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Ingrese telefono");
        String telefono = scanner.nextLine();

        System.out.println("Ingrese año");
        int año = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Ingrese mes");
        int mes = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Ingrese mes");
        int dia = scanner.nextInt();
        scanner.nextLine();

        int hora;
        do{
            System.out.println("Ingrese hora del turno (de 8:00 a 16:00)");
            hora = scanner.nextInt();
            scanner.nextLine();
        }while (hora < 8 && hora > 16);


        Cliente c = new Cliente(nombre, dni, telefono);
        LocalDate fecha = LocalDate.of(año,mes,dia);
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden crear turnos en fechas pasadas");
        }
        LocalTime horario = LocalTime.of(hora,0 );

        agendaService.crearTurno(c, fecha, horario);

        System.out.println("✅ Turno creado correctamente");
    }

    private void cancelarTurno() {
        System.out.println("\n===CANCELAR TURNO===");

        System.out.println("Ingrese el ID del turno a cancelar");
        int id = scanner.nextInt();
        scanner.nextLine();

        agendaService.cancelarTurno(id);

        System.out.println("✅ Turno cancelado correctamente");
    }

    private void listarTurnos(){
        System.out.println("\n===LISTAR TURNOS===");

        if (agendaService.listarTurnos().isEmpty()){
            System.out.println("No hay turnos registrados en este momento");
            return;
        }

        agendaService.listarTurnos().forEach(System.out::println);
    }






}
