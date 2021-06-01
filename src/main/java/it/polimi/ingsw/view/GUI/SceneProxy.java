package it.polimi.ingsw.view.GUI;


import it.polimi.ingsw.model.essentials.Card;
import javafx.application.Platform;
import javafx.scene.image.Image;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.polimi.ingsw.network.client.Client.getClient;


public class SceneProxy {
    private static SceneProxy instance;
    private InitSceneController initSceneController;
    private StartingPhaseSceneController startingPhaseSceneController;
    private TurnSceneController turnSceneController;
    private RematchPhaseSceneController rematchPhaseSceneController;
    private Map<String, Image> idToImageMap;
    private Map<Image, String> imageToIdMap;
    private SceneController actualController;
    private SceneName actualScene;

    public static SceneProxy getSceneProxy(){
        if (instance == null)
            instance = new SceneProxy();

        return instance;
    }

    //%%%%%%%%%%%%%%%%%%%%%%%%SETTER%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    public void setInitSceneController(InitSceneController initSceneController) {
        this.initSceneController = initSceneController;
        this.actualController = initSceneController;
        this.startingPhaseSceneController = null;
        this.turnSceneController = null;
        this.rematchPhaseSceneController = null;
    }

    public void setStartingPhaseSceneController(StartingPhaseSceneController startingPhaseSceneController) {
        this.initSceneController = null;
        this.startingPhaseSceneController = startingPhaseSceneController;
        this.actualController = startingPhaseSceneController;
        this.turnSceneController = null;
        this.rematchPhaseSceneController = null;

    }

    public void setTurnSceneController(TurnSceneController turnSceneController) {
        this.initSceneController = null;
        this.startingPhaseSceneController = null;
        this.turnSceneController = turnSceneController;
        this.actualController = turnSceneController;
        this.rematchPhaseSceneController = null;

    }

    public void setRematchPhaseSceneController(RematchPhaseSceneController rematchPhaseSceneController) {
        this.initSceneController = null;
        this.startingPhaseSceneController = null;
        this.turnSceneController = null;
        this.rematchPhaseSceneController = rematchPhaseSceneController;
        this.actualController = rematchPhaseSceneController;

    }

    public void setMap(Map<String, Card> cardMap){
        InputStream imageStream;
        Image image;

        idToImageMap = new HashMap<>();
        imageToIdMap = new HashMap<>();

        for(String cardId :cardMap.keySet()){
            if((cardId.charAt(0) == 'D' && Integer.parseInt(cardId.substring(1)) > 48) || (cardId.charAt(0) == 'L' && Integer.parseInt(cardId.substring(1)) > 16))
                break;
            imageStream = getClass().getResourceAsStream("images/" +
                        ((cardId.startsWith("L")) ? "leaderCards/" : "developmentCards/front/") + cardId +".png");
        if (imageStream != null) {
            image = new Image(imageStream);
            idToImageMap.put(cardId, image);
            imageToIdMap.put(image, cardId);
        }

        }



        //TODO: modify in case of editor
    }

    /**
     * Returns the image of the card associated with the given ID, this should be of the form L* or D*
     * @param ID the unique id of the card
     * @return the relative image or null if there isn't an image associated with that id
     */
    public Image getImage(String ID){
        return idToImageMap.get(ID);
    }

    /**
     * Returns the ID of the card associated with the given image
     * @param image the unique image of the card
     * @return the relative ID or null if there isn't an ID associated with that image
     */
    public String getID(Image image){
        return imageToIdMap.get(image);
    }

    public void changeScene(SceneName scene){
        if(actualScene == scene)
            return;

        actualScene = scene;
        Platform.runLater(()->{
            try {
                JavaFXGUI.setRoot(scene.name());
            } catch (IOException e) {
                e.printStackTrace();
                getClient().exit();
            }
        });
    }

    public void disableAll(){
        actualController.disableAll();
    }

    public void loadLeaderCards(List<String> leaders){
        Platform.runLater(()->{
            if(startingPhaseSceneController != null)
                startingPhaseSceneController.loadLeaderCards(leaders);
        });
    }

    public void loadStartingResources(int numResources){
        Platform.runLater(()->{
            if(startingPhaseSceneController != null)
                startingPhaseSceneController.loadStartingResources(numResources);
        });
    }

    public void loginError(String errMessage) {
        Platform.runLater(()->{
            if(initSceneController != null)
                initSceneController.loginError(errMessage);
        });
    }

    public void leadersChoiceError(String errMessage){
        Platform.runLater(()->{
            if (startingPhaseSceneController != null)
                startingPhaseSceneController.leadersChoiceError(errMessage);
        });
    }



}
