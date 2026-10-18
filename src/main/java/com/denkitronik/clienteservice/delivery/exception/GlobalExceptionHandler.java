package com.denkitronik.clienteservice.delivery.exception;

import com.denkitronik.clienteservice.domain.exception.ClienteNotFoundException;
import com.denkitronik.clienteservice.domain.exception.ClienteServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 - Cliente no encontrado
    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> manejarNoEncontrado(ClienteNotFoundException ex) {
        log.warn("Cliente no encontrado: {}", ex.getMessage());
        ApiErrorResponse respuesta = new ApiErrorResponse(404, "Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
    }

    // 500 - Error del servicio (sin detalles internos)
    @ExceptionHandler(ClienteServiceException.class)
    public ResponseEntity<ApiErrorResponse> manejarErrorServicio(ClienteServiceException ex) {
        log.error("Error en el servicio de clientes", ex);
        ApiErrorResponse respuesta = new ApiErrorResponse(500, "Internal Server Error",
                "Error interno del servicio de clientes");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
    }

    // 400 - Datos inválidos: mapa campo -> mensaje
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        log.debug("Errores de validación: {}", errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    // 500 - Cualquier otro error (sin detalles internos)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> manejarGenerico(Exception ex) {
        log.error("Error inesperado", ex);
        ApiErrorResponse respuesta = new ApiErrorResponse(500, "Internal Server Error",
                "Ocurrió un error inesperado");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
    }
}