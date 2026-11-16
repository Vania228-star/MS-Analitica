package com.ticketfilms.msanalitica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling: cada 5 minutos se cierra la ventana de agregación,
// se sube el resumen a S3 y recién ahí se confirman los ACK de Kafka.
@SpringBootApplication
@EnableScheduling
public class MsanaliticaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsanaliticaApplication.class, args);
	}

}
