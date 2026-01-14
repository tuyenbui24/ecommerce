package com.example.ecommerce.security.auth;

import com.example.ecommerce.config.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fieldErrors.put(e.getField(), e.getDefaultMessage()));

        String top = fieldErrors.values().stream().findFirst().orElse("Dữ liệu không hợp lệ");
        Map<String, Object> body = new HashMap<>();
        body.put("message", top);
        body.put("errors", fieldErrors);
        return body;
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBadRequest(BadRequestException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        if (ex.getMessage() != null &&
                ex.getMessage().toLowerCase().contains("tên sản phẩm")) {
            errors.put("name", ex.getMessage());
        }
        if (!errors.isEmpty()) body.put("errors", errors);
        return body;
    }

}

