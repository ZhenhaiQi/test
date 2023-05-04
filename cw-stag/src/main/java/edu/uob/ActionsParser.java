package edu.uob;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ActionsParser {
    public ActionsParser() {
    }
    public HashMap<String,HashSet<GameAction>> loadingActions(File actionsFile){
        HashSet<GameAction> storeAction = new HashSet<>();
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(actionsFile);
            Element root = document.getDocumentElement();
            NodeList actions = root.getChildNodes();
            int elementCount = 0;
            for (int i = 0; i < actions.getLength(); i++) {
                Node node = actions.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    elementCount++;
                }
            }
            for (int i = 0; i < elementCount; i++){
                Element getAction = (Element)actions.item(2 * i + 1);
                GameAction gameAction = new GameAction();
                gameAction.setTriggerPhrases(getActionAttribute(getAction, "triggers", "keyphrase"));
                gameAction.setSubjectEntities(getActionAttribute(getAction, "subjects", "entity"));
                gameAction.setConsumedEntities(getActionAttribute(getAction, "consumed", "entity"));
                gameAction.setProducedEntities(getActionAttribute(getAction, "produced", "entity"));
                String getNarration = getAction.getElementsByTagName("narration").item(0).getTextContent();
                gameAction.setNarration(getNarration);
                storeAction.add(gameAction);
            }
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
        return getActionMap(storeAction);
    }

    private ArrayList<String> getActionAttribute(Element getAction, String attrTagName, String elementTagName){
        Element element = (Element)getAction.getElementsByTagName(attrTagName).item(0);
        int elementLength = element.getElementsByTagName(elementTagName).getLength();
        ArrayList<String> attrList = new ArrayList<>();
        for (int i = 0; i < elementLength; i++){
            String getPhrase = (element.getElementsByTagName(elementTagName).item(i).getTextContent()).toLowerCase();
            while (getPhrase.contains("  ")) getPhrase = getPhrase.replaceAll("  ", " ");
            getPhrase = getPhrase.replace(" ", "");
            attrList.add(getPhrase);
        }
        return attrList;
    }

    private HashMap<String,HashSet<GameAction>> getActionMap(HashSet<GameAction> storeAction){
        HashMap<String,HashSet<GameAction>> actionsMap = new HashMap<String, HashSet<GameAction>>();
        Iterator<GameAction> iterator = storeAction.iterator();
        while (iterator.hasNext()){
            GameAction gameAction = iterator.next();
            ArrayList<String> actionPhrases = gameAction.getTriggerPhrases();
            for (int i = 0; i < actionPhrases.size(); i++){
                String storePhrases = actionPhrases.get(i);
                Set<String> actionKeySet = actionsMap.keySet();
                boolean isActionExist = false;
                for(String actionKey: actionKeySet){
                    if (actionKey.equals(storePhrases)){
                        isActionExist = true;
                        HashSet<GameAction> gameActionSet = actionsMap.get(actionKey);
                        gameActionSet.add(gameAction);
                        actionsMap.put(actionKey, gameActionSet);
                    }
                }
                if(!isActionExist){
                    HashSet<GameAction> gameActionSet = new HashSet<>();
                    gameActionSet.add(gameAction);
                    actionsMap.put(storePhrases, gameActionSet);
                }
            }

        }
        return actionsMap;
    }
}
