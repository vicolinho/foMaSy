package de.uni_leipzig.dbs.formRepository.matching.preprocessing.TokenWeighting;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class TFIDFTokenWeightGenerator implements WeightGenerator {

	Logger log = Logger.getLogger(getClass());
	private static TFIDFTokenWeightGenerator instance ; 
	private Int2ObjectMap<Int2IntMap> globalCountMap;
	private TFIDFTokenWeightGenerator (){
		this.globalCountMap = new  Int2ObjectOpenHashMap<Int2IntMap>();
		
	}
	public void initializeGlobalCount (int srcId, int[][][][]propertyValues, 
			Map<GenericProperty,Integer> propPos,GenericProperty... props){
		int [] positions = new int[props.length];
		Int2IntMap countMap= new Int2IntOpenHashMap();
		this.globalCountMap.put(srcId, countMap);
		for (int i= 0;i<props.length;i++){
			positions[i] = propPos.get(props[i]);
		}
		IntSet set = new IntOpenHashSet();
		for (int[][][]propValues: propertyValues){
			
			for (int pos:positions){
				int[][] values = propValues[pos];
				for (int[]v:values){
					for (int t:v){
						if (!set.contains(t)){
							if (!countMap.containsKey(t))
								countMap.put(t, 1);
							else
								countMap.put(t, countMap.get(t)+1);
							set.add(t);
						}
						
					}//each token
				} // each value
			}// each selected property
			
			set.clear();
		}
	}
	
	public void initializeGlobalCount (EncodedEntityStructure ees, GenericProperty... props){
		int [] positions = new int[props.length];
		Int2IntMap countMap= new Int2IntOpenHashMap();
		this.globalCountMap.put(ees.getStructureId(), countMap);
		for (int i= 0;i<props.length;i++){
			positions[i] = ees.getPropertyPosition().get(props[i]);
		}
		IntSet set = new IntOpenHashSet();
		for (int[][][]propValues: ees.getPropertyValueIds()){
			
			for (int pos:positions){
				int[][] values = propValues[pos];
				for (int[]v:values){
					for (int t:v){
						if (!set.contains(t)){
							if (!countMap.containsKey(t))
								countMap.put(t, 1);
							else
								countMap.put(t, countMap.get(t)+1);
							set.add(t);
						}
						
					}//each token
				} // each value
			}// each selected property
			set.clear();
		}
	}
	
	public void initializeGlobalCountPerForm (int srcId, int[][][][]propertyValues, 
			Map<GenericProperty,Integer> propPos,GenericProperty... props){
		int [] positions = new int[props.length];
		Int2IntMap countMap= new Int2IntOpenHashMap();
		this.globalCountMap.put(srcId, countMap);
		for (int i= 0;i<props.length;i++){
			positions[i] = propPos.get(props[i]);
		}
		for (int[][][]propValues: propertyValues){
			
			for (int pos:positions){
				int[][] values = propValues[pos];
				for (int[]v:values){
					for (int t:v){
						if (!countMap.containsKey(t))
							countMap.put(t, 1);
					}	//each token	
				}// each value
			} //each property
		}
	}
	
	
	
	
	
	public void removeCountForStructure (EncodedEntityStructure ees){
		this.globalCountMap.remove(ees.getStructureId());
	}
	
	public void removeAll (){
		this.globalCountMap.clear();
	}
	
	public void initializeGlobalCountPerForm (EncodedEntityStructure ees, GenericProperty... props){
		int [] positions = new int[props.length];
		Int2IntMap countMap= new Int2IntOpenHashMap();
		this.globalCountMap.put(ees.getStructureId(), countMap);
		for (int i= 0;i<props.length;i++){
			positions[i] = ees.getPropertyPosition().get(props[i]);
		}
		
		for (int[][][]propValues: ees.getPropertyValueIds()){
			
			for (int pos:positions){
				int[][] values = propValues[pos];
				for (int[]v:values){
					for (int t:v){
						if (!countMap.containsKey(t))
							countMap.put(t, 1);
					}	//each token	
				}// each value
			} //each property
		}
	}
	
	
	public Int2FloatMap generateIDFValues (int srcId, int size){
		Int2FloatMap idfMap = new Int2FloatOpenHashMap();
		Int2IntMap countPerSource = this.globalCountMap.get(srcId);
		for (Entry<Integer,Integer> e:countPerSource.entrySet()){
			float idf = (float) Math.log(1+((float)size-(float)e.getValue()+0.5f)/((float)e.getValue()+0.5f));
			idfMap.put((int)e.getKey(), idf);
		}
		return idfMap;
	}
	
	public Int2FloatMap generateIDFValues (Set<Integer> srcId, int size){
	
			Int2FloatMap idfMap = new Int2FloatOpenHashMap();
			for (int src:srcId){
				Int2IntMap countPerSource= this.globalCountMap.get(src);
				for (Entry<Integer,Integer> e:countPerSource.entrySet()){
					if (idfMap.containsKey((int)e.getKey())){
						idfMap.put((int)e.getKey(),(float) e.getValue()+idfMap.get(e.getKey()));
					}else{
						idfMap.put((int)e.getKey(),(float) e.getValue());
					}
				}
			}
			for (int id: idfMap.keySet()){
				float count = idfMap.get(id); 
				float idf = (float) Math.log(1+((float)size-(float)count+0.5f)/((float)count+0.5f));
				//float idf = (float) Math.log((float)size/(float)count);
				if (idf<0)
					log.error("idf is negative"+count+" size" +size);
				idfMap.put(id, idf);
			}
		return idfMap;
	}
	
	public Int2FloatMap generateIDFValuesForAllSources (int size){
		Int2FloatMap idfMap = new Int2FloatOpenHashMap();
		for (Int2IntMap countPerSource: this.globalCountMap.values()){
			for (Entry<Integer,Integer> e:countPerSource.entrySet()){
				if (idfMap.containsKey((int)e.getKey())){
					idfMap.put((int)e.getKey(),(float) e.getValue()+idfMap.get(e.getKey()));
				}else{
					idfMap.put((int)e.getKey(),(float) e.getValue());
				}
			}
		}
		for (int id: idfMap.keySet()){
			float idf = idfMap.get(id); 
//			idf = (float) Math.log(1+((float)size-(float)idf+0.5f)/((float)idf+0.5f));
			idf = (float) Math.log((float)size/idf);
			if (idf<0)
				log.error("idf is negative"+idf);
			idfMap.put(id, idf);
		}
		
		return idfMap;
	}
	
	
	
	
	
	@Override
	public  Int2FloatMap getWeightMap() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static TFIDFTokenWeightGenerator getInstance(){
		if (instance ==null){
			instance = new TFIDFTokenWeightGenerator();
		}
		return instance;
	}


}
