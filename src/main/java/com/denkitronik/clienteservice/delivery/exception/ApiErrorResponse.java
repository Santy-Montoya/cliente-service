package com.denkitronik.clienteservice.delivery.exception;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ApiErrorResponse {

    private int status;
    private String error;
    private String mensaje;
    private LocalDateTime timestamp;

    public ApiErrorResponse(int status, String error, String mensaje) {
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.timestamp = LocalDateTime.now();
    }
}