package de.uni_leipzig.dbs.formRepository.matching;

import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
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
			if (evidence.size()<=2){
				boolean onlyInteger =true;
				for (int t :evidence){
					String string = EncodingManager.getInstance().getReverseDict().get(t);
					onlyInteger = onlyInteger && isInteger(string);
				}
				if (!onlyInteger){
					long corrId = CantorDecoder.code(entitySrcId, te);
					this.result.put(corrId, sim);
					this.evidenceMap.put(corrId, evidence);
				}
			}else {
				long corrId = CantorDecoder.code(entitySrcId, te);
				this.result.put(corrId, sim);
				this.evidenceMap.put(corrId, evidence);
			}
		}
	}

	protected void addResultWithPositions(int entitySrcId, Integer te, Set<Integer> evidence, Set<Integer> position,
																				float sim) {
		if (sim>= threshold){
			if (evidence.size()<=2){
				boolean onlyInteger =true;
				for (int t :evidence){
					String string = EncodingManager.getInstance().getReverseDict().get(t);
					onlyInteger = onlyInteger && isInteger(string);
				}
				if (!onlyInteger){
					long corrId = CantorDecoder.code(entitySrcId, te);
					this.result.put(corrId, sim);
					this.evidenceMap.put(corrId, position);
				}
			}else {
				long corrId = CantorDecoder.code(entitySrcId, te);
				this.result.put(corrId, sim);
				this.evidenceMap.put(corrId, position);
			}
		}
	}


	private static boolean isInteger(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}
	
}
