package de.uni_leipzig.dbs.formRepository.matching.holistic.clustering;

import de.uni_leipzig.dbs.formRepository.matching.holistic.data.TokenCluster;

public interface ClusterSimilarityFunction {

  
  float calculateSimilarity(TokenCluster c1, TokenCluster c2);
}
