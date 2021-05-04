package it.polimi.ingsw.network.message.ctosmessage;

import it.polimi.ingsw.model.essentials.PhysicalResource;
import it.polimi.ingsw.network.message.Message;
import it.polimi.ingsw.network.server.ControlBase;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a message from the client to choose the initial bonus PhysicalResource
 * Message structure: { nickname, the chosen resource }
 */
public class StartingResourcesMessage extends CtoSMessage {
    private static final CtoSMessageType type = CtoSMessageType.STARTING_RESOURCES;
    private final List<PhysicalResource> resources;

    public StartingResourcesMessage(String nickname, List<PhysicalResource> resources) {
        super(nickname);
        this.resources = resources;
    }

    /**
     * Getter
     * @return the chosen bonus resource
     */
    public List<PhysicalResource> getResources() {
        return resources;
    }

    @Override
    public boolean computeMessage(ControlBase controlBase) {
        return false;
        //TODO
    }

    /**
     * Getter
     * @return the message's type
     */
    public CtoSMessageType getType() {
        return type;
    }

}
