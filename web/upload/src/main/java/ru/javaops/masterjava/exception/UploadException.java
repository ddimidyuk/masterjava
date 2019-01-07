package ru.javaops.masterjava.exception;

/**
 * Exception thrown while upload processing
 */
public class UploadException extends MasterjavaException {

    public UploadException() {
    }

    public UploadException(String message) {
        super(message);
    }
}
