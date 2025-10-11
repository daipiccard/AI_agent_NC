package com.transacciones.transaction_ingestor.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase que actúa como un Manejador de Excepciones Global (@ControllerAdvice).
 * Intercepta los errores de validación de Spring (@Valid) y los formatea
 * en un JSON de respuesta limpio.
 */
@ControllerAdvice
public class ValidationExceptionHandler {

    /**
     * Maneja la excepción específica que ocurre cuando falla la anotación @Valid.
     * @param ex La excepción generada por Spring Boot.
     * @return Una respuesta HTTP 400 (Bad Request) con los detalles del error.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 1. Crear un mapa para almacenar los errores
        Map<String, String> errors = new HashMap<>();

        // 2. Iterar sobre todos los errores de campo que encontró la validación
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            // Obtener el nombre del campo que falló (e.g., "monto")
            String fieldName = error.getField();
            // Obtener el mensaje de error definido en el modelo (e.g., "El monto es obligatorio.")
            String errorMessage = error.getDefaultMessage();
            
            // 3. Agregar el error al mapa
            errors.put(fieldName, errorMessage);
        });

        // 4. Devolver una respuesta con el estado HTTP 400 (Bad Request) y el cuerpo JSON de errores
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}

