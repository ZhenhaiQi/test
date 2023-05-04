package edu.uob;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class EntitiesParser {
    public EntitiesParser() {
    }

    public ArrayList<LocationsEntity> loadingEntities(File entitiesFile){
        ArrayList<LocationsEntity> locationsList = new ArrayList<>();
        try {
            Parser parser = new Parser();
            FileReader reader = new FileReader(entitiesFile);
            parser.parse(reader);
            Graph wholeDocument = parser.getGraphs().get(0);
            ArrayList<Graph> sections = wholeDocument.getSubgraphs();
            ArrayList<Graph> locations = sections.get(0).getSubgraphs();
            for (int i = 0; i < locations.size(); i++){
                Graph eachLocation = locations.get(i);
                ArrayList<Graph> locationsAttr = eachLocation.getSubgraphs();

                Node firstDetails = eachLocation.getNodes(false).get(0);
                String locationName = (firstDetails.getId().getId()).toLowerCase();
                String locationDescription = firstDetails.getAttribute("description");
                LocationsEntity locationsEntity = new LocationsEntity(locationName, locationDescription);

                for (int j = 0; j < locationsAttr.size(); j++){
                    Graph eachLocationAttr = locationsAttr.get(j);
                    String attrName = (eachLocationAttr.getId().getId()).toLowerCase();

                    if(attrName.equals("characters")){
                        locationsEntity.setStoreCharacters(setCharactersAttr(eachLocationAttr));
                    }
                    if(attrName.equals("artefacts")){
                        locationsEntity.setStoreArtefacts(setArtefactsAttr(eachLocationAttr));
                    }
                    if(attrName.equals("furniture")){
                        locationsEntity.setStoreFurniture(setFurnitureAttr(eachLocationAttr));
                    }
                }
                locationsList.add(locationsEntity);
            }

            ArrayList<Edge> paths = sections.get(1).getEdges();
            for (int i = 0; i < paths.size(); i++){
                Edge eachPath = paths.get(i);
                Node fromLocation = eachPath.getSource().getNode();
                String fromName = (fromLocation.getId().getId()).toLowerCase();
                Node toLocation = eachPath.getTarget().getNode();
                String toName = (toLocation.getId().getId()).toLowerCase();
                for (int j = 0; j < locationsList.size(); j++){
                    if(locationsList.get(j).getName().equals(fromName)){
                        LocationsEntity locationsEntity = locationsList.get(j);
                        ArrayList<String> storePath = locationsEntity.getStorePath();
                        storePath.add(toName);
                        locationsEntity.setStorePath(storePath);
                        locationsList.set(j, locationsEntity);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return locationsList;
    }

    private ArrayList<CharactersEntity> setCharactersAttr(Graph locationAttr){
        int attrNum = locationAttr.getNodes(false).size();
        ArrayList<CharactersEntity> charactersList = new ArrayList<>();
        for(int i = 0; i < attrNum; i++){
            Node attributeNode = locationAttr.getNodes(false).get(i);
            String attributeName = (attributeNode.getId().getId()).toLowerCase();
            String attributeDesc = attributeNode.getAttribute("description");
            CharactersEntity charactersEntity = new CharactersEntity(attributeName, attributeDesc);
            charactersList.add(charactersEntity);
        }
        return charactersList;
    }

    private ArrayList<ArtefactsEntity> setArtefactsAttr(Graph locationAttr){
        int attrNum = locationAttr.getNodes(false).size();
        ArrayList<ArtefactsEntity> artefactsList = new ArrayList<>();
        for(int i = 0; i < attrNum; i++){
            Node attributeNode = locationAttr.getNodes(false).get(i);
            String attributeName = (attributeNode.getId().getId()).toLowerCase();
            String attributeDesc = attributeNode.getAttribute("description");
            ArtefactsEntity artefactsEntity = new ArtefactsEntity(attributeName, attributeDesc);
            artefactsList.add(artefactsEntity);
        }
        return artefactsList;
    }

    private ArrayList<FurnitureEntity> setFurnitureAttr(Graph locationAttr){
        int attrNum = locationAttr.getNodes(false).size();
        ArrayList<FurnitureEntity> furnitureList = new ArrayList<>();
        for(int i = 0; i < attrNum; i++){
            Node attributeNode = locationAttr.getNodes(false).get(i);
            String attributeName = (attributeNode.getId().getId()).toLowerCase();
            String attributeDesc = attributeNode.getAttribute("description");
            FurnitureEntity furnitureEntity = new FurnitureEntity(attributeName, attributeDesc);
            furnitureList.add(furnitureEntity);
        }
        return furnitureList;
    }
}
