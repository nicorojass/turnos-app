package com.grupo8.turnos_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableScheduling
@SpringBootApplication
public class TurnosAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(TurnosAppApplication.class, args);
	}

}
