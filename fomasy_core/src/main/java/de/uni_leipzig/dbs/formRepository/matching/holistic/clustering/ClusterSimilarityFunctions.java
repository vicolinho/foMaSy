package de.uni_leipzig.dbs.formRepository.matching.holistic.clustering;

import java.util.HashSet;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.matching.holistic.data.TokenCluster;

public enum ClusterSimilarityFunctions implements ClusterSimilarityFunction{
  
  
  DICE{

    @Override
    public float calculateSimilarity(TokenCluster c1, TokenCluster c2) {
      // TODO Auto-generated method stub
      Set<Integer> intersect = new HashSet<Integer>(c1.getItems());
      intersect.retainAll(c2.getItems());
      float denominator = (c1.getItems().size()+ c2.getItems().size());
      return intersect.size()/denominator;
    }
    
  }


}
