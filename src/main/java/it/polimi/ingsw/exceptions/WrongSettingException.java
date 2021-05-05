package it.polimi.ingsw.exceptions;

public class WrongSettingException extends Exception
{
    private String error;
    public WrongSettingException(String error)
    {
        this.error=error;
    }

    public String getError() {return error;}
}