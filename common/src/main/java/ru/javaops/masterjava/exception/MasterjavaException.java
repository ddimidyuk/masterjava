package ru.javaops.masterjava.exception;

/**
 *  Base project exception
 */
public class MasterjavaException extends RuntimeException {

    public MasterjavaException() {
    }

    public MasterjavaException(String message) {
        super(message);
    }
}
