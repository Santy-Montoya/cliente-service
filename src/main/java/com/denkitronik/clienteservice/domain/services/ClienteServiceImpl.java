package com.denkitronik.clienteservice.domain.services;

import com.denkitronik.clienteservice.domain.entities.Cliente;
import com.denkitronik.clienteservice.domain.entities.Region;
import com.denkitronik.clienteservice.domain.exception.ClienteNotFoundException;
import com.denkitronik.clienteservice.domain.exception.ClienteServiceException;
import com.denkitronik.clienteservice.domain.repositories.IClienteDao;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ClienteServiceImpl implements IClienteService {

    private final IClienteDao clienteDao;

    // Inyección por constructor (estilo recomendado)
    public ClienteServiceImpl(IClienteDao clienteDao) {
        this.clienteDao = clienteDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> findAll() {
        return clienteDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente findById(Long id) {
        return clienteDao.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));
    }

    @Override
    @Transactional
    public Cliente save(Cliente cliente) {
        try {
            return clienteDao.save(cliente);
        } catch (DataAccessException e) {
            throw new ClienteServiceException("Error al guardar el cliente", e);
        }
    }

    @Override
    @Transactional
    public Cliente update(Long id, Cliente cliente) {
        Cliente clienteActual = clienteDao.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));
        clienteActual.setNombre(cliente.getNombre());
        clienteActual.setApellido(cliente.getApellido());
        clienteActual.setEmail(cliente.getEmail());
        clienteActual.setFoto(cliente.getFoto());
        clienteActual.setRegion(cliente.getRegion());
        try {
            return clienteDao.save(clienteActual);
        } catch (DataAccessException e) {
            throw new ClienteServiceException("Error al actualizar el cliente", e);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!clienteDao.existsById(id)) {
            throw new ClienteNotFoundException(id);
        }
        clienteDao.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Region> findAllRegiones() {
        return clienteDao.findAllRegiones();
    }
}