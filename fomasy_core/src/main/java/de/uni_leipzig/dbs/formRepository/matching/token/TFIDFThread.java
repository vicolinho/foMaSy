package de.uni_leipzig.dbs.formRepository.matching.token;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import cern.colt.Arrays;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.AbstractPartMatcher;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;

public class TFIDFThread extends AbstractPartMatcher{
	
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
	
	public TFIDFThread(EncodedEntityStructure src, EncodedEntityStructure target,IntSet srcIds, IntSet targetIds,
			AggregationFunction aggFunc,
			float threshold, Set<Integer> set, Set<Integer> set2,Int2FloatMap idfMap, Int2FloatMap idfTargetMap, Pruning pruning){
		super(threshold);
		this.srcStructure = src;
		this.targetStructure = target;
		this.aggFunc = aggFunc;
		this.pruning = pruning;
		this.propertyIds1 = set;
		this.propertyIds2 = set2;
		this.srcIds = srcIds;
		this.targetIds =targetIds;
		this.idfTargetMap = idfTargetMap;
		this.idfSourceMap = idfMap;
		//this.srcPropertyValueIds = srcPropertyValueIds;
		//this.targetPropertyValueIds = targetPropertyValueIds;
	}
	
	@Override
	public void run(){
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
				int targetPos = targetStructure.getObjIds().get(te);
				for (int srcPropertyPos : propertyIds1){
					for (int[] trigramSrc:srcStructure.getPropertyValueIds()[entitySrcPos][srcPropertyPos]){
						Int2IntMap countMapSrc = new Int2IntOpenHashMap();
						countMapSrc = this.getFrequencies(trigramSrc, countMapSrc);
						for (int targetPropertyPos :propertyIds2){
							for (int [] trigramTarget: targetStructure.getPropertyValueIds()[targetPos][targetPropertyPos]){
								Int2IntMap countMapTarget = new Int2IntOpenHashMap();
								countMapTarget = this.getFrequencies(trigramTarget, countMapTarget);
								float sim = this.computeSimilarity(countMapSrc , countMapTarget);	
								confidenceList.add(sim);
							}
						}
					}
				}
				float sim = aggFunc.aggregateFloatList(confidenceList);
				this.addResult(srcStructure.getStructureId(), targetStructure.getStructureId(), entitySrcId, te, sim);
				confidenceList.clear();
			}
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	}
	
	
	int srcEntId = 0;
	int targetID =0;
	private float computeSimilarity (Int2IntMap frequencySrc, Int2IntMap frequencyTarget){
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
			if (sim >1){
				log.error(sim+" dotProduct: "+ dotProduct +" length src: "+lengthSrc +" length target: " + lengthTarget);
			}
		}
		return sim;
	}
	
	
	 
	private Int2IntMap getFrequencies (int [] tokens, Int2IntMap frequencies){
		for (int t : tokens){
			if (!frequencies.containsKey(t)){
				frequencies.put(t, 1);
			}else{
				frequencies.put(t, frequencies.get(t)+1);
			}
		}
		return frequencies;
	}
	
}
