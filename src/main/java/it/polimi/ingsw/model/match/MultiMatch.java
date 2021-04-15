package it.polimi.ingsw.model.match;

import it.polimi.ingsw.model.exceptions.SingleMatchException;
import it.polimi.ingsw.model.exceptions.WrongSettingException;
import it.polimi.ingsw.model.match.player.Player;
import it.polimi.ingsw.model.match.player.personalBoard.PersonalBoard;
import it.polimi.ingsw.model.match.player.personalBoard.faithPath.Cell;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MultiMatch extends Match {
    private Player currentPlayer;
    private final List<Player> players;
    private final CardGrid cardGrid;
    private int numPlayer = 0;

    public MultiMatch(List<Player> players,String config) throws SingleMatchException, FileNotFoundException, WrongSettingException {
        super(config);
        if(players.size() == 1)
            throw new SingleMatchException("This match has only one player");

        MatchConfiguration matchConfiguration = super.getMatchConfiguration();

        this.players = players;
        this.currentPlayer = players.get(0);
        this.cardGrid = new CardGrid(matchConfiguration.getAllDevCards());
        for (int i = 0; i < 2; i++) {
            players.get(i).setPersonalBoard(new PersonalBoard((ArrayList<Cell>) matchConfiguration.getCustomPath(), 0, matchConfiguration.getBasicProduction()));
            players.get(i).setMatch(this);
            players.get(i).setHandLeaders(getLeaderStack().draw(4));
            }
        if (players.size() > 2) {
            for (int j = 2; j < players.size(); j++) {
                players.get(j).setPersonalBoard(new PersonalBoard((ArrayList<Cell>) matchConfiguration.getCustomPath(), 1, matchConfiguration.getBasicProduction()));
                players.get(j).setMatch(this);
                players.get(j).setHandLeaders(getLeaderStack().draw(4));

            }
        }
    }


    @Override
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public CardGrid getCardGrid() {
        return cardGrid;
    }

    @Override
    public List<Player> getPlayers() {
        return players;
    }

    public Player getNextPlayer(){
        if(numPlayer < players.size()-1)
            return players.get(numPlayer+1);
        else return players.get(0);
    }
    @Override
    public boolean nextTurn(){
        numPlayer++;
        if(numPlayer < players.size())
            currentPlayer = players.get(numPlayer);
        else
            currentPlayer = players.get(0);

        return true;
    }

    @Override
    public Player getPlayer(String nickname){
        return (Player) players.stream().filter((x)->x.getNickname().equals(nickname));

    }

}