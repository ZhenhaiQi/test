package edu.uob;

import java.util.ArrayList;

public class LocationsEntity extends GameEntity{

    private ArrayList<String> storePath = new ArrayList<>();
    private ArrayList<CharactersEntity> storeCharacters = new ArrayList<>();
    private ArrayList<ArtefactsEntity> storeArtefacts = new ArrayList<>();
    private ArrayList<FurnitureEntity> storeFurniture = new ArrayList<>();

    public LocationsEntity(String name, String description) {
        super(name, description);
    }

    public ArrayList<String> getStorePath() {
        return storePath;
    }

    public ArrayList<CharactersEntity> getStoreCharacters() {
        return storeCharacters;
    }

    public ArrayList<ArtefactsEntity> getStoreArtefacts() {
        return storeArtefacts;
    }

    public ArrayList<FurnitureEntity> getStoreFurniture() {
        return storeFurniture;
    }

    public void setStorePath(ArrayList<String> storePath) {
        this.storePath = storePath;
    }

    public void setStoreCharacters(ArrayList<CharactersEntity> storeCharacters) {
        this.storeCharacters = storeCharacters;
    }

    public void setStoreArtefacts(ArrayList<ArtefactsEntity> storeArtefacts) {
        this.storeArtefacts = storeArtefacts;
    }

    public void setStoreFurniture(ArrayList<FurnitureEntity> storeFurniture) {
        this.storeFurniture = storeFurniture;
    }
}
