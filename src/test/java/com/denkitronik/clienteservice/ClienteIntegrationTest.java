package com.denkitronik.clienteservice;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// Prueba E2E: levanta la app completa contra un PostgreSQL real en Docker.
// Si no hay Docker disponible, el test se salta (no falla).
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClienteIntegrationTest {

    private static GenericContainer<?> postgres;

    @Value("${local.server.port}")
    private int puerto;

    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeAll
    static void iniciarPostgres() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker no disponible");
        postgres = new GenericContainer<>(DockerImageName.parse("postgres:16"))
                .withEnv("POSTGRES_DB", "testdb")
                .withEnv("POSTGRES_USER", "test")
                .withEnv("POSTGRES_PASSWORD", "test")
                .withExposedPorts(5432)
                .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\s", 2));
        postgres.start();
    }

    @AfterAll
    static void detenerPostgres() {
        if (postgres != null) {
            postgres.stop();
        }
    }

    @DynamicPropertySource
    static void configurarBaseDeDatos(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", () -> "jdbc:postgresql://" + postgres.getHost()
                + ":" + postgres.getMappedPort(5432) + "/testdb");
        registro.add("spring.datasource.username", () -> "test");
        registro.add("spring.datasource.password", () -> "test");
        registro.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    private HttpResponse<String> enviar(String metodo, String ruta, String json) throws Exception {
        HttpRequest.BodyPublisher cuerpo = (json == null)
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json);
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + puerto + "/api/v1/cliente-service" + ruta))
                .header("Content-Type", "application/json")
                .method(metodo, cuerpo)
                .build();
        return http.send(peticion, HttpResponse.BodyHandlers.ofString());
    }

    private String clienteJson(String nombre, String email) {
        return "{\"nombre\":\"" + nombre + "\",\"apellido\":\"Prueba\",\"email\":\"" + email
                + "\",\"region\":{\"id\":1}}";
    }

    @Test
    void listarClientesDevuelve200() throws Exception {
        assertEquals(200, enviar("GET", "/clientes", null).statusCode());
    }

    @Test
    void crearClienteValidoDevuelve201() throws Exception {
        HttpResponse<String> respuesta = enviar("POST", "/clientes", clienteJson("Grace", "grace@mail.com"));
        assertEquals(201, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"id\""));
    }

    @Test
    void crearClienteConEmailInvalidoDevuelve400() throws Exception {
        assertEquals(400, enviar("POST", "/clientes", clienteJson("Grace", "no-es-email")).statusCode());
    }

    @Test
    void buscarClienteInexistenteDevuelve404() throws Exception {
        assertEquals(404, enviar("GET", "/clientes/999999", null).statusCode());
    }

    @Test
    void actualizarClienteInexistenteDevuelve404() throws Exception {
        assertEquals(404, enviar("PUT", "/clientes/999999", clienteJson("Alan", "alan@mail.com")).statusCode());
    }

    @Test
    void eliminarClienteInexistenteDevuelve404() throws Exception {
        assertEquals(404, enviar("DELETE", "/clientes/999999", null).statusCode());
    }
}