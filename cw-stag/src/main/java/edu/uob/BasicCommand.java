package edu.uob;

import java.util.ArrayList;

public class BasicCommand {
    private String[] commandTokens;

    public BasicCommand(String[] commandTokens) {
        this.commandTokens = commandTokens;
    }

    public String respondToCommand(PlayersEntity player, ArrayList<LocationsEntity> locationsEntities, ArrayList<PlayersEntity> allPlayer){
        String[] basicCommand = {"inventory", "inv", "get", "drop", "goto", "look", "health"};
        String getCommand = "";
        int commandIndex = 0;
        for (int i = 0; i < commandTokens.length; i++){
            String getToken = commandTokens[i];
            for (int j = 0; j < basicCommand.length; j++){
                if(getToken.equals(basicCommand[j])){
                    getCommand = getToken;
                    commandIndex = i;
                }
            }
        }
        if(getCommand.equals("inventory") || getCommand.equals("inv")){
            return invCommand(player);
        }
        if(getCommand.equals("get")){
            return getCommand(player, locationsEntities, commandIndex);
        }
        if(getCommand.equals("drop")){
            return dropCommand(player, locationsEntities, commandIndex);
        }
        if(getCommand.equals("goto")){
            return gotoCommand(player, locationsEntities, allPlayer, commandIndex);
        }
        if(getCommand.equals("look")){
            return lookCommand(player, locationsEntities, allPlayer);
        }
        if(getCommand.equals("health")){
            return "Player " + player.getName() + "'s health level is " + player.getHealthLevel() + "\n";
        }
        return "Command is invalid\n";
    }
    private String invCommand(PlayersEntity player){
        ArrayList<ArtefactsEntity> playerInventory = player.getInventory();
        String invString = "";
        for (int i = 0; i < playerInventory.size(); i++){
            invString = invString + playerInventory.get(i).getName() + "\n";
        }
        invString = "You carry these artefacts:\n" + invString;
        return invString;
    }
    private String getCommand(PlayersEntity player, ArrayList<LocationsEntity> locationsEntities, int commandIndex){
        String locationName = player.getCurrentLocation();
        int locationIndex = getLocationIndex(locationName, locationsEntities);
        LocationsEntity currentLocation = locationsEntities.get(locationIndex);
        ArrayList<ArtefactsEntity> storeArtefacts = currentLocation.getStoreArtefacts();
        int artefactIndex = 0;
        int artefactsNum = 0;
        int entityIndex = 0;
        for (int i = 0 ; i < commandTokens.length; i++){
            for (int j = 0; j < storeArtefacts.size(); j++){
                String artefactName = storeArtefacts.get(j).getName();
                if(artefactName.equals(commandTokens[i])){
                    artefactsNum++;
                    artefactIndex = j;
                    entityIndex = i;
                }
            }
        }
        if(artefactsNum == 0){
            return "The artefact doesn't exist\n";
        }else if(artefactsNum == 1){
            if(entityIndex > commandIndex){
                ArtefactsEntity addArtefact = storeArtefacts.get(artefactIndex);
                String getArtefactName = addArtefact.getName();
                ArrayList<ArtefactsEntity> playerInventory = player.getInventory();
                playerInventory.add(addArtefact);
                player.setInventory(playerInventory);
                storeArtefacts.remove(artefactIndex);
                currentLocation.setStoreArtefacts(storeArtefacts);
                locationsEntities.set(locationIndex, currentLocation);
                return "You picked up the " + getArtefactName + "\n";
            }else{
                return "Wrong command format";
            }
        }else{
            return "Composite Commands are not supported\n";
        }
    }

    private String dropCommand(PlayersEntity player, ArrayList<LocationsEntity> locationsEntities, int commandIndex){
        String locationName = player.getCurrentLocation();
        int locationIndex = getLocationIndex(locationName, locationsEntities);
        LocationsEntity currentLocation = locationsEntities.get(locationIndex);
        ArrayList<ArtefactsEntity> playerInventory = player.getInventory();
        int artefactIndex = 0;
        int artefactsNum = 0;
        int entityIndex = 0;
        for (int i = 0 ; i < commandTokens.length; i++){
            for (int j = 0; j < playerInventory.size(); j++){
                String artefactName = playerInventory.get(j).getName();
                if(artefactName.equals(commandTokens[i])){
                    artefactsNum++;
                    artefactIndex = j;
                    entityIndex = i;
                }
            }
        }
        if(artefactsNum == 0){
            return "The artefact doesn't exist\n";
        }else if(artefactsNum == 1){
            if(entityIndex > commandIndex){
                ArtefactsEntity placeArtefact = playerInventory.get(artefactIndex);
                String getArtefactName = placeArtefact.getName();
                ArrayList<ArtefactsEntity> storeArtefacts = currentLocation.getStoreArtefacts();
                storeArtefacts.add(placeArtefact);
                currentLocation.setStoreArtefacts(storeArtefacts);
                locationsEntities.set(locationIndex, currentLocation);
                playerInventory.remove(artefactIndex);
                player.setInventory(playerInventory);
                return "You drop the " + getArtefactName + "\n";
            }else{
                return "Wrong command format";
            }

        }else{
            return "Composite Commands are not supported\n";
        }

    }

    private String gotoCommand(PlayersEntity player, ArrayList<LocationsEntity> locationsEntities, ArrayList<PlayersEntity> allPlayer, int commandIndex){
        String locationName = player.getCurrentLocation();
        int locationIndex = getLocationIndex(locationName,locationsEntities);
        LocationsEntity currentLocation = locationsEntities.get(locationIndex);
        ArrayList<String> storePath = currentLocation.getStorePath();
        int pathIndex = 0;
        int pathsNum = 0;
        int entityIndex = 0;
        for (int i = 0 ; i < commandTokens.length; i++){
            for (int j = 0; j < storePath.size(); j++){
                String pathName = storePath.get(j);
                if(pathName.equals(commandTokens[i])){
                    pathsNum++;
                    pathIndex = j;
                    entityIndex = i;
                }
            }
        }
        if(pathsNum == 0){
            return "The path doesn't exist\n";
        }else if(pathsNum == 1){
            if(entityIndex > commandIndex){
                String getPathName = storePath.get(pathIndex);
                player.setCurrentLocation(getPathName);
                return lookCommand(player, locationsEntities, allPlayer);
            }else{
                return "Wrong command format";
            }
        }else{
            return "Composite Commands are not supported\n";
        }
    }

    private String lookCommand(PlayersEntity player, ArrayList<LocationsEntity> locationsEntities, ArrayList<PlayersEntity> allPlayer){
        String locationName = player.getCurrentLocation();
        int locationIndex = getLocationIndex(locationName,locationsEntities);
        LocationsEntity currentLocation = locationsEntities.get(locationIndex);
        ArrayList<ArtefactsEntity> storeArtefacts = currentLocation.getStoreArtefacts();
        String artefactsStr = "";
        for (int i = 0; i < storeArtefacts.size(); i++){
            ArtefactsEntity artefactsEntity = storeArtefacts.get(i);
            artefactsStr = artefactsStr + artefactsEntity.getName() + ", "+ artefactsEntity.getDescription() + "\n";
        }

        ArrayList<CharactersEntity> storeCharacters = currentLocation.getStoreCharacters();
        String charactersStr = "";
        for (int i = 0; i < storeCharacters.size(); i++){
            CharactersEntity charactersEntity = storeCharacters.get(i);
            charactersStr = charactersStr + charactersEntity.getName() + ", " + charactersEntity.getDescription()+ "\n";
        }

        ArrayList<FurnitureEntity> storeFurniture = currentLocation.getStoreFurniture();
        String furnitureStr = "";
        for (int i = 0; i < storeFurniture.size(); i++){
            FurnitureEntity furnitureEntity = storeFurniture.get(i);
            furnitureStr = furnitureStr + furnitureEntity.getName() + ", " + furnitureEntity.getDescription()+ "\n";
        }

        ArrayList<String> storePath = currentLocation.getStorePath();
        String pathList = "";
        for (int i = 0; i < storePath.size(); i++){
            String pathStr = storePath.get(i);
            pathList = pathList + pathStr + "\n";
        }
        pathList = "You can access from here:\n" + pathList;
        String nowPlayer = player.getName();
        String playersStr = "";
        for (int i = 0; i < allPlayer.size(); i++){
            PlayersEntity otherPlayer = allPlayer.get(i);
            if(!nowPlayer.equals(otherPlayer.getName())){
                if(locationName.equals(otherPlayer.getCurrentLocation())){
                    playersStr = playersStr + otherPlayer.getName() + "\n" ;
                }
            }
        }
        if(playersStr.equals("")){
            playersStr = "There are no other players here\n";
        }else{
            playersStr = "You can see other players:\n" + playersStr;
        }
        String respondStr = "The location name is " + currentLocation.getName() + ", you are in " + currentLocation.getDescription() + ". You can see:\n";

        return respondStr + artefactsStr + charactersStr + furnitureStr + pathList + playersStr;
    }

    private int getLocationIndex(String locationName, ArrayList<LocationsEntity> locationsEntities){
        int locationIndex = 0;
        for (int i = 0; i < locationsEntities.size(); i++){
            if(locationName.equals(locationsEntities.get(i).getName())){
                locationIndex = i;
            }
        }
        return locationIndex;
    }
}
