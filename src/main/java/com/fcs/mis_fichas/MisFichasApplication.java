package com.fcs.mis_fichas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal de la aplicacion Spring Boot.
 * Inicia el contexto de la aplicacion y escanea los componentes del paquete base.
 */
@SpringBootApplication
public class MisFichasApplication {

    /**
     * Metodo principal que inicia la aplicacion.
     *
     * @param args argumentos de linea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(MisFichasApplication.class, args);
    }

}
