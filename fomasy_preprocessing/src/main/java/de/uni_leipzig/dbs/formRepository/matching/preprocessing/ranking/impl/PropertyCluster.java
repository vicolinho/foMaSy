package de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.impl;

import de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.Cluster;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.ClusterElement;

import java.util.Set;

/**
 * Created by christen on 14.02.2017.
 */
public class PropertyCluster implements Cluster{




  private Set<ClusterElement> elements;



  @Override
  public float getSim(Cluster c) {
    return 0;
  }

  @Override
  public Set<ClusterElement> getElements() {
    return null;
  }

  @Override
  public void setElements(Set<ClusterElement> elements) {

  }

  @Override
  public void addElement(ClusterElement element) {
    this.elements.add(element);
  }
}
