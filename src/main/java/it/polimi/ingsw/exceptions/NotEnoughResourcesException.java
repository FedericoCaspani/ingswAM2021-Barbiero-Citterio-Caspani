package it.polimi.ingsw.exceptions;

public class NotEnoughResourcesException extends InvalidOperationException
{
    private String err;
    public NotEnoughResourcesException (String err) {
        super(err);
        this.err=err; }

    public String getError() {return err;}
}