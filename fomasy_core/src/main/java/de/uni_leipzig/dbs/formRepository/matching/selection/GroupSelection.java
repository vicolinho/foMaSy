package de.uni_leipzig.dbs.formRepository.matching.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import de.uni_leipzig.dbs.formRepository.dataModel.EncodedAnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.util.CantorDecoder;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.execution.RegisteredMatcher;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.ExecutionTree;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.MatchOperator;
import edu.ucla.sspace.similarity.SimilarityFunction;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;

public class GroupSelection implements Selection{

	Logger log = Logger.getLogger(getClass());
	
	private boolean debug = false;
	
	public static boolean isCommonToken = true;

	public static boolean useGranular = false;
	
	Map <Integer,Integer> questionSizeMap ;
	
	static int multipleMaxCount = 0;
	@Override
	public AnnotationMapping select(AnnotationMapping am, float threshold,
			float delta) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AnnotationMapping select(AnnotationMapping am,
			EncodedEntityStructure src, EncodedEntityStructure target,Set<GenericProperty>preDomAtts,Set<GenericProperty> preRanAtts,
			float threshold, float delta,float avgEntitySize, FormRepository rep) {
		questionSizeMap = this.getQuestionSize(src);
		//TODO
		am = this.keepMaxCorrespondencePerGroupOverall(src, am, target, preDomAtts, preRanAtts, rep, threshold, 0,avgEntitySize);
		//am = this.keepMaxCorrespondencePerGroup(src, am, target, preDomAtts, preRanAtts, rep, threshold, 0);
		return am;
	}


	
	public AnnotationMapping keepMaxCorrespondencePerGroup(EncodedEntityStructure trial,AnnotationMapping corSet,
		 	EncodedEntityStructure umls, Set<GenericProperty>preDomAtts,Set<GenericProperty> preRanAtts,FormRepository gi, float t, int topK)  {
		Map<Integer,List<EntityAnnotation>> groups = GroupFunctions.groupByItem(corSet);
		//iterate over each item with its umls correspondences
		for (Entry<Integer,List<EntityAnnotation>> e:groups.entrySet()){
			if (e.getValue().size()>1){
				EncodedEntityStructure umlsCons = this.getConcepts(e.getValue(), umls, true);
				Map<Integer, Set<Integer>> umlsGroups;
				isCommonToken = corSet.getEvidenceMap()!=null;
				if (isCommonToken){
					isCommonToken = !corSet.getEvidenceMap().isEmpty();
				}
				if (isCommonToken)
					umlsGroups = GroupFunctions.groupSimilarUMLSConsByCommonToken(umlsCons, e.getKey(), corSet);
				else{
					umlsGroups = GroupFunctions.groupSimilarUMLSConsByConnectedComponent(umlsCons, preRanAtts,
									umls.getObjIds().size(), gi,t);
				}
				for (Entry<Integer, Set<Integer>> e2: umlsGroups.entrySet()){
					Set<Integer> umlsGroup = e2.getValue();
					TreeMap<Float,List<EntityAnnotation>> maxCorrs = new TreeMap<>();
					EntityAnnotation currentCor;
					for (Integer u : umlsGroup){
						currentCor = corSet.getAnnotation(e.getKey(), u);
						if (currentCor !=null){						
							float conf;
							conf = (currentCor.getSim());
							List<EntityAnnotation> list= maxCorrs.get(conf);
							if (list==null){
								list = new ArrayList<>();
								maxCorrs.put(conf, list);
							}
							list.add(currentCor);
						}else {

						}
					}//build treeMap with confidence as key
					
					//select the correspondences with the maximum concept for the current item of the group
					Float lastKey = null;
					try {
						lastKey= maxCorrs.lastKey();
						for(int i=0;i<topK;i++){
							if (maxCorrs.lowerKey(lastKey)!=null){
								lastKey = maxCorrs.lowerKey(lastKey);
							}
						}
					}catch(NoSuchElementException ex){}


					if (lastKey!=null){
						SortedMap<Float,List<EntityAnnotation>> topCons = maxCorrs.subMap(maxCorrs.firstKey(), lastKey);
						//remove annotations that are not topK
						for (List<EntityAnnotation> rc: topCons.values()){
							for (EntityAnnotation c:rc){
								corSet.removeAnnotation(e.getKey(), c.getTargetId());
							}
						}
					}
				}// iterate over each similar umls Concept groups
			}
		}
		return corSet;
	}

	public AnnotationMapping keepMaxCorrespondencePerGroupOverall(EncodedEntityStructure trial,AnnotationMapping corSet,EncodedEntityStructure umls,
			Set<GenericProperty>preDomAtts,Set<GenericProperty> preRanAtts,FormRepository gi, float t, int topK,float avgEntitySize)  {
		Map<Integer,List<EntityAnnotation>> groups = GroupFunctions.groupByItem(corSet);
		//iterate over each item with its umls correspondences
		for (Entry<Integer,List<EntityAnnotation>> e:groups.entrySet()){
			if (e.getValue().size()>1){
				EncodedEntityStructure umlsCons = this.getConcepts(e.getValue(), umls,true);
				EncodedEntityStructure srcEntity = this.getConcepts(e.getValue(), trial, false);
				EncodedAnnotationMapping eam = new EncodedAnnotationMapping();

				if (useGranular){
					eam = this.getGranularSimilarity(srcEntity, umlsCons, 0.4f, RegisteredMatcher.TRIGRAM_MATCHER, preDomAtts,
									preRanAtts, gi);
				}
				//groups of similar umls concepts for the current Item
				Map<Integer, Set<Integer>> umlsGroups;
				isCommonToken = corSet.getEvidenceMap()!=null;
				if (isCommonToken){
					isCommonToken = !corSet.getEvidenceMap().isEmpty();
				}
				if (isCommonToken)
					umlsGroups = GroupFunctions.groupSimilarUMLSConsByCommonToken(umlsCons, e.getKey(), corSet);
				else
					umlsGroups = GroupFunctions.groupSimilarUMLSConsByConnectedComponent(umlsCons, preRanAtts, umls.getObjIds().size(), gi,t);
				Set <Long> keptAnnotations = new HashSet<Long>();
				for (Entry<Integer, Set<Integer>> e2: umlsGroups.entrySet()){
					Set<Integer> umlsGroup = e2.getValue();
					TreeMap<Float,List<EntityAnnotation>> maxCorrs = new TreeMap<Float,List<EntityAnnotation>>();
					EntityAnnotation currentCor;
					for (Integer u : umlsGroup){
						currentCor = corSet.getAnnotation(e.getKey(), u);
						if (currentCor !=null){						
							float conf = (currentCor.getSim());
							if (useGranular) {
								if (eam.getAnnotation(e.getKey(), u)!=null)
									conf = conf*0.5f +0.5f* eam.getAnnotation(e.getKey(), u).getSim();
								else
									conf*=0.3;
							}
							List<EntityAnnotation> list;
							list= maxCorrs.get(conf);
							if (list==null){
								list = new ArrayList<>();
								maxCorrs.put(conf, list);
							}
							list.add(currentCor);
						}

					}//build treeMap with confidence as key
					
					//select the correspondences with the maximum concept for the current item of the group
					Float lastKey = null;
					try {
						lastKey= maxCorrs.lastKey();
					}catch(NoSuchElementException ex){}
					if (lastKey!=null){
						for (EntityAnnotation ea: maxCorrs.get(lastKey)){
							keptAnnotations.add(ea.getId());
						}
					}
				}// iterate over each similar umls Concept groups
				for (EntityAnnotation ea: e.getValue()){
					if (!keptAnnotations.contains(ea.getId())){
						corSet.removeAnnotation(ea.getSrcId(), ea.getTargetId());
					}
				}
				int questionSize = this.questionSizeMap.get(e.getKey());
				int estimatedEntityCount = (int) Math.ceil(questionSize/(avgEntitySize));
				
				if (keptAnnotations.size()>estimatedEntityCount){
					List <EntityAnnotation> list = new ArrayList<EntityAnnotation> ();
					for (long eaId : keptAnnotations){
						list.add(corSet.getAnnotation(eaId));
					}
					Collections.sort(list, Collections.reverseOrder());
					
					for (int i=estimatedEntityCount+1;i<list.size();i++){
						corSet.removeAnnotation(list.get(i).getSrcId(),list.get(i).getTargetId());
					}
				}
			}// complex annotation
		}//each item annotation
	
		//log.info("after filtering:" +corSet.getNumberOfAnnotations());
		return corSet;
		
		
	}



	private EncodedEntityStructure getConcepts(List<EntityAnnotation> list, EncodedEntityStructure umls, boolean isTarget){
		Set<Integer> targetIds  = new HashSet<Integer>();
		for (EntityAnnotation cor:list){
			if (isTarget)
				targetIds.add(cor.getTargetId());
			else {
				targetIds.add(cor.getSrcId());
			}
		}
		EncodedEntityStructure set = EncodingManager.getInstance().getSubset(umls, targetIds);
		return set;
	}

	private Map<Integer,Integer> getQuestionSize (EncodedEntityStructure src){
		Map <Integer,Integer> srcToSizeMap = new HashMap<Integer,Integer>();
		for (Entry<Integer,Integer> ePos :src.getObjIds().entrySet()){
			int pos = ePos.getValue();
			int maxSize =0;
			for (int [][] prop : src.getPropertyValueIds()[pos]){
				for (int [] pvs: prop){
					if (maxSize< pvs.length){
						maxSize = pvs.length;
					}
				}
			}
			srcToSizeMap.put(ePos.getKey(), maxSize);
		}
		return srcToSizeMap;
	}


	private EncodedAnnotationMapping getGranularSimilarity(EncodedEntityStructure ees,  EncodedEntityStructure umlsConcepts,
			float threshold, String matcher, Set<GenericProperty> src, Set<GenericProperty> target, FormRepository rep){
		MatchOperator mop = new MatchOperator(matcher, AggregationFunction.MAX, src, target, threshold);
		ExecutionTree executionTree = new ExecutionTree();
		executionTree.addOperator(mop);
		EncodedAnnotationMapping map = new EncodedAnnotationMapping();
		try {
			map = rep.getMatchManager().matchEncoded(ees, umlsConcepts, executionTree, null);
		} catch (MatchingExecutionException e) {
			e.printStackTrace();
		}
		return map;
	}
}
