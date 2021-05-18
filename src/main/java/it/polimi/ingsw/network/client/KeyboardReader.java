package it.polimi.ingsw.network.client;

import it.polimi.ingsw.controller.StateName;
import it.polimi.ingsw.exceptions.NegativeQuantityException;
import it.polimi.ingsw.model.essentials.*;
import it.polimi.ingsw.network.message.ctosmessage.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static it.polimi.ingsw.controller.StateName.*;
import static java.util.Map.entry;

public class KeyboardReader extends Thread{
    private final BufferedReader keyboard;
    private final Client client;
    private String nickname;
    private static final Map<StateName, List<String>> helpMap;
    static {
        helpMap = Map.ofEntries(
                entry(LOGIN, List.of("login [nickname]")),
                entry(RECONNECTION, List.of("selection [y/n]")),
                entry(NEW_PLAYER, List.of("selection [y/n]")),
                entry(NUMBER_OF_PLAYERS, List.of("numPlayers [numPlayers]")),
                entry(SP_CONFIGURATION_CHOOSE, List.of("selection [y/n]")),
                entry(MP_CONFIGURATION_CHOOSE, List.of("selection [y/n]")),
                entry(WAITING, List.of("It's not your turn.... please wait")),
                entry(START_GAME, List.of("leadersChoice [LeadersID]\n" + "cardInfo [cardIDs]")),
                entry(WAITING_LEADERS, List.of("leadersChoice [LeadersID]\n" + "cardInfo [cardIDs]")),
                entry(WAITING_RESOURCES, List.of("startingResource [ResourceType,Shelf]\n" + "cardInfo [cardIDs]")),
                entry(WAITING_FOR_TURN, List.of("It's not your turn.... please wait")),
                entry(STARTING_TURN, List.of("leaderActivation [LeaderID]\n" + "leaderDiscarding [LeaderId]\n" + "switchShelf [firstShelf,secondShelf]\n" +
                        "marketDraw [r/c(row/column),number]\n" + "devCardDraw [RowNumber,ColumnNumber]\n" + "production [\"cardsId\" cardID1,cardID2 \"uCosts\" ResourceType,Quantity \"uEarnings\" ResourceType,Quantity]\n" +
                        "cardInfo [cardIDs]")),
                entry(MARKET_ACTION, List.of("whiteMarblesConversion [ResourceType,Quantity]\n" + "switchShelf [firstShelf,secondShelf]\n" + "cardInfo [cardIDs]")),
                entry(RESOURCES_PLACEMENT, List.of("warehouseInsertion [SingleResourceType,Shelf]\n" + "switchShelf [firstShelf,secondShelf]\n" + "cardInfo [cardIDs]")),
                entry(BUY_DEV_ACTION, List.of("payments [\"strongbox\" ResourceType,Quantity \"warehouse\" Shelf,ResourceType,Quantity]\n" + "switchShelf [firstShelf,secondShelf]\n" + "cardInfo [cardIDs]")),
                entry(PLACE_DEV_CARD, List.of("devCardPlacement [Column]\n" + "cardInfo [cardIDs]")),
                entry(PRODUCTION_ACTION, List.of("payments [\"strongbox\" ResourceType,Quantity \"warehouse\" Shelf,ResourceType,Quantity]\n" + "switchShelf [firstShelf,secondShelf]\n" + "cardInfo [cardIDs]")),
                entry(END_TURN, List.of("leaderActivation [LeaderID]\n" + "leaderDiscarding [LeaderId]\n" + "switchShelf [firstShelf,secondShelf]\n" + "endTurn\n" + "cardInfo [cardIDs]")),
                entry(END_MATCH, List.of("rematch [y/n]")),
                entry(REMATCH_OFFER, List.of("rematch [y/n]"))
        );
    }

    public KeyboardReader(Client client) {
        keyboard = new BufferedReader(new InputStreamReader(System.in));
        this.client = client;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Parse a message CtoS from this client
     * @param input the line read
     * @return the message read
     */
    private CtoSMessage parseInMessage(String input){
        List<String> words = new ArrayList(Arrays.asList(input.toLowerCase().split("\\s+")));
        String command = words.get(0);
        List<String> params = words.subList(1, words.size());
        if(params.size() == 0){
            if(command.equals("endturn"))
                endTurn();
            else{
                CtoSMessage message = selection(command);
                if(message == null) {
                    System.out.println("please insert a valid command");
                    return null;
                }
                else return message;
            }
        }

        switch(command){
            case "login":
                return login(params);

            case "selection":
                return selection(params.get(0));

            case "numplayers":
                return numPlayers(params);

            case "leaderschoice":
                return leadersChoice(params);

            case "startingresource":
                return startingResource(params);

            case "switchshelf":
                return switchShelf(params);

            case "leaderactivation":
                return leaderActivation(params);

            case "leaderdiscarding":
                return leaderDiscarding(params);

            case "marketdraw":
                return marketDraw(params);

            case "whitemarblesconversion":
                return whiteMarblesConversion(params);

            case "warehouseinsertion":
                return warehouseInsertion(params);

            case "devcarddraw":
                return devCardDraw(params);

            case "payments":
                return payments(params);

            case "devcardplacement":
                return devCardPlacement(params);

            case "production":
                return production(params);

            case "endturn":
                return endTurn();

            case "rematch":
                return rematch(params);

            default:
                System.out.println("Wrong command, write \"help\" to print the possible commands");

        }
        return null;
    }

    private CtoSMessage login(List<String> params){
        if(params == null || params.size() > 1) {
            System.out.println("The nickname can't contains spaces. PLease insert a new nickname");
            return null;
        }
        nickname = params.get(0);
        return new LoginMessage(params.get(0));
    }

    private CtoSMessage selection(String input){

        boolean choice;
        switch (input) {
            case "y":
            case "yes":
            case "single":
                choice = true;
                break;
            case "n":
            case "no":
            case "multi":
                choice = false;
                break;
            default:
                System.out.println("invalid command");
                return null;
        }

        return new BinarySelectionMessage(nickname, choice);
    }

    private CtoSMessage numPlayers(List<String> params){
        if(params == null || params.size() > 1){
            System.out.println("please insert only an integer");
            return null;
        }
        int num;
        try{
            num = Integer.parseInt(params.get(0));
        }catch (NumberFormatException e){
            System.out.println("please insert only an integer");
            return null;
        }
        return new NumPlayersMessage(nickname, num);
    }

    private CtoSMessage leadersChoice(List<String> params){
        if(params == null){
            System.out.println("please insert a valid list of player");
            return null;
        }

        return new LeadersChoiceMessage(nickname, new ArrayList<>(List.of(params.get(0).toUpperCase().split(","))));
    }

    private CtoSMessage startingResource(List<String> params){
        if(params == null){
            System.out.println("please insert a valid list of resources");
            return null;
        }
        List<PhysicalResource> resources = new ArrayList<>();
        for (String param : params) {
            PhysicalResource resource = parseInPhysicalResource(param);
            resources.add(resource);
        }

        return new StartingResourcesMessage(nickname, resources);
    }

    private CtoSMessage switchShelf(List<String> params){
        if(params == null || params.size() != 1){
            System.out.println("please insert a valid choice for shelves");
            return null;
        }
        List<String> elements = List.of(params.get(0).split(","));
        int shelf1;
        int shelf2;
        try{
            shelf1 = Integer.parseInt(elements.get(0));
            shelf2 = Integer.parseInt(elements.get(1));
        }catch (NumberFormatException e){
            System.out.println("please insert an integer");
            return null;
        }

        return new SwitchShelfMessage(nickname, shelf1, shelf2);
    }

    private CtoSMessage leaderActivation(List<String> params){
        if(params == null || params.size() > 1){
            System.out.println("you have to chose only one leader");
            return null;
        }

        return new LeaderActivationMessage(nickname, params.get(0).toUpperCase());
    }

    private CtoSMessage leaderDiscarding(List<String> params){
        if(params == null || params.size() > 1){
            System.out.println("you have to chose only one leader");
            return null;
        }
        return new LeaderDiscardingMessage(nickname, params.get(0).toUpperCase());
    }

    private CtoSMessage marketDraw(List<String> params){
        if(params == null || params.size() != 1){
            System.out.println("please select a valid choice of row/column");
            return null;
        }
        List<String> elements = List.of(params.get(0).split(","));
        if(elements.size() != 2)
            return null;

        String input = elements.get(0);
        boolean choice;
        if(input.equals("row") || input.equals("r"))
            choice = true;
        else if(input.equals("column") || input.equals("c"))
            choice = false;
        else{
            System.out.println("please insert r/row or c/column");
            return null;
        }

        int num;
        try {
            num = Integer.parseInt(elements.get(1));
        }catch (NumberFormatException e){
            System.out.println("please insert an integer");
            return null;
        }

        return new MarketDrawMessage(nickname, choice, num);

    }

    private CtoSMessage whiteMarblesConversion(List<String> params){
        if(params == null){
            System.out.println("please select some resources");
            return null;
        }
        List<PhysicalResource> resources = new ArrayList<>();
        for (String param : params) {
            PhysicalResource resource = parseInPhysicalResource(param);
            resources.add(resource);
        }

        return new WhiteMarblesConversionMessage(nickname, resources);
    }

    private CtoSMessage warehouseInsertion(List<String> params){
        if(params == null){
            System.out.println("please select some resources");
            return null;
        }
        List<PhysicalResource> resources = new ArrayList<>();
        for (String param : params) {
            PhysicalResource resource = parseInPhysicalResource(param);
            resources.add(resource);
        }

        return new WarehouseInsertionMessage(nickname, resources);
    }

    private CtoSMessage devCardDraw(List<String> params){
        if(params == null || params.size() != 2){
            System.out.println("please select a row and a column");
        }
        List<String> elements = List.of(params.get(0).split(","));
        if(elements.size() != 2)
            return null;
        int row;
        int column = 0;
        try {
            row = Integer.parseInt(elements.get(0));
            column = Integer.parseInt(elements.get(1));
        }catch (NumberFormatException e){
            System.out.println("please insert an integer");
            return null;
        }

        return new DevCardDrawMessage(nickname, row, column);
    }

    private CtoSMessage payments(List<String> params){
        if(params == null || params.size() < 2){
            System.out.println("please insert a valid list of resources");
        }
        PhysicalResource voidResource = null;
        try {
            voidResource = new PhysicalResource(ResType.UNKNOWN, 0);
        } catch (NegativeQuantityException e) {
            System.exit(1);
        }
        List<PhysicalResource> strongboxCosts = new ArrayList<>();
        Map<Integer,PhysicalResource> warehouseCosts = new HashMap<>();

        int i=0;
        String element;
        if(params.get(i).equals("strongbox")){
            i++;
            element = params.get(i);
            while (!element.equals("warehouse")) {
                PhysicalResource resource = parseInPhysicalResource(element);
                strongboxCosts.add(resource);
                i++;
                if (i == params.size())
                    break;
                element = params.get(i);
            }
        }
        else
            strongboxCosts.add(voidResource);

        if(i == params.size()) {
            warehouseCosts.put(0, voidResource);
            return new PaymentsMessage(nickname, strongboxCosts, warehouseCosts);
        }

        if(params.get(i).equals("warehouse")){
            for (int j=i+1; j<params.size(); j++){
                element = params.get(j);
                if(!addWarehouseCosts(element, warehouseCosts))
                    warehouseCosts = new HashMap<>();
            }
        }
        else warehouseCosts.put(0, voidResource);


        return new PaymentsMessage(nickname, strongboxCosts, warehouseCosts);
    }

    private CtoSMessage production(List<String> params){
        if(params == null || params.size() < 2){
            System.out.println("please insert a valid production message");
            return null;
        }

        if(!params.get(0).equals("cardsid")){
            System.out.println("please insert the cards you want to produce");
            return null;
        }
        List<PhysicalResource> uCosts = new ArrayList<>();
        List<Resource> uEarnings = new ArrayList<>();
        List<String> IDs = new ArrayList<>();

        PhysicalResource voidResource = null;
        try {
            voidResource = new PhysicalResource(ResType.UNKNOWN, 0);
        } catch (NegativeQuantityException e) {
            System.exit(1);
        }


        PhysicalResource cost;
        Resource earning;

        int i=1;
        String element;
        element = params.get(i);
        while (!element.equals("ucosts") && !element.equals("uearnings")){
            IDs.add(element.toUpperCase());
            i++;
            if (i == params.size())
                break;
            element = params.get(i);
        }

        if(i == params.size()) {
            uCosts.add(voidResource);
            uEarnings.add(voidResource);

            return new ProductionMessage(nickname, IDs, new Production(uCosts, uEarnings));
        }

        if(params.get(i).equals("ucosts")) {
            i++;
            element = params.get(i);
            while (!element.equals("uearnings")) {
                cost = parseInPhysicalResource(element);
                uCosts.add(cost);
                i++;
                if (i == params.size())
                    break;
                element = params.get(i);
            }

            if (i == params.size()) {
                uEarnings.add(voidResource);
                return new ProductionMessage(nickname, IDs, new Production(uCosts, uEarnings));
            }
        }
        else
            uCosts.add(voidResource);

        if(params.get(i).equals("uearnings")) {
            i++;
            do {
                element = params.get(i);
                earning = parseInPhysicalResource(element);
                uEarnings.add(earning);
                i++;
            } while (i < params.size());
        }
        else
            uEarnings.add(voidResource);

        return new ProductionMessage(nickname, IDs, new Production(uCosts, uEarnings));

    }

    private CtoSMessage devCardPlacement(List<String> params){
        if(params == null || params.size() != 1){
            System.out.println("please select a valid column");
        }
        int num ;
        try {
            num = Integer.parseInt(params.get(0));
        }catch (NumberFormatException e){ return null;}
        return new DevCardPlacementMessage(nickname, num);
    }

    private CtoSMessage endTurn(){
        return new EndTurnMessage(nickname);
    }

    private CtoSMessage rematch(List<String> params){
        if(params == null || params.size() > 1){
            System.out.println("please insert only y for yes or n for no");
        }
        return new RematchMessage(nickname, params.get(0).equals("y"));
    }

    private PhysicalResource parseInPhysicalResource(String param){
        List<String> elements = List.of(param.split(","));
        if(elements.size() != 2)
            return null;

        String stringType = elements.get(0);
        int quantity;
        try {
            quantity = Integer.parseInt(elements.get(1));
        }catch (NumberFormatException e){
            System.out.println("please insert an integer");
            return null;
        }

        ResType type;
        switch (stringType){
            case "coin":
                type = ResType.COIN;
                break;

            case "shield":
                type = ResType.SHIELD;
                break;

            case "stone":
                type = ResType.STONE;
                break;

            case "servant":
                type = ResType.SERVANT;
                break;

            default:
                type = ResType.UNKNOWN;
                break;
        }

        if(type.ordinal() == ResType.UNKNOWN.ordinal()) {
            System.out.println("please insert a valid type");
            return null;
        }

        try {
            return new PhysicalResource(type, quantity);
        } catch (NegativeQuantityException e) {
            System.out.println("please insert an integer");
            return null;
        }
    }

    private boolean addWarehouseCosts(String param, Map<Integer, PhysicalResource> warehouseCosts){
        int shelf;
        PhysicalResource resource;
        List<String> elements = new ArrayList<>(Arrays.asList(param.split(",")));
        if(elements.size() != 3)
            return false;
        try {
            shelf = Integer.parseInt(elements.get(0));
        }catch (NumberFormatException e) {
            System.out.println("please insert an integer");
            return false;
        }

        resource = parseInPhysicalResource(elements.get(1).concat(",").concat(elements.get(2)));

        warehouseCosts.put(shelf, resource);

        return true;
    }

    @Override
    public void run() {
        CtoSMessage messageToSend;
        String userInput;
        boolean play = true;
        while(play){
            try {
                userInput = keyboard.readLine();
                if(userInput == null) {
                    System.out.println("please insert a valid command");
                    continue;
                }
                if (!userInput.matches(".*\\w.*")) {
                    System.out.println("please insert a valid command");
                    continue;
                }
                if(userInput.equals("help")) {
                    printHelpMap();
                    continue;
                }
                if(userInput.indexOf("viewEnemy") == 0){
                    client.getController().viewEnemy(userInput);
                    continue;
                }
                if(userInput.indexOf("cardInfo") == 0) {
                    client.getController().printCardInfo(userInput);
                    continue;
                }
                if(userInput.equals("exit"))
                    play = false;
                messageToSend = parseInMessage(userInput);
                if(messageToSend == null)
                    continue;
                client.writeMessage(messageToSend);

            } catch (IOException e) {
                System.out.println("Error while reading");
                e.printStackTrace();
            }
        }
    }

    public void printHelpMap() {
        StateName currentState = client.getController().getCurrentState();

        for (StateName state : helpMap.keySet()) {
            if(state.equals(currentState)) {
                System.out.println("This are your possible moves:");
                System.out.println(helpMap.get(currentState));
            }
        }


    }

}
