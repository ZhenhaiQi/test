package edu.uob;

import java.util.*;
import java.util.stream.Stream;

public class SpecificCommand {
    private String[] commandTokens;

    public SpecificCommand(String[] commandTokens) {
        this.commandTokens = commandTokens;
    }
    public String respondToCommand(ArrayList<LocationsEntity> entities, HashMap<String, HashSet<GameAction>> actions, PlayersEntity player){
        String actionPhrase = getPhrase(actions);
        int compositeCommand = compositeCommand(actionPhrase, actions);
        if(compositeCommand > 0){
            return "Composite commands aren't supported\n";
        }
        HashSet<GameAction> gameActions = actions.get(actionPhrase);
        Iterator<GameAction> actionIterator = gameActions.iterator();
        String narration = "";
        ArrayList<GameAction> actionList = new ArrayList<>();
        ArrayList<Integer> entitiesNumList = new ArrayList<>();
        while (actionIterator.hasNext()){
            GameAction eachAction = actionIterator.next();
            actionList.add(eachAction);
            entitiesNumList.add(subjectsNum(eachAction));
        }

        int validNum = 0;
        for (int i = 0; i < entitiesNumList.size(); i++){
            int getNum = entitiesNumList.get(i);
            if(getNum > 0){
                validNum++;
            }
        }
        if(validNum == 0){
            return "The command doesn't have enough subject details\n";
        }
        if(validNum > 1){
            return "There is more than one '" + actionPhrase + "' action possible - which one do you want to perform ?\n";
        }
        if(validNum == 1){
            int actionIndex = 0;
            for (int i = 0; i < entitiesNumList.size(); i++){
                int getNum = entitiesNumList.get(i);
                if(getNum > 0){
                    actionIndex = i;
                }
            }
            GameAction performAction = actionList.get(actionIndex);
            if(isActionValid(player, entities, performAction)){
                produceEntity(player,entities, performAction);
                String isPlayerDied = consumeEntity(player, entities, performAction);
                narration = performAction.getNarration() + "\n" + isPlayerDied;
            }else{
                narration = "Subjects are not all available\n";
            }
        }
        return narration;
    }
    private String getPhrase(HashMap<String, HashSet<GameAction>> actions){
        Set<String> actionSet = actions.keySet();
        String actionPhrase = "";
        for (int i = 0; i < commandTokens.length; i++){
            Iterator<String> iterator = actionSet.iterator();
            String firstToken = commandTokens[i];
            while (iterator.hasNext()){
                String phrase = iterator.next();
                if(phrase.contains(firstToken)){
                    if(phrase.equals(firstToken)){
                        actionPhrase = firstToken;
                        return actionPhrase;
                    }else if(i < commandTokens.length - 1){
                        String secondToken = commandTokens[i + 1];
                        String addStr = firstToken + secondToken;
                        if(phrase.equals(addStr)){
                            actionPhrase = addStr;
                            return actionPhrase;
                        }
                    }
                }
            }
        }
        return actionPhrase;
    }
    private int subjectsNum(GameAction gameAction){
        int entitiesNum = 0;
        ArrayList<String> subjectEntities = gameAction.getSubjectEntities();
        for (int i = 0; i < commandTokens.length; i++){
            String token = commandTokens[i];
            for (int j = 0; j < subjectEntities.size(); j++){
                if(token.equals(subjectEntities.get(j))){
                    entitiesNum++;
                }
            }
        }
        return entitiesNum;
    }
    public int compositeCommand(String phrase, HashMap<String, HashSet<GameAction>> actions){
        int compositeNum = 0;
        HashSet<GameAction> getAction = actions.get(phrase);
        Iterator<GameAction> getActionIterator = getAction.iterator();
        ArrayList<String> subjectsList = new ArrayList<>();
        while (getActionIterator.hasNext()){
            GameAction eachGameAction = getActionIterator.next();
            ArrayList<String> subjectEntities = eachGameAction.getSubjectEntities();
            for (int i = 0; i< subjectEntities.size(); i++){
                subjectsList.add(subjectEntities.get(i));
            }
        }

        Set<String> actionPhrase = actions.keySet();
        Iterator<String> iterator = actionPhrase.iterator();
        while (iterator.hasNext()){
            String eachPhrase = iterator.next();
            HashSet<GameAction> gameActions = actions.get(eachPhrase);
            Iterator<GameAction> actionIterator = gameActions.iterator();
            while (actionIterator.hasNext()){
                GameAction gameAction = actionIterator.next();
                ArrayList<String> subjectEntities = gameAction.getSubjectEntities();
                for (int i = 0; i < commandTokens.length; i++){
                    String getEachToken = commandTokens[i];
                    if(subjectEntities.contains(getEachToken) && !subjectsList.contains(getEachToken)){
                        compositeNum++;
                    }
                }
            }
        }
        return compositeNum;
    }

    private boolean isActionValid(PlayersEntity player, ArrayList<LocationsEntity> locationsEntities, GameAction gameAction){
        ArrayList<String> subjectEntities = gameAction.getSubjectEntities();
        ArrayList<ArtefactsEntity> inventory = player.getInventory();
        ArrayList<String> artefacts = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++){
            artefacts.add(inventory.get(i).getName());
        }
        String locationName = player.getCurrentLocation();
        int locationIndex = getLocationIndex(locationName, locationsEntities);
        LocationsEntity currentLocation = locationsEntities.get(locationIndex);
        ArrayList<String> locationEntities = new ArrayList<>();
        locationEntities.add(locationName);
        ArrayList<ArtefactsEntity> storeArtefacts = currentLocation.getStoreArtefacts();
        for (int i = 0; i < storeArtefacts.size(); i++){
            locationEntities.add(storeArtefacts.get(i).getName());
        }
        ArrayList<CharactersEntity> storeCharacters = currentLocation.getStoreCharacters();
        for (int i = 0; i < storeCharacters.size(); i++){
            locationEntities.add(storeCharacters.get(i).getName());
        }
        ArrayList<FurnitureEntity> storeFurniture = currentLocation.getStoreFurniture();
        for (int i = 0; i < storeFurniture.size(); i++){
            locationEntities.add(storeFurniture.get(i).getName());
        }
        ArrayList<String> storeEntities = new ArrayList<>();
        storeEntities.addAll(artefacts);
        storeEntities.addAll(locationEntities);
        for (int i = 0; i < subjectEntities.size(); i++){
            if(!storeEntities.contains(subjectEntities.get(i))){
                return false;
            }
        }
        return true;
    }

    private void produceEntity(PlayersEntity player, ArrayList<LocationsEntity> locationsEntities, GameAction gameAction){
        ArrayList<String> producedEntities = gameAction.getProducedEntities();
        String locationName = player.getCurrentLocation();
        int locationIndex = getLocationIndex(locationName, locationsEntities);
        for (int i = 0; i < producedEntities.size(); i++){
            String getEntity = producedEntities.get(i);
            int healthLevel = player.getHealthLevel();
            if(getEntity.equals("health") && healthLevel < 3){
                healthLevel++;
                player.setHealthLevel(healthLevel);
            }
            for (int j = 0; j < locationsEntities.size(); j++){
                LocationsEntity currentLocation = locationsEntities.get(locationIndex);
                if(j != locationIndex){
                    LocationsEntity getLocation = locationsEntities.get(j);
                    String getLocationName = getLocation.getName();
                    if(getEntity.equals(getLocationName)){
                        ArrayList<String> storePath = currentLocation.getStorePath();
                        storePath.add(getLocationName);
                        currentLocation.setStorePath(storePath);
                        locationsEntities.set(locationIndex, currentLocation);
                    }

                    ArrayList<ArtefactsEntity> storeArtefacts = getLocation.getStoreArtefacts();
                    for (int k = 0; k < storeArtefacts.size(); k++){
                        ArtefactsEntity artefact = storeArtefacts.get(k);
                        if(getEntity.equals(artefact.getName())){
                            storeArtefacts.remove(k);
                            getLocation.setStoreArtefacts(storeArtefacts);
                            locationsEntities.set(j, getLocation);
                            ArrayList<ArtefactsEntity> currentArtefacts = currentLocation.getStoreArtefacts();
                            currentArtefacts.add(artefact);
                            currentLocation.setStoreArtefacts(currentArtefacts);
                            locationsEntities.set(locationIndex, currentLocation);
                        }
                    }

                    ArrayList<CharactersEntity> storeCharacters = getLocation.getStoreCharacters();
                    for (int k = 0; k < storeCharacters.size(); k++){
                        CharactersEntity character = storeCharacters.get(k);
                        if(getEntity.equals(character.getName())){
                            storeCharacters.remove(k);
                            getLocation.setStoreCharacters(storeCharacters);
                            locationsEntities.set(j, getLocation);
                            ArrayList<CharactersEntity> currentCharacters = currentLocation.getStoreCharacters();
                            currentCharacters.add(character);
                            currentLocation.setStoreCharacters(currentCharacters);
                            locationsEntities.set(locationIndex, currentLocation);
                        }
                    }

                    ArrayList<FurnitureEntity> storeFurniture = getLocation.getStoreFurniture();
                    for (int k = 0; k < storeFurniture.size(); k++){
                        FurnitureEntity furniture = storeFurniture.get(k);
                        if(getEntity.equals(furniture.getName())){
                            storeFurniture.remove(k);
                            getLocation.setStoreFurniture(storeFurniture);
                            locationsEntities.set(j, getLocation);
                            ArrayList<FurnitureEntity> currentFurniture = currentLocation.getStoreFurniture();
                            currentFurniture.add(furniture);
                            currentLocation.setStoreFurniture(currentFurniture);
                            locationsEntities.set(locationIndex, currentLocation);
                        }
                    }
                }
            }
        }
    }

    private String consumeEntity(PlayersEntity player, ArrayList<LocationsEntity> locationsEntities, GameAction gameAction){
        ArrayList<String> consumedEntities = gameAction.getConsumedEntities();
        int storeroomIndex = getLocationIndex("storeroom", locationsEntities);
        String playerLocationName = player.getCurrentLocation();
        int currentIndex = getLocationIndex(playerLocationName, locationsEntities);
        for (int i = 0; i < consumedEntities.size(); i++){
            String getEntity = consumedEntities.get(i);
            ArrayList<ArtefactsEntity> inventory = player.getInventory();
            LocationsEntity playerLocation = locationsEntities.get(currentIndex);
            int healthLevel = player.getHealthLevel();
            if (getEntity.equals("health") && healthLevel > 0){
                healthLevel--;
                player.setHealthLevel(healthLevel);
                if(healthLevel == 0){
                    ArrayList<ArtefactsEntity> dropArtefacts = playerLocation.getStoreArtefacts();
                    for (int j = 0; j < inventory.size(); j++){
                        dropArtefacts.add(inventory.get(j));
                    }
                    playerLocation.setStoreArtefacts(dropArtefacts);
                    locationsEntities.set(currentIndex, playerLocation);
                    inventory.clear();
                    player.setInventory(inventory);
                    player.setCurrentLocation(locationsEntities.get(0).getName());
                    player.setHealthLevel(3);
                    return "You died and lost all of your items, you must return to the start of the game";
                }
            }
            for (int j = 0; j < inventory.size(); j++){
                ArtefactsEntity playerInv = inventory.get(j);
                LocationsEntity storeroom = locationsEntities.get(storeroomIndex);
                if(getEntity.equals(playerInv.getName())){
                    inventory.remove(j);
                    player.setInventory(inventory);
                    ArrayList<ArtefactsEntity> currentArtefacts = storeroom.getStoreArtefacts();
                    currentArtefacts.add(playerInv);
                    storeroom.setStoreArtefacts(currentArtefacts);
                    locationsEntities.set(storeroomIndex, storeroom);
                }
            }
            for (int j = 0; j < locationsEntities.size(); j++) {
                LocationsEntity storeroom = locationsEntities.get(storeroomIndex);
                if(j != storeroomIndex) {
                    LocationsEntity currentLocation = locationsEntities.get(j);
                    String getLocationName = currentLocation.getName();
                    if(getEntity.equals(getLocationName)){
                        ArrayList<String> storePath = playerLocation.getStorePath();
                        int removeIndex = 0;
                        for (int k = 0; k < storePath.size(); k++){
                            String consumePath = storePath.get(k);
                            if(consumePath.equals(getLocationName)){
                                removeIndex = k;
                            }
                        }
                        storePath.remove(removeIndex);
                        playerLocation.setStorePath(storePath);
                        locationsEntities.set(currentIndex, playerLocation);
                    }
                    ArrayList<ArtefactsEntity> storeArtefacts = currentLocation.getStoreArtefacts();
                    for (int k = 0; k < storeArtefacts.size(); k++) {
                        ArtefactsEntity artefact = storeArtefacts.get(k);
                        if (getEntity.equals(artefact.getName())) {
                            storeArtefacts.remove(k);
                            currentLocation.setStoreArtefacts(storeArtefacts);
                            locationsEntities.set(j, currentLocation);
                            ArrayList<ArtefactsEntity> currentArtefacts = storeroom.getStoreArtefacts();
                            currentArtefacts.add(artefact);
                            storeroom.setStoreArtefacts(currentArtefacts);
                            locationsEntities.set(storeroomIndex, storeroom);
                        }
                    }

                    ArrayList<CharactersEntity> storeCharacters = currentLocation.getStoreCharacters();
                    for (int k = 0; k < storeCharacters.size(); k++) {
                        CharactersEntity character = storeCharacters.get(k);
                        if (getEntity.equals(character.getName())) {
                            storeCharacters.remove(k);
                            currentLocation.setStoreCharacters(storeCharacters);
                            locationsEntities.set(j, currentLocation);
                            ArrayList<CharactersEntity> currentCharacters = storeroom.getStoreCharacters();
                            currentCharacters.add(character);
                            storeroom.setStoreCharacters(currentCharacters);
                            locationsEntities.set(storeroomIndex, storeroom);
                        }
                    }

                    ArrayList<FurnitureEntity> storeFurniture = currentLocation.getStoreFurniture();
                    for (int k = 0; k < storeFurniture.size(); k++) {
                        FurnitureEntity furniture = storeFurniture.get(k);
                        if (getEntity.equals(furniture.getName())) {
                            storeFurniture.remove(k);
                            currentLocation.setStoreFurniture(storeFurniture);
                            locationsEntities.set(j, currentLocation);
                            ArrayList<FurnitureEntity> currentFurniture = storeroom.getStoreFurniture();
                            currentFurniture.add(furniture);
                            storeroom.setStoreFurniture(currentFurniture);
                            locationsEntities.set(storeroomIndex, storeroom);
                        }
                    }
                }
            }
        }
        return "";
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
