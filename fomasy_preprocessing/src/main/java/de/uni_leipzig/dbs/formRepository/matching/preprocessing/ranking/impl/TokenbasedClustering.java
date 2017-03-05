package de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.impl;

import de.uni_leipzig.dbs.formRepository.dataModel.*;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.Cluster;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.ClusterElement;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.OntologyClustering;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.*;

/**
 * Created by christen on 14.02.2017.
 */
public class TokenbasedClustering implements OntologyClustering {

  public static final String SPLIT_REGEX = "[^A-Za-z0-9]";



  @Override
  public Collection<Cluster> cluster(EntityStructureVersion esv, String type, Map<String, Object> props,
                                     Set<GenericProperty> genericProperties) {
    List<ClusterElement> clusterElements = this.generateElements(esv, genericProperties);
    float threshold = (float) props.get(THRESHOLD);
    UndirectedGraph<ClusterElement, Integer> graph = this.buildGraph(clusterElements,props, type, threshold);
    WeakComponentClusterer<ClusterElement, Integer> clusterer = new WeakComponentClusterer<>();
    Set<Set<ClusterElement>> clusterSets = clusterer.transform(graph);
    Collection <Cluster> clusters= new HashSet<>();
    for (Set<ClusterElement> cluster: clusterSets){
      Cluster c = new PropertyCluster();
      for (ClusterElement ce: cluster){
        c.addElement(ce);
      }
      clusters.add(c);
    }
    return clusters;
  }

  private UndirectedGraph<ClusterElement,Integer> buildGraph (List<ClusterElement> elements, Map<String, Object> props,
          String distance, float threshold){
    SimMeasure measure = new SimMeasure();
    UndirectedGraph<ClusterElement, Integer> graph = new UndirectedSparseGraph<>();
    int id = 0;
    for (int i=0;i<elements.size();i++){
      graph.addVertex(elements.get(i));
      int add =0;
      for (int j = i+1; j<elements.size();j++){
        if (elements.get(i).getEntityId()!= elements.get(j).getEntityId()) {

        }
      }
      if (i%100000==0)
        System.out.println("added:"+add);
    }
    return graph;
  }

  private List<ClusterElement> generateElements(EntityStructureVersion esv, Set<GenericProperty> properties) {
    List<ClusterElement> elements = new ArrayList<>();
    for (GenericEntity ge: esv.getEntities()){
      List<PropertyValue> propertyValueSet = ge.getValues(properties.toArray(new GenericProperty[]{}));
      for (PropertyValue pv : propertyValueSet){
        int[] tids = this.getTokens(pv);
        if (tids.length!=0){
          ClusterElement ce = new PropertyElement(ge.getId(),pv.getId(),tids);
          elements.add(ce);
        }
      }
    }
    return elements;
  }


  private int[] getTokens (PropertyValue pv){
    String tokens[] = pv.getValue().split(SPLIT_REGEX);
    IntList tids = new IntArrayList();
    for (String token: tokens){
      String trimmedString = token.trim();
      if (!trimmedString.isEmpty()){
        tids.add(EncodingManager.getInstance().checkToken(trimmedString));
      }
    }
    Collections.sort(tids);
    return tids.toArray(new int[]{});
  }
}
