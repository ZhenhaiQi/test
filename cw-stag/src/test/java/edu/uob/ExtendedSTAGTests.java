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
public class ExtendedSTAGTests {
    private GameServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    void setup() {
        File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
    }

    String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }
    @Test
    void testActions(){
        sendCommandToServer("Justin: look");
        sendCommandToServer("John Lemon: look");
        String response = sendCommandToServer("simon: look");
        assertTrue(response.contains("Justin"), "You can't see other players");
        assertTrue(response.contains("John Lemon"), "You can't see other players");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        sendCommandToServer("simon: goto cabin");
        response = sendCommandToServer("simon: unlock trapdoor");
        assertTrue(response.contains("You unlock the door and see steps leading down into a cellar"), "Fail to unlock trapdoor");
        response = sendCommandToServer("simon: look");
        assertTrue(response.contains("cellar"), "Fail to unlock trapdoor");
        sendCommandToServer("simon: get axe");
        sendCommandToServer("simon: get coin");
        sendCommandToServer("simon: goto forest");
        response = sendCommandToServer("simon: cut down tree");
        assertTrue(response.contains("You cut down the tree with the axe"), "Fail to cut down the tree");
        sendCommandToServer("simon: get log");
        response = sendCommandToServer("simon: inv");
        assertTrue(response.contains("log"), "Fail to cut down tree");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: goto cellar");
        response = sendCommandToServer("simon: pay elf");
        sendCommandToServer("simon: get shovel");
        assertTrue(response.contains("You pay the elf your silver coin and he produces a shovel"), "Fail to pay to the elf");
        response = sendCommandToServer("simon: inv");
        assertTrue(response.contains("shovel"), "Don't produce a shovel");
        assertFalse(response.contains("coin"), "Coin isn't consumed");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: goto riverbank");
        sendCommandToServer("simon: get horn");
        response = sendCommandToServer("simon: bridge river");
        assertTrue(response.contains("You bridge the river with the log and can now reach the other side"), "Fail to bridge river");
        response = sendCommandToServer("simon: inv");
        assertFalse(response.contains("log"), "Don't consume the artefact");
        response = sendCommandToServer("simon: look");
        assertTrue(response.contains("clearing"), "Don't produce the path to clearing");
        sendCommandToServer("simon: goto clearing");
        response = sendCommandToServer("simon: dig ground");
        assertTrue(response.contains("You dig into the soft ground and unearth a pot of gold !!!"), "Fail to dig ground");
        response = sendCommandToServer("simon: look");
        assertTrue(response.contains("hole"), "Don't produce the hole");
        sendCommandToServer("simon: get gold");
        response = sendCommandToServer("simon: inv");
        assertTrue(response.contains("gold"), "Don't produce the gold");
        response = sendCommandToServer("simon: blow horn");
        assertTrue(response.contains("You blow the horn and as if by magic, a lumberjack appears !"), "Fail to blow horn");
        response = sendCommandToServer("simon: look");
        assertTrue(response.contains("lumberjack"), "Don't produce the lumberjack");
    }
}
