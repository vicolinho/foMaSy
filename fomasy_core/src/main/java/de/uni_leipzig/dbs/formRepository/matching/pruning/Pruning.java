package de.uni_leipzig.dbs.formRepository.matching.pruning;

import java.util.Set;

public interface Pruning {

  
  Set<Integer> getSimilarEntities(int[] trigrams) throws InterruptedException;
  Set<Integer> getSimilarEntitiesByTokens(int[] tokens) throws InterruptedException;
}
