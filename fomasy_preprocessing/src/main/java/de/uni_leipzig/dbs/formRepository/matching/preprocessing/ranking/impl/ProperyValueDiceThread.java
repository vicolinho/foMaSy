package de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.impl;

import de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.ClusterElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christen on 17.02.2017.
 */
public class ProperyValueDiceThread extends Thread{

  private List<ClusterElement> srcElements;
  private List<ClusterElement> targetElements;
  private float simThreshold;


  private List<PropertyEdge> edges;


  public ProperyValueDiceThread(List<ClusterElement> srcPart, List<ClusterElement> targets, float simThreshold){
    super();
    this.srcElements = srcPart;
    this.targetElements = targets;
    this.simThreshold = simThreshold;
    this.edges = new ArrayList<>();

  }

  @Override
  public void run() {
    SimMeasure measure = new SimMeasure();
    for (ClusterElement ce1: srcElements){
      for (ClusterElement ce2: targetElements){
        float sim = measure.computeSim(ce1.getTokens(), ce2.getTokens(), null, SimMeasure.DICE);
        if (sim>=simThreshold){
          PropertyEdge pe = new PropertyEdge(ce1.getPropertyId(), ce2.getPropertyId(), sim);
          this.edges.add(pe);
        }
      }
    }
  }

  public List<PropertyEdge> getEdges() {
    return edges;
  }

  public void setEdges(List<PropertyEdge> edges) {
    this.edges = edges;
  }

}
