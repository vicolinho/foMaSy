package de.uni_leipzig.dbs.formRepository.matching.holistic.clustering;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.holistic.data.TokenCluster;

public interface ClusteringAlgorithm {

	
	public Map<Integer, TokenCluster> cluster(Map<Integer,TokenCluster> initialCluster,
			Int2ObjectMap<List<SimilarCluster>> simMatrix,Set<EncodedEntityStructure> ees , Set<GenericProperty> props,float minSim);
		
	
}