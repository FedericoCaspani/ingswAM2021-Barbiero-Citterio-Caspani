
package it.polimi.ingsw.model.match.market;

import it.polimi.ingsw.model.exceptions.MatchEndedException;
import it.polimi.ingsw.model.match.player.Adder;

public abstract class Marble {

    /**
     * This method adds a single resource or a single faithPoint to the player passed
     * @param adder the player's interface
     * @return true only if is the marble is white
     * @throws MatchEndedException if the player reaches the end of his FaithPath
     */
    
    public abstract boolean onDraw(Adder adder) throws MatchEndedException;
}
