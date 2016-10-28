package de.uni_leipzig.dbs.formRepository.matching.holistic.clustering;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.holistic.data.TokenCluster;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.TokenWeighting.TFIDFTokenWeightGenerator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;

public class TokenClusterInitialization {

	Logger log = Logger.getLogger(getClass());
	public Map<Integer,TokenCluster> initializeCluster(Set<EntityStructureVersion> forms, Set<GenericProperty> questionProperty,Set<String> types, float epsilon){
		Map<Integer,TokenCluster> clusters = new HashMap<Integer,TokenCluster>();
		Set<EncodedEntityStructure> encodedForms = new HashSet<EncodedEntityStructure>();
		for (EntityStructureVersion f: forms){
			EncodedEntityStructure ees = EncodingManager.getInstance().encoding(f, types, true);
			encodedForms.add(ees);
			for (Entry<Integer,Integer> e : ees.getObjIds().entrySet()){
				int pos = e.getValue();
				for (GenericProperty gp : questionProperty){
					int propPos = ees.getPropertyPosition().get(gp);
					int [][] propValues = ees.getPropertyValueIds()[pos][propPos];
					for (int [] value: propValues){
						for (int token : value){
							TokenCluster c = clusters.get(token);
							if (c == null){
								c = new TokenCluster();
								c.addToken(token);
								clusters.put(token, c);
							}
							c.addItem(e.getKey());
						}
					}
				}
			}
			
		}
		
		Int2FloatMap idfMap =  TFIDFTokenWeightGenerator.getInstance().generateIDFValuesForAllSources(forms.size());
		Set <Integer> unnecessaryTokens = new HashSet<Integer>(); 
		for (Entry<Integer,TokenCluster> e : clusters.entrySet()) {
			int tid = e.getKey();
			TokenCluster c = e.getValue();
			float idf = idfMap.get(tid);
			float tfidf =idf;
			if (tfidf<epsilon){
				unnecessaryTokens.add(e.getKey());
			}
			c.setAggregateTFIDF(tfidf);
		}
		for (Integer i :unnecessaryTokens){
			clusters.remove(i);
			log.debug("too frequent tokens: " + EncodingManager.getInstance().getReverseDict().get(i));
		}
		
		return clusters;
	}
	
	public Map<Integer,TokenCluster> initializeClusterByEncodedStructures(Set<EncodedEntityStructure> forms, Set<GenericProperty> questionProperty, float epsilon){
		Map<Integer,TokenCluster> clusters = new HashMap<Integer,TokenCluster>();
		for (EncodedEntityStructure ees : forms){
			for (Entry<Integer,Integer> e : ees.getObjIds().entrySet()){
				int pos = e.getValue();
				for (GenericProperty gp : questionProperty){
					try{
						int propPos = ees.getPropertyPosition().get(gp);
						int [][] propValues = ees.getPropertyValueIds()[pos][propPos];
						for (int [] value: propValues){
							for (int token : value){
								TokenCluster c = clusters.get(token);
								if (c == null){
									c = new TokenCluster();
									c.addToken(token);
									clusters.put(token, c);
								}
								c.addItem(e.getKey());
							}
						}
					}catch(NullPointerException exc){}
				}
			}
		}
		Int2FloatMap idfMap =  TFIDFTokenWeightGenerator.getInstance().generateIDFValuesForAllSources(forms.size());
		Set <Integer> unnecessaryTokens = new HashSet<Integer>(); 
		for (Entry<Integer,TokenCluster> e : clusters.entrySet()) {
			int tid = e.getKey();
			TokenCluster c = e.getValue();
			float idf = idfMap.get(tid);
			float tfidf =idf;
			if (tfidf<epsilon){
				unnecessaryTokens.add(e.getKey());
			}
			c.setAggregateTFIDF(tfidf);
		}
		for (Integer i :unnecessaryTokens){
			clusters.remove(i);
		}
		
		return clusters;
	}


	
	public Int2ObjectMap<List<SimilarCluster>> calculateSimilarityMatrix(Map<Integer, TokenCluster> clusters,
			ClusterSimilarityFunction func, float minSimilarity){
		Int2ObjectMap<List<SimilarCluster>> map = new Int2ObjectOpenHashMap<List<SimilarCluster>>();
		TokenCluster[] array1 = clusters.values().toArray(new TokenCluster[]{});
		TokenCluster[] array2 = clusters.values().toArray(new TokenCluster[]{});
		for (int i =0 ;i<array1.length;i++){
			TokenCluster c1 = array1[i];
			for (int j =i+1; j <array2.length;j++){
				TokenCluster c2 = array2[j];
				
				float sim = func.calculateSimilarity(c1, c2);
				if (sim==1){
					log.warn(c1+"-"+c2);
				}
				if (sim>minSimilarity){
					SimilarCluster simC2 = new SimilarCluster(sim,c2.getTokenId(),c1.getTokenId());
					List <SimilarCluster> simClusters = map.get(c1.getTokenId());
					if (simClusters ==null){
						simClusters  = new ArrayList<SimilarCluster>();
						map.put(c1.getTokenId(), simClusters);
					}
					List <SimilarCluster> simClustersC2 = map.get(c2.getTokenId());
					if (simClustersC2 ==null){
						simClustersC2  = new ArrayList<SimilarCluster>();
						map.put(c2.getTokenId(), simClustersC2);
					}
					
					simC2.setSim(sim);
					simClustersC2.add(simC2);
					simClusters.add(simC2);
					
				}
				
			}
		}
		/*
		for (List<SimilarCluster> simClusters : map.values()){
			float overallWeight =0;
			for (SimilarCluster c: simClusters){
				overallWeight+= c.getSim();
			}
			for (int i = 0;i<simClusters.size();i++){
				SimilarCluster c = simClusters.get(i);
				c.setSim(c.getSim()/overallWeight);
				if (c.getSim()<minSimilarity){
					simClusters.remove(i--);	
				}
			}	
		}*/
		return map;
	}
}
