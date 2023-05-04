package edu.uob;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.*;

/** This class implements the STAG server. */
public final class GameServer {

    private static final char END_OF_TRANSMISSION = 4;
    File entitiesFile;
    private ArrayList<PlayersEntity> players = new ArrayList<>();
    private ArrayList<ArrayList<LocationsEntity>> storeEntitiesList = new ArrayList<>();
    private HashMap<String, HashSet<GameAction>> storeActions = new HashMap<>();


    public static void main(String[] args) throws IOException {
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.GameServer(File, File)}) otherwise we won't be able to mark
    * your submission correctly.
    *
    * <p>You MUST use the supplied {@code entitiesFile} and {@code actionsFile}
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    *
    */
    public GameServer(File entitiesFile, File actionsFile) {
        // TODO implement your server logic here
        this.entitiesFile = entitiesFile;
        ActionsParser actionsParser = new ActionsParser();
        storeActions = actionsParser.loadingActions(actionsFile);
    }


    /**
    * KEEP this signature (i.e. {@code edu.uob.GameServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming game commands and carries out the corresponding actions.
    */
    public String handleCommand(String command) {
        // TODO implement your server logic here
        while (command.contains("  ")) command = command.replaceAll("  ", " ");
        command = command.trim();
        String[] tokens =  command.split(":");
        if(tokens.length != 2){
            return "Invalid command\n";
        }
        String playerName = tokens[0];
        if(!playerName.matches("[A-Za-z\\s'-]+")){
            return "Invalid player name\n";
        }
        initializeData(playerName);
        int playerIndex = 0;
        for (int i = 0; i < players.size(); i++){
            if(playerName.equals(players.get(i).getName())){
                playerIndex = i;
            }
        }
        PlayersEntity nowPlayer = players.get(playerIndex);
        ArrayList<LocationsEntity> nowEntities = storeEntitiesList.get(playerIndex);
        String commandStr = tokens[1];
        commandStr = commandStr.trim();
        commandStr = commandStr.toLowerCase();

        String[] commandTokens = commandStr.split(" ");
        String[] basicCommand = {"inventory", "inv", "get", "drop", "goto", "look", "health"};
        Set<String> actionPhrases = storeActions.keySet();
        String respondToCommand = "";
        int basicNum = 0;
        int specificNum = 0;
        for (int i = 0; i < commandTokens.length; i++){
            String getToken = commandTokens[i];
            for (int j = 0; j < basicCommand.length; j++){
                if(getToken.equals(basicCommand[j])){
                    basicNum++;
                }
            }
        }
        Iterator<String> phraseIterator = actionPhrases.iterator();
        ArrayList<String> repeatPhrase = new ArrayList<>();
        String longPhrase = "";
        int repeatNum = 0;
        while (phraseIterator.hasNext()){
            String getPhrase = phraseIterator.next();
            for (int i = 0; i < commandTokens.length; i++){
                String getToken = commandTokens[i];
                if(getPhrase.contains(getToken)){
                    if(getPhrase.equals(getToken)){
                        specificNum++;
                        repeatPhrase.add(getPhrase);
                    }else if(i < commandTokens.length - 1){
                        String getNextToken = getToken + commandTokens[i + 1];
                        if(getPhrase.equals(getNextToken)){
                            specificNum++;
                            longPhrase = getPhrase;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < repeatPhrase.size(); i++){
            if(longPhrase.contains(repeatPhrase.get(i))){
                repeatNum++;
            }
        }
        specificNum = specificNum - repeatNum;
        if(basicNum == 0 && specificNum == 0){
            return "Invalid basic command or action phrase\n";
        }else if(basicNum == 1 && specificNum == 0){
            BasicCommand buildInCommand = new BasicCommand(commandTokens);
            respondToCommand = buildInCommand.respondToCommand(nowPlayer, nowEntities, players);
        }else if(basicNum == 0 && specificNum == 1){
            SpecificCommand specificCommand = new SpecificCommand(commandTokens);
            respondToCommand = specificCommand.respondToCommand(nowEntities, storeActions, nowPlayer);
        }else {
            return "Composite Commands are not supported\n";
        }
        storeEntitiesList.set(playerIndex, nowEntities);
        players.set(playerIndex, nowPlayer);
        return respondToCommand;
    }
    private void initializeData(String playerName){
        boolean isNewPlayer = true;
        for (int i = 0; i < players.size(); i++){
            PlayersEntity player = players.get(i);
            if(playerName.equals(player.getName())){
                isNewPlayer = false;
            }
        }
        if(isNewPlayer){
            EntitiesParser entitiesParser = new EntitiesParser();
            ArrayList<LocationsEntity> newEntities = entitiesParser.loadingEntities(entitiesFile);
            storeEntitiesList.add(newEntities);
            PlayersEntity newPlayer = new PlayersEntity(playerName, "");
            newPlayer.setCurrentLocation(newEntities.get(0).getName());
            players.add(newPlayer);

        }
    }

    public ArrayList<PlayersEntity> getPlayers() {
        return players;
    }

    public ArrayList<ArrayList<LocationsEntity>> getStoreEntitiesList() {
        return storeEntitiesList;
    }

    public HashMap<String, HashSet<GameAction>> getStoreActions() {
        return storeActions;
    }
    //  === Methods below are there to facilitate server related operations. ===

    /**
    * Starts a *blocking* socket server listening for new connections. This method blocks until the
    * current thread is interrupted.
    *
    * <p>This method isn't used for marking. You shouldn't have to modify this method, but you can if
    * you want to.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Handles an incoming connection from the socket server.
    *
    * <p>This method isn't used for marking. You shouldn't have to modify this method, but you can if
    * * you want to.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                System.out.println("Received message from " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
