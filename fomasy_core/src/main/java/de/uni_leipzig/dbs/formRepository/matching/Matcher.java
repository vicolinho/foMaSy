package de.uni_leipzig.dbs.formRepository.matching;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;

public abstract class Matcher {

	
	private Set<Integer> propertyIds1;
	private Set<Integer> propertyIds2;
	private Long2FloatMap result;
	private Long2ObjectMap <Set<Integer>> evidenceMap;
	private Map<String,Object> globalObjects;
	public Matcher(){}
	
	
	public void setComparingProperties(Map<GenericProperty,Integer> propMap1, Map<GenericProperty,Integer> propMap2,
			Set<GenericProperty> comparingProp1, Set<GenericProperty> comparingProp2){
		propertyIds1 = new HashSet<Integer>();
		propertyIds2 = new HashSet<Integer>();
		for (GenericProperty gp:comparingProp1){
			propertyIds1.add(propMap1.get(gp));
		}
		for (GenericProperty gp: comparingProp2){
			propertyIds2.add(propMap2.get(gp));
		}
		this.result = new Long2FloatOpenHashMap();
		this.evidenceMap = new Long2ObjectOpenHashMap<Set<Integer>>();
	}
	
	
	public abstract Long2FloatMap computeSimilarity(EncodedEntityStructure source,
			EncodedEntityStructure target, AggregationFunction function, float threshold, Pruning pruning) throws MatchingExecutionException;
	

	
	public abstract Long2FloatMap computeSimilarityByReuse(int[][][] propValues1);


	public Map<String,Object> getGlobalObjects() {
		return globalObjects;
	}


	public void setGlobalObjects(Map<String, Object> map) {
		this.globalObjects = map;
	}


	public Long2FloatMap getResult() {
		return result;
	}


	public Long2ObjectMap<Set<Integer>> getEvidenceMap() {
		return evidenceMap;
	}


	protected void addResult (long encCor,float sim){
		result.put(encCor, sim);
	}

	protected void mergeResult (Long2FloatMap ir){
		result.putAll(ir);
		
	}
	
	protected void mergeResult (Long2FloatMap ir,Long2ObjectMap<Set<Integer>>evidenceMap){
		result.putAll(ir);
		this.evidenceMap.putAll(evidenceMap);
		
	}

	
	protected void addResult(int structureId, int structureId2, int entitySrcId,
			Integer te, float sim) {
		// TODO Auto-generated method stub
		
	}
	

	public Set<Integer> getPropertyIds1() {
		return propertyIds1;
	}


	public void setPropertyIds1(Set<Integer> propertyIds1) {
		this.propertyIds1 = propertyIds1;
	}


	public Set<Integer> getPropertyIds2() {
		return propertyIds2;
	}


	public void setPropertyIds2(Set<Integer> propertyIds2) {
		this.propertyIds2 = propertyIds2;
	}
	
	
	
}
