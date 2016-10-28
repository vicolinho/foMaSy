package de.uni_leipzig.dbs.formRepository.matching.pruning;

import java.util.Set;

public interface Pruning {

	
	public Set<Integer> getSimilarEntities(int[] trigrams) throws InterruptedException;
	public Set<Integer> getSimilarEntitiesByTokens(int[] tokens) throws InterruptedException;
}
