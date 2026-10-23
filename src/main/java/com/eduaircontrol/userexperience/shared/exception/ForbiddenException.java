package com.eduaircontrol.userexperience.shared.exception;

/**
 * Peticion autenticada pero fuera de su institución (ADR-016).
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
