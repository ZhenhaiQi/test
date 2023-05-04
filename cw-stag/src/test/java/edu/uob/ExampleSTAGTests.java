package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Paths;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

class ExampleSTAGTests {

  private GameServer server;

  // Create a new server _before_ every @Test
  @BeforeEach
  void setup() {
      File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
      File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
      server = new GameServer(entitiesFile, actionsFile);
  }

  String sendCommandToServer(String command) {
      // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
      return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
      "Server took too long to respond (probably stuck in an infinite loop)");
  }

  // A lot of tests will probably check the game state using 'look' - so we better make sure 'look' works well !
  @Test
  void testLook() {
    String response = sendCommandToServer("simon: look");
    response = response.toLowerCase();
    assertTrue(response.contains("cabin"), "Did not see the name of the current room in response to look");
    assertTrue(response.contains("log cabin"), "Did not see a description of the room in response to look");
    assertTrue(response.contains("magic potion"), "Did not see a description of artifacts in response to look");
    assertTrue(response.contains("wooden trapdoor"), "Did not see description of furniture in response to look");
    assertTrue(response.contains("forest"), "Did not see available paths in response to look");
  }

  // Test that we can pick something up and that it appears in our inventory
  @Test
  void testGet()
  {
      String response;
      sendCommandToServer("simon: get potion");
      response = sendCommandToServer("simon: inv");
      response = response.toLowerCase();
      assertTrue(response.contains("potion"), "Did not see the potion in the inventory after an attempt was made to get it");
      response = sendCommandToServer("simon: look");
      response = response.toLowerCase();
      assertFalse(response.contains("potion"), "Potion is still present in the room after an attempt was made to get it");
  }

  // Test that we can goto a different location (we won't get very far if we can't move around the game !)
  @Test
  void testGoto()
  {
      sendCommandToServer("simon: goto forest");
      String response = sendCommandToServer("simon: look");
      response = response.toLowerCase();
      assertTrue(response.contains("key"), "Failed attempt to use 'goto' command to move to the forest - there is no key in the current location");
  }


  // Add more unit tests or integration tests here.
    @Test
    void testEntitiesParser(){
        sendCommandToServer("simon: look");
        ArrayList<ArrayList<LocationsEntity>> storeEntitiesList = server.getStoreEntitiesList();
        assertTrue(storeEntitiesList.size() == 1, "Failed to parse entities");
        ArrayList<LocationsEntity> locationsEntities = storeEntitiesList.get(0);
        LocationsEntity locationsEntity = locationsEntities.get(0);
        assertTrue((locationsEntity.getName()).equals("cabin"), "Failed to parse entities");
        ArrayList<ArtefactsEntity> storeArtefacts = locationsEntity.getStoreArtefacts();
        assertTrue((storeArtefacts.get(0).getName()).equals("potion"), "Failed to parse entities");
        ArrayList<FurnitureEntity> storeFurniture = locationsEntity.getStoreFurniture();
        assertTrue((storeFurniture.get(0).getName()).equals("trapdoor"), "Failed to parse entities");
        ArrayList<String> storePath = locationsEntity.getStorePath();
        assertTrue((storePath.get(0)).equals("forest"), "Failed to parse entities");

    }

    @Test
    void testActionsParser(){
        sendCommandToServer("simon: look");
        HashMap<String, HashSet<GameAction>> storeActions = server.getStoreActions();
        Set<String> actionPhrases = storeActions.keySet();
        Iterator<String> iterator = actionPhrases.iterator();
        ArrayList<String> storePhrases = new ArrayList<>();
        while (iterator.hasNext()){
            storePhrases.add(iterator.next());
        }
        assertTrue(storePhrases.contains("open"), "Fail to parse actions");
        assertTrue(storePhrases.contains("chop"), "Fail to parse actions");
        assertTrue(storePhrases.contains("drink"), "Fail to parse actions");
        assertTrue(storePhrases.contains("fight"), "Fail to parse actions");

        HashSet<GameAction> actionSet = storeActions.get("open");
        GameAction getAction = actionSet.iterator().next();
        ArrayList<String> subjectEntities = getAction.getSubjectEntities();
        assertTrue(subjectEntities.contains("trapdoor"), "Fail to parse actions");
        assertTrue(subjectEntities.contains("key"), "Fail to parse actions");
        ArrayList<String> consumedEntities = getAction.getConsumedEntities();
        assertTrue(consumedEntities.contains("key"), "Fail to parse actions");
        ArrayList<String> producedEntities = getAction.getProducedEntities();
        assertTrue(producedEntities.contains("cellar"), "Fail to parse actions");
        assertTrue(getAction.getNarration().equals("You unlock the trapdoor and see steps leading down into a cellar"), "Fail to parse actions");
    }

    @Test
    void testPlayer(){
        sendCommandToServer("simon: look");
        sendCommandToServer("jack: look");
        sendCommandToServer("alber: look");
        ArrayList<PlayersEntity> players = server.getPlayers();
        assertTrue(players.size() == 3, "Fail to add player");
        assertTrue(players.get(0).getName().equals("simon"), "Fail to add player");
        assertTrue(players.get(1).getName().equals("jack"), "Fail to add player");
        assertTrue(players.get(2).getName().equals("alber"), "Fail to add player");
        String response = sendCommandToServer("name@: look");
        assertTrue(response.contains("Invalid player name"), "Add wrong player name");
        players = server.getPlayers();
        assertTrue(players.size() == 3, "Add wrong player name");
        sendCommandToServer("jack: goto forest");
        players = server.getPlayers();
        assertTrue(players.size() == 3, "Add the same player");
    }

    @Test
    void testInvalidCommand(){
        String response = sendCommandToServer("simon look");
        assertTrue(response.contains("Invalid command"), "Don't detect the invalid command");
        response = sendCommandToServer("simon: : look");
        assertTrue(response.contains("Invalid command"), "Don't detect the invalid command");
        response = sendCommandToServer("simon: test command");
        assertTrue(response.contains("Invalid basic command or action phrase"), "Don't detect the invalid command");
        response = sendCommandToServer("simon: look and inv");
        assertTrue(response.contains("Composite Commands are not supported"), "Don't detect the invalid command");
        response = sendCommandToServer("simon: look and open");
        assertTrue(response.contains("Composite Commands are not supported"), "Don't detect the invalid command");
        response = sendCommandToServer("simon: open and unlock");
        assertTrue(response.contains("Composite Commands are not supported"), "Don't detect the invalid command");
    }

    @Test
    void testInv(){
        sendCommandToServer("simon: inventory");
        PlayersEntity playersEntity = server.getPlayers().get(0);
        ArrayList<ArtefactsEntity> inventory = playersEntity.getInventory();
        assertTrue(inventory.size() == 0, "The inventory is not empty");
        sendCommandToServer("simon: inv");
        playersEntity = server.getPlayers().get(0);
        inventory = playersEntity.getInventory();
        assertTrue(inventory.size() == 0, "The inventory is not empty");
        sendCommandToServer("simon: get potion");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        String response = sendCommandToServer("simon: inv");
        assertTrue(response.contains("potion"), "Fail to list artefacts carried by the player");
        assertTrue(response.contains("key"), "Fail to list artefacts carried by the player");
    }

    @Test
    void testGetCommand(){
        String response = sendCommandToServer("simon: get test");
        assertTrue(response.contains("The artefact doesn't exist"), "Get the artefact which doesn't exist");
        response = sendCommandToServer("simon: potion get");
        assertTrue(response.contains("Wrong command format"), "Perform the command with wrong format");
        response = sendCommandToServer("simon: get potion");
        assertTrue(response.contains("potion"), "Fail to get the artefact");

    }

    @Test
    void testDropCommand(){
        String response = sendCommandToServer("simon: drop potion");
        assertTrue(response.contains("The artefact doesn't exist"), "Drop the artefact that doesn't exist");
        sendCommandToServer("simon: get potion");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        response = sendCommandToServer("simon: drop potion and key");
        assertTrue(response.contains("Composite Commands are not supported"), "Drop more than one artefact");
        response = sendCommandToServer("simon: inv");
        assertTrue(response.contains("key"), "Fail to get the artefact");
        sendCommandToServer("simon: goto cabin");
        response = sendCommandToServer("simon: key drop");
        assertTrue(response.contains("Wrong command format"), "Perform the command with wrong format");
        response = sendCommandToServer("simon: drop key");
        assertTrue(response.contains("key"), "Fail to drop the artefact");
        response = sendCommandToServer("simon: inv");
        assertFalse(response.contains("key"), "Fail to drop the artefact");
        response = sendCommandToServer("simon: look");
        assertTrue(response.contains("key"), "Fail to drop the artefact in current location");

    }
    @Test
    void testGotoCommand(){
        String response = sendCommandToServer("simon: look");
        assertTrue(response.contains("cabin"), "You are in wrong location");
        response = sendCommandToServer("simon: goto cellar");
        assertTrue(response.contains("The path doesn't exist"), "You can goto the location where the path doesn't exist");
        response = sendCommandToServer("simon: forest goto");
        assertTrue(response.contains("Wrong command format"), "Perform the command with wrong format");
        response = sendCommandToServer("simon: goto forest");
        assertTrue(response.contains("forest"), "Fail to goto the location");

    }
    @Test
    void testLookPlayers(){
        sendCommandToServer("simon: look");
        sendCommandToServer("jack: look");
        sendCommandToServer("alber: look");
        String response = sendCommandToServer("tom: look");
        assertTrue(response.contains("simon"), "Fail to see other players");
        assertTrue(response.contains("jack"), "Fail to see other players");
        assertTrue(response.contains("alber"), "Fail to see other players");
    }
    @Test
    void testHealthCommand(){
        String response = sendCommandToServer("simon: health");
        assertTrue(response.contains("3"), "Fail to get player's health level");
        sendCommandToServer("simon: get potion");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: open trapdoor");
        sendCommandToServer("simon: goto cellar");
        sendCommandToServer("simon: goto cellar");
        sendCommandToServer("simon: hit elf");
        sendCommandToServer("simon: hit elf");
        response = sendCommandToServer("simon: hit elf");
        assertTrue(response.contains("You died and lost all of your items, you must return to the start of the game"), "You didn't die");
        ArrayList<ArtefactsEntity> inventory = server.getPlayers().get(0).getInventory();
        ArrayList<String> artefactsName = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++){
            artefactsName.add(inventory.get(i).getName());
        }
        assertFalse(artefactsName.contains("potion"), "The player didn't lose all of the items in his inventory");
        ArrayList<ArtefactsEntity> storeArtefacts = server.getStoreEntitiesList().get(0).get(2).getStoreArtefacts();
        ArrayList<String> loseArtefacts = new ArrayList<>();
        for (int i = 0; i < storeArtefacts.size(); i++){
            loseArtefacts.add(storeArtefacts.get(i).getName());
        }
        assertTrue(loseArtefacts.contains("potion"), "Artefacts are not dropped in the location where player ran out of health");
        assertTrue(server.getPlayers().get(0).getCurrentLocation().equals("cabin"), "The player is not transported to the start location of the game");
        assertTrue(server.getPlayers().get(0).getHealthLevel() == 3, "Player's health level didn't restore to full");
    }

    @Test
    void testSpecificCommand(){
        String response = sendCommandToServer("simon: open the trapdoor and tree");
        assertTrue(response.contains("Composite commands aren't supported"), "Can't detect invalid command");
        response = sendCommandToServer("simon: open");
        assertTrue(response.contains("The command doesn't have enough subject details"), "Perform the command without subjects");
        response = sendCommandToServer("simon: open the trapdoor");
        assertTrue(response.contains("Subjects are not all available"), "Perform the command that subjects are not available");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        response = sendCommandToServer("simon: open the trapdoor");
        assertTrue(response.contains("Subjects are not all available"), "Perform the command that subjects are not available");

        sendCommandToServer("jack: look");
        sendCommandToServer("jack: goto forest");
        sendCommandToServer("jack: get key");
        sendCommandToServer("jack: goto cabin");
        response = sendCommandToServer("jack: open trapdoor");
        assertTrue(response.contains("You unlock the trapdoor and see steps leading down into a cellar"), "Fail to perform action");
        ArrayList<LocationsEntity> locationsEntities = server.getStoreEntitiesList().get(1);
        assertTrue(locationsEntities.get(0).getStorePath().contains("cellar"), "Fail to produce the entity");
        PlayersEntity playersEntity = server.getPlayers().get(1);
        assertFalse(playersEntity.getInventory().contains("key"), "Fail to consume the entity");
        sendCommandToServer("jack: goto cellar");
        response = sendCommandToServer("jack: fight elf");
        assertTrue(response.contains("You attack the elf, but he fights back and you lose some health"), "Fail to perform the action");
        response = sendCommandToServer("jack: health");
        assertTrue(response.contains("2"), "Fail to lose health");
        assertFalse(response.contains("3"), "Fail to lose health");
        sendCommandToServer("jack: goto cabin");
        response = sendCommandToServer("jack: drink potion");
        assertTrue(response.contains("You drink the potion and your health improves"), "Fail to perform the action");
        response = sendCommandToServer("jack: health");
        assertTrue(response.contains("3"), "Fail to increase health");
        assertFalse(response.contains("2"), "Fail to increase health");

    }

}
