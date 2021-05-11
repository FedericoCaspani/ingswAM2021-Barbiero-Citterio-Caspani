package it.polimi.ingsw.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.essentials.Card;
import it.polimi.ingsw.model.essentials.PhysicalResource;
import it.polimi.ingsw.model.essentials.Production;
import it.polimi.ingsw.model.match.*;
import it.polimi.ingsw.model.match.player.Player;
import it.polimi.ingsw.network.message.ctosmessage.CtoSMessageType;
import it.polimi.ingsw.network.message.stocmessage.EndGameResultsMessage;
import it.polimi.ingsw.network.message.stocmessage.NextStateMessage;
import it.polimi.ingsw.observer.ModelObservable;
import it.polimi.ingsw.observer.ModelObserver;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.polimi.ingsw.network.message.ctosmessage.CtoSMessageType.*;
import static java.util.Map.entry;

import static it.polimi.ingsw.gsonUtilities.GsonHandler.*;
import static it.polimi.ingsw.gsonUtilities.GsonHandler.effectConfig;

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
        MatchConfiguration configuration = assignConfiguration("src/test/resources/StandardConfiguration.json");
        this.cardMap = new HashMap<>();
        for (int i=1; i<=configuration.getAllDevCards().size(); i++)
            cardMap.put("D"+i,configuration.getAllDevCards().get(i-1));
        for (int i=1; i<=configuration.getAllLeaderCards().size(); i++)
            cardMap.put("L"+i,configuration.getAllLeaderCards().get(i-1));

        //setting the starting summary to eveyone
        ModelObserver obs = new Summary(playersInMatch);
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
        this.cardMap = new HashMap<>();
        for (int i=1; i<=configuration.getAllDevCards().size(); i++)
            cardMap.put("D"+i,configuration.getAllDevCards().get(i-1));
        for (int i=1; i<=configuration.getAllLeaderCards().size(); i++)
            cardMap.put("L"+i,configuration.getAllLeaderCards().get(i-1));

        //setting the starting summary to eveyone
        ModelObserver obs = new Summary(playersInMatch);
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
        MatchConfiguration configuration = assignConfiguration("src/test/resources/StandardConfiguration.json");
        this.cardMap = new HashMap<>();
        for (int i=1; i<=configuration.getAllDevCards().size(); i++)
            cardMap.put("D"+i,configuration.getAllDevCards().get(i-1));
        for (int i=1; i<=configuration.getAllLeaderCards().size(); i++)
            cardMap.put("L"+i,configuration.getAllLeaderCards().get(i-1));

        //setting the starting summary to the player
        ModelObserver obs = new Summary(new ArrayList<>(List.of(player)));
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
        this.cardMap = new HashMap<>();
        for (int i=1; i<=configuration.getAllDevCards().size(); i++)
            cardMap.put("D"+i,configuration.getAllDevCards().get(i-1));
        for (int i=1; i<=configuration.getAllLeaderCards().size(); i++)
            cardMap.put("L"+i,configuration.getAllLeaderCards().get(i-1));

        //setting the starting summary to the player
        ModelObserver obs = new Summary(new ArrayList<>(List.of(player)));
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

    public StateName getCurrentState(){ return turnController.getCurrentState(); }
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
        //TODO: send it to the player
        if (startingPhaseController.hasEnded())
            turnController = new TurnController(match, cardMap);
        return true;

    }

    public synchronized boolean startingResources(String nickname, List<PhysicalResource> resources) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.STARTING_RESOURCES);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, startingPhaseController.startingResources(nickname, resources));
        //TODO: send it to the player
        if (startingPhaseController.hasEnded())
            turnController = new TurnController(match, cardMap);
        return true;
    }

    public synchronized boolean nextTurn(String nickname) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.END_TURN);
        if (!isComputable)
            return false;

        try {
            turnController.nextTurn();
            NextStateMessage message = new NextStateMessage(turnController.getCurrentPlayer().getNickname(), StateName.STARTING_TURN);
            //TODO: send it in broadcast
        } catch (MatchEndedException e) {
            rematchPhaseController = new RematchPhaseController(match.getPlayers());
            EndGameResultsMessage message = new EndGameResultsMessage(nickname, e.getMsg(), e.getRanking());
            //TODO: send it in broadcast
        }

        return true;
    }

    public synchronized boolean switchShelf(String nickname, int shelf1, int shelf2) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.SWITCH_SHELF);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.switchShelf(shelf1, shelf2));
        //TODO: send it to the player
        return true;

    }

    public synchronized boolean leaderActivation(String nickname, String leaderId) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.LEADER_ACTIVATION);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.leaderActivation(leaderId));
        //TODO: send it to the player

        return true;
    }

    public synchronized boolean leaderDiscarding(String nickname, String leaderId) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.LEADER_DISCARDING);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.leaderDiscarding(leaderId));
        //TODO: send it to the player

        return true;
    }

    public synchronized boolean marketDraw(String nickname, boolean row, int num) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.MARKET_DRAW);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.marketDraw(row, num));
        //TODO: send it to the player

        return true;
    }

    public synchronized boolean whiteMarblesConversion(String nickname, List<PhysicalResource> resources) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.WHITE_MARBLE_CONVERSIONS);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.whiteMarblesConversion(resources));
        //TODO: send it to the player

        return true;
    }

    public synchronized boolean warehouseInsertion(String nickname, List<PhysicalResource> resources) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.WAREHOUSE_INSERTION);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.warehouseInsertion(resources));
        //TODO: send it to the player

        return true;
    }

    public synchronized boolean devCardDraw(String nickname, int row, int column) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.DEV_CARD_DRAW);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.devCardDraw(row, column));
        //TODO: send it to the player

        return true;
    }

    public synchronized boolean payments(String nickname, List<PhysicalResource> strongboxCosts, Map<Integer, PhysicalResource> warehouseCosts) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.PAYMENTS);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.payments(strongboxCosts, warehouseCosts));
        //TODO: send it to the player

        return true;
    }

    public synchronized boolean devCardPlacement(String nickname, int column) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.DEV_CARD_PLACEMENT);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.devCardPlacement(column));
        //TODO: send it to the player

        return true;
    }
    public synchronized boolean production(String nickname, List<String> cardIds, Production productionOfUnknown) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.PRODUCTION);
        if (!isComputable)
            return false;

        NextStateMessage message = new NextStateMessage(nickname, turnController.production(cardIds, productionOfUnknown));
        //TODO: send it to the player
        return true;
    }

    public synchronized boolean response(String nickname, boolean value) throws RetryException {
        boolean isComputable = isComputable(nickname, CtoSMessageType.PRODUCTION);
        if (!isComputable)
            return false;
        rematchPhaseController.response(nickname, value);
        return true;
    }


    // %%%%% UTILITY METHODS %%%%%

    //Returns the configuration at the path "config".
    private MatchConfiguration assignConfiguration(String config){
        Gson g = cellConfig(resourceConfig(requirableConfig(effectConfig(new GsonBuilder())))).setPrettyPrinting().create();
        try {
            FileReader reader = new FileReader(config);
            return g.fromJson(reader, MatchConfiguration.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Application shutdown due to an internal error in " + this.getClass().getSimpleName());
            System.exit(1);
            return null;
        }
    }


}
