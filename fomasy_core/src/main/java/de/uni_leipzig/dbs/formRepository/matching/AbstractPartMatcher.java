package de.uni_leipzig.dbs.formRepository.matching;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.util.CantorDecoder;

public abstract class AbstractPartMatcher extends Thread{
	private Long2FloatMap result;
	private Long2ObjectMap<Set<Integer>> evidenceMap;
	private float threshold; 
	private Map<String,Object> globalObjects;
	
	public AbstractPartMatcher (float threshold){
		this.threshold = threshold;
		result = new Long2FloatOpenHashMap();
		evidenceMap = new Long2ObjectOpenHashMap<Set<Integer>>();
	}

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
		if (sim>= threshold){
			result.put(encCor, sim);
		}
	}

	protected void addResult(int structureId, int structureId2, int entitySrcId,
			Integer te, float sim) {
		if (sim>= threshold){
			long corrId = CantorDecoder.code(entitySrcId, te);
			this.result.put(corrId, sim);
		}
	}
	
	protected void addResultWithEvidence(int structureId, int structureId2, int entitySrcId,
			Integer te, Set<Integer> evidence,float sim) {
		if (sim>= threshold){
			long corrId = CantorDecoder.code(entitySrcId, te);
			this.result.put(corrId, sim);
			this.evidenceMap.put(corrId, evidence);
		}
	}
	
	
}
