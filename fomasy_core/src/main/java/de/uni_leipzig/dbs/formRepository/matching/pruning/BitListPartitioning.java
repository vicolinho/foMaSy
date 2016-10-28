package de.uni_leipzig.dbs.formRepository.matching.pruning;

import it.unimi.dsi.fastutil.ints.Int2IntMap;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;




import java.util.concurrent.CopyOnWriteArrayList;

import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;

public class BitListPartitioning implements Pruning{

	public static final int PARTITION_SIZE = 1000;
	public static final int BIT_LIST_LENGTH =45*45*45;
	
	CopyOnWriteArrayList<BitSet> bitListsPerPartition = new CopyOnWriteArrayList<BitSet>();
	CopyOnWriteArrayList<int[]> partitions = new CopyOnWriteArrayList<int[]>();
	
	public void initialize (EncodedEntityStructure ees,EntityStructureVersion esv, GenericProperty orderAttribute){
		List <BitSet> bitListsPerPartition = new ArrayList<BitSet>();
		List <int[]>partitions = new ArrayList<int[]>();
		EntityComparator comp = new EntityComparator(orderAttribute);
		Int2IntMap eMap = (Int2IntMap)ees.getObjIds();
		int[][][][] triIds = (int[][][][]) ees.getTrigramIds();
		List<GenericEntity> ents = new ArrayList<GenericEntity>(esv.getEntities());
		//ents.sort(comp);
		int counter =0;
		BitSet currentBitSet = new BitSet (BIT_LIST_LENGTH);
		int [] geIds = new int[PARTITION_SIZE];
		for (GenericEntity ge : ents){
			int pos = eMap.get(ge.getId());
			int [][][] tris = triIds[pos];
			for (int [][] propTris: tris){
				for (int[] triValues:propTris){
					for (int tid:triValues){
						currentBitSet.set(tid);
					}//each trigram ID
				}//each value from multi value
			}//each property
			geIds[counter] = ge.getId();
			counter++;
			
			if (counter>=1000){
				bitListsPerPartition.add(currentBitSet);
				this.partitions.add(geIds);
				currentBitSet = new BitSet(BIT_LIST_LENGTH);
				geIds = new int[PARTITION_SIZE];
				counter =0;
				
			}
		}//each entity
		if (counter !=0){
			bitListsPerPartition.add(currentBitSet);
			partitions.add(geIds);
		}
		
		this.bitListsPerPartition.addAll(bitListsPerPartition);
		this.partitions.addAll(partitions);
	}
	
	
	@Override
	public Set<Integer> getSimilarEntities( int[] trigrams) throws InterruptedException {
		Set <Integer> set = new HashSet<Integer>();
		float minOverlapRatio = 0.5f;
		ArrayList <ArrayList<BitSet>> bitSetParts = new ArrayList<ArrayList<BitSet>>();
		ArrayList <ArrayList<int[]>> objIdsPerBitList = new  ArrayList <ArrayList<int[]>> ();
		int threadCount = Runtime.getRuntime().availableProcessors();
		BitSet objBitList = new BitSet(BIT_LIST_LENGTH);
		for (int tid : trigrams){
			objBitList.set(tid);
		}
		for (int i =0; i< threadCount; i++){
			bitSetParts.add(new ArrayList<BitSet>());
			objIdsPerBitList.add(new ArrayList<int[]>());
		}
		
		
		for (int i =0;i<bitListsPerPartition.size();i++){
			bitSetParts.get(i%threadCount).add(bitListsPerPartition.get(i));
			objIdsPerBitList.get(i%threadCount).add(partitions.get(i));
		}
		
		List<BitlistSearch> threadList = new ArrayList<BitlistSearch>();
		for (int i =0; i< threadCount; i++){
			Integer [][] objPart = objIdsPerBitList.get(i).toArray(new Integer[][]{});
			BitlistSearch thread = new BitlistSearch (bitSetParts.get(i),objPart, objBitList, 0.5f, BIT_LIST_LENGTH);
			threadList.add(thread);
		}
		
		for (int i =0; i< threadCount; i++){
			threadList.get(i).start();
		}
		//int maxOverlap=0;
		//HashSet<Integer> maxSet =null; 
		HashSet<Integer> objSet = new HashSet<Integer>();
		//TreeMap <Integer, HashSet<Integer>> topMap = new TreeMap<Integer,HashSet<Integer>>();
		for (int i =0; i< threadCount; i++){
			threadList.get(i).join();
			//topMap.putAll(threadList.get(i).getTopMap());
			objSet.addAll(threadList.get(i).getResult());
			
		}
		
		
		
		/*
		if (!topMap.isEmpty()){
			Integer lastKey = topMap.lastKey();
			int count =0;
			while (lastKey!=null&&count<5){
				maxSet = topMap.get(lastKey);
				for (Integer i : maxSet){
					set.addObj(umls.getObj(i));
				}
				lastKey = topMap.lowerKey(lastKey);
				count++;
			}
		}*/
		return set;
	}

	
	
	private class EntityComparator implements Comparator<GenericEntity>{

		GenericProperty orderProperty;
		EntityComparator (GenericProperty p){
			this.orderProperty =p;
		}
		@Override
		public int compare(GenericEntity o1, GenericEntity o2) {
			String value1 ="";
			String value2 ="";
			try{
				value1 = o1.getStringValues(orderProperty).get(0);
			}catch(ArrayIndexOutOfBoundsException e){
				
			}catch (NullPointerException e){}
			try{
				value2 = o2.getStringValues(orderProperty).get(0);
			}catch(ArrayIndexOutOfBoundsException e){
				
			}catch (NullPointerException e){}
			return value1.compareTo(value2);
		}
		
	}



	@Override
	public Set<Integer> getSimilarEntitiesByTokens(int[] tokens)
			throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
