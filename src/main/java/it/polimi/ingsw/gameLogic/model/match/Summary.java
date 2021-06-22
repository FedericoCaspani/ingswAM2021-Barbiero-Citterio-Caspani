package it.polimi.ingsw.gameLogic.model.match;

import it.polimi.ingsw.gameLogic.controller.StateName;
import it.polimi.ingsw.gameLogic.model.essentials.*;
import it.polimi.ingsw.gameLogic.model.essentials.leader.LeaderCard;
import it.polimi.ingsw.gameLogic.model.match.market.Market;
import it.polimi.ingsw.gameLogic.model.match.player.Player;
import it.polimi.ingsw.gameLogic.model.match.player.PlayerSummary;
import it.polimi.ingsw.gameLogic.model.match.player.personalBoard.DevCardSlots;
import it.polimi.ingsw.gameLogic.model.match.player.personalBoard.DiscountMap;
import it.polimi.ingsw.gameLogic.model.match.player.personalBoard.PersonalBoard;
import it.polimi.ingsw.gameLogic.model.match.player.personalBoard.StrongBox;
import it.polimi.ingsw.gameLogic.model.match.player.personalBoard.faithPath.Cell;
import it.polimi.ingsw.gameLogic.model.match.player.personalBoard.warehouse.Warehouse;
import it.polimi.ingsw.gameLogic.model.observer.ModelObserver;
import it.polimi.ingsw.network.message.stocmessage.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static it.polimi.ingsw.gameLogic.controller.MatchController.getKeyByValue;

/**
 * This class implements a summary of the match situation.
 * It contains whatever the client is able and needs to see in its view,
 * and it's particularly useful for making easier user reconnections.
 */
public class Summary implements ModelObserver {

    private final Map<String, Card> cardMap;
    private final Production basicProd;
    private final char[][] market;
    private char sideMarble;
    private final List<String>[][] cardGrid;
    private List<String> faithPath;

    private int lorenzoMarker;  //stays "-1" in multi-player matches!

    private List<PlayerSummary> playersSummary;

    /**
     * This constructor is used for Summary's initialization in a custom configuration match.
     * @param playersInMatch the players' list
     * @param cardMap the already compiled cards map
     * @param faithPath the custom (or default eventually) faith path
     * @param basicProd the custom (or default eventually) basic production
     */
    public Summary(List<Player> playersInMatch, Map<String, Card> cardMap, List<Cell> faithPath, Production basicProd){
        //cardMap init
        this.cardMap = cardMap;
        //basic production init
        this.basicProd = basicProd;
        // market init
        this.market = new char[3][4];
        // cardGrid init
        this.cardGrid = new ArrayList[CardGrid.MAX_LEVEL][CardColor.values().length];
        //lorenzo's marker init: moved here in Summary because knowing if the match is single or multi is needed.
        this.lorenzoMarker = -1;
        //faith path init
        this.faithPath = new ArrayList<>();
        for(Cell c : faithPath){
            String cell = c.getWinPoints() + "-" +
                    c.getReportSection() + "-" +
                    c.isVatican();
            this.faithPath.add(cell);
        }
        //players summaries init
        playersSummary = new ArrayList<>();
        for(Player p : playersInMatch)
            playersSummary.add(new PlayerSummary(p));
    }

    /**
     * This constructor is called once at the beginning of the match.
     * It initializes the Summary basing on the actual (starting) situation.
     * @param match   the match
     * @param cardMap the card map
     */
    public Summary(Match match, Map<String, Card> cardMap, List<Cell> faithPath, Production basicProd){
        //cardMap init
        this.cardMap = cardMap;

        //basic production init
        this.basicProd = basicProd;

        // market init
        this.market = new char[3][4];
        sideMarble = Character.toLowerCase(match.getMarket().getSlide().toString().charAt(0));
        for(int i = 0; i< match.getMarket().getBoard().length; i++)
            for(int j = 0; j<match.getMarket().getBoard()[i].length; j++)
                this.market[i][j] = Character.toLowerCase(match.getMarket().getBoard()[i][j].toString().charAt(0));

        // cardGrid init
        this.cardGrid = new ArrayList[CardGrid.MAX_LEVEL][CardColor.values().length];
        CardType[] remaining = match.getCardGrid().countRemaining();
        for(int i = 0; i<CardGrid.MAX_LEVEL; i++)
            for(int j = 0; j<CardColor.values().length; j++) {
                this.cardGrid[i][j] = new ArrayList<>(2);
                this.cardGrid[i][j].add(getKeyByValue(cardMap, match.getCardGrid().peek(i+1,j+1)));
                this.cardGrid[i][j].add(""+remaining[i*CardGrid.MAX_LEVEL+j].getQuantity());

            }

        //lorenzo's marker init: moved here in Summary because knowing if the match is single or multi is needed.
        this.lorenzoMarker = -1;
        if(match.getPlayers().size()==1)
            this.lorenzoMarker = 0;

        //faith path init
        this.faithPath = new ArrayList<>();
        for(Cell c : faithPath){
            String cell = c.getWinPoints() + "-" +
                    c.getReportSection() + "-" +
                    c.isVatican();
            this.faithPath.add(cell);
        }


        //players summaries init
        playersSummary = new ArrayList<>();
        for(Player p : match.getPlayers())
            playersSummary.add(new PlayerSummary(p, cardMap));
    }

    /**
     * Copy constructor, returns a shallow copy of the Summary passed
     * @param toCopy the summary you want to copy
     */
    public Summary(Summary toCopy){
        this.cardMap = toCopy.cardMap;
        this.basicProd = toCopy.basicProd;
        this.market = toCopy.market;
        this.sideMarble = toCopy.sideMarble;
        this.cardGrid = toCopy.cardGrid;
        this.faithPath = toCopy.faithPath;
        this.lorenzoMarker = toCopy.lorenzoMarker;
        this.playersSummary = toCopy.playersSummary;
    }


    /**
     * This method returns the PlayerSummary associated to the requested nickname.
     * @param nickname the nickname
     * @return the summary
     */
    public PlayerSummary getPlayerSummary(String nickname){
        for(PlayerSummary s : playersSummary)
            if(s.getNickname().equals(nickname))
                return s;
        return null;
    }


    //UPDATE METHODS ( CALLED BY THE "OBSERVABLE" CLASS ) :

    /**
     * This method, when called, updates the market in the summary.
     * @param market the market
     */
    @Override
    public void updateMarket(Market market) {
        sideMarble = Character.toLowerCase(market.getSlide().toString().charAt(0));
        for(int i = 0; i< market.getBoard().length; i++)
            for(int j = 0; j<market.getBoard()[i].length; j++)
                this.market[i][j] = Character.toLowerCase(market.getBoard()[i][j].toString().charAt(0));

        new MarketChangeMessage("", this.sideMarble, this.market).sendBroadcast(getPlayersNicknames());

    }

    /**
     * This method, when called, updates the card grid in the summary.
     * It inserts into every slot the id of the top card and the depth of the
     * stack (got from cardGrid.getGrid()[i][j].size()).
     * @param cardGrid the card grid
     */
    @Override
    public void updateCardGrid(CardGrid cardGrid) {
        for(int i = 0; i<cardGrid.getTop().length; i++)
            for(int j = 0; j<cardGrid.getTop()[i].length; j++) {
                this.cardGrid[i][j] = new ArrayList<>();
                this.cardGrid[i][j].add(getKeyByValue(cardMap,cardGrid.getTop()[i][j]));
                this.cardGrid[i][j].add("" + cardGrid.getGrid()[i][j].size());
            }
        new CardGridChangeMessage("", this.cardGrid).sendBroadcast(getPlayersNicknames());
    }

    /**
     * This method, when called, updates the lorenzo marker (if single-player) in the summary.
     * @param lorenzoMarker the black marker
     */
    @Override
    public void updateLorenzoMarker(int lorenzoMarker) {
        this.lorenzoMarker = lorenzoMarker;
        new NewFaithPositionMessage("Lorenzo the Magnificent", lorenzoMarker).sendBroadcast(getPlayersNicknames());
    }
    /**
     * This method, when called, updates the faith path in the summary.
     * @param faithPath the faith path
     */
    @Override
    public void updateFaithPath(List<Cell> faithPath) {
        this.faithPath = new ArrayList<>();
        for(Cell c : faithPath){
            String cell = c.getWinPoints() + "-" +
                    c.getReportSection() + "-" +
                    c.isVatican();
            this.faithPath.add(cell);
        }
    }

    /**
     * This method, when called, updates the personal board of the requested player in the summary.
     * @param nickname  the requested player
     * @param connected the new state
     */
    public void updateConnectionState(String nickname, boolean connected){
        getPlayerSummary(nickname).updateConnectionState(connected);
    }


    /**
     * This method, when called, updates the personal board of the requested player in the summary.
     * @param nickname  the requested player
     * @param personalBoard the personal board
     */
    public void updatePersonalBoard(String nickname, PersonalBoard personalBoard){
        getPlayerSummary(nickname).updatePersonalBoard(personalBoard, cardMap);
    }

    /**
     * This method, when called, updates the market buffer of the requested player in the summary.
     * @param nickname  the requested player
     * @param warehouse the warehouse
     */
    public void updateMarketBuffer(String nickname, Warehouse warehouse){
        getPlayerSummary(nickname).updateMarketBuffer(warehouse);

        new MarketBufferChangeMessage(nickname,getPlayerSummary(nickname).getMarketBuffer()).sendBroadcast(getPlayersNicknames());
    }

    /**
     * This method, when called, updates the warehouse of the requested player in the summary.
     * @param nickname  the requested player
     * @param warehouse the warehouse
     */
    @Override
    public void updateWarehouse(String nickname, Warehouse warehouse) {
        getPlayerSummary(nickname).updateWarehouse(warehouse);

        new WarehouseChangeMessage(nickname, getPlayerSummary(nickname).getWarehouse()).sendBroadcast(getPlayersNicknames());
    }

    /**
     * This method, when called, updates the strongbox of the requested player in the summary.
     * @param nickname  the requested player
     * @param strongbox the strongbox
     */
    @Override
    public void updateStrongbox(String nickname, StrongBox strongbox) {
        getPlayerSummary(nickname).updateStrongbox(strongbox);

        new StrongboxChangeMessage(nickname, getPlayerSummary(nickname).getStrongbox()).sendBroadcast(getPlayersNicknames());
    }

    /**
     * This method, when called, updates the faith marker of the requested player in the summary.
     * @param nickname  the requested player
     * @param faithMarker the faith marker
     */
    @Override
    public void updateFaithMarker(String nickname, int faithMarker) {
        getPlayerSummary(nickname).updateFaithMarker(faithMarker);

        new NewFaithPositionMessage(nickname, faithMarker).sendBroadcast(getPlayersNicknames());
    }

    /**
     * This method, when called, updates the pope tiles' state of the requested player in the summary.
     * @param nickname  the requested player
     * @param popeTiles the pope tiles array
     */
    @Override
    public void updatePopeTiles(String nickname, Map<String, List<Integer>> popeTiles) {
        for(String nn : getPlayersNicknames())
            getPlayerSummary(nn).updatePopeTiles(popeTiles.get(nn));

        new VaticanReportMessage(nickname, popeTiles).sendBroadcast(getPlayersNicknames());
    }

    /**
     * This method, when called, updates the dev card slots of the requested player in the summary.
     * @param nickname  the requested player
     * @param devCardSlots the dev card slots array
     */
    @Override
    public void updateDevCardSlots(String nickname, DevCardSlots devCardSlots) {
        getPlayerSummary(nickname).updateDevCardSlots(devCardSlots, cardMap);

        new DevCardSlotChangeMessage(nickname, getPlayerSummary(nickname).getDevCardSlots()).sendBroadcast(getPlayersNicknames());
    }

    /**
     * This method, when called, updates the hand leaders' state of the requested player in the summary.
     * Used for the starting choice of leaders (this will be sent ONLY to the interested player).
     * @param nickname  the requested player
     * @param handLeaders the hand leaders
     */
    @Override
    public void updateHandLeaders(String nickname, List<LeaderCard> handLeaders) {
        getPlayerSummary(nickname).updateHandLeaders(handLeaders, cardMap);
        new HandLeadersStateMessage(nickname, handLeaders.stream().map((x)->getKeyByValue(cardMap,x)).collect(Collectors.toList())).send(nickname);
        new HandLeadersStateMessage(nickname, handLeaders.stream().map((x)->"-1").collect(Collectors.toList())).sendBroadcast(playersSummary.stream().map(PlayerSummary::getNickname).filter((x)->!x.equals(nickname)).collect(Collectors.toList()));
    }

    /**
     This method, when called, updates the hand leaders' state of the requested player in the summary.
     @param nickname  the requested player
     @param handLeader the hand leader discarded
     */
    @Override
    public void updateHandLeadersDiscard(String nickname, LeaderCard handLeader) {
        getPlayerSummary(nickname).updateHandLeadersDiscard(handLeader, cardMap);

        new DiscardedLeaderMessage(nickname).sendBroadcast(getPlayersNicknames());
    }

    /**
     * This method, when called, updates the active leaders' state of the requested player in the summary.
     * @param nickname  the requested player
     * @param activeLeader the activated leader
     */
    @Override
    public void updateActiveLeaders(String nickname, LeaderCard activeLeader) {
        boolean ok = getPlayerSummary(nickname).updateActiveLeaders(activeLeader, cardMap);
        if(ok)
            new ActivatedLeaderMessage(nickname, getKeyByValue(cardMap, activeLeader)).sendBroadcast(getPlayersNicknames());
    }

    /**
     * This update method receives the new conversion to add in the list of the others.
     * @param nickname              the player who activated the conversion leader
     * @param whiteMarbleConversion the related conversion
     */
    @Override
    public void updateWhiteMarbleConversions(String nickname, PhysicalResource whiteMarbleConversion) {
        getPlayerSummary(nickname).updateWhiteMarbleConversions(whiteMarbleConversion);

        new WhiteMarbleConversionMessage(nickname, whiteMarbleConversion).send(nickname);
    }

    /**
     * This method, when called, updates the discount map of the requested player in the summary.
     * @param nickname  the requested player
     * @param discountMap the discount map
     */
    @Override
    public void updateDiscountMap(String nickname, DiscountMap discountMap) {
        getPlayerSummary(nickname).updateDiscountMap(discountMap);

        new UpdatedDiscountMapMessage(nickname, getPlayerSummary(nickname).getDiscountMap()).send(nickname);
    }

    /**
     * This method, when called, updates the temporary dev card of the requested player in the summary.
     * @param nickname  the requested player
     * @param tempDevCard the temporary dev card
     */
    @Override
    public void updateTempDevCard(String nickname, DevelopmentCard tempDevCard) {
        getPlayerSummary(nickname).updateTempDevCard(tempDevCard, cardMap);

        new DevCardDrawnMessage(nickname, getPlayerSummary(nickname).getTempDevCard()).sendBroadcast(getPlayersNicknames());
    }

    /**
     * This method, when called, updates the temporary production of the requested player in the summary.
     * @param nickname  the requested player
     * @param tempProduction the temporary production
     */
    @Override
    public void updateTempProduction(String nickname, Production tempProduction) {
        getPlayerSummary(nickname).updateTempProduction(tempProduction);
    }

    /**
     * This method, when called, updates the last used state in this player's summary, useful for disconnection
     * in the middle of a complex operation ( such as Dev card buy, production, etc. ).
     * @param lastUsedState the last used state
     */
    @Override
    public void updateLastUsedState(String nickname, StateName lastUsedState) {
        getPlayerSummary(nickname).updateLastUsedState(lastUsedState);
        new NextStateMessage(nickname, lastUsedState).send(nickname);
    }


    //GETTERS:

    /** Getter for summarized market */
    public char[][] getMarket() { return market; }
    /** Getter for summarized slide marble */
    public char getSideMarble() { return sideMarble; }
    /** Getter for summarized card grid (list<String id, Integer depth>) */
    public List<String>[][] getCardGrid() { return cardGrid; }
    /** Getter for summarized faith path */
    public List<String> getFaithPath() { return faithPath; }
    /** Getter for summarized lorenzo marker */
    public int getLorenzoMarker() { return lorenzoMarker; }
    /** Getter for summarized cards map */
    public Map<String, Card> getCardMap() { return cardMap; }
    /** Getter for summarized basic production */
    public Production getBasicProd() { return basicProd; }
    /** Getter for the list of all players' summaries */
    public List<PlayerSummary> getPlayersSummary() { return playersSummary; }
    /** Getter for the list of all players' nicknames. */
    public List<String> getPlayersNicknames() { return playersSummary.stream().map(PlayerSummary::getNickname).collect(Collectors.toList()); }


    //CLONE METHOD:

    /**
     * Returns a personalized summary based on this for the player whom nickname is passed, his hand leaders will be explicit,
     * whereas the rival's leaders will be hidden
     * @param nickname the player you want to personalize the summary for
     * @return a personalized summary if the player passed is actually in this match,
     *          this summary unmodified if there isn't such a player in this summary
     */
    public Summary personalizedSummary(String nickname){
        if(!getPlayersNicknames().contains(nickname))
            return this;
        Summary res = new Summary(this);
        List<PlayerSummary> personalizedPSs = new ArrayList<>();

        for(PlayerSummary ps : playersSummary) {
            if (!ps.getNickname().equals(nickname))
                personalizedPSs.add(new PlayerSummary(ps, true));
            else
                personalizedPSs.add(ps);
        }

        res.playersSummary=personalizedPSs;

        return res;
    }

}