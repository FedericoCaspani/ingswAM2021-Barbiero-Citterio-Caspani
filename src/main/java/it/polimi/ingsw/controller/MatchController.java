package it.polimi.ingsw.controller;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.essentials.Card;
import it.polimi.ingsw.model.essentials.PhysicalResource;
import it.polimi.ingsw.model.essentials.Production;
import it.polimi.ingsw.model.match.*;
import it.polimi.ingsw.model.match.player.Player;
import it.polimi.ingsw.network.message.ctosmessage.CtoSMessageType;
import it.polimi.ingsw.network.message.stocmessage.EndGameResultsMessage;
import it.polimi.ingsw.network.message.stocmessage.NextStateMessage;
import it.polimi.ingsw.network.server.ControlBase;
import it.polimi.ingsw.observer.ModelObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.polimi.ingsw.network.message.ctosmessage.CtoSMessageType.*;
import static java.util.Map.entry;
import static it.polimi.ingsw.model.match.MatchConfiguration.assignConfiguration;
import static it.polimi.ingsw.network.server.ServerUtilities.findControlBase;


public class MatchController {
    private Match match;
    private TurnController turnController;
    private StartingPhaseController startingPhaseController;
    private RematchPhaseController rematchPhaseController;
    private final Map<String, Card> cardMap;
    private static final Map<StateName, List<CtoSMessageType>> acceptedMessagesMap;
    static {
        acceptedMessagesMap = Map.ofEntries(
                entry(StateName.WAITING_LEADERS, List.of(LEADERS_CHOICE)),
                entry(StateName.WAITING_RESOURCES, List.of(STARTING_RESOURCES)),
                entry(StateName.STARTING_TURN, List.of(LEADER_ACTIVATION, LEADER_DISCARDING, SWITCH_SHELF,
                        MARKET_DRAW, DEV_CARD_DRAW, PRODUCTION)),
                entry(StateName.MARKET_ACTION, List.of(WHITE_MARBLE_CONVERSIONS, SWITCH_SHELF)),
                entry(StateName.RESOURCES_PLACEMENT, List.of(WAREHOUSE_INSERTION, SWITCH_SHELF)),
                entry(StateName.BUY_DEV_ACTION, List.of(SWITCH_SHELF, PAYMENTS)),
                entry(StateName.PLACE_DEV_CARD, List.of(SWITCH_SHELF, DEV_CARD_PLACEMENT)),
                entry(StateName.PRODUCTION_ACTION, List.of(SWITCH_SHELF, PAYMENTS)),
                entry(StateName.END_TURN, List.of(SWITCH_SHELF, LEADER_ACTIVATION, LEADER_DISCARDING, END_TURN)),
                entry(StateName.END_MATCH, List.of(REMATCH, DISCONNECTION)),
                entry(StateName.REMATCH_OFFER, List.of(REMATCH))
        );
    }


    public MatchController(List<Player> playersInMatch) {
        MatchConfiguration configuration = assignConfiguration("src/main/resources/StandardConfiguration.json");
        cardMap = initCardMap(configuration);

        //setting the starting summary to everyone
        ModelObserver obs = new Summary(playersInMatch, cardMap);
        for(Player p : playersInMatch)
            p.setSummary(obs);

        try {
            this.match = new MultiMatch(playersInMatch, configuration);
        } catch (SingleMatchException e) {
            System.err.println("internal error");
            System.exit(1);
        } catch (WrongSettingException e) {
            System.exit(1);
        }

        startingPhaseController = new StartingPhaseController(match, cardMap);
        turnController = null;
        rematchPhaseController = null;

    }

    public MatchController(List<Player> playersInMatch, MatchConfiguration configuration) throws RetryException {
        cardMap = initCardMap(configuration);

        //setting the starting summary to everyone
        ModelObserver obs = new Summary(playersInMatch, cardMap);
        for(Player p : playersInMatch)
            p.setSummary(obs);

        try {
            this.match = new MultiMatch(playersInMatch, configuration);
        } catch (SingleMatchException e) {
            System.err.println("internal error");
            System.exit(1);
        } catch (WrongSettingException e) {
            throw new RetryException("Wrong configuration");
        }

        startingPhaseController = new StartingPhaseController(match, cardMap);
        turnController = null;
        rematchPhaseController = null;

    }

    public MatchController(Player player) {
        MatchConfiguration configuration = MatchConfiguration.assignConfiguration("src/main/resources/StandardConfiguration.json");
        cardMap = initCardMap(configuration);

        //setting the starting summary to the player
        ModelObserver obs = new Summary(new ArrayList<>(List.of(player)), cardMap);
        player.setSummary(obs);

        try {
            this.match = new SingleMatch(player, configuration);
        } catch (WrongSettingException e) {
            System.exit(1);
        }

        startingPhaseController = new StartingPhaseController(match, cardMap);
        turnController = null;
        rematchPhaseController = null;

    }

    public MatchController(Player player, MatchConfiguration configuration) throws RetryException {
        cardMap = initCardMap(configuration);

        //setting the starting summary to the player
        ModelObserver obs = new Summary(new ArrayList<>(List.of(player)), cardMap);
        player.setSummary(obs);

        try {
            this.match = new SingleMatch(player, configuration);
        } catch (WrongSettingException e) {
            throw new RetryException("Wrong configuration");
        }

        startingPhaseController = new StartingPhaseController(match, cardMap);
        turnController = null;
        rematchPhaseController = null;

    }
    //%%%%%%%%%%%%%%%%%% GETTER %%%%%%%%%%%%%%%%%%%
    public Player getCurrentPlayer(){
        return match.getCurrentPlayer();
    }

    public StateName getCurrentState(String nickname){
        if(turnController == null)
            return startingPhaseController.getPlayerState(nickname);
        if(rematchPhaseController != null)
            return StateName.REMATCH_OFFER;
        else
            return turnController.getCurrentState(); }
    public Map<String, Card> getCardMap(){
        return cardMap;
    }

    public Match getMatch(){
        return match;
    }


    //%%%%%%%%%%%%%%%%%% SETTER %%%%%%%%%%%%%%%%%%%

    public void setState(String nickname, StateName state){
        if(state.getVal()<4)
            startingPhaseController.setState(nickname, state);
        else if(state.getVal()<12){
            if (turnController == null)
                turnController = new TurnController(match, cardMap);
            turnController.setCurrentState(state);
        }
    }

    public void setWhiteMarblesDrawn(int num){ turnController.setWhiteMarbleDrawn(num);}

    public Map<String, Card> initCardMap(MatchConfiguration configuration){
        Map<String, Card> map = new HashMap<>();
        for (int i=1; i<=configuration.getAllDevCards().size(); i++)
            map.put("D"+i,configuration.getAllDevCards().get(i-1));
        for (int i=1; i<=configuration.getAllLeaderCards().size(); i++)
            map.put("L"+i,configuration.getAllLeaderCards().get(i-1));

        return map;
    }

    private boolean isComputable(String nickname, CtoSMessageType type) throws RetryException {
        if (turnController == null) {
            if(match.getPlayer(nickname) == null)
                return false;
        if (!acceptedMessagesMap.get(startingPhaseController.getPlayerState(nickname)).contains(type))
                throw new RetryException("This message is not accepted in this phase");
        } else if (rematchPhaseController == null) {
            if (!nickname.equals(turnController.getCurrentPlayer().getNickname()))
                return false;
            else if (!acceptedMessagesMap.get(turnController.getCurrentState()).contains(type))
                throw new RetryException("This message is not accepted in this phase");
        } else if (!type.equals(CtoSMessageType.REMATCH))
            throw new RetryException("This message is not accepted in this phase");


        return true;
    }

    public synchronized boolean startingLeader(String nickname, List<String> leaders) throws RetryException {
        boolean isComputable = isComputable(nickname, LEADERS_CHOICE);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, startingPhaseController.leadersChoice(nickname, leaders));
        ControlBase playerHandler = findControlBase(nickname);
        if(playerHandler != null)
            playerHandler.write(message);

        if (startingPhaseController.hasEnded())
            turnController = new TurnController(match, cardMap);
        return true;

    }

    public synchronized boolean startingResources(String nickname, List<PhysicalResource> resources) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.STARTING_RESOURCES);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, startingPhaseController.startingResources(nickname, resources));
        ControlBase playerHandler = findControlBase(nickname);
        if(playerHandler != null)
            playerHandler.write(message);

        if (startingPhaseController.hasEnded())
            turnController = new TurnController(match, cardMap);
        return true;
    }

    public synchronized boolean nextTurn(String nickname) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.END_TURN);
        if (!isComputable)
            return false;
        ControlBase playerHandler = findControlBase(nickname);

        try {
            NextStateMessage message = new NextStateMessage(turnController.getCurrentPlayer().getNickname(), StateName.WAITING_FOR_TURN);
            turnController.nextTurn();

            if(playerHandler != null)
                playerHandler.write(message);
        } catch (MatchEndedException e) {
            rematchPhaseController = new RematchPhaseController(match.getPlayers());
            EndGameResultsMessage message = new EndGameResultsMessage(nickname, e.getMsg(), e.getRanking());

            if(playerHandler != null)
                playerHandler.write(message);
        }

        return true;
    }

    public synchronized boolean switchShelf(String nickname, int shelf1, int shelf2) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.SWITCH_SHELF);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.switchShelf(shelf1, shelf2));
        ControlBase playerHandler = findControlBase(nickname);
        if(playerHandler != null)
            playerHandler.write(message);

        return true;

    }

    public synchronized boolean leaderActivation(String nickname, String leaderId) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.LEADER_ACTIVATION);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.leaderActivation(leaderId));
        ControlBase playerHandler = findControlBase(nickname);
        if(playerHandler != null)
            playerHandler.write(message);

        return true;
    }

    public synchronized boolean leaderDiscarding(String nickname, String leaderId) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.LEADER_DISCARDING);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.leaderDiscarding(leaderId));
        ControlBase playerHandler = findControlBase(nickname);
        if(playerHandler != null)
            playerHandler.write(message);

        return true;
    }

    public synchronized boolean marketDraw(String nickname, boolean row, int num) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.MARKET_DRAW);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.marketDraw(row, num));
        ControlBase playerHandler = findControlBase(nickname);
        if(playerHandler != null)
            playerHandler.write(message);

        return true;
    }

    public synchronized boolean whiteMarblesConversion(String nickname, List<PhysicalResource> resources) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.WHITE_MARBLE_CONVERSIONS);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.whiteMarblesConversion(resources));
        ControlBase playerHandler = findControlBase(nickname);
        if(playerHandler != null)
            playerHandler.write(message);

        return true;
    }

    public synchronized boolean warehouseInsertion(String nickname, List<PhysicalResource> resources) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.WAREHOUSE_INSERTION);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.warehouseInsertion(resources));
        ControlBase playerHandler = findControlBase(nickname);
        if(playerHandler != null)
            playerHandler.write(message);

        return true;
    }

    public synchronized boolean devCardDraw(String nickname, int row, int column) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.DEV_CARD_DRAW);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.devCardDraw(row, column));
        ControlBase playerHandler = findControlBase(nickname);
        if(playerHandler != null)
            playerHandler.write(message);

        return true;
    }

    public synchronized boolean payments(String nickname, List<PhysicalResource> strongboxCosts, Map<Integer, PhysicalResource> warehouseCosts) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.PAYMENTS);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.payments(strongboxCosts, warehouseCosts));
        ControlBase playerHandler = findControlBase(nickname);
        if(playerHandler != null)
            playerHandler.write(message);

        return true;
    }

    public synchronized boolean devCardPlacement(String nickname, int column) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.DEV_CARD_PLACEMENT);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.devCardPlacement(column));
        ControlBase playerHandler = findControlBase(nickname);
        if(playerHandler != null)
            playerHandler.write(message);

        return true;
    }
    public synchronized boolean production(String nickname, List<String> cardIds, Production productionOfUnknown) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.PRODUCTION);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.production(cardIds, productionOfUnknown));
        ControlBase playerHandler = findControlBase(nickname);
        if(playerHandler != null)
            playerHandler.write(message);

        return true;
    }

    public synchronized boolean response(String nickname, boolean value) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.PRODUCTION);
        if (!isComputable)
            return false;
        rematchPhaseController.response(nickname, value);
        return true;
    }
}
