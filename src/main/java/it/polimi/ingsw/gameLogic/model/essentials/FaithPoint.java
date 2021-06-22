package it.polimi.ingsw.gameLogic.model.essentials;


import it.polimi.ingsw.gameLogic.exceptions.LastRoundException;
import it.polimi.ingsw.gameLogic.exceptions.NegativeQuantityException;
import it.polimi.ingsw.gameLogic.model.match.player.Adder;

/**
 * Basic element of the game, it is an immutable element.
 * It represents a certain quantity of faith points.
 */
public class FaithPoint implements Resource{
    private final int quantity;

    /**
     * constructor
     * @param quantity how much FaithPoints contains this FaithPoints object
     * @throws NegativeQuantityException if the number of points is negative or 0
     */
    public FaithPoint(int quantity) throws NegativeQuantityException {
        if(quantity >= 0)
            this.quantity = quantity;
        else
            throw new NegativeQuantityException("Negative quantity of faith");
    }

    /**
     * Getter
     * @return false to show that this is a faith point.
     */
    @Override
    public boolean isPhysical(){ return false; }

    /**
     * Adds the points contained in this to the player's FaithPath thanks to the adder's method addFaithPoints
     * @param adder the player's interface
     * @return true if the player's method addFaithPoints worked
     * @throws LastRoundException if the player reaches the end of his FaithPath
     */
    @Override
    public boolean add(Adder adder) throws LastRoundException {
       return adder.addFaithPoints(quantity);
    }

    /**
     * The override of the "getQuantity" method.
     * @return the quantity of faith points that compose this resource's instance.
     */
    @Override
    public int getQuantity(){ return quantity; }

    /**
     * The override of the equals method
     * @param o the other FaithPoint object to be compared
     * @return true if this and o have the same quantity
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FaithPoint that = (FaithPoint) o;
        return quantity == that.quantity;
    }


    @Override
    public String toString() {
        return "FaithPoint{" +
                "quantity=" + quantity +
                '}';
    }
}