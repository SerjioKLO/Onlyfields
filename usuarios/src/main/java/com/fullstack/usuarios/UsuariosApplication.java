package com.fullstack.usuarios;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Clase principal que inicia la aplicación del microservicio de Usuarios.
 * Habilita la autoconfiguración de Spring Boot.
 */
@SpringBootApplication
public class UsuariosApplication {

	/**
	 * Punto de entrada principal para la aplicación Spring Boot.
	 * @param args Argumentos de línea de comandos.
	 */
	public static void main(String[] args) {
		SpringApplication.run(UsuariosApplication.class, args);
	}

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("API 2026 de Onlyfields")
						.version("1.0")
						.description("API para la gestión de usuarios en Onlyfields."));
	}
}