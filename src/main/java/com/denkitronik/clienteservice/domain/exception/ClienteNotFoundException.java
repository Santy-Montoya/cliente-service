package com.denkitronik.clienteservice.domain.exception;

public class ClienteNotFoundException extends RuntimeException {

    public ClienteNotFoundException(Long id) {
        super("Cliente con id " + id + " no encontrado");
    }
}