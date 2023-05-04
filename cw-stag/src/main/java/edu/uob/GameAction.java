package edu.uob;

import java.util.ArrayList;
import java.util.Objects;

public class GameAction
{
    private ArrayList<String> triggerPhrases = new ArrayList<>();
    private ArrayList<String> subjectEntities = new ArrayList<>();
    private ArrayList<String> consumedEntities = new ArrayList<>();
    private ArrayList<String> producedEntities = new ArrayList<>();
    private String narration;

    public GameAction() {
    }

    public ArrayList<String> getTriggerPhrases() {
        return triggerPhrases;
    }

    public String getNarration() {
        return narration;
    }

    public ArrayList<String> getSubjectEntities() {
        return subjectEntities;
    }

    public ArrayList<String> getConsumedEntities() {
        return consumedEntities;
    }

    public ArrayList<String> getProducedEntities() {
        return producedEntities;
    }

    public void setTriggerPhrases(ArrayList<String> triggerPhrases) {
        this.triggerPhrases = triggerPhrases;
    }

    public void setSubjectEntities(ArrayList<String> subjectEntities) {
        this.subjectEntities = subjectEntities;
    }

    public void setConsumedEntities(ArrayList<String> consumedEntities) {
        this.consumedEntities = consumedEntities;
    }

    public void setProducedEntities(ArrayList<String> producedEntities) {
        this.producedEntities = producedEntities;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }


}
