package it.polimi.ingsw.network.message.stocmessage;

import it.polimi.ingsw.network.client.Client;
import it.polimi.ingsw.network.server.ControlBase;

/**
 * This class implements the BinarySelectionMessage from server to client.
 * Message structure: { nickname, selection, string-made comment }
 */
public class BinarySelectionMessage extends StoCMessage {
    private static final StoCMessageType type = StoCMessageType.BINARY_SELECTION;
    private final boolean selection;
    private final String comment;

    public BinarySelectionMessage(String nickname, boolean selection, String comment){
        super(nickname);
        this.selection = selection;
        this.comment = comment;
    }

    public BinarySelectionMessage(String nickname, boolean selection) {
        super(nickname);
        this.selection = selection;
        comment = null;
    }

    @Override
    public boolean compute(Client client) {
        return true;
    }

    /**
     * Getter
     * @return the message's type
     */
    public StoCMessageType getType() {
        return type;
    }

    public boolean getSelection() {
        return selection;
    }

    public String getComment() {
        return comment;
    }

}