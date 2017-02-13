package de.uni_leipzig.dbs.formRepository.matching.token;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;

import java.util.*;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.AbstractPartMatcher;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;

public class LCSThread extends  AbstractPartMatcher{

	Logger log = Logger.getLogger(LCSThread.class);

	EncodedEntityStructure srcStructure;
	
	EncodedEntityStructure targetStructure;
	
	AggregationFunction aggFunc;
	
	IntSet srcIds;
	
	IntSet targetIds;
	
	float threshold;
	
	Pruning pruning;

	private Set<Integer> propertyIds1;

	private Set<Integer> propertyIds2;
	
	public LCSThread(EncodedEntityStructure source,
			EncodedEntityStructure target, IntSet partObjIds, IntSet targetSet,
			AggregationFunction function, float threshold,
			Set<Integer> propertyIds1, Set<Integer> propertyIds2,
			Pruning pruning) {
		super(threshold);
		this.srcStructure = source;
		this.targetStructure = target;
		this.aggFunc = function;
		this.pruning = pruning;
		this.propertyIds1 = propertyIds1;
		this.propertyIds2 = propertyIds2;
		this.srcIds = partObjIds;
		this.targetIds =targetSet;
	}

	@Override
	public void run() {
		List <Float> confidenceList = new ArrayList<Float>();
		
		for (int srcId: srcIds){
			int entitySrcPos = srcStructure.getObjIds().get(srcId);
			Set<Integer> targetEntities = targetIds;
			if (pruning != null){

			}else{
				targetEntities = targetIds;
			}

			for (Integer te :targetEntities){
				int targetPos = targetStructure.getObjIds().get(te);
				for (int srcPropertyPos : propertyIds1){
					for (int[] tokenSrc:srcStructure.getPropertyValueIds()[entitySrcPos][srcPropertyPos]){
						for (int targetPropertyPos :propertyIds2){
							for (int [] tokenTarget: targetStructure.getPropertyValueIds()[targetPos][targetPropertyPos]){
								confidenceList.add(this.computeSimilarity(tokenSrc , tokenTarget));
							}
						}
					}
				}
				float sim = aggFunc.aggregateFloatList(confidenceList);
				this.addResult(srcStructure.getStructureId(), targetStructure.getStructureId(), srcId, te, sim);
				confidenceList.clear();
			}
		}
	}

	private Float computeSimilarity(int[] encDomValue,
			int[] encRangeValue) {
		HashSet<Integer> set1 = new HashSet<Integer> ();
		for (Integer i : encDomValue)
			set1.add(i);
		HashSet<Integer> set2 = new HashSet<Integer> ();
		for (Integer i :encRangeValue){
			set2.add(i);
		}
		set1.retainAll(set2);
		float longestMatch =0F;
		
		if (set1.size()>1){
			for (Integer c : set1){
				int index1 = 0;
				while (index1<encDomValue.length){
					if (encDomValue[index1]==c) {
						int index2 =0;
						while (index2<encRangeValue.length){
							if (encRangeValue[index2]==c) {
								boolean nextMatch =true;
								int matchedPos=0;
								int i1 = index1;
								int i2 = index2;
								while (nextMatch) {
									if (i2 < encRangeValue.length && i1 < encDomValue.length) {
										if (encRangeValue[i2] == encDomValue[i1]) {
											matchedPos++;
											i1++;
											i2++;
										} else {
											nextMatch = false;
										}
									} else nextMatch = false;
								}
								if (matchedPos > longestMatch && matchedPos > 1) {
									longestMatch = matchedPos;
								}
							}
							index2++;
						}
					}
					index1++;
				}
			}
			return (1.25f*longestMatch)/((float)Math.min(encDomValue.length, encRangeValue.length)+
							0.25F*Math.max(encDomValue.length, encRangeValue.length));
		}else return 0F;
	}
}
