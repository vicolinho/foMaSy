package de.uni_leipzig.dbs.formRepository.evals;

import de.uni_leipzig.dbs.formRepository.matching.preprocessing.exception.PreprocessingException;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.operation.SetAnnotationOperator;
import de.uni_leipzig.dbs.formRepository.evals.calculation.EvaluationResult;
import de.uni_leipzig.dbs.formRepository.evals.calculation.MappingEvaluation;
import de.uni_leipzig.dbs.formRepository.evals.io.CliqueResultWriter;
import de.uni_leipzig.dbs.formRepository.evals.io.EvaluationResultWriter;
import de.uni_leipzig.dbs.formRepository.evals.io.KeywordMappingWriter;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.execution.RegisteredMatcher;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.ExecutionTree;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.MatchOperator;
import de.uni_leipzig.dbs.formRepository.matching.holistic.HolisticMatchingAnnotation;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.EntityStructureFilter;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.FINAlgorithm;
import de.uni_leipzig.dbs.formRepository.matching.holistic.data.TokenCluster;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessingSteps;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorConfig;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorExecutor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.TokenWeighting.TFIDFTokenWeightGenerator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction.EntityFilterPreprocessor;
import de.uni_leipzig.dbs.formRepository.matching.selection.GroupSelection;
import de.uni_leipzig.dbs.formRepository.matching.selection.Selection;
import de.uni_leipzig.dbs.formRepository.matching.selection.SelectionOperator;
import de.uni_leipzig.dbs.formRepository.matching.selection.SelectionType;
import de.uni_leipzig.dbs.formRepository.matching.token.TFIDFMatcher;
import de.uni_leipzig.dbs.formRepository.matching.token.TFIDFWindowMatcher;

public class HolisticAnnotationMain {
	Logger log = Logger.getLogger(getClass());
	
	public static void main(String[] args){
		HolisticAnnotationMain main = new HolisticAnnotationMain();
		FormRepository rep = new FormRepositoryImpl();
		PropertyConfigurator.configure("log4j.properties");
		try {
			
			rep.initialize(args[0]);
			Set<String> types = new HashSet<String>();
			types.add("eligibility form");
			Set<EntityStructureVersion> set = rep.getFormManager().getStructureVersionsByType(types);
			HashMap<Integer,EntityStructureVersion> map = new HashMap<Integer,EntityStructureVersion>();
			for (EntityStructureVersion esv:set){
				map.put(esv.getStructureId(), esv);
			}
			Set<GenericProperty> formProperties = set.iterator().next().getAvailableProperties("question", "EN", null);
			Set<GenericProperty> formMatchProperties = new HashSet<GenericProperty>(formProperties);
			formMatchProperties.addAll(set.iterator().next().getAvailableProperties("name", "EN", null));
			
			
				PreprocessorConfig formConfig = new PreprocessorConfig ();
				PreprocessorConfig ontologyConfig = new PreprocessorConfig ();
				PreprocessProperty propForms = new PreprocessProperty("question", "EN", null);
				PreprocessProperty propFormName = new PreprocessProperty("name", "EN", null);
				PreprocessProperty propName = new PreprocessProperty("name","EN",null);
				PreprocessProperty propSyn = new PreprocessProperty ("synonym","EN",null);
				PreprocessProperty propDef = new PreprocessProperty ("definition","EN",null);
				PreprocessProperty propST = new PreprocessProperty ("sem_type","EN",null);
				formConfig.addPreprocessingStepForProperties(PreprocessingSteps.NORMALIZE, propForms);
				//formConfig.addPreprocessingStepForProperties(PreprocessingSteps.NUMBER_NORMALIZATION, propForms);
				formConfig.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, propForms,propFormName);
				formConfig.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, propForms,propFormName);
				formConfig.addPreprocessingStepForProperties(PreprocessingSteps.LENGTH_FILTER, propForms,propFormName);
				
				ontologyConfig.addPreprocessingStepForProperties(PreprocessingSteps.NORMALIZE, propName,propSyn,propDef);
				//ontologyConfig.addPreprocessingStepForProperties(PreprocessingSteps.PROPERTY_TOKEN_COUNT_FILTER, propName,propSyn);
				//ontologyConfig.addPreprocessingStepForProperties(PreprocessingSteps.DISCRIMINATOR_PROPERTY_ENTITY_FILTER,propST);
				//ontologyConfig.addPreprocessingStepForProperties(PreprocessingSteps.NUMBER_NORMALIZATION, propName,propSyn,propDef);
				ontologyConfig.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, propName,propSyn,propDef);
				ontologyConfig.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, propName,propSyn,propDef);
				ontologyConfig.addPreprocessingStepForProperties(PreprocessingSteps.LENGTH_FILTER, propName,propSyn,propDef);
				PreprocessorExecutor exec = new PreprocessorExecutor();
				Set<EncodedEntityStructure>	encodedForms = new HashSet<EncodedEntityStructure>();
				for (EntityStructureVersion esv :set){
					esv = exec.preprocess(esv, formConfig);
					EncodedEntityStructure ees = EncodingManager.getInstance().encoding(esv, true);
					encodedForms.add(ees);
					TFIDFTokenWeightGenerator.getInstance().initializeGlobalCountPerForm(ees.getStructureId(), ees.getPropertyValueIds(),
							ees.getPropertyPosition(), formProperties.toArray(new GenericProperty[]{}));
				}
				
				System.out.println("preprocess form");
				HolisticMatchingAnnotation holisticer = new HolisticMatchingAnnotation();
				Set<String> elementTypes = new HashSet<String>();
				elementTypes.add("item");
				holisticer.computeTermGroups(set, elementTypes, formProperties, 0.5f,6f,new FINAlgorithm());
				EncodedEntityStructure encodedCluster = holisticer.getEncodedCluster();
				EntityStructureVersion clusterStructure = holisticer.getClusterStructure();
				Set<GenericProperty> termGroupProperty = holisticer.getClusterStructure().getAvailableProperties("keyword",null,null);
				System.out.println("identify termgroups: "+clusterStructure.getNumberOfEntities());
				CliqueResultWriter cw = new CliqueResultWriter();
				cw.writeResult("coocurrenceAnnotation", holisticer.getClusters().values(), set);
				set.clear();
				for (EncodedEntityStructure ees :encodedForms){
					TFIDFTokenWeightGenerator.getInstance().removeCountForStructure(ees);
					//TFIDFTokenWeightGenerator.getInstance().initializeGlobalCount(ees, formProperties.toArray(new GenericProperty[]{}));
				}
				
				EntityStructureVersion ontology = rep.getFormManager().getStructureVersion("umls2014AB", "ontology", "2014-01-01");
				Set<GenericProperty> ontologyProps = new HashSet<GenericProperty>();
				ontologyProps.addAll(ontology.getAvailableProperties("name", "EN", null));
				ontologyProps.addAll(ontology.getAvailableProperties("synonym", "EN", null));
				//ontologyProps.addAll(ontology.getAvailableProperties("definition", "EN", null));
				exec.preprocess(ontology, ontologyConfig);
				EncodedEntityStructure encodedOntology = EncodingManager.getInstance().encoding(ontology, true);
				TFIDFTokenWeightGenerator.getInstance().initializeGlobalCount(encodedOntology, ontologyProps.toArray(new GenericProperty[]{}));
				TFIDFTokenWeightGenerator.getInstance().initializeGlobalCount(encodedCluster, termGroupProperty.toArray(new GenericProperty[]{}));
				Int2FloatMap idfMap = TFIDFTokenWeightGenerator.getInstance().generateIDFValuesForAllSources(clusterStructure.getNumberOfEntities()+ontology.getNumberOfEntities());
				Map<String,Object> externalObjects = new HashMap<String,Object>();
				externalObjects.put(TFIDFMatcher.IDF_MAP_SOURCE, idfMap);
				externalObjects.put(TFIDFMatcher.IDF_MAP_TARGET, idfMap);
				externalObjects.put(TFIDFMatcher.TFIDF_SOURCE_SEPARATED, false);
				MatchOperator trigram = new MatchOperator(RegisteredMatcher.TRIGRAM_MATCHER, AggregationFunction.MAX, termGroupProperty, ontologyProps, 0.8f);
				MatchOperator tfidf = new MatchOperator(RegisteredMatcher.TFIDF_MATCHER, AggregationFunction.MAX, termGroupProperty, ontologyProps, 0.8f);
				tfidf.setGlobalObjects(externalObjects);
				//SetOperator op= new SetOperator(AggregationFunction.MAX, SetOperator.UNION);
				//MatchGroup matchGroup = new MatchGroup(op, tfidf,trigram);	
				ExecutionTree tree= new ExecutionTree();
				tree.addOperator(tfidf);
				ExecutionTree tree2= new ExecutionTree();
				tree2.addOperator(trigram);
				List<TokenCluster> list = new ArrayList<TokenCluster> (holisticer.getClusters().values());
				Map<Integer,Set<TokenCluster>> subsetMap = holisticer.getSubsetRelationships(holisticer.getClusters());	
				Map<Integer,Set<TokenCluster>> supersetMap = holisticer.getSupersetRelationships();	
				Collections.sort(list, Collections.reverseOrder());
				try {
				AnnotationMapping tfidfMapping = rep.getMatchManager().match(clusterStructure, encodedCluster, encodedOntology, ontology, tree, null);
				AnnotationMapping triMapping = rep.getMatchManager().match(clusterStructure, encodedCluster, encodedOntology, ontology, tree2, null);
				
				
				for (float diff: new float[]{0f,0.05f,0.1f,0.15f}){
					
					float triThreshold = 0.8f+diff;
					float tfidfThrehold = 0.8f+diff;
					SelectionOperator selector = new SelectionOperator();
					AnnotationMapping tfidfFilterMapping = selector.select(tfidfMapping, SelectionType.THRESHOLD, tfidfThrehold, 0);
					//System.out.println("#tfidfMapping: "+ tfidfFilterMapping.getNumberOfAnnotations());
					AnnotationMapping triFilterMapping = selector.select(triMapping, SelectionType.THRESHOLD, triThreshold, 0);
					
					AnnotationMapping mapping = SetAnnotationOperator.union(AggregationFunction.MAX, triFilterMapping, tfidfFilterMapping);
					mapping = selector.select(mapping, SelectionType.MAX_DELTA_ONE_DIRECTION, 0, 0);
					mapping = main.filterMappingByOverlappedTermGroups(holisticer.getClusters(),list, subsetMap,supersetMap, mapping);
				
					TFIDFTokenWeightGenerator.getInstance().removeCountForStructure(encodedCluster);
					
					
					/*
					 * reduce property values of questions by removing mapped tokens
					 */
					AnnotationMapping calculatedMapping = new AnnotationMapping(null,ontology.getMetadata());
					EntityStructureFilter filter = new EntityStructureFilter ();
					System.out.println("compute reduced question mapping");
					int[] selectedForms = new int[]{1,2,7,8,9,10,11,12,13,14,15,19,20,75,76,77,78,79,80,81,82,83,84,85,86};
					Set <Integer> selForms = new HashSet<Integer>();
					for(int i : selectedForms)selForms.add(i);
					for (EncodedEntityStructure ees:encodedForms){
						if (selForms.contains(ees.getStructureId())){
							
						ees = filter.filterStructureByClusters(ees, holisticer.getClusters().values(), encodedCluster, termGroupProperty, mapping);
							TFIDFTokenWeightGenerator.getInstance().initializeGlobalCount(ees, formMatchProperties.toArray(new GenericProperty[]{}));
							
							Set<Integer> structSet = new HashSet<Integer>();
							structSet.add(ees.getStructureId());structSet.add(encodedOntology.getStructureId());
							Int2FloatMap idfMapPerForm = TFIDFTokenWeightGenerator.getInstance().generateIDFValues(structSet, ees.getObjIds().size()
									+encodedOntology.getObjIds().size());
							externalObjects.put(TFIDFMatcher.IDF_MAP_SOURCE, idfMapPerForm);
							externalObjects.put(TFIDFMatcher.IDF_MAP_TARGET, idfMapPerForm);
							externalObjects.put(TFIDFMatcher.TFIDF_SOURCE_SEPARATED, false);
							externalObjects.put(TFIDFWindowMatcher.WND_SIZE,5);
							MatchOperator trigramFormMapping = new MatchOperator(RegisteredMatcher.TRIGRAM_MATCHER, AggregationFunction.MAX, formProperties, ontologyProps, 0.7f);
							MatchOperator tfidfFormMapping = new MatchOperator(RegisteredMatcher.TFIDF_MATCHER, AggregationFunction.MAX, formMatchProperties, ontologyProps, 0.7f);
							tfidfFormMapping.setGlobalObjects(externalObjects);
							ExecutionTree treeForm= new ExecutionTree();
							treeForm.addOperator(tfidfFormMapping);
							ExecutionTree treeForm2= new ExecutionTree();
							treeForm2.addOperator(trigramFormMapping);
							AnnotationMapping tfidfMappingForm = rep.getMatchManager().match(ees, encodedOntology, treeForm, null);
							AnnotationMapping triMappingForm = rep.getMatchManager().match(ees,  encodedOntology, treeForm2, null);
							AnnotationMapping formMapping = SetAnnotationOperator.union(AggregationFunction.MAX, tfidfMappingForm, triMappingForm);
							Selection select = new GroupSelection();
							formMapping = select.select(formMapping, ees, encodedOntology, formMatchProperties, ontologyProps, 0.4f, 0,1, rep);
							//System.out.println (ees.getStructureId()+" number of items: "+ees.getObjIds().size() +" number of annotations: "+formMapping.getNumberOfAnnotations());
							for (EntityAnnotation ea :formMapping.getAnnotations()){
								calculatedMapping.addAnnotation(ea);
							}
							for (int id :ees.getObjIds().keySet()){
								int size = formMapping.getCorrespondingTargetIds(id).size();
								if (size>100){
									main.log.warn("number annotations is unexpected high:"+id+" size: "+size);
								}
							}
							System.out.println("calculated for entityStructure: "+calculatedMapping.getNumberOfAnnotations());
						}
						
					}
					
					
					
					
					
					
					System.out.println("size: "+mapping.getNumberOfAnnotations());
					//KeywordMappingWriter writer = new KeywordMappingWriter();
					//writer.writeKeywordMapping("keywordMapping3.csv", mapping, clusterStructure);
					
					
					/*
					 * generate overall item mapping;
					 */
					//AnnotationMapping calculatedMapping = new AnnotationMapping(null,ontology.getMetadata());
					Set <Integer> entities = new HashSet<Integer>();
					
				
					for (TokenCluster c: holisticer.getClusters().values()){
						entities.addAll(c.getItems());
						try{
						EntitySet<GenericEntity> eSet = rep.getFormManager().getEntitiesById(entities);
						Set<Integer> targetIds = mapping.getCorrespondingTargetIds(c.getClusterId());
						for (int tid :targetIds){
							for (int id : c.getItems()){
								
								GenericEntity ge = eSet.getEntity(id);
								//if (selForms.contains(ge.getSrcVersionStructureId())){
									EntityAnnotation am = mapping.getAnnotation(c.getClusterId(), tid);
									EntityAnnotation amItem  = new EntityAnnotation(ge.getId(), am.getTargetId(),
											ge.getAccession(), am.getTargetAccession(), am.getSim(), false);
									calculatedMapping.addAnnotation(amItem);
								//}
							}
						}
						entities.clear();
						}catch(EntityAPIException ea){}
					}
					
					/*
					 * retrieve reference mapping
					 */
					AnnotationMapping referenceMapping = new AnnotationMapping();
					 set = rep.getFormManager().getStructureVersionsByType(types);
					
					 int notAnnotated =0;
					 EntitySet<GenericEntity> eset = new GenericEntitySet();
					 for (EntityStructureVersion esv : set){
						 //if (selForms.contains(esv.getStructureId())){
						String name= esv.getMetadata().getName()+"["+esv.getMetadata().getTopic()+"]-"
								+ontology.getMetadata().getName()+"["+ontology.getMetadata().getTopic()+"]_odm";
						AnnotationMapping am1 = rep.getMappingManager().getAnnotationMapping(esv.getMetadata(),
								ontology.getMetadata(), name);
						for (GenericEntity ge : esv.getEntities()){
							Set<Integer> targetIds = calculatedMapping.getCorrespondingTargetIds(ge.getId());
							if (am1.getCorrespondingTargetIds(ge.getId()).isEmpty()&&!targetIds.isEmpty()){
								for (int tid : targetIds){
									calculatedMapping.removeAnnotation(ge.getId(), tid);
									notAnnotated ++;
								}
							}
							eset.addEntity(ge);
						}
						referenceMapping = SetAnnotationOperator.union(AggregationFunction.MAX, am1, referenceMapping);
						//}
					}
					 System.out.println("not annotated items: "+notAnnotated);
					System.out.println("reference annoations: "+referenceMapping.getNumberOfAnnotations());
					System.out.println("calculated mapping: "+calculatedMapping.getNumberOfAnnotations());
					MappingEvaluation eval = new MappingEvaluation();
				
					EvaluationResult er = eval.getResult(calculatedMapping, referenceMapping, "eligibility forms", ontology.getMetadata().getName());
					
					System.out.println(" threshold tfidf: "+tfidfThrehold+ " threshold trigram: " +triThreshold);
					System.out.println("precision: " +er.getMeasures().get("precision"));
					
					EvaluationResultWriter erw = new EvaluationResultWriter();
					erw.writeEvaluationDetail("annotationDetailNormal", eset, ontology, formProperties.iterator().next(), ontology.getAvailableProperties("synonym", "EN", null).iterator().next(),
							er);
					System.out.println("recall: "+er.getMeasures().get("recall"));
									
					System.out.println("f-measure: "+er.getMeasures().get("fmeasure"));
				}
			
				} catch (MatchingExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
		} catch (PreprocessingException e) {
			e.printStackTrace();
		}

	}
	
	private AnnotationMapping filterMappingByOverlappedTermGroups (Map<Integer,TokenCluster> clusterMap,List<TokenCluster> list, Map<Integer,Set<TokenCluster>> subsetMap,Map<Integer,Set<TokenCluster>> supersetMap, AnnotationMapping mapping) {
		for (int i = 0;i<list.size();i++){
			Set<TokenCluster> subsets = subsetMap.get(list.get(i).getClusterId());
			TreeMap<Float,Set<EntityAnnotation>> simOrder = new TreeMap<Float,Set<EntityAnnotation>>();
			if (subsets!= null){
				for (TokenCluster c : subsets){
					float subsetCount = (supersetMap.get(c.getClusterId())!=null)?supersetMap.get(c.getClusterId()).size():1;
					//log.debug(c.getTokenIds().toString()+" : "+subsetCount);
					Set<Integer> targetIds = mapping.getCorrespondingTargetIds(c.getClusterId());
					for (Integer target :targetIds){
						EntityAnnotation ea = mapping.getAnnotation(c.getClusterId(), target);
						float aggSim = ea.getSim();
						
						Set<EntityAnnotation> sameSim = simOrder.get(aggSim);
						if (sameSim == null){
							sameSim = new HashSet<EntityAnnotation>();
							simOrder.put(aggSim, sameSim);
						}
						sameSim.add(ea);
					}	
				}
				if (!simOrder.isEmpty()){
					Map<Float, Set<EntityAnnotation>> toRemove = simOrder.subMap(simOrder.firstKey(), true, simOrder.lastKey(), false);
					for (Set<EntityAnnotation> eas: toRemove.values()){
						for (EntityAnnotation ea: eas){
							mapping.removeAnnotation(ea.getSrcId(), ea.getTargetId());
						}
					}
					/*
					if (simOrder.lastEntry().getValue().size()>1){
						List<TokenCluster> highSimCluster = new ArrayList<TokenCluster>();
						for (EntityAnnotation ea: simOrder.lastEntry().getValue()){
							highSimCluster.add(clusterMap.get(ea.getSrcId()));
						}
						TokenCluster c = Collections.max(highSimCluster);
						log.debug("token count: "+c.getTokenIds().size());
						for (EntityAnnotation ea:simOrder.lastEntry().getValue()){
							if (ea.getSrcId()!=c.getClusterId()){
								mapping.removeAnnotation(ea.getSrcId(), ea.getTargetId());
							}
						}
					}*/
					
				}else {
				}
			}
		}
		return mapping;
	}
	
}
