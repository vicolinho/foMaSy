package de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking;

import java.util.Set;

/**
 * Created by christen on 14.02.2017.
 */
public interface Cluster {


  float getSim(Cluster c);
  Set<ClusterElement> getElements();
  void setElements(Set<ClusterElement> elements);
  void addElement(ClusterElement element);

}
