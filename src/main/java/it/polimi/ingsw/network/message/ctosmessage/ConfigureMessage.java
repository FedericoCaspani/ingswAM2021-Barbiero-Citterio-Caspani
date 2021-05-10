package it.polimi.ingsw.network.message.ctosmessage;

import it.polimi.ingsw.model.match.MatchConfiguration;
import it.polimi.ingsw.network.server.ControlBase;

/**
 * This class implements a message from the client to initialize the configuration of the match
 * Message structure: { nickname, configuration }
 */
public class ConfigureMessage extends CtoSMessage {
    private static final CtoSMessageType type = CtoSMessageType.CONFIGURE;
    private final MatchConfiguration config;

    public ConfigureMessage(String nickname, MatchConfiguration config){
        super(nickname);
        this.config = config;
    }

    /**
     * Getter
     * @return the configuration of the match
     */
    public MatchConfiguration getConfig(){
        return config;
    }

    @Override
    public boolean computeMessage(ControlBase controlBase) {
        //TODO
        return false;
    }

    /**
     * Getter
     * @return the message's type
     */
    public CtoSMessageType getType() {
        return type;
    }

}