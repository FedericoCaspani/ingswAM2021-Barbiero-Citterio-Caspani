package it.polimi.ingsw.network.server;

import com.google.gson.JsonSyntaxException;
import it.polimi.ingsw.gameLogic.controller.InitController;
import it.polimi.ingsw.gameLogic.controller.MatchController;
import it.polimi.ingsw.gameLogic.controller.StateName;
import it.polimi.ingsw.gameLogic.exceptions.DisconnectionException;
import it.polimi.ingsw.jsonUtilities.MyJsonParser;
import it.polimi.ingsw.jsonUtilities.Parser;
import it.polimi.ingsw.gameLogic.model.match.player.Player;
import it.polimi.ingsw.network.message.ctosmessage.CtoSMessage;
import it.polimi.ingsw.network.message.stocmessage.GoodbyeMessage;
import it.polimi.ingsw.network.message.stocmessage.RetryMessage;
import it.polimi.ingsw.network.message.stocmessage.StoCMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static it.polimi.ingsw.network.server.Server.TIME_FOR_PING;
import static it.polimi.ingsw.network.server.ServerUtilities.*;
import static it.polimi.ingsw.network.server.TimeoutBufferedReader.getNewTimeoutBufferedReader;

/**
 * This class manages the direct talk with the player, every exchanged message between client
 * and server has to pass from here
 */
public class PlayerHandler implements Runnable, ControlBase {
    private static final Parser parser = MyJsonParser.getParser();

    private final Socket socket;
    private Player player;
    private BufferedReader in;
    private PrintWriter out;
    private MatchController matchController;
    private InitController initController;
    private final AtomicBoolean inMatch;
    private Timer heartbeat;

    /**
     * Generates a PlayerHandler
     * @param socket the socket for communication with the player
     */
    public PlayerHandler(Socket socket) {

        this.socket = socket;
        inMatch = new AtomicBoolean(false);
        try {
            in = getNewTimeoutBufferedReader(socket);
            out = new PrintWriter(socket.getOutputStream(), true);
        }catch (IOException e) {
            terminateConnection(false);
            System.out.println("A problem occurs when trying to connect with a player");
        }
    }


    @Override
    public StateName getCurrentState(){
        if(inMatch.get())
            return matchController.getCurrentState(player.getNickname());
        return initController.getCurrentState();
    }
    @Override
    public MatchController getMatchController() {
        return matchController;
    }
    @Override
    public InitController getInitController() {
        return initController;
    }
    @Override
    public Player getPlayer() {
        return player;
    }
    @Override
    public String getNickname() {
        return player == null ? null : player.getNickname();
    }
    @Override
    public void setPlayer(Player player) {
        this.player = player;
        heartbeat.cancel();
        heartbeat = null;
    }
    @Override
    public void setMatchController(MatchController matchController) {
        this.matchController = matchController;
        inMatch.set(true);
    }
    @Override
    public void endGame(){
        inMatch.set(false);
    }


//%%%%%%%%%%%%%%%% MAIN FUNCTION %%%%%%%%%%%%%%%%%%%%

    /**
     * Handles the communication with the client, reading the messages from the socket and redistributing them
     * to the correct controller until the connection drops or the play session comes to an end.
     * If the player disconnects, this thread run comes to an end but the instance remains saved in {@link ServerUtilities}
     * ready to be used for reconnection issues
     */
    @Override
    public void run() {
        try {
            //initialize the player and do the configuration, exit from here when is assigned a MatchController
            initializationAndSetting();

            playTheGame();

            //if the game comes naturally to an end remove the player from the global list
            terminateConnection(true);

        } catch (DisconnectionException e) {
            if(e.isVoluntary())
                write(new GoodbyeMessage(getNickname(), "Hope to see you again", false));
            disconnection();
        }
        catch (SocketException e){
            disconnection();
        }
        catch(Exception e) {
            e.printStackTrace();
            System.err.println("Unknown exception for player " +getNickname()+". Inform the client if possible and close the connection");
            if(!socket.isClosed())
                write(new GoodbyeMessage(getNickname(), "I'm sorry the server is temporary offline, retry soon", true));
            disconnection();
        }
    }

    /**
     * Initialization of the player, sets writer and reader, talks with the client and sets nickname and the correct match
     * @throws IOException if something goes wrong with the reading or writing with the player
     * @throws DisconnectionException if the client disconnects
     */
    private void initializationAndSetting() throws IOException, DisconnectionException {
        heartbeat = heartbeat();
        CtoSMessage inMsg;
        initController = new InitController(this);

        while(!inMatch.get()) {
            inMsg = read();
            if (inMatch.get()) { //The player entered the next cycle but is actually in the match, so the message has to be computed before returning
                inMsg.computeMessage(this);
                return;
            }
            if (inMsg.getType().getCode() == 0)
                inMsg.computeMessage(this);
            else
                write(new RetryMessage(getNickname(), getCurrentState(),  "The match is not started yet, you cannot send messages like that"));
        }
    }

    /**
     * Continues reading messages and compute them until the match finishes without rematch or a disconnection occurs
     * @throws IOException if something goes wrong while waiting for the message
     * @throws DisconnectionException if the player disconnects
     */
    private void playTheGame() throws IOException, DisconnectionException {
        CtoSMessage inMsg;
        while (inMatch.get()) {
            inMsg = read();
            inMsg.computeMessage(this);
        }
    }

    /**
     * Closes stream and socket, if removePlayer is true, remove also the player from the global list of players
     * this parameter is set false in abnormal conditions
     * @param removePlayer if true, remove also the player from the global list of players
     *                     is set false in abnormal conditions
     */
    private void terminateConnection(boolean removePlayer){
        try {
            in.close();
            out.close();
            socket.close();
        }catch (IOException ignored) { /*this exception is thrown when trying to close an already closed stream */}
        if(removePlayer) {
            System.out.println("Closed connection with " + player + " and removed it from global list");
            serverCall().removePlayer(this);
        }
    }

    /**
     * Reads a message CtoS from this client, if the message is not present, wait until a message is sent,
     * if the player disconnects interrupt the reading and set the player disconnected then throw the DisconnectionException
     * @return the message read
     * @throws IOException if something goes wrong while waiting for the message
     * @throws DisconnectionException if the player disconnects
     */
    private CtoSMessage read() throws IOException, DisconnectionException {
        String readLine;
        CtoSMessage inMsg;
        while(true){
            readLine = in.readLine();
            System.out.println("Client wrote: "+readLine);
            if(readLine == null)
                throw new DisconnectionException("player " + player + " disconnected");
            if(!readLine.equals(""))
                try {
                    inMsg = parser.parseInCtoSMessage(readLine);
                    return inMsg;
                } catch (Exception e) {
                    System.out.println("Arrived wrong message syntax from " + player + "\n--> message: " + readLine);
                    this.write(new RetryMessage(getNickname(), getCurrentState(), "Wrong Json Syntax " + e.getMessage()));
                }
        }
    }

    /**
     * Writes something at this player, the message has to be written in json in the correct format
     * @param msg the message you want to send to this player
     * @return true if the message has been sent, false if something goes wrong in the output stream
     */
    public synchronized boolean write(StoCMessage msg){
        try {
            String outMsg = parser.parseFromStoCMessage(msg);
            System.out.println("out: "+outMsg+" to "+player);
            out.println(outMsg);
            return true;
        } catch (JsonSyntaxException e) {
            System.out.println("System shutdown due to internal error in parsing a StoC message");
            System.exit(1);
            return false;
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("error in write");
            return false;
        }
    }

    /**
     * Creates a thread that periodically calls {@link PlayerHandler#keepAlive()} to keep alive the
     * connection with the client until he give us a valid nickname
     * @return the Timer that has to be canceled when the player give us a valid nickname
     */
    public Timer heartbeat(){
        Timer t = new Timer(true);
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                keepAlive();
            }
        };

        t.scheduleAtFixedRate(tt,6000,TIME_FOR_PING);
        return t;
    }

    /**
     * Keeps alive the connection with the client sending a ping
     */
    public synchronized void keepAlive(){
        out.println("ping");
    }

    /**
     * Disconnects the player associated with this player handler
     * -> if the player was previously in game, set the player disconnected and notify the other players,
     *    if it was his turn switch to the next player, if he will try to reconnect he could restart from where he left
     * -> otherwise remove globally the nickname associated with the player
     */
    public synchronized void disconnection(){

        System.err.println("Probably something goes wrong or the player " + getNickname() + " closed the connection --> disconnection");
        if(inMatch.get()) {
            terminateConnection(false);
            if (!player.disconnect())
                System.err.println("Tried to disconnect a previously disconnected player");

            matchController.disconnection(player); //notify other players here
            return;
        }

        if(player!=null) {
            StateName currentState = initController.getCurrentState();
            if (currentState == StateName.NUMBER_OF_PLAYERS || currentState == StateName.MP_CONFIGURATION_CHOOSE)
                serverCall().rejectPriority(player);
            else if(currentState == StateName.WAITING)
                serverCall().removeFromWaitingList(player);
            //since the player is not yet in game -> remove totally his nickname from the server
            terminateConnection(true);
        }
    }
}

