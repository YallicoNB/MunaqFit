package com.munaqfit.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<Map<String, Object>> build(String message, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(CustomExceptions.NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(CustomExceptions.NotFoundException ex) {
        return build(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomExceptions.BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(CustomExceptions.BadRequestException ex) {
        return build(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomExceptions.DuplicateException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(CustomExceptions.DuplicateException ex) {
        return build(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CustomExceptions.UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(CustomExceptions.UnauthorizedException ex) {
        return build(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(CustomExceptions.ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(CustomExceptions.ForbiddenException ex) {
        return build(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return build("DNI o contraseña incorrectos", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        return build(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return build("Acceso denegado", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return build("Violación de integridad de datos", HttpStatus.CONFLICT);
    }

    /**
     * Regla de negocio violada por el estado actual de los datos: stock
     * insuficiente, venta ya pagada o cancelada. Es un conflicto de
     * proceso (409) para que el empleado reintente o cancele, no un
     * error de servidor.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleEstadoInvalido(IllegalStateException ex) {
        log.warn("Regla de negocio rechazada: {}", ex.getMessage());
        return build(ex.getMessage(), HttpStatus.CONFLICT);
    }

    /**
     * Dos transacciones escribieron el mismo registro con la version ya
     * cambiada por la otra (bloqueo optimista del @Version). Es un choque
     * esperado de concurrencia: 409 para que el cliente reintente, no 500.
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLock(OptimisticLockingFailureException ex) {
        log.warn("Conflicto de concurrencia detectado: {}", ex.getMessage());
        return build("El registro fue modificado por otra operacion mientras se procesaba. Reintente.",
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .findFirst()
                .orElse("Datos inválidos");
        return build(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Una URL que no corresponde a ningun endpoint es un error del cliente
     * (404), no del servidor. Sin esto, un simple typo en la ruta devolvia
     * 500 y ensuciaba el monitoreo de errores reales.
     */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNoHandler(Exception ex) {
        return build("Recurso no encontrado: " + ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Un parametro con el tipo equivocado (por ejemplo un rango de fechas
     * con formato "x") es un error del cliente, no una caida del servidor.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String esperado = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "el tipo esperado";
        return build("El parametro '" + ex.getName() + "' no tiene un valor valido. Se esperaba "
                + esperado + ".", HttpStatus.BAD_REQUEST);
    }

    /**
     * Un parametro obligatorio que no viene en la URL es un error del
     * cliente (400), no del servidor. Sin esto, por ejemplo
     * GET /api/admin/reportes/ventas sin inicio ni fin caia en el
     * handler generico y devolvia 500.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        return build("Falta el parametro obligatorio '" + ex.getParameterName() + "'", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return build("El cuerpo de la peticion no es un JSON valido", HttpStatus.BAD_REQUEST);
    }

    /**
     * La ruta existe pero no acepta ese verbo HTTP (un GET sobre un
     * endpoint que es POST). Responde 405 con el verbo correcto, no 500.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.METHOD_NOT_ALLOWED.value());
        body.put("message", "Metodo no permitido en esta ruta. Se admiten: "
                + String.join(", ", ex.getSupportedHttpMethods() == null
                        ? new String[0] : ex.getSupportedMethods()));
        return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        // El detalle se registra en el log del servidor, no en la respuesta:
        // ex.getMessage() puede traer fragmentos de SQL o rutas del sistema
        // y no debe llegarle al cliente.
        log.error("Error no controlado", ex);
        return build("Error interno del servidor. Intentalo de nuevo o contacta al administrador.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
