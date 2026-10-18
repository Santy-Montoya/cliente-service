package com.denkitronik.clienteservice.domain.services;

import com.denkitronik.clienteservice.domain.entities.Cliente;
import com.denkitronik.clienteservice.domain.entities.Region;
import com.denkitronik.clienteservice.domain.exception.ClienteNotFoundException;
import com.denkitronik.clienteservice.domain.exception.ClienteServiceException;
import com.denkitronik.clienteservice.domain.repositories.IClienteDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private IClienteDao clienteDao;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente crearCliente(Long id, String nombre, String email) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setNombre(nombre);
        c.setApellido("Prueba");
        c.setEmail(email);
        c.setFoto("foto.jpg");
        return c;
    }

    @Test
    void findAllDevuelveLaLista() {
        when(clienteDao.findAll()).thenReturn(List.of(crearCliente(1L, "Ana", "ana@mail.com")));
        assertEquals(1, clienteService.findAll().size());
    }

    @Test
    void findByIdDevuelveElCliente() {
        when(clienteDao.findById(1L)).thenReturn(Optional.of(crearCliente(1L, "Ana", "ana@mail.com")));
        assertEquals("Ana", clienteService.findById(1L).getNombre());
    }

    @Test
    void findByIdLanzaExcepcionSiNoExiste() {
        when(clienteDao.findById(999L)).thenReturn(Optional.empty());
        ClienteNotFoundException ex = assertThrows(ClienteNotFoundException.class,
                () -> clienteService.findById(999L));
        assertEquals("Cliente con id 999 no encontrado", ex.getMessage());
    }

    @Test
    void saveGuardaElCliente() {
        Cliente c = crearCliente(null, "Ana", "ana@mail.com");
        when(clienteDao.save(c)).thenReturn(c);
        assertEquals("Ana", clienteService.save(c).getNombre());
    }

    @Test
    void saveLanzaClienteServiceExceptionSiFallaLaBd() {
        Cliente c = crearCliente(null, "Ana", "ana@mail.com");
        when(clienteDao.save(c)).thenThrow(new DataIntegrityViolationException("email duplicado"));
        assertThrows(ClienteServiceException.class, () -> clienteService.save(c));
    }

    @Test
    void updateCopiaLosCamposYConservaCreateAt() {
        Date fecha = new Date();
        Cliente actual = crearCliente(1L, "Ana", "ana@mail.com");
        actual.setCreateAt(fecha);
        Cliente datos = crearCliente(null, "Ana Maria", "anamaria@mail.com");
        datos.setRegion(new Region());

        when(clienteDao.findById(1L)).thenReturn(Optional.of(actual));
        when(clienteDao.save(actual)).thenReturn(actual);

        Cliente resultado = clienteService.update(1L, datos);
        assertEquals("Ana Maria", resultado.getNombre());
        assertEquals("anamaria@mail.com", resultado.getEmail());
        assertEquals(fecha, resultado.getCreateAt());
    }

    @Test
    void updateLanzaExcepcionSiNoExiste() {
        when(clienteDao.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ClienteNotFoundException.class,
                () -> clienteService.update(999L, crearCliente(null, "X", "x@mail.com")));
    }

    @Test
    void updateLanzaClienteServiceExceptionSiFallaLaBd() {
        Cliente actual = crearCliente(1L, "Ana", "ana@mail.com");
        when(clienteDao.findById(1L)).thenReturn(Optional.of(actual));
        when(clienteDao.save(actual)).thenThrow(new DataIntegrityViolationException("error"));
        assertThrows(ClienteServiceException.class,
                () -> clienteService.update(1L, crearCliente(null, "B", "b@mail.com")));
    }

    @Test
    void deleteEliminaSiExiste() {
        when(clienteDao.existsById(1L)).thenReturn(true);
        clienteService.delete(1L);
        verify(clienteDao).deleteById(1L);
    }

    @Test
    void deleteLanzaExcepcionSiNoExiste() {
        when(clienteDao.existsById(999L)).thenReturn(false);
        assertThrows(ClienteNotFoundException.class, () -> clienteService.delete(999L));
        verify(clienteDao, never()).deleteById(999L);
    }

    @Test
    void findAllRegionesDevuelveLaLista() {
        when(clienteDao.findAllRegiones()).thenReturn(List.of(new Region()));
        assertEquals(1, clienteService.findAllRegiones().size());
    }

    @Test
    void prePersistAsignaLaFecha() {
        Cliente c = new Cliente();
        c.prePersist();
        assertNotNull(c.getCreateAt());
    }

    @Test
    void clienteServiceExceptionGuardaElMensaje() {
        ClienteServiceException ex = new ClienteServiceException("fallo");
        assertEquals("fallo", ex.getMessage());
    }
}