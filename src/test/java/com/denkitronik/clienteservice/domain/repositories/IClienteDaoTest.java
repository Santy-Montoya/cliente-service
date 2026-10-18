package com.denkitronik.clienteservice.domain.repositories;

import com.denkitronik.clienteservice.domain.entities.Cliente;
import com.denkitronik.clienteservice.domain.entities.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

// Prueba de la capa de datos contra H2 (cada test se deshace al terminar)
@SpringBootTest
@Transactional
class IClienteDaoTest {

    @Autowired
    private IClienteDao clienteDao;

    @Autowired
    private IRegionDao regionDao;

    private Region region;

    @BeforeEach
    void setUp() {
        region = new Region();
        region.setNombre("Europa");
        region = regionDao.save(region);
    }

    private Cliente nuevoCliente(String email) {
        Cliente c = new Cliente();
        c.setNombre("Ada");
        c.setApellido("Lovelace");
        c.setEmail(email);
        c.setRegion(region);
        return c;
    }

    @Test
    void guardarAsignaIdYFecha() {
        Cliente guardado = clienteDao.save(nuevoCliente("ada@mail.com"));
        assertNotNull(guardado.getId());
        assertNotNull(guardado.getCreateAt());
    }

    @Test
    void buscarPorIdEncuentraElCliente() {
        Cliente guardado = clienteDao.save(nuevoCliente("ada2@mail.com"));
        assertTrue(clienteDao.findById(guardado.getId()).isPresent());
    }

    @Test
    void existsByIdDevuelveFalseSiNoExiste() {
        assertFalse(clienteDao.existsById(999999L));
    }

    @Test
    void eliminarBorraElCliente() {
        Cliente guardado = clienteDao.save(nuevoCliente("ada3@mail.com"));
        clienteDao.deleteById(guardado.getId());
        assertFalse(clienteDao.existsById(guardado.getId()));
    }

    @Test
    void findAllRegionesDevuelveRegiones() {
        assertFalse(clienteDao.findAllRegiones().isEmpty());
    }
}