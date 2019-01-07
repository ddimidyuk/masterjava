package ru.javaops.masterjava.utils;

import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.exception.ValidationException;

@Slf4j
public class Validator {
    private Validator() {
    }

    public static void checkNotNull(Object o){
        if(o == null){
            throw new ValidationException();
        }
    }
}
