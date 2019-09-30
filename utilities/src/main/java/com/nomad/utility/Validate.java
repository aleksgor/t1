package com.nomad.utility;

public class Validate {
    public static void notNull(Object parameter, String s) {
        if (parameter == null) {
            throw new IllegalArgumentException(s);
        }

    }
}
