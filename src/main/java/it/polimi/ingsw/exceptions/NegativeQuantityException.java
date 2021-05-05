package it.polimi.ingsw.exceptions;

public class NegativeQuantityException extends InvalidQuantityException {
    private String error;

    public NegativeQuantityException(String error) { super(error);  }

    public String getError() {return error;}
}