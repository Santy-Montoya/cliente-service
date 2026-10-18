package com.denkitronik.clienteservice.delivery.rest;

import com.denkitronik.clienteservice.delivery.exception.GlobalExceptionHandler;
import com.denkitronik.clienteservice.domain.entities.Cliente;
import com.denkitronik.clienteservice.domain.entities.Region;
import com.denkitronik.clienteservice.domain.exception.ClienteNotFoundException;
import com.denkitronik.clienteservice.domain.exception.ClienteServiceException;
import com.denkitronik.clienteservice.domain.services.IClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClienteRestControllerTest {

    private static final String URL = "/api/v1/cliente-service/clientes";

    private static final String JSON_VALIDO = """
            {
              "nombre": "Linus",
              "apellido": "Torvalds",
              "email": "linus@linux.org",
              "foto": "linus.jpg",
              "region": { "id": 1 }
            }
            """;

    @Mock
    private IClienteService clienteService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ClienteRestController controller = new ClienteRestController(clienteService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .addPlaceholderValue("api.version", "v1")
                .build();
    }

    private Cliente crearCliente(Long id) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setNombre("Linus");
        c.setApellido("Torvalds");
        c.setEmail("linus@linux.org");
        c.setFoto("linus.jpg");
        return c;
    }

    @Test
    void listarClientesDevuelve200() throws Exception {
        when(clienteService.findAll()).thenReturn(List.of(crearCliente(1L)));
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Linus"));
    }

    @Test
    void listarRegionesDevuelve200() throws Exception {
        Region r = new Region();
        r.setId(1L);
        r.setNombre("Europa");
        when(clienteService.findAllRegiones()).thenReturn(List.of(r));
        mockMvc.perform(get(URL + "/regiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Europa"));
    }

    @Test
    void buscarClienteExistenteDevuelve200() throws Exception {
        when(clienteService.findById(1L)).thenReturn(crearCliente(1L));
        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("linus@linux.org"));
    }

    @Test
    void buscarClienteInexistenteDevuelve404ConJson() throws Exception {
        when(clienteService.findById(999L)).thenThrow(new ClienteNotFoundException(999L));
        mockMvc.perform(get(URL + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.mensaje").value("Cliente con id 999 no encontrado"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void crearClienteValidoDevuelve201() throws Exception {
        when(clienteService.save(any(Cliente.class))).thenReturn(crearCliente(1L));
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crearClienteInvalidoDevuelve400() throws Exception {
        String invalido = """
                { "nombre": "", "email": "correo-malo" }
                """;
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nombre").exists())
                .andExpect(jsonPath("$.email").exists());
        verify(clienteService, never()).save(any(Cliente.class));
    }

    @Test
    void actualizarClienteDevuelve201() throws Exception {
        when(clienteService.update(eq(1L), any(Cliente.class))).thenReturn(crearCliente(1L));
        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Linus"));
    }

    @Test
    void actualizarClienteInexistenteDevuelve404() throws Exception {
        when(clienteService.update(eq(999L), any(Cliente.class)))
                .thenThrow(new ClienteNotFoundException(999L));
        mockMvc.perform(put(URL + "/999").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void eliminarClienteDevuelve204() throws Exception {
        mockMvc.perform(delete(URL + "/1"))
                .andExpect(status().isNoContent());
        verify(clienteService).delete(1L);
    }

    @Test
    void eliminarClienteInexistenteDevuelve404() throws Exception {
        doThrow(new ClienteNotFoundException(999L)).when(clienteService).delete(999L);
        mockMvc.perform(delete(URL + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Cliente con id 999 no encontrado"));
    }

    @Test
    void errorDelServicioDevuelve500SinDetalles() throws Exception {
        when(clienteService.save(any(Cliente.class)))
                .thenThrow(new ClienteServiceException("detalle interno de la bd"));
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.mensaje", not(containsString("detalle interno"))));
    }

    @Test
    void errorGenericoDevuelve500SinDetalles() throws Exception {
        when(clienteService.findAll()).thenThrow(new RuntimeException("boom"));
        mockMvc.perform(get(URL))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.mensaje", not(containsString("boom"))));
    }
}