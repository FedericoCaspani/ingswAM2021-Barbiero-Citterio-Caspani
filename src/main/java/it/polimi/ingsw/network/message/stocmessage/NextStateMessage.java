package it.polimi.ingsw.network.message.stocmessage;

import it.polimi.ingsw.controller.StateName;
import it.polimi.ingsw.network.client.Client;

/**
 * This class implements the NextStateMessage from server to client.
 * Notifies the player about his next in-game possibilities.
 * Message structure: { nickname, new state }
 */
public class NextStateMessage extends StoCMessage {

    private static final StoCMessageType type = StoCMessageType.NEXT_STATE;
    private final StateName newState;

    public NextStateMessage(String nickname, StateName newState){
        super(nickname);
        this.newState = newState;
    }

    @Override
    public boolean compute(Client client){
        client.getController().updateCurrentState(this);
        return true;
    }
    @Override
    public StoCMessageType getType() { return type; }
    public StateName getNewState() { return newState; }

}
