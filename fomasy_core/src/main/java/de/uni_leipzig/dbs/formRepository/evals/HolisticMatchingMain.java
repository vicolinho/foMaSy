package de.uni_leipzig.dbs.formRepository.evals;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.mysql.jdbc.log.Log;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.evals.io.CliqueResultWriter;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.CliqueIdentification;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.TokenClusterInitialization;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.ClusterSimilarityFunctions;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.ClusteringAlgorithm;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.FINAlgorithm;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.SimilarCluster;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.ThresholdFunctions;
import de.uni_leipzig.dbs.formRepository.matching.holistic.data.TokenCluster;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessingSteps;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorConfig;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorExecutor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.TokenWeighting.TFIDFTokenWeightGenerator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;

public class HolisticMatchingMain {

	Logger log = Logger.getLogger(getClass());
	 
	
	public static void main (String[] args){
		HolisticMatchingMain hmm = new HolisticMatchingMain();
		PropertyConfigurator.configure("log4j.properties");
		FormRepository fr = new FormRepositoryImpl();
		
		Set<String> structureTypes = new HashSet<String>();
		structureTypes.add("eligibility form");
		try {
			fr.initialize("fms.ini");
			Set<EntityStructureVersion> forms = fr.getFormManager().getStructureVersionsByType(structureTypes);
			hmm.log.info(forms.size());
			TokenClusterInitialization init = new TokenClusterInitialization();
			Set<GenericProperty> questionProperty = forms.iterator().next().getAvailableProperties("question", "EN", null);
			PreprocessorConfig config = new PreprocessorConfig();
			PreprocessProperty prop =new PreprocessProperty("question", "EN", null);
			config.addPreprocessingStepForProperties(PreprocessingSteps.NORMALIZE, prop);
			config.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, prop);
			config.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, prop);
			//config.addPreprocessingStepForProperties(PreprocessingSteps.ABBREVIATION_FILTER, prop);
			config.addPreprocessingStepForProperties(PreprocessingSteps.LENGTH_FILTER, prop);
			
			PreprocessorExecutor exec = new PreprocessorExecutor();
			
			Set<EncodedEntityStructure> eess = new HashSet<EncodedEntityStructure>();
			Set<String> types = new HashSet<String>();
			types.add("item");
			for(EntityStructureVersion esv:forms){
				esv = exec.preprocess(esv, config);
				EncodedEntityStructure ees = EncodingManager.getInstance().encoding(esv,types, true);
				eess.add(ees);
				TFIDFTokenWeightGenerator.getInstance().initializeGlobalCountPerForm(ees.getStructureId(), ees.getPropertyValueIds(),
						ees.getPropertyPosition(), questionProperty.toArray(new GenericProperty[]{}));
			}
		
			
			Map<Integer,TokenCluster> clusters = init.initializeCluster(forms,questionProperty,types,0.5f);
			
			Int2ObjectMap<List<SimilarCluster>> similarities = init.calculateSimilarityMatrix(clusters, ClusterSimilarityFunctions.DICE,0.15f);
			ClusteringAlgorithm identification = new FINAlgorithm ();
			hmm.log.info("#clusters"+clusters.size());
			
			Map<Integer,TokenCluster> cliques = identification.cluster(clusters, similarities, eess, questionProperty, 10f);
			Set<Integer> notRepresentative = new HashSet<Integer>();
			hmm.log.info("#FI: "+cliques.size());
			
			for (TokenCluster c :cliques.values()){
				if (c.getTokenIds().size()<2)
					notRepresentative.add(c.getClusterId());
			}
			for (Integer rid :notRepresentative)
				cliques.remove(rid);
			hmm.log.info("#FI: "+cliques.size());
			CliqueResultWriter writer = new CliqueResultWriter();
			writer.writeResult("holisticEligibility", cliques.values(), forms);
			/*
			for (Entry<Integer,List<SimilarCluster>> e:similarities.entrySet()){
				hmm.log.info(EncodingManager.getInstance().getReverseDict().get(e.getKey()));
				for (SimilarCluster sc: e.getValue()){
					count++;
				}
				
			}
			System.out.println(count/2);*/
			
			/*
			TreeMap <Float,List<Integer>> simMap = new TreeMap<Float, List<Integer>>();
			Set <Integer> important = new HashSet<Integer>();
			for (Entry<Integer,Cluster> e : clusters.entrySet()){
				
				List <Integer> list = simMap.get((float) e.getValue().getAggregateTFIDF());
				if (list==null){
					list = new ArrayList<Integer>();
					simMap.put((float) e.getValue().getAggregateTFIDF(), list);
				}
				list.add(e.getKey());
			}*/
			/*
			BufferedWriter br = new BufferedWriter (new FileWriter("tfidf- distribution.csv"));
			Entry <Float,List <Integer>> minList = simMap.firstEntry();
			br.append("tfidf;count"+System.getProperty("line.separator"));
			for (Entry <Float,List<Integer>> e:simMap.entrySet()){
				if (e.getKey()<1f){
					for (int i :e.getValue()){
						hmm.log.info(EncodingManager.getInstance().getReverseDict().get(i));
					}
				}
				br.append(e.getKey()+";"+e.getValue().size()+System.getProperty("line.separator"));
				
			}
			br.close();*/
			
			
			
		} catch (VersionNotExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StructureBuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
