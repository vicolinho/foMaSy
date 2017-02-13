package de.uni_leipzig.dbs.formRepository.matching.lookup;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.dataModel.*;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.execution.RegisteredMatcher;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.ExecutionTree;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.MatchOperator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import org.apache.log4j.Logger;

import java.util.*;

public class TokenSimilarityLookup {

	
	private static Map<Integer,Set<Integer>> lookup;
	private static TokenSimilarityLookup instance;
	private Logger log = Logger.getLogger(getClass());
	
	public void computeTrigramLookup(Set<EntityStructureVersion> set, EntityStructureVersion target,FormRepository rep) throws MatchingExecutionException{
		Set<String> words = new HashSet<String>();
		EntityStructureVersion wordStructure = new EntityStructureVersion(new VersionMetadata(-2, null, null, null, null));
		for (EntityStructureVersion esv:set){
			Collection<GenericProperty> properties =esv.getAvailableProperties();
			for (GenericEntity ge:esv.getEntities()){
				for (GenericProperty gp :properties){
					List<String> values = ge.getPropertyValues(gp);
					for (String v : values){
						String[] tokens = this.getTokens(v);
						for (String t:tokens){
							if (!t.trim().isEmpty())
							words.add(t);
						}
					}
				}
			}
		}
		GenericProperty wordgp = new GenericProperty(-1, "word", null, null);
		wordStructure.addAvailableProperty(wordgp);
		for (String w :words){
			GenericEntity ge = new GenericEntity (EncodingManager.getInstance().checkToken(w), w, "word", -1);
			PropertyValue pvPropertyValue =new PropertyValue(EncodingManager.getInstance().checkToken(w),w);
			ge.addPropertyValue(wordgp, pvPropertyValue);
			wordStructure.addEntity(ge);
		}
		
		words.clear();
		log .info("word src: " + wordStructure.getNumberOfEntities());

		Collection<GenericProperty> properties =target.getAvailableProperties();
		for (GenericEntity ge:target.getEntities()){
			for (GenericProperty gp :properties){
				List<String> values = ge.getPropertyValues(gp);
				for (String v : values){
					String[] tokens = this.getTokens(v);
					for (String t:tokens){
						if (!t.trim().isEmpty())
						words.add(t);
					}
				}
			}
		}

		EntityStructureVersion targetWord = new EntityStructureVersion(new VersionMetadata(-3, null, null, null, null));
		targetWord.addAvailableProperty(wordgp);
		for (String w :words){
			GenericEntity ge = new GenericEntity (EncodingManager.getInstance().checkToken(w), w, "word", -1);
			PropertyValue pvPropertyValue =new PropertyValue(EncodingManager.getInstance().checkToken(w),w);
			ge.addPropertyValue(wordgp, pvPropertyValue);
			targetWord.addEntity(ge);
		}
		log.info("target word: " +targetWord.getNumberOfEntities());
		EncodedEntityStructure src = EncodingManager.getInstance().encoding(wordStructure, true);
		EncodedEntityStructure trg = EncodingManager.getInstance().encoding(targetWord, true);
		ExecutionTree tree = new ExecutionTree();
		Set<GenericProperty> setGp = new HashSet<GenericProperty>();
		setGp.add(wordgp);
		MatchOperator mop = new MatchOperator (RegisteredMatcher.TRIGRAM_MATCHER, AggregationFunction.MAX, setGp, setGp, 0.7f);
		tree.addOperator(mop);
		AnnotationMapping mapping = rep.getMatchManager().match(wordStructure, src, trg, targetWord, tree, null);
		Map<Integer,Set<Integer>> wordCluster = new HashMap<Integer,Set<Integer>>();
		log.info("sim words:"+mapping.getNumberOfAnnotations());
		for (EntityAnnotation ea: mapping.getAnnotations()){
			Set<Integer> srcSet = wordCluster.get(ea.getSrcId());
			Set<Integer> trgSet = wordCluster.get(ea.getTargetId());
			if (ea.getSim()<1){
				if (srcSet ==null){
					srcSet = new HashSet<Integer>();
					wordCluster.put(ea.getSrcId(), srcSet);
				}
				srcSet.add(ea.getTargetId());
				if (trgSet ==null){
					trgSet = new HashSet<Integer>();
					wordCluster.put(ea.getTargetId(), trgSet);
				}
				trgSet.add(ea.getSrcId());
			}
		}
		setLookup(wordCluster);
	}
	
	
	public void computeTrigramLookup(EntitySet<GenericEntity> src2, EntitySet<GenericEntity> target,Set<GenericProperty> srcProps, Set<GenericProperty> targetProps,FormRepository rep) throws MatchingExecutionException{
		Set<String> words = new HashSet<String>();
		EntityStructureVersion wordStructure = new EntityStructureVersion(new VersionMetadata(-2, null, null, null, null));
		for (GenericEntity ge:src2){
			for (GenericProperty gp :srcProps){
				List<String> values = ge.getPropertyValues(gp);
				for (String v : values){
					String[] tokens = this.getTokens(v);
					for (String t:tokens){
						if (!t.trim().isEmpty())
						words.add(t);
					}
				}
			}
		}
		
		GenericProperty wordgp = new GenericProperty(-1, "word", null, null);
		wordStructure.addAvailableProperty(wordgp);
		log.info(words.size());
		for (String w :words){
			GenericEntity ge = new GenericEntity (EncodingManager.getInstance().checkToken(w), w, "word", -1);
			PropertyValue pvPropertyValue =new PropertyValue(EncodingManager.getInstance().checkToken(w),w);
			ge.addPropertyValue(wordgp, pvPropertyValue);
			wordStructure.addEntity(ge);
		}
		
		words.clear();
		log .info("word src: " + wordStructure.getNumberOfEntities());
		
		for (GenericEntity ge:target){
			for (GenericProperty gp :targetProps){
				List<String> values = ge.getPropertyValues(gp);
				for (String v : values){
					String[] tokens = this.getTokens(v);
					for (String t:tokens){
						if (!t.trim().isEmpty())
						words.add(t);
					}
				}
			}
		}
	
		
		EntityStructureVersion targetWord = new EntityStructureVersion(new VersionMetadata(-3, null, null, null, null));
		targetWord.addAvailableProperty(wordgp);
		for (String w :words){
			GenericEntity ge = new GenericEntity (EncodingManager.getInstance().checkToken(w), w, "word", -1);
			PropertyValue pvPropertyValue =new PropertyValue(EncodingManager.getInstance().checkToken(w),w);
			ge.addPropertyValue(wordgp, pvPropertyValue);
			targetWord.addEntity(ge);
		}
		log.info("target word: " +targetWord.getNumberOfEntities());
		EncodedEntityStructure src = EncodingManager.getInstance().encoding(wordStructure, true);
		EncodedEntityStructure trg = EncodingManager.getInstance().encoding(targetWord, true);
		ExecutionTree tree = new ExecutionTree();
		Set<GenericProperty> setGp = new HashSet<GenericProperty>();
		setGp.add(wordgp);
		MatchOperator mop = new MatchOperator (RegisteredMatcher.TRIGRAM_MATCHER, AggregationFunction.MAX, setGp, setGp, 0.7f);
		tree.addOperator(mop);
		AnnotationMapping mapping = rep.getMatchManager().match(wordStructure, src, trg, targetWord, tree, null);
		Map<Integer,Set<Integer>> wordCluster = new HashMap<Integer,Set<Integer>>();
		log.info("sim words:"+mapping.getNumberOfAnnotations());
		for (EntityAnnotation ea: mapping.getAnnotations()){
			Set<Integer> srcSet = wordCluster.get(ea.getSrcId());
			Set<Integer> trgSet = wordCluster.get(ea.getTargetId());
			if (ea.getSim()<1){
				if (srcSet ==null){
					srcSet = new HashSet<Integer>();
					wordCluster.put(ea.getSrcId(), srcSet);
				}
				srcSet.add(ea.getTargetId());
				if (trgSet ==null){
					trgSet = new HashSet<Integer>();
					wordCluster.put(ea.getTargetId(), trgSet);
				}
				trgSet.add(ea.getSrcId());
			}
		}
		setLookup(wordCluster);
	}
	
	
	
	private String[] getTokens (String value){
		String[] tokens = value.split("[^A-Za-z0-9]");
		return tokens;
	}


	
	public static TokenSimilarityLookup getInstance(){
		if (instance ==null){
			instance = new TokenSimilarityLookup();
		}
		return instance;
	}

	public static Map<Integer,Set<Integer>> getLookup() {
		return lookup;
	}

	public static void setLookup(Map<Integer,Set<Integer>> lookup) {
		TokenSimilarityLookup.lookup = lookup;
	}

}
