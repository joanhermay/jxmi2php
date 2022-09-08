package io.github.joanhermay.jxmi2php;

/**
 * @author Josué Andrés Hernández Martínez
 */
public class GeneradorPHPException extends Exception {
    public GeneradorPHPException(String message) {
        super(message);
    }

    public GeneradorPHPException(String message, Throwable cause) {
        super(message, cause);
    }
}
