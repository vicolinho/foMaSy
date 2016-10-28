package de.uni_leipzig.dbs.formRepository.matching.token;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.matching.AbstractPartMatcher;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;



public class SoftTFIDFThread extends AbstractPartMatcher{

Logger log = Logger.getLogger(getClass());
	
	EncodedEntityStructure srcStructure;
	
	EncodedEntityStructure targetStructure;
	
	AggregationFunction aggFunc;
	
	Set<Integer> srcIds;
	
	Set<Integer> targetIds;
	
	float threshold;
	
	Pruning pruning;

	private Set<Integer> propertyIds1;

	private Set<Integer> propertyIds2;
	
	private Int2FloatMap idfTargetMap;
	
	private Int2FloatMap idfSourceMap ; 
	
	private int windowSize; 
	
	private Map<Integer,Set<Integer>> lookup;
	
	private HashMap<Float, Set<Integer>> evidenceConfMap;
	
	boolean isAdaptive;
	
	public SoftTFIDFThread(EncodedEntityStructure src, EncodedEntityStructure target,IntSet srcIds, IntSet targetIds,
			AggregationFunction aggFunc,
			float threshold, Set<Integer> set, Set<Integer> set2,Int2FloatMap idfMap,
			Int2FloatMap idfTargetMap,Map<Integer,Set<Integer>> lookup, Pruning pruning,
			int wndSize,boolean isAdaptive){
		super(threshold);
		this.srcStructure = src;
		this.targetStructure = target;
		this.aggFunc = aggFunc;
		this.pruning = pruning;
		this.propertyIds1 = set;
		this.propertyIds2 = set2;
		this.srcIds = srcIds;
		this.targetIds =targetIds;
		this.windowSize = wndSize;
		this.idfTargetMap = idfTargetMap;
		this.idfSourceMap = idfMap;
		this.lookup = lookup;
		this.isAdaptive = isAdaptive;
		//this.srcPropertyValueIds = srcPropertyValueIds;
		//this.targetPropertyValueIds = targetPropertyValueIds;
	}
	
	@Override
	public void run(){
		evidenceConfMap = new HashMap<Float,Set<Integer>>();
		Collection <Integer> propertyPos = srcStructure.getPropertyPosition().values();
		List <Float> confidenceList = new ArrayList<Float>();
		
		for (int srcId: srcIds){
			IntOpenHashSet trigrams = new IntOpenHashSet();
			int entitySrcId = srcId;
			int entitySrcPos = srcStructure.getObjIds().get(srcId);
//			for (Integer pp: propertyPos){
//				for (int [] valueTrigram: srcStructure.getTrigramIds()[entitySrcPos][pp] ){
//					for (int trigramId : valueTrigram){
//						trigrams.add(trigramId);
//					}
//				}
//			}
		try {
			Set<Integer> targetEntities;
			if (pruning != null){
				targetEntities = pruning.getSimilarEntities(trigrams.toArray(new int[]{}));
				targetEntities.retainAll(targetIds);
			}else{
				targetEntities = targetIds;
			}
			
			int maximalSize = 0;
			for (Integer te :targetEntities){
				
				evidenceConfMap.clear();
				int targetPos = targetStructure.getObjIds().get(te);
				for (int srcPropertyPos : propertyIds1){
					for (int[] trigramSrc:srcStructure.getPropertyValueIds()[entitySrcPos][srcPropertyPos]){
						int start = 0;
						if (trigramSrc.length>maximalSize){
							maximalSize = trigramSrc.length;
						}
						//for (int currentWindow =2; currentWindow<windowSize;currentWindow++){
							int end = (windowSize<trigramSrc.length)?windowSize: trigramSrc.length;
							do{
								Int2IntMap countMapSrc = this.getFrequencies(trigramSrc, start, end);
								for (int targetPropertyPos :propertyIds2){
									for (int [] trigramTarget: targetStructure.getPropertyValueIds()[targetPos][targetPropertyPos]){
										Int2IntMap countMapTarget = this.getFrequencies(trigramTarget, 0,trigramTarget.length);
										confidenceList.add(this.computeSimilarity(countMapSrc , countMapTarget,this.lookup));
									}
								}
								start++;
								end++;
							}while(end<=trigramSrc.length);
						//}
					}
				}
				
				
				float sim = aggFunc.aggregateFloatList(confidenceList);
				if (entitySrcId==3 && te == 46423){
					log.info(sim);
					log.info("end");
				}
				Set<Integer> evidence = evidenceConfMap.get(sim);
				this.addResultWithEvidence(srcStructure.getStructureId(), targetStructure.getStructureId(), entitySrcId, te,evidence, sim);
				confidenceList.clear();
			}
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	}

	private float  computeSimilarity (Int2IntMap frequencySrc, Int2IntMap frequencyTarget, Map<Integer, Set<Integer>> lookup2){
		Set <Integer> intersect = new HashSet<Integer>(frequencySrc.keySet());
		intersect.retainAll(frequencyTarget.keySet());
		
		float sim =0; 
		float dotProduct = 0;
		float lengthSrc =0;
		float lengthTarget=0;
		
		Set<Integer> onlyInSrc = new HashSet<Integer>(frequencySrc.keySet());
		onlyInSrc.removeAll(intersect);
		Set<Integer> onlyInTarget = new HashSet<Integer>(frequencyTarget.keySet());
		onlyInTarget.removeAll(intersect);
		Int2IntMap srcToTarget = new Int2IntOpenHashMap();
		Set<Integer> removeSrc = new HashSet<Integer>();
		Set<Integer> removeTarget = new HashSet<Integer>();
		for (int s: onlyInSrc){
			Set <Integer> simWords = lookup.get(s);
			if (simWords!=null){
				for (int tt: onlyInTarget){
					if (simWords.contains(tt)){
						intersect.add(s);
						srcToTarget.put(s, tt);
						removeSrc.add(s);
						removeTarget.add(tt);
					}
				}
			}
		}
		onlyInTarget.removeAll(removeTarget);
		onlyInSrc.removeAll(removeSrc);
		
		if (intersect.size() ==0){
			return 0;
		}
		
			
		for (Integer t: intersect){
			float tfidfSrc = frequencySrc.get(t)*idfSourceMap.get(t);
			if (idfSourceMap.get(t)<0)
				log.warn(idfSourceMap.get(t));
			
			float tfidfTarget =0;
			if (srcToTarget.containsKey(t)){
				int targetToken = srcToTarget.get(t);
				float idf = idfTargetMap.get(targetToken);
				tfidfTarget = frequencyTarget.get(targetToken)* idf;
			}else{
				float idf = idfTargetMap.get(t);
				tfidfTarget = frequencyTarget.get(t)* idf;
			}
			
			dotProduct +=tfidfSrc*tfidfTarget; 
			lengthSrc+= tfidfSrc*tfidfSrc;
			lengthTarget += tfidfTarget*tfidfTarget;
		}
		
		for (Integer i :frequencySrc.keySet()){
			if (!intersect.contains(i)){
			
				float tfidfSrc = frequencySrc.get(i)*idfSourceMap.get(i);
				lengthSrc+= tfidfSrc*tfidfSrc;
			}
		}
		
		for (Integer i :frequencyTarget.keySet()){
			if (!intersect.contains(i)){
				float tfidfTarget = frequencyTarget.get(i)*idfTargetMap.get(i);
				lengthTarget+= tfidfTarget*tfidfTarget;
			}
		}
		
		if (lengthSrc !=0 &&lengthTarget!=0){
			
			sim = (float) (dotProduct/(Math.sqrt(lengthSrc)*Math.sqrt(lengthTarget)));
			evidenceConfMap.put(sim, intersect);
			if (sim >1){
				log.error(sim+" dotProduct: "+ dotProduct +" length src: "+lengthSrc +" length target: " + lengthTarget);
			}
		}
		return sim;
	}
	
	
	 
	 
		private Int2IntMap getFrequencies (int [] tokens, int offset, int limit){
			Int2IntMap frequencies = new Int2IntOpenHashMap();
			for (int i=offset; i<limit; i++){
				int t = tokens[i];
				if (!frequencies.containsKey(t)){
					frequencies.put(t, 1);
				}else{
					frequencies.put(t, frequencies.get(t)+1);
				}
			}
			return frequencies;
		}
		
	

}
