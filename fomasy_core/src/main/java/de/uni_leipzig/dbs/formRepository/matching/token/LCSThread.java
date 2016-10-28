package de.uni_leipzig.dbs.formRepository.matching.token;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.AbstractPartMatcher;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;

public class LCSThread extends  AbstractPartMatcher{

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
						for (int targetPropertyPos :propertyIds2){
							for (int [] trigramTarget: targetStructure.getPropertyValueIds()[targetPos][targetPropertyPos]){
								confidenceList.add(this.computeSimilarity(trigramSrc , trigramTarget));
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
				int current =0;
				int index1 = 0;
				while (index1<encDomValue.length){
					if (encDomValue[index1]==c)
						break;
					else
						index1++;
				}
				int index2 =0;
				while (index2<encRangeValue.length){
					if (encRangeValue[index2]==c)
						break;
					else
						index2++;
				}
				boolean nextMatch =true;
						int matchedPos=0;
				while (nextMatch){
					if (index2<encRangeValue.length&&index1<encDomValue.length){
						if (encRangeValue[index2]==encDomValue[index1]){
							//current+= this.idfReaderDomain.getIDF(0, encDomValue[index1])*this.idfReaderRange.getIDF(0, encRangeValue[index2]);
							matchedPos++;
						}/*else if ((index2+1)<encRangeValue.length||
								(index1+1)<encDomValue.length){
							int oldIndex2 = index2;
							try{
								if (encRangeValue[index2+1]==encDomValue[index1]){
									matchedPos++;
									index2++;
								}
							}catch (IndexOutOfBoundsException e){}
							try{
								if (encRangeValue[oldIndex2]==encDomValue[index1+1]){
									matchedPos++;
									index1++;
								}
							}catch (IndexOutOfBoundsException e){}
						}*/else{
							nextMatch =false;
						}
						index2++;index1++;
					}else nextMatch = false;
				}
				if (matchedPos>longestMatch&&matchedPos>1){
					longestMatch =matchedPos;
				}
			}
			/*
			Integer[] shortArray = (encRangeValue.length<encDomValue.length)? encRangeValue:encDomValue;
			float length = 0;
			for (Integer id: shortArray){
				float idf = (shortArray ==encRangeValue)? this.idfReaderRange.getIDF(0, id):this.idfReaderDomain.getIDF(0, id);
				length += idf*idf;	
			}
			length = length*length;
			length = (float) Math.sqrt(length);
			System.out.println (set1+"\t"+Arrays.toString(encDomValue)+"\t"
			+Arrays.toString(encRangeValue)+"\t"+longestMatch);
			float sim = (float) longestMatch/length;
			return sim;*/
			//return (longestMatch)/((float)Math.min(encDomValue.length, encRangeValue.length));
			return (1.25F*longestMatch)/((float)Math.min(encDomValue.length, encRangeValue.length)+0.25F*Math.max(encDomValue.length, encRangeValue.length));
		}else return 0F;
	}
}
