package de.uni_leipzig.dbs.formRepository.reuse;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import cern.colt.Arrays;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.holistic.HolisticMatchingAnnotation;
import de.uni_leipzig.dbs.formRepository.matching.holistic.data.TokenCluster;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;

public class CommonTokenIdentifier {

	Logger log = Logger.getLogger(getClass());
	
	private static int  clusterId =0;
	
	public Map<Integer, TokenCluster> getCommonTokens (EncodedEntityStructure srcEntities, EncodedEntityStructure targetEnt,
			Set<GenericProperty>srcProps, Set<GenericProperty> targetProperties, Map<Integer, Set<Integer>> lookup){
		Map<Set<Integer>,Float> countToken = new HashMap<Set<Integer>,Float>();
		int numberDescr=0;
		Map<Integer, TokenCluster> tokenClusters = new HashMap<Integer,TokenCluster>();
		for (int srcId: srcEntities.getObjIds().keySet()){
			countToken.clear();
			int entitySrcPos = srcEntities.getObjIds().get(srcId);
			for (Integer te :targetEnt.getObjIds().keySet()){
				int targetPos = targetEnt.getObjIds().get(te);
				for (GenericProperty srcGp : srcProps){
					try{
						int srcPropertyPos = srcEntities.getPropertyPosition().get(srcGp);
						for (int[] trigramSrc:srcEntities.getPropertyValueIds()[entitySrcPos][srcPropertyPos]){
							Set<Integer> countMapSrc = new HashSet<Integer>();
							for (int tsrcid :trigramSrc){
								countMapSrc.add(tsrcid);
							}
							for (GenericProperty gp :targetProperties){
								if (targetEnt.getPropertyPosition().containsKey(gp)){
									int targetPropertyPos = targetEnt.getPropertyPosition().get(gp);
									for (int [] trigramTarget: targetEnt.getPropertyValueIds()[targetPos][targetPropertyPos]){
										Set<Integer> targetSet = new HashSet<Integer>();
										for (int tarId : trigramTarget){
											if (countMapSrc.contains(tarId)){
												targetSet.add(tarId);	
											}else {
												Set<Integer> simTokens = lookup.get(tarId);
												if (simTokens!=null){
													for (int srcToken : countMapSrc){
														if (simTokens.contains(srcToken)){
															targetSet.add(srcToken);
														}
													}
												}
											}	
										}
										//targetSet.retainAll(countMapSrc);
										float numberOfTrigrams = (trigramTarget.length<trigramSrc.length)?trigramTarget.length:trigramSrc.length;
										float dice = (float)targetSet.size()/numberOfTrigrams;
										if (!countToken.containsKey(targetSet)&& targetSet.size()!=0){
											countToken.put(targetSet, dice);
										}else if (targetSet.size()!=0 && dice>countToken.get(targetSet)){
											countToken.put(targetSet,dice);
											
											//countToken.put(targetSet,countToken.get(targetSet)+1);
										}
									}
								}
							}
							
						}
						
					}catch(NullPointerException e){e.printStackTrace();};
				}//each source property
				 tokenClusters = this.getTokenCluster(countToken,tokenClusters);
			}
		}// each source entity
		
		
		return tokenClusters;
	}
	
	 private Map<Integer,TokenCluster> getTokenCluster (Map<Set<Integer>, Float> countToken,Map<Integer, TokenCluster> tokenClusters){
		 Map<Integer,TokenCluster> subTokenClusterMap = new HashMap<Integer,TokenCluster>();
		 TreeMap <Float,List<Set<Integer>>> countCommon = new TreeMap<Float,List<Set<Integer>>>();
			for (Entry <Set<Integer>,Float> e: countToken.entrySet()){
				List<Set<Integer>> list = countCommon.get(e.getValue());
				if (list == null){
					list = new ArrayList<Set<Integer>>();
					countCommon.put(e.getValue(),list);
				}
				list.add(e.getKey());
			}
			Float avgKey= null;
			try {
			 avgKey = countCommon.lastKey();
			}catch (NoSuchElementException e){}
			Float nextKey =null;
					do{
						try{
							nextKey = countCommon.lowerKey(avgKey);
							if (nextKey!=null&&nextKey >=(countCommon.lastKey()-0.1f)){
								avgKey = nextKey;
							}
						}catch (NoSuchElementException e){}
					}while(nextKey!=null && (nextKey >=(countCommon.lastKey()-0.1f)));
					
			if (avgKey!= null){
				if (countCommon.lastKey()>0.45){
					Map <Float,List<Set<Integer>>> avgMap = countCommon.subMap(avgKey, true, countCommon.lastKey(), true);
					for (List<Set<Integer>> list:avgMap.values()){
						for (Set<Integer> set: list){
							TokenCluster tc = new TokenCluster();
							tc.setClusterId(clusterId++);
							subTokenClusterMap.put(tc.getClusterId(), tc);
							for (int tid: set){
								tc.addToken(tid);
							}
						}
					}
				}
			}
			HolisticMatchingAnnotation hol = new HolisticMatchingAnnotation ();
			Map<Integer,Set<TokenCluster>> subsetMap = hol.getSupersetRelationships(subTokenClusterMap);
			//Map<Integer,Set<TokenCluster>> supersetMap = hol.getSubsetRelationships(fiCluster);
			Set<Integer> remove =new HashSet<Integer>();
			for (Integer key :subTokenClusterMap.keySet()){
				
				if (subsetMap.containsKey(key)){
					remove.add(key);
				}
			}
			for (int key:remove){
				subTokenClusterMap.remove(key);
			}
			tokenClusters.putAll(subTokenClusterMap);
			return tokenClusters;
	 }
}
