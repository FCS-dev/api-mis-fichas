package com.fcs.mis_fichas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal de la aplicación Spring Boot.
 * Inicia el contexto de la aplicación y escanea los componentes del paquete base.
 */
@SpringBootApplication
public class MisFichasApplication {

    /**
     * Método principal que inicia la aplicación.
     *
     * @param args argumentos de linea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(MisFichasApplication.class, args);
    }

}
