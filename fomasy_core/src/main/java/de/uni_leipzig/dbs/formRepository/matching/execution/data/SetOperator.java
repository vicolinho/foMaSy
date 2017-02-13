package de.uni_leipzig.dbs.formRepository.matching.execution.data;

import java.util.Map.Entry;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;

public class SetOperator extends Operator{

	private int id;
	
	int type ;
	
	private AggregationFunction aggFunction;
	
	public static final int UNION =0;
	
	public static final int INTERSECT =1;
	
	
	
	public void setType(int type){
		this.type = type;
		
	}
	
	
	public SetOperator (AggregationFunction func, int type){
		this.type = type;
		this.aggFunction = func;
	}
	
	public Long2FloatMap setOperation(Long2FloatMap... mappings){
		Long2FloatMap combinedMap= new Long2FloatOpenHashMap();
		LongSet resultingSet = new LongOpenHashSet();
		resultingSet.addAll(mappings[0].keySet());
		for (int i =1; i<mappings.length;i++){
			Long2FloatMap mapping = mappings[i];
			if (type == UNION){
				resultingSet.addAll(mapping.keySet());
			}else if (type == INTERSECT){
				resultingSet.retainAll(mapping.keySet());
			}
		}
		Long2IntMap occMap = new Long2IntOpenHashMap();
		for (long cor: resultingSet){
			for (Long2FloatMap mapping : mappings){
				Float sim = mapping.get(cor);
				if (sim!=null){
					Float aggSim = combinedMap.get(cor);
					if (aggSim==null){
						combinedMap.put(cor,sim.floatValue());
						if (aggFunction == AggregationFunction.AVG){
							occMap.put(cor, 1);
						}
					}else {
						if (aggFunction == AggregationFunction.MAX){
							if (sim>aggSim){
								combinedMap.put(cor, sim.floatValue());
							}
						}else if (aggFunction == AggregationFunction.MIN){
							if (sim<aggSim){
								combinedMap.put(cor, sim.floatValue());
							}
						}else if (aggFunction == AggregationFunction.AVG){
							combinedMap.put(cor, sim.floatValue()+aggSim);
							occMap.put(cor, occMap.get(cor)+1);
						}
					}
				}
			}
		}
		if (aggFunction == AggregationFunction.AVG){
			for (Entry <Long,Float> e: combinedMap.entrySet()){
				combinedMap.put(e.getKey().longValue(), e.getValue()/(float)occMap.get(e.getKey()));
			}
		}
		return combinedMap;
	}
}
