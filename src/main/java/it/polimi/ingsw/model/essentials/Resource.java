package it.polimi.ingsw.model.essentials;

import it.polimi.ingsw.exceptions.LastRoundException;
import it.polimi.ingsw.model.match.player.Adder;

public interface Resource {

    /**
     * Adds the resource to the StrongBox or increments the faitMarker
     * @param adder the player's interface
     * @return true if the resource was added or the faitMarker was incremented
     * @throws LastRoundException if the player reaches the end of his FaithPath
     */

    boolean add(Adder adder) throws LastRoundException;

    boolean isPhysical();
}
