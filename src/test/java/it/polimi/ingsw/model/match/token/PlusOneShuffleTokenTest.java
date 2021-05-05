package it.polimi.ingsw.model.match.token;

import it.polimi.ingsw.exceptions.LastRoundException;
import it.polimi.ingsw.exceptions.NegativeQuantityException;
import it.polimi.ingsw.exceptions.WrongSettingException;
import it.polimi.ingsw.model.match.SingleMatch;
import it.polimi.ingsw.model.match.player.Player;
import it.polimi.ingsw.model.match.player.personalBoard.faithPath.SingleFaithPath;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

public class PlusOneShuffleTokenTest {
    private List<Player> players = new ArrayList<>();
    private SingleMatch singleMatch;
    private PlusOneShuffleToken plusOneShuffleToken = new PlusOneShuffleToken();
    @Test
    public void testOnDraw() throws NegativeQuantityException, FileNotFoundException, WrongSettingException, LastRoundException {
        singleMatch = new SingleMatch(new Player("player1"),"src/test/resources/StandardConfiguration.json");
        Stack<Token> tokenStack = singleMatch.getTokenStack().getStack();

        assertTrue(plusOneShuffleToken.onDraw(singleMatch));
        SingleFaithPath singleFaithPath = (SingleFaithPath) singleMatch.getCurrentPlayer().getPersonalBoard().getFaithPath();

        assertEquals(singleFaithPath.getBlackPosition(),1);
        assertNotEquals(tokenStack,singleMatch.getTokenStack().getStack());
        assertEquals(tokenStack.size(),singleMatch.getTokenStack().getStack().size());
    }
}
