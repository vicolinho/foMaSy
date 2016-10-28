package de.uni_leipzig.dbs.formRepository.matching.string;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import cern.colt.Arrays;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.AbstractPartMatcher;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;

public class TrigramThread extends AbstractPartMatcher{
	
	EncodedEntityStructure srcStructure;
	
	EncodedEntityStructure targetStructure;
	
	AggregationFunction aggFunc;
	
	Integer[] srcIds;
	
	Integer[] targetIds;
	
	float threshold;
	
	Pruning pruning;

	private int[] propertyIds1;

	private int[] propertyIds2;

	private int[][][][] srcTrigrams;

	private int[][][][] targetTrigrams;
	
	Logger log =Logger.getLogger(getClass());
	
	public TrigramThread(EncodedEntityStructure src, EncodedEntityStructure target,Integer[] sourceIds, Integer[] partObjIds,int[][][][] srcTrigrams, 
			int [][][][] targetTrigrams, AggregationFunction aggFunc,
			float threshold, int[] set, int[] set2, Pruning pruning){
		super(threshold);
		this.srcStructure = src;
		this.targetStructure = target;
		this.aggFunc = aggFunc;
		this.pruning = pruning;
		this.propertyIds1 = set;
		this.propertyIds2 = set2;
		this.srcIds = sourceIds;
		this.targetIds =partObjIds;
		this.srcTrigrams = srcTrigrams; 
		this.targetTrigrams = targetTrigrams;
		
	}
	
	long mapTime =0;
	long start= System.currentTimeMillis();
	@Override
	public void run(){
		Collection <Integer> propertyPos = srcStructure.getPropertyPosition().values();
		List <Float> confidenceList = new ArrayList<Float>();
		int srcIndex =0;
		for (int srcId: srcIds){
			IntOpenHashSet trigrams = new IntOpenHashSet();
			int entitySrcId = srcId;
			
			//int entitySrcPos = srcStructure.getObjIds().get(srcId);
			
			if (pruning!=null){
				for (int pp: propertyPos){
					for (int [] valueTrigram: srcTrigrams[srcIndex][pp] ){
						for (int trigramId : valueTrigram){
							trigrams.add(trigramId);
						}
					}
				}
			}
			Integer[] targetEntities = new Integer[]{};
			if (pruning != null){
				//targetEntities = pruning.getSimilarEntities(trigrams.toArray(new int[]{}));
				//targetEntities.retainAll(targetIds);
			}else{
				targetEntities = targetIds;
			}
			int targetIndex = 0;
			for (int te :targetEntities){
				//int targetPos = targetStructure.getObjIds().get(te);
				for (int srcPropertyPos : propertyIds1){
					for (int targetPropertyPos :propertyIds2){
						for (int[] trigramSrc:srcTrigrams[srcIndex][srcPropertyPos]){
							for (int [] trigramTarget: targetTrigrams[targetIndex][targetPropertyPos]){
								float sim = this.computeSimilarity(trigramSrc, trigramTarget);
//								if (srcId ==447&& te == 876257)
//									log.debug(sim);
								confidenceList.add(sim);
							}
						}
					}
				}
				float sim = aggFunc.aggregateFloatList(confidenceList);
				this.addResult(srcStructure.getStructureId(), targetStructure.getStructureId(), entitySrcId, te, sim);
				confidenceList.clear();
				targetIndex++;
			}
			srcIndex++;
		}
	}

	
	
	private float computeSimilarity (int[] trigramSrc, int[] triTarget){
		int srcIndex =0;
		int targetIndex =0;
		int common = 0;
	
		while (srcIndex<trigramSrc.length&& targetIndex<triTarget.length){
			if (trigramSrc[srcIndex]<triTarget[targetIndex]){
				srcIndex++;
			}else if (trigramSrc[srcIndex]>triTarget[targetIndex]){
				targetIndex++;
			}else if (trigramSrc[srcIndex]==triTarget[targetIndex]){
				common++;
				targetIndex ++;
				srcIndex++;
			}
			
		}
		
		
		float dice = (2*common)/(float)(trigramSrc.length+triTarget.length);
		return dice;
	}
	
	
	
}
