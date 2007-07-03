package com.enea.jcarder.util;

public class InvalidOptionException extends Exception {
    private final String mOption;

    public InvalidOptionException(String option) {
        mOption = option;
    }

    public String getOption() {
        return mOption;
    }
}
