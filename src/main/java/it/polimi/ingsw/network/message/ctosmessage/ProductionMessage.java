package it.polimi.ingsw.network.message.ctosmessage;

import it.polimi.ingsw.model.essentials.Production;
import it.polimi.ingsw.network.message.Message;

import java.util.List;

/**
 * This class implements a message from the client for starting a production
 * Message structure: { nickname, leaders' Id, a production the contains the translation of the unknown resources }
 */
public class ProductionMessage extends Message {
    private final CtoSMessageType type = CtoSMessageType.PRODUCTION;
    private final List<String> cardIds;
    private final Production ProductionOfUnknown;

    public ProductionMessage(String nickname, List<String> cardIds, Production productionOfUnknown) {
        super(nickname);
        this.cardIds = cardIds;
        ProductionOfUnknown = productionOfUnknown;
    }

    /**
     * Getter
     * @return the leaders' Id
     */
    public List<String> getCardIds() {
        return cardIds;
    }

    /**
     * Getter
     * @return the production the contains the translation of the unknown resources
     */
    public Production getProductionOfUnknown() {
        return ProductionOfUnknown;
    }

    /**
     * Getter
     * @return the message's type
     */
    public CtoSMessageType getType() {
        return type;
    }
}
