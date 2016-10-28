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
	
	Map <Integer,Integer> questionSizeMap ;
	
	
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
		am = this.keepMaxCorrespondencePerGroupOverall(src, am, target, preDomAtts, preRanAtts, rep, threshold, 0,avgEntitySize);
//		am = this.keepMaxCorrespondencePerGroup(src, am, target, preDomAtts, preRanAtts, rep, threshold, 0);
		return am;
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
	
	public AnnotationMapping keepMaxCorrespondencePerGroup(EncodedEntityStructure trial,AnnotationMapping corSet,EncodedEntityStructure umls,
			Set<GenericProperty>preDomAtts,Set<GenericProperty> preRanAtts,FormRepository gi, float t, int topK)  {
		//log.info("befor filtering:"+corSet.getNumberOfAnnotations());
		Map<Integer,List<EntityAnnotation>> groups = GroupFunctions.groupByItem(corSet);
		
		//iterate over each item with its umls correspondences
		for (Entry<Integer,List<EntityAnnotation>> e:groups.entrySet()){
			if (e.getValue().size()>1){
				EncodedEntityStructure umlsCons = this.getUMLSConcepts(e.getValue(), umls);
				if (e.getValue().size()>1000){
					log.warn("bad item"+ e.getKey());
				}
				HashMap<Integer, List<Integer>> umlsGroups;
				isCommonToken = corSet.getEvidenceMap()!=null;
				if (isCommonToken){
					isCommonToken = !corSet.getEvidenceMap().isEmpty();
				}
				if (isCommonToken)
					umlsGroups = GroupFunctions.groupSimilarUMLSConsByCommonToken(umlsCons, e.getKey(), corSet);
				else{
					umlsGroups = GroupFunctions.groupSimilarUMLSConsByConnectedComponent(umlsCons, preRanAtts, umls.getObjIds().size(), gi,t);
					log.debug("group concepts:"+umlsCons.getObjIds().size());
				}
				for (Entry<Integer, List<Integer>> e2: umlsGroups.entrySet()){
					List<Integer> umlsGroup = e2.getValue();
					TreeMap<Float,List<EntityAnnotation>> maxCorrs = new TreeMap<Float,List<EntityAnnotation>>();
					EntityAnnotation currentCor;
					//log.info("umls group:"+umlsGroup.toString());
					//this.testPrint(umlsGroup, umls);
					for (Integer u : umlsGroup){
						currentCor = corSet.getAnnotation(e.getKey(), u);
						if (currentCor !=null){						
							float conf =0;
							conf = (currentCor.getSim());
							List<EntityAnnotation> list= maxCorrs.get(conf);
							if (list==null){
								list = new ArrayList<EntityAnnotation>();
								maxCorrs.put(conf, list);
							}
							list.add(currentCor);
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
						//log.info("min confidence:" +maxCorrs.firstKey()+"max confidence"+ maxCorrs.lastKey());
						SortedMap<Float,List<EntityAnnotation>> topCons = maxCorrs.subMap(maxCorrs.firstKey(), lastKey);
						//log.info(topCons.keySet().toString());
						for (List<EntityAnnotation> rc: topCons.values()){
							for (EntityAnnotation c:rc){
								corSet.removeAnnotation(e.getKey(), c.getTargetId());
							}
						}
					}
				}// iterate over each similar umls Concept groups
			}
			
			
		}
	
		//log.info("after filtering:" +corSet.getNumberOfAnnotations());
		return corSet;
		
		
	}
	
	
	public AnnotationMapping keepMaxCorrespondencePerGroupOverall(EncodedEntityStructure trial,AnnotationMapping corSet,EncodedEntityStructure umls,
			Set<GenericProperty>preDomAtts,Set<GenericProperty> preRanAtts,FormRepository gi, float t, int topK,float avgEntitySize)  {
		//log.info("befor filtering:"+corSet.getNumberOfAnnotations());
		Map<Integer,List<EntityAnnotation>> groups = GroupFunctions.groupByItem(corSet);
		
		//iterate over each item with its umls correspondences
		for (Entry<Integer,List<EntityAnnotation>> e:groups.entrySet()){
			if (e.getValue().size()>1){
				EncodedEntityStructure umlsCons = this.getUMLSConcepts(e.getValue(), umls);
				//get confidence with different values
				//log.info("umls concepts:"+umlsCons.size());
				
				//calculate new confidences with trigram matcher
				/*
				ObjSet item = new ObjSet();
				item.addObj(trial.getObject(e.getKey()));
				ObjCorrespondenceSet granularConfidence = this.getCorrespondence(item, umlsCons,preDomAtts,preRanAtts, gi);
				*/
				//groups of similar umls concepts for the current Item
				HashMap<Integer, List<Integer>> umlsGroups;
				isCommonToken = corSet.getEvidenceMap()!=null;
				if (isCommonToken){
					isCommonToken = !corSet.getEvidenceMap().isEmpty();
				}
				if (isCommonToken)
					umlsGroups = GroupFunctions.groupSimilarUMLSConsByCommonToken(umlsCons, e.getKey(), corSet);
				else
					umlsGroups = GroupFunctions.groupSimilarUMLSConsByConnectedComponent(umlsCons, preRanAtts, umls.getObjIds().size(), gi,t);
				Set <Long> keptAnnotations = new HashSet<Long>();
				for (Entry<Integer, List<Integer>> e2: umlsGroups.entrySet()){
					if (debug&& e.getKey()==3){
						log.info(EncodingManager.getInstance().getReverseDict().get(e2.getKey()));
						log.info(e2.getValue().toString());
					}
					List<Integer> umlsGroup = e2.getValue();
					TreeMap<Float,List<EntityAnnotation>> maxCorrs = new TreeMap<Float,List<EntityAnnotation>>();
					EntityAnnotation currentCor;
					for (Integer u : umlsGroup){
						currentCor = corSet.getAnnotation(e.getKey(), u);
						if (currentCor !=null){						
							float conf =0;
							conf = (currentCor.getSim());
							List<EntityAnnotation> list= maxCorrs.get(conf);
							if (list==null){
								list = new ArrayList<EntityAnnotation>();
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
	
	
	
	public EncodedEntityStructure getUMLSConcepts(List<EntityAnnotation> list, EncodedEntityStructure umls){
		Set<Integer> targetIds  = new HashSet<Integer>();
		for (EntityAnnotation cor:list){
			targetIds.add(cor.getTargetId());
		}
		EncodedEntityStructure set = EncodingManager.getInstance().getSubset(umls, targetIds);
		return set;
	}
}
