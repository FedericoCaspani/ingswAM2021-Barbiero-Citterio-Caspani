package it.polimi.ingsw.network.message.stocmessage;

import it.polimi.ingsw.network.client.Client;

/**
 * This class implements the PlayerConnectionStateMessage from server to client.
 * Used to notify players about a player that has disconnected/reconnected.
 * Message structure: { nickname, connection flag }
 */
public class PlayerConnectionStateMessage extends StoCMessage{
    private static final StoCMessageType type = StoCMessageType.PLAYER_DISCONNECTED;
    private final boolean connected;

    public PlayerConnectionStateMessage(String nickname, boolean connected) {
        super(nickname);
        this.connected = connected;
    }


    @Override
    public StoCMessageType getType() {
        return type;
    }

    @Override
    public boolean compute(Client client) {
        client.getController().getMatch().setConnected(getNickname(), connected);
        return false;
    }

    public boolean getConnected(){
        return connected;
    }
}
