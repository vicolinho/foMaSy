package de.uni_leipzig.dbs.formRepository.matching.token;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.AbstractPartMatcher;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;

public class TFIDFWindowThread extends AbstractPartMatcher{

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
	
	boolean isAdaptive;

	private HashMap<Float, Set<Integer>> evidenceConfMap;
	
	public TFIDFWindowThread(EncodedEntityStructure src, EncodedEntityStructure target,IntSet srcIds, IntSet targetIds,
			AggregationFunction aggFunc,
			float threshold, Set<Integer> set, Set<Integer> set2,Int2FloatMap idfMap, Int2FloatMap idfTargetMap, Pruning pruning,
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
			for (Integer pp: propertyPos){
				for (int [] valueTrigram: srcStructure.getTrigramIds()[entitySrcPos][pp] ){
					for (int trigramId : valueTrigram){
						trigrams.add(trigramId);
					}
				}
			}
		try {
			Set<Integer> targetEntities;
			if (pruning != null){
				targetEntities = pruning.getSimilarEntities(trigrams.toArray(new int[]{}));
				targetEntities.retainAll(targetIds);
			}else{
				targetEntities = targetIds;
			}
			
			
			for (Integer te :targetEntities){
				evidenceConfMap.clear();
				int targetPos = targetStructure.getObjIds().get(te);
				for (int srcPropertyPos : propertyIds1){
					for (int[] trigramSrc:srcStructure.getPropertyValueIds()[entitySrcPos][srcPropertyPos]){
						int start = 0;
						//for (int currentWindow =2; currentWindow<windowSize;currentWindow++){
							int end = (windowSize<trigramSrc.length)?windowSize: trigramSrc.length;
							do{
								Int2IntMap countMapSrc = this.getFrequencies(trigramSrc, start, end);
								for (int targetPropertyPos :propertyIds2){
									for (int [] trigramTarget: targetStructure.getPropertyValueIds()[targetPos][targetPropertyPos]){
										Int2IntMap countMapTarget = this.getFrequencies(trigramTarget, 0,trigramTarget.length);
										confidenceList.add(this.computeSimilarity(trigramSrc, trigramTarget, countMapSrc , countMapTarget));
									}
								}
								start++;
								end++;
							}while(end<trigramSrc.length);
						//}
					}
				}
				
				
				float sim = aggFunc.aggregateFloatList(confidenceList);
				Set<Integer> evidence = this.evidenceConfMap.get(sim);
				//this.addResult(srcStructure.getStructureId(), targetStructure.getStructureId(), entitySrcId, te, sim);
				this.addResultWithEvidence(srcStructure.getStructureId(), targetStructure.getStructureId(), entitySrcId, te,evidence,
						sim);
				confidenceList.clear();
			}
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	}
	
	
	
	private float  computeSimilarity (int [] tokenSrc,int[] tokenTarget, Int2IntMap frequencySrc, Int2IntMap frequencyTarget){
		Set <Integer> intersect = new HashSet<Integer>(frequencySrc.keySet());
		intersect.retainAll(frequencyTarget.keySet());
		if (intersect.size() ==0){
			return 0;
		}
		float sim =0; 
		float dotProduct = 0;
		float lengthSrc =0;
		float lengthTarget=0;
		
			
		for (Integer t: intersect){
			float tfidfSrc = frequencySrc.get(t)*idfSourceMap.get(t);
			if (idfSourceMap.get(t)<0)
				log.warn(idfSourceMap.get(t));
			float tfidfTarget = frequencyTarget.get(t)* idfTargetMap.get(t);
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
	
	private float  computeMinSimilarity (int [] tokenSrc,int[] tokenTarget, Int2IntMap frequencySrc, Int2IntMap frequencyTarget){
		Set <Integer> intersect = new HashSet<Integer>(frequencySrc.keySet());
		intersect.retainAll(frequencyTarget.keySet());
		if (intersect.size() ==0){
			return 0;
		}
		float sim =0; 
		float dotProduct = 0;
		float lengthSrc =0;
		float lengthTarget=0;
		
			
		for (Integer t: intersect){
			float tfidfSrc = frequencySrc.get(t)*idfSourceMap.get(t);
			if (idfSourceMap.get(t)<0)
				log.warn(idfSourceMap.get(t));
			float tfidfTarget = frequencyTarget.get(t)* idfTargetMap.get(t);
			dotProduct +=(tfidfSrc*tfidfTarget); 
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
			float minSim = 0;
					if (frequencyTarget.size()<frequencySrc.size()){
						minSim = lengthTarget;
					}else {
						minSim = lengthSrc;
					}
			sim = (float) (dotProduct/((Math.sqrt(minSim)*Math.sqrt(minSim))));
			if (sim >1){
				StringBuffer sb =new StringBuffer ();
				for (Entry<Integer,Integer> e:frequencySrc.entrySet()){
					sb.append(EncodingManager.getInstance().getReverseDict().get(e.getKey())+" ");
				}
				sb.append("---");
				for (Entry<Integer,Integer> e:frequencyTarget.entrySet()){
					sb.append(EncodingManager.getInstance().getReverseDict().get(e.getKey())+" ");
				}
				log.info(sb.toString());
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
