package me.ryzeon.dinet.orders.adapter.in.web.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import me.ryzeon.dinet.orders.adapter.in.web.pipeline.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 15:10</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponse> missingHeader(
            MissingRequestHeaderException ex, HttpServletRequest request) {
        String msg = "Falta la cabecera: " + ex.getHeaderName();
        return build(
                HttpStatus.BAD_REQUEST,
                "MISSING_HEADER",
                msg,
                List.of(new ApiErrorDetail(ex.getHeaderName(), msg)),
                request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> badRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), List.of(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> missingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        String msg = "Falta el parametro: " + ex.getParameterName();
        return build(
                HttpStatus.BAD_REQUEST,
                "MISSING_PARAMETER",
                msg,
                List.of(new ApiErrorDetail(ex.getParameterName(), msg)),
                request);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiErrorResponse> missingPart(
            MissingServletRequestPartException ex, HttpServletRequest request) {
        String msg = "Falta la parte multipart: " + ex.getRequestPartName();
        return build(
                HttpStatus.BAD_REQUEST,
                "MISSING_PART",
                msg,
                List.of(new ApiErrorDetail(ex.getRequestPartName(), msg)),
                request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> validation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ApiErrorDetail(err.getField(), err.getDefaultMessage()))
                .collect(Collectors.toList());
        return build(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Error de validacion",
                details,
                request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> constraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        List<ApiErrorDetail> details = ex.getConstraintViolations().stream()
                .map(v -> new ApiErrorDetail(v.getPropertyPath().toString(), v.getMessage()))
                .collect(Collectors.toList());
        return build(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Violacion de restricciones",
                details,
                request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> notReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST",
                "No se pudo leer el body",
                List.of(),
                request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> uploadTooLarge(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return build(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "FILE_TOO_LARGE",
                "El archivo pesa mas de lo permitido",
                List.of(),
                request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(NoResourceFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", "No existe esa ruta", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> fallback(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Error interno (ver logs)",
                List.of(),
                request);
    }

    private static ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String code,
            String message,
            List<ApiErrorDetail> details,
            HttpServletRequest request) {
        String correlationId = correlationId(request);
        var body = new ApiErrorResponse(code, message, details, correlationId);
        return ResponseEntity.status(status).header(CorrelationIdFilter.HEADER_NAME, correlationId).body(body);
    }

    private static String correlationId(HttpServletRequest request) {
        Object attr = request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE);
        if (attr instanceof String s && !s.isBlank()) {
            return s;
        }
        return UUID.randomUUID().toString();
    }
}
