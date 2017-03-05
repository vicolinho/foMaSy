package de.uni_leipzig.dbs.formRepository.matching.token;

import java.util.*;

import de.uni_leipzig.dbs.formRepository.matching.blocking.data.BlockSet;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.token.util.TermFrequenceCounter;
import it.unimi.dsi.fastutil.ints.*;
import org.apache.log4j.Logger;

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
	
	BlockSet blocks;

	private Set<Integer> propertyIds1;

	private Set<Integer> propertyIds2;
	
	private Int2FloatMap idfTargetMap;
	
	private Int2FloatMap idfSourceMap ; 
	
	private int windowSize; 
	
	private Map<Integer,Set<Integer>> lookup;
	
	private HashMap<Float, Set<Integer>> evidenceConfMap;

	private HashMap<Float, Set<Integer>> positionMap;

	private Int2ObjectMap<Int2IntMap> termFrequencies;
	
	boolean isAdaptive;
	
	public SoftTFIDFThread(EncodedEntityStructure src, EncodedEntityStructure target,IntSet srcIds, IntSet targetIds,
			AggregationFunction aggFunc,
			float threshold, Set<Integer> set, Set<Integer> set2,Int2FloatMap idfMap,
			Int2FloatMap idfTargetMap,Map<Integer,Set<Integer>> lookup, BlockSet blocks,
			int wndSize,boolean isAdaptive){
		super(threshold);
		this.srcStructure = src;
		this.targetStructure = target;
		this.aggFunc = aggFunc;
		this.blocks = blocks;
		this.propertyIds1 = set;
		this.propertyIds2 = set2;
		this.srcIds = srcIds;
		this.targetIds =targetIds;
		this.windowSize = wndSize;
		this.idfTargetMap = idfTargetMap;
		this.idfSourceMap = idfMap;
		this.lookup = lookup;
		this.isAdaptive = isAdaptive;
		//TermFrequenceCounter tfc = new TermFrequenceCounter();
		//termFrequencies = tfc.getTermFrequencies(srcIds, targetIds, src, target, propertyIds1,propertyIds2);
	}
	
	@Override
	public void run(){
		evidenceConfMap = new HashMap<Float,Set<Integer>>();
		positionMap = new HashMap<>();
		List <Float> confidenceList = new ArrayList<Float>();
		for (int srcId: srcIds){
			int entitySrcId = srcId;
			int entitySrcPos = srcStructure.getObjIds().get(srcId);
			Set<Integer> targetEntities;
			targetEntities = targetIds;
			int maximalSize = 0;
			for (Integer te :targetEntities) {
				evidenceConfMap.clear();
				positionMap.clear();
				int targetPos = targetStructure.getObjIds().get(te);
				boolean compare = blocks == null || blocks.isPairToCompare(entitySrcPos, targetPos);
				if (compare){
					int offset = 0;
					for (int srcPropertyPos : propertyIds1) {
						for (int[] trigramSrc : srcStructure.getPropertyValueIds()[entitySrcPos][srcPropertyPos]) {
							int start = 0;
							if (trigramSrc.length > maximalSize) {
								maximalSize = trigramSrc.length;
							}
							int end = (windowSize < trigramSrc.length) ? windowSize : trigramSrc.length;
							do {
								Int2IntMap countMapSrc = this.getFrequencies(trigramSrc, start, end);
								//Set<Integer> srcTerms = this.getTermSet(trigramSrc, start, end);
								for (int targetPropertyPos : propertyIds2) {
									for (int[] trigramTarget : targetStructure.getPropertyValueIds()[targetPos][targetPropertyPos]) {
										Int2IntMap countMapTarget = this.getFrequencies(trigramTarget, 0, trigramTarget.length);
										//Set<Integer> targetTerms = this.getTermSet(trigramTarget, 0,trigramTarget.length);
										float sim =0;

										sim = this.computeSimilarity(countMapSrc , countMapTarget);
										//sim = this.computeSimilarity(srcTerms, targetTerms, srcId, te);
										//sim = this.computeWeightedConfidence(countMapSrc, countMapTarget);
										if (sim > 0) {
											Set<Integer> pos = new HashSet<>();
											positionMap.put(sim, pos);
											Set<Integer> et = evidenceConfMap.get(sim);
											for (int i = start; i < end; i++) {
												if (et.contains(trigramSrc[i])) {
													pos.add(offset + i);
												}
											}
											confidenceList.add(sim);
										}
									}
								}
								start++;
								end++;
							} while (end <= trigramSrc.length);
							//}
							offset = end;
						}
					}
					float sim = aggFunc.aggregateFloatList(confidenceList);
					Set<Integer> evidence = evidenceConfMap.get(sim);
					Set<Integer> pos = positionMap.get(sim);
					this.addResultWithPositions(entitySrcId, te, evidence, pos, sim);
					//this.addResultWithEvidence(srcStructure.getStructureId(), targetStructure.getStructureId(),
					// entitySrcId, te,evidence, sim);
					confidenceList.clear();
				}
			}
	}

	}

	private float  computeSimilarity (Int2IntMap frequencySrc, Int2IntMap frequencyTarget){
		Set <Integer> intersect = new HashSet<>(frequencySrc.keySet());
		intersect.retainAll(frequencyTarget.keySet());

		float sim =0; 
		float dotProduct = 0;
		float lengthSrc =0;
		float lengthTarget=0;
		//terms that are not exact in target
		Set<Integer> onlyInSrc = new HashSet<>(frequencySrc.keySet());
		onlyInSrc.removeAll(intersect);
		//terms that are not exact in src
		Set<Integer> onlyInTarget = new HashSet<>(frequencyTarget.keySet());
		onlyInTarget.removeAll(intersect);

		Int2IntMap srcToTarget = new Int2IntOpenHashMap();
		Set<Integer> removeSrc = new HashSet<>();
		Set<Integer> removeTarget = new HashSet<>();
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
			float tfidfSrc = frequencySrc.get(t)/(float)frequencySrc.size()*idfSourceMap.get(t);
			if (idfSourceMap.get(t)<0)
				log.warn(idfSourceMap.get(t));
			
			float tfidfTarget;
			if (srcToTarget.containsKey(t)){
				int targetToken = srcToTarget.get(t);
				float idf = idfTargetMap.get(targetToken);
				tfidfTarget = frequencyTarget.get(targetToken)/(float)frequencyTarget.size()* idf;
			}else{
				if (!frequencyTarget.containsKey(t)){
					log.error("not in map"+t);
				}
				if (idfTargetMap.get(t)<0)
					log.warn(idfTargetMap.get(t));
				float idf = idfTargetMap.get(t);
				tfidfTarget = frequencyTarget.get(t)/(float)frequencyTarget.size()* idf;
			}
			
			dotProduct +=tfidfSrc*tfidfTarget; 
			lengthSrc+= tfidfSrc*tfidfSrc;
			lengthTarget += tfidfTarget*tfidfTarget;
		}

		for (Integer i :onlyInSrc){
				if (frequencySrc.get(i)==null){
					System.out.println("error token");
				}
				if (idfSourceMap.get(i) ==null){
					System.out.println("error token"+idfSourceMap.size());
				}

				float tfidfSrc = frequencySrc.get(i)/(float)frequencySrc.size()*idfSourceMap.get(i);
				lengthSrc+= tfidfSrc*tfidfSrc;
		}

		for (Integer i :onlyInTarget){
			if (!frequencyTarget.containsKey(i)){
				log.error("not in map"+i);
			}
				float tfidfTarget = frequencyTarget.get(i)/(float)frequencyTarget.size()*idfTargetMap.get(i);
				lengthTarget+= tfidfTarget*tfidfTarget;
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

	private float  computeSimilarity (Set<Integer> frequencySrc, Set<Integer> frequencyTarget, int srcId, int targetId){
		Set<Integer> intersect = new HashSet<>(frequencySrc);
		intersect.retainAll(frequencyTarget);
		float sim =0;
		float dotProduct = 0;
		float lengthSrc =0;
		float lengthTarget=0;
		//terms that are not exact in target
		Set<Integer> onlyInSrc = new HashSet<>(frequencySrc);
		onlyInSrc.removeAll(intersect);
		//terms that are not exact in src
		Set<Integer> onlyInTarget = new HashSet(frequencyTarget);
		onlyInTarget.removeAll(intersect);

		Int2IntMap srcToTarget = new Int2IntOpenHashMap();
		IntSet removeSrc = new IntOpenHashSet();
		IntSet removeTarget = new IntOpenHashSet();
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
			float tfidfSrc = termFrequencies.get(srcId).get(t)*idfSourceMap.get(t);
			if (idfSourceMap.get(t)<0)
				log.warn(idfSourceMap.get(t));

			float tfidfTarget;
			if (srcToTarget.containsKey(t)){
				int targetToken = srcToTarget.get(t);
				float idf = idfTargetMap.get(targetToken);
				tfidfTarget = termFrequencies.get(targetId).get(targetToken)* idf;
			}else{
				float idf = idfTargetMap.get(t);
				tfidfTarget = termFrequencies.get(targetId).get(t)* idf;
			}

			dotProduct+= tfidfSrc*tfidfTarget;
			lengthSrc+= tfidfSrc*tfidfSrc;
			lengthTarget+= tfidfTarget*tfidfTarget;
		}

		for (Integer i :onlyInSrc){
			float tfidfSrc = termFrequencies.get(srcId).get(i)*idfSourceMap.get(i);
			lengthSrc+= tfidfSrc*tfidfSrc;
		}

		for (Integer i :onlyInTarget){
			float tfidfTarget = termFrequencies.get(targetId).get(i)*idfTargetMap.get(i);
			lengthTarget+= tfidfTarget*tfidfTarget;
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

	public float computeWeightedConfidence(Int2IntMap frequencySrc, Int2IntMap frequencyTarget){

		Set <Integer> intersect = new HashSet<Integer>(frequencySrc.keySet());
		intersect.retainAll(frequencyTarget.keySet());

		float sim =0;
		float dotProduct = 0;
		float lengthSrc =0;
		float simpleLengthSrc = 0;
		float lengthTarget=0;
		float simpleLengthTarget = 0;
		//terms that are not exact in target
		Set<Integer> onlyInSrc = new HashSet<>(frequencySrc.keySet());
		onlyInSrc.removeAll(intersect);
		//terms that are not exact in src
		Set<Integer> onlyInTarget = new HashSet<>(frequencyTarget.keySet());
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
			float tfidfSrc = frequencySrc.get(t)/(float)frequencySrc.size()*idfSourceMap.get(t);
			float tfidfTarget;
			if (srcToTarget.containsKey(t)){
				int targetToken = srcToTarget.get(t);
				float idf = idfTargetMap.get(targetToken);
				tfidfTarget = frequencyTarget.get(targetToken)/(float)frequencyTarget.size()* idf;
			}else{
				if (idfTargetMap.get(t)<0)
					log.warn(idfTargetMap.get(t));
				float idf = idfTargetMap.get(t);
				tfidfTarget = frequencyTarget.get(t)/(float)frequencyTarget.size()* idf;
			}

			dotProduct +=tfidfSrc*tfidfTarget;
			lengthSrc+= tfidfSrc*tfidfSrc;
			simpleLengthSrc += tfidfSrc;
			lengthTarget += tfidfTarget*tfidfTarget;
			simpleLengthTarget += tfidfTarget;
		}

		float hamming1 =0;
		for (Integer i :onlyInSrc){
			float tfidfSrc = frequencySrc.get(i)/(float)frequencySrc.size()*idfSourceMap.get(i);
			lengthSrc+= tfidfSrc*tfidfSrc;
			simpleLengthSrc += tfidfSrc;
			hamming1 += tfidfSrc;
		}
		float hamming2 =0;
		for (Integer i :onlyInTarget){
			if (!frequencyTarget.containsKey(i)){
				log.error("not in map"+i);
			}
			float tfidfTarget = frequencyTarget.get(i)/(float)frequencyTarget.size()*idfTargetMap.get(i);
			lengthTarget+= tfidfTarget*tfidfTarget;
			simpleLengthTarget += tfidfTarget;
			hamming2 += simpleLengthTarget;
		}
		double weightedSim =0;
		if (lengthSrc !=0 &&lengthTarget!=0){
			float hammingDistance = hamming1/simpleLengthSrc+hamming2/simpleLengthTarget;
			sim = (float) (dotProduct/(Math.sqrt(lengthSrc)*Math.sqrt(lengthTarget)));
			weightedSim= /*(cosine);*/sim/(Math.pow(hammingDistance, 2)+sim);
			evidenceConfMap.put((float)weightedSim, intersect);
			if (sim >1){
				log.error(sim+" dotProduct: "+ dotProduct +" length src: "+lengthSrc +" length target: " + lengthTarget);
			}
		}
		return (float)weightedSim;
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

		private Set<Integer> getTermSet (int [] tokens, int offset, int limit){
			Set<Integer> set  =new HashSet<>();
			for (int i=offset; i<limit; i++){
				set.add(tokens[i]);
			}
			return set;
		}
	

}
