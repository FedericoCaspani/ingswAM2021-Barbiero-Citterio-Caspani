package it.polimi.ingsw.model.match;

import it.polimi.ingsw.model.essentials.Card;
import it.polimi.ingsw.model.match.player.Player;
import it.polimi.ingsw.observer.ModelObserver;

import java.util.*;

public class CommonThingsTest {

    public Map<String, Card> getCardMap(MatchConfiguration configuration) {
        Map<String, Card> cardMap = new HashMap<>();
        for (int i = 1; i <= configuration.getAllDevCards().size(); i++)
            cardMap.put("D" + i, configuration.getAllDevCards().get(i - 1));
        for (int i = 1; i <= configuration.getAllLeaderCards().size(); i++)
            cardMap.put("L" + i, configuration.getAllLeaderCards().get(i - 1));
        return cardMap;
    }
    public void setSummaries(List<Player> players, Map<String, Card> cardMap){
        ModelObserver obs = new Summary(players, cardMap);
        for(Player p : players)
            p.setSummary(obs);
    }

    public void setSummary(Player player, Map<String, Card> cardMap){
        ModelObserver obs = new Summary(new ArrayList<>(List.of(player)), cardMap);
        player.setSummary(obs);

    }

}