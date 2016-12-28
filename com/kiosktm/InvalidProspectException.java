package com.kiosktm;

import java.util.ArrayList;

public class InvalidProspectException extends Exception {

    private static final String MESSAGE = "Submitted Prospect Contains Invalid Fields";

    private ArrayList<String> invalidFields;

    public InvalidProspectException() {
        super(InvalidProspectException.MESSAGE);
        this.setInvalidFields(new ArrayList<String>());
    }

    public InvalidProspectException(ArrayList<String> invalidFields) {
        super(InvalidProspectException.MESSAGE);
        this.setInvalidFields(invalidFields);
    }

    public ArrayList<String> getInvalidFields() {
        return this.invalidFields;
    }

    public void setInvalidFields(ArrayList<String> invalidFields) {
        this.invalidFields = invalidFields;
    }

}
