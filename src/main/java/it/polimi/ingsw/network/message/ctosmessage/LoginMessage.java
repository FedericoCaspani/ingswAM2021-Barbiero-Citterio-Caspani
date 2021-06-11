package it.polimi.ingsw.network.message.ctosmessage;

import it.polimi.ingsw.network.message.stocmessage.RetryMessage;
import it.polimi.ingsw.network.server.ControlBase;

/**
 * This class implements the LoginMessage.
 */
public class LoginMessage extends CtoSMessage{
    private static final CtoSMessageType type = CtoSMessageType.LOGIN;

    public LoginMessage(String nickname) {
        super(nickname);
    }

    @Override
    public boolean computeMessage(ControlBase controlBase) {
        if(controlBase.getInitController().login(getNickname()))
            return true;
        controlBase.write(new RetryMessage(controlBase.getNickname(), controlBase.getCurrentState(), "You can't send a " + type + " message in this moment"));
        return false;
    }

    @Override
    public boolean isSomethingNull() {
        return getNickname() == null;
    }

    @Override
    public CtoSMessageType getType() {
        return type;
    }
}
