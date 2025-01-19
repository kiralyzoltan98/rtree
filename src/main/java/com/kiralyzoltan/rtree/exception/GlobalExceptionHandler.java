package com.kiralyzoltan.rtree.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // BAD_REQUEST 400

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException e) {
        log.error("IOException ", e);
        return ResponseEntity.badRequest().body("IOException on: " + e.getMessage());
    }


    // INTERNAL_SERVER_ERROR 500

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Uncaught exception! ", e);
        return ResponseEntity.internalServerError().body("Uncaught exception: " + e.getMessage());
    }
}
