package it.polimi.ingsw.controller;

public enum StateName {
    //States used in LoginPhase
    LOGIN(0),
    RECONNECTION(0),
    NEW_PLAYER(0),
    NUMBER_OF_PLAYERS(0),
    SP_CONFIGURATION_CHOOSE(0),
    MP_CONFIGURATION_CHOOSE(0),
    CONFIGURATION(0),
    WAITING(0),
    START_GAME(0),
    //States used in StartingPhase
    WAITING_LEADERS (1),
    WAITING_RESOURCES (2),
    STARTING_PHASE_DONE (3),
    //States used in TurnController
    STARTING_TURN (4),
    MARKET_ACTION (5),
    RESOURCES_PLACEMENT (6),
    BUY_DEV_ACTION (7),
    PLACE_DEV_CARD (8),
    PRODUCTION_ACTION (9),
    END_TURN (10),
    END_MATCH (11),
    REMATCH_OFFER (12),
    NEW_MATCH (13),
    END_GAME (14);


    private final int val;
    StateName(int val){
        this.val = val;
    }

    public int getVal(){ return val; }
}
