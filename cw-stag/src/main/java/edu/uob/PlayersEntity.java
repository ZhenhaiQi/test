package edu.uob;

import java.util.ArrayList;

public class PlayersEntity extends GameEntity{
    private ArrayList<ArtefactsEntity> inventory = new ArrayList<>();
    private String currentLocation;
    private int healthLevel = 3;
    public PlayersEntity(String name, String description) {
        super(name, description);
    }

    public ArrayList<ArtefactsEntity> getInventory() {
        return inventory;
    }

    public void setInventory(ArrayList<ArtefactsEntity> inventory) {
        this.inventory = inventory;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public int getHealthLevel() {
        return healthLevel;
    }

    public void setHealthLevel(int healthLevel) {
        this.healthLevel = healthLevel;
    }
}
