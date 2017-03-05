package de.uni_leipzig.dbs.formRepository.evals;

import de.uni_leipzig.dbs.formRepository.matching.preprocessing.exception.PreprocessingException;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationCluster;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.operation.SetAnnotationOperator;
import de.uni_leipzig.dbs.formRepository.evals.calculation.EvaluationResult;
import de.uni_leipzig.dbs.formRepository.evals.calculation.MappingEvaluation;
import de.uni_leipzig.dbs.formRepository.evals.io.EvaluationResultWriter;
import de.uni_leipzig.dbs.formRepository.exception.ClusterAPIException;
import de.uni_leipzig.dbs.formRepository.exception.ClusterNotExistsException;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;
import de.uni_leipzig.dbs.formRepository.exception.GraphAPIException;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.execution.RegisteredMatcher;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.ExecutionTree;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.MatchOperator;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Edge;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;
import de.uni_leipzig.dbs.formRepository.matching.graph.operations.GraphOperator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessingSteps;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorConfig;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorExecutor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.TokenWeighting.TFIDFTokenWeightGenerator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction.POSBasedExtractingPreprocessor;
import de.uni_leipzig.dbs.formRepository.matching.lookup.TokenSimilarityLookup;
import de.uni_leipzig.dbs.formRepository.matching.selection.GraphBasedSelection;
import de.uni_leipzig.dbs.formRepository.matching.selection.Selection;
import de.uni_leipzig.dbs.formRepository.matching.selection.SelectionFactory;
import de.uni_leipzig.dbs.formRepository.matching.selection.SelectionType;
import de.uni_leipzig.dbs.formRepository.matching.token.SoftTFIDFMatcher;
import de.uni_leipzig.dbs.formRepository.matching.token.TFIDFWindowMatcher;
import de.uni_leipzig.dbs.formRepository.reuse.AnnotationClusterTransformer;
import de.uni_leipzig.dbs.formRepository.reuse.CooccurenceCalculator;
import edu.uci.ics.jung.graph.DirectedGraph;

public class ReuseEvaluationWithContext {
	public static String usage(){
		return "This class annotate a set of forms by reuse a set of annotation clusters"
				+ " The application runs by command: java <rep.ini> cs=<clusterStructureName> ReuseClusterEvaluation";
	}
	static Logger log = Logger.getLogger(ReuseEvaluation.class);
	
	static boolean filterGeneralConcepts =true; 
	static String[] generalConcepts = new String[]{"Qualitative Concept","Quantitative Concept",
		"Functional Concept","Conceptual Entity","Temporal Concept"};
	

	static int[] selectedForms = new int[]{2,3,8,9,10,
		11,12,13,14,15,
		16,20,21,76,77,
		78,79,80,81,82,
	83,84,85,86,87};

/*
	static int[] selectedForms = new int[]{1,2,7,8,9,
										  10,11,12,13,14,
										  15,19,20,75,76,
										  77,78,79,80,81,
										  82,83,84,85,86};
*/
//	439,440
//	461,455,456,457,458,459,464,466,467,468,465,463,462,452,453,454,469,470,460,473,475,476
//	static int[] selectedForms = new int[]{450, 451, 452, 453, 454, 455, 456, 457, 458, 459, 460, 461, 462, 463, 
//	 464, 465, 466, 467, 468, 469, 470, 471, 472, 473,
//	 474, 475, 476, 477, 478, 479, 480, 481, 482};
//	static int[] selectedForms = new int[]{461,455,456,457,458,459,464,466,
//		467,468,465,463,462,452,453,454,469,470,460,473,475,476,439,440};
	static Set <Integer> selForms = new HashSet<Integer>();
	static String[] t = new String[]{"CD","FW","JJ","JJR","JJS","LS","NN","NNS","NNP","RB","RBS","SYM","IN"};
	static float threshold =0.55f;
	static int context_depth = 1;
	static boolean onlySelectedForms = true;
	private static long matchTime;
	private static long selectionTime;
	private static long preprocessingTime;
	
	public static void main (String[] args){
		FormRepository rep = new FormRepositoryImpl();
		
		Set<String> wordTypes = new HashSet<>();
		for(String i: t){
			wordTypes.add(i);
		}
		PropertyConfigurator.configure("log4j.properties");
		Properties prop = new Properties ();
		try {
			prop.load(new FileReader("main.properties"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (prop!=null){
			threshold =  Float.parseFloat((String) prop.get("threshold"));
			context_depth = Integer.parseInt((String) prop.get("context_depth"));
			log.info("threshold="+threshold);
			log.info("context_depth="+context_depth);
		}
		String clusterConfigName = null;
		long startTime = System.currentTimeMillis();
		preprocessingTime =0;
		long preStart = System.currentTimeMillis();
		if (args.length ==2){
			clusterConfigName = args[1].replace("cn=", "").trim();
		}
		
		try {
			
			for (int f: selectedForms){
				selForms.add(f);
			}
			rep.initialize(args[0]);
			VersionMetadata ontology = rep.getFormManager().getMetadata("umls2014AB", "ontology", "2014-01-01");
			Set<String> types = new HashSet<String>();
			
			types.add("eligibility form");
//			types.add("quality assurance");
			Set<EntityStructureVersion> set = rep.getFormManager().getStructureVersionsByType(types);
			HashMap<Integer,EntityStructureVersion> map = new HashMap<Integer,EntityStructureVersion>();
			for (EntityStructureVersion esv:set){
				map.put(esv.getStructureId(), esv);
			}
			
			
			Set <String> entTypes = new HashSet<String>();
			entTypes.add("item");
			Set<EncodedEntityStructure> encSet = new HashSet<EncodedEntityStructure>();
			Set<GenericProperty> formProperties = set.iterator().next().getAvailableProperties("question", "EN", null);
			Set<GenericProperty> formMatchProperties = new HashSet<GenericProperty>(formProperties);
			formMatchProperties.addAll(set.iterator().next().getAvailableProperties("name", "EN", null));
			
			
			PreprocessorConfig formConfig = new PreprocessorConfig ();
			PreprocessProperty propForms = new PreprocessProperty("question", "EN", null);
			PreprocessProperty propFormName = new PreprocessProperty("name", "EN", null);
			Map<String,Object> preMap = new HashMap<String,Object>();
			preMap.put(POSBasedExtractingPreprocessor.FILTER_TYPES, wordTypes);
			formConfig.setExternalSourceMap(preMap);
			Set<String> semTypes = new HashSet<String> ();
			for (String sem: generalConcepts){
				semTypes.add(sem);
			}
	
			Set<GenericProperty> umlsProperty = rep.getFormManager().getAvailableProperties("umls2014AB", "2014-01-01", "ontology");
			Set<GenericProperty> semProperty = new HashSet<GenericProperty>();
			for (GenericProperty gp : umlsProperty){
				semProperty.add(gp);
			}
			VersionMetadata vm = rep.getFormManager().getMetadata("umls2014AB", "ontology", "2014-01-01");
			EntitySet<GenericEntity> generalConceptEntities = rep.getFormManager().getEntitiesByPropertyWithProperties(semTypes, vm, semProperty);
			Set<Integer> toFilteringConceptIds = new HashSet<Integer>();
			for (GenericEntity ge:generalConceptEntities){
				toFilteringConceptIds.add(ge.getId());
			}
			log.info("number of general concepts: "+ toFilteringConceptIds.size());
			
			formConfig.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, propForms,propFormName);
			formConfig.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, propForms,propFormName);
			formConfig.addPreprocessingStepForProperties(PreprocessingSteps.KEYWORD_EXTRACTION, propForms, propFormName);
			PreprocessorExecutor executor = new PreprocessorExecutor();
			int size = 0;
			
			for (EntityStructureVersion esv: set){
				esv = executor.preprocess(esv, formConfig);
				size += esv.getTypeCount().get("item");
				
				EncodedEntityStructure ees = EncodingManager.getInstance().encoding(esv, entTypes, true);
				TFIDFTokenWeightGenerator.getInstance().initializeGlobalCount(ees, formMatchProperties.toArray(
						new GenericProperty[]{}));
				encSet.add(ees);	
			}
			
			Map <GenericEntity, AnnotationCluster> clusterMap = rep.getClusterManager().getDeterminedClusters(clusterConfigName);
			Set <Integer> generalConcepts = new HashSet<Integer>();
			Map<GenericEntity,AnnotationCluster> generalMap = new HashMap<GenericEntity,AnnotationCluster> ();
			int generalConceptCount = 0;
			if (filterGeneralConcepts){
				for (Entry<GenericEntity,AnnotationCluster> entry: clusterMap.entrySet()){
					List <String> semType = entry.getKey().getPropertyValues("sem_type", null, null);
					for (String st :semType){
						if (semTypes.contains(st)){
							generalMap.put(entry.getKey(), entry.getValue());
							generalConcepts.add(entry.getKey().getId());
							generalConceptCount++;
							break;
						}
					}
				}
				for (GenericEntity ge:generalMap.keySet()){
					clusterMap.remove(ge);
				}
			}
			float avgEntitySize =0;
			float totalCount =0;
			for (AnnotationCluster ac : clusterMap.values()){
				for (String repString : ac.getRepresentants()){
					avgEntitySize +=repString.split("\\s").length;
					totalCount++;
				}
			}
			avgEntitySize /=totalCount;
			log.info("avgSize: "+avgEntitySize);
			log.info("general concepts: " +generalConceptCount);
			log.info("reuse of "+clusterMap.size()+" clusters");
			AnnotationClusterTransformer transformer = new AnnotationClusterTransformer();
			EncodedEntityStructure encodedCluster = transformer.transformClusters(clusterMap);
			EntityStructureVersion clusterESV = transformer.transformClustersToStructure(clusterMap);
			log.info(encodedCluster.getPropertyPosition().keySet());
			Set<GenericProperty> gp = new  HashSet<GenericProperty>(encodedCluster.getPropertyPosition().keySet());
			TFIDFTokenWeightGenerator.getInstance().initializeGlobalCount(encodedCluster, gp.toArray(
					new GenericProperty[]{}));
			size += encodedCluster.getObjIds().size();
			
			MatchOperator mop = new MatchOperator (RegisteredMatcher.SOFT_TFIDF_WND_MATCHER,
					AggregationFunction.MAX, formMatchProperties, gp, threshold);
			Map<String,Object> externalMap = new HashMap<String,Object>();
			externalMap.put(TFIDFWindowMatcher.WND_SIZE, 5);
			Int2FloatMap idfMap = TFIDFTokenWeightGenerator.getInstance().generateIDFValuesForAllSources(size);
			externalMap.put(TFIDFWindowMatcher.IDF_MAP_SOURCE, idfMap);
			externalMap.put(TFIDFWindowMatcher.IDF_MAP_TARGET, idfMap);
			externalMap.put(TFIDFWindowMatcher.TFIDF_SOURCE_SEPARATED, false);
		
			preprocessingTime += (System.currentTimeMillis()-preStart);
			log.info("calculates token similarity...");
			long tokenTime = System.currentTimeMillis();
			TokenSimilarityLookup.getInstance().computeTrigramLookup(set, clusterESV, rep);
			matchTime+=(System.currentTimeMillis()-tokenTime);
			log.info("token lookup built");
			Map <Integer,Set<Integer>> tokenSimLookup = TokenSimilarityLookup.getInstance().getLookup();
			externalMap.put(SoftTFIDFMatcher.LOOKUP, tokenSimLookup);
			mop.setGlobalObjects(externalMap);
			AnnotationMapping calculatedMapping = new AnnotationMapping();
			Map<Integer,AnnotationCluster> idClusterMap = new HashMap<Integer,AnnotationCluster>();
			
			
			for (AnnotationCluster ac: clusterMap.values()){
				idClusterMap.put(ac.getId(), ac);
			}
			
			Map<Integer,AnnotationMapping> annoMap = new HashMap<Integer,AnnotationMapping>();	
			long candidateIdentTime = System.currentTimeMillis();
			for (EncodedEntityStructure ees : encSet){
				if (onlySelectedForms){
					if(selForms.contains(ees.getStructureId())){
						ExecutionTree tree = new ExecutionTree();
						tree.addOperator(mop);
						AnnotationMapping am = rep.getMatchManager().match(ees, encodedCluster, tree, null);
						annoMap.put(ees.getStructureId(), am);
					}
				}else {
					ExecutionTree tree = new ExecutionTree();
					tree.addOperator(mop);
					AnnotationMapping am = rep.getMatchManager().match(ees, encodedCluster, tree, null);
					annoMap.put(ees.getStructureId(), am);
				}
			}
			size-=encodedCluster.getObjIds().size();
			matchTime+=(System.currentTimeMillis()-candidateIdentTime);
			long umlsPreStart =System.currentTimeMillis();
			TFIDFTokenWeightGenerator.getInstance().removeCountForStructure(encodedCluster);
			EntityStructureVersion umls = rep.getFormManager().getStructureVersion("umls2014AB", "ontology", "2014-01-01");
			Set<GenericProperty> umlsProperties = umls.getAvailableProperties("name", "EN", null);
			umlsProperties.addAll(umls.getAvailableProperties("synonym", "EN", null));
			EncodedEntityStructure umlsEnc = prepareUMLS(umls);
			preprocessingTime += (System.currentTimeMillis()-umlsPreStart);
			long restLookup = System.currentTimeMillis();
			TokenSimilarityLookup.getInstance().computeTrigramLookup(set, umls, rep);
			System.out.println("lookup time:"+(System.currentTimeMillis()-restLookup));
			matchTime += (System.currentTimeMillis()-restLookup);
			log.info("calcualte umls lookup ready");
			Set<String> semTypesGraph = new HashSet<String> ();
			semTypesGraph.add("Qualitative Concept");semTypesGraph.add("Quantitative Concept");
			semTypesGraph.add("Conceptual Entity");semTypesGraph.add("Functional Concept");
			DirectedGraph<Node,Edge> graph = ReuseEvaluationWithContext.getGraph(clusterConfigName, clusterMap, semTypesGraph, rep);
			
			
			//DirectedGraph<Node,Edge> ontologyGraph = ReuseEvaluationWithContext.getSemanticTypeGraph(annoMap, rep);
			DirectedGraph<Node,Edge> ontologyGraph = ReuseEvaluationWithContext.getOntologyGraph(annoMap, ontology, context_depth, umls, rep);
			log.info("coocurrence count "+graph.getVertexCount()+"edges: "+graph.getEdgeCount());
			log.info("semantic network count: "+ ontologyGraph.getVertexCount()+"edges: "+ ontologyGraph.getEdgeCount());
			DirectedGraph<Node,Edge> unionGraph = GraphOperator.union(graph, ontologyGraph);
			
			GraphBasedSelection graphBasedSelection = SelectionFactory.getInstance().getGraphSelectionOperator(SelectionType.COOCCURRENCE_SELECTION);
			Selection groupSelection = SelectionFactory.getInstance().getSelectionOperator(SelectionType.GROUPSELECTION);
			
			log.debug("start selection");
			selectionTime =0;
			for (EncodedEntityStructure ees1 : encSet){
				if (onlySelectedForms){
					if (selForms.contains(ees1.getStructureId())){
						AnnotationMapping reuseAnno = annoMap.get(ees1.getStructureId());
						int sizeOnt = size+umlsEnc.getObjIds().size();
						long restMatchTime = System.currentTimeMillis();
						AnnotationMapping restAnno = getRestAnnoations(reuseAnno,umlsEnc, umls, ees1,map.get(ees1.getStructureId()),
								formProperties, umlsProperties, sizeOnt, rep);
						/*
						 * filtering general correspondences from mapping 
						 */
						Set<EntityAnnotation> remAnno = new HashSet<EntityAnnotation>();
						for (EntityAnnotation ea: restAnno.getAnnotations()){
							if (toFilteringConceptIds.contains(ea.getTargetId())){
								remAnno.add(ea);
							}
						}
						for(EntityAnnotation ea: remAnno){
							restAnno.removeAnnotation(ea.getSrcId(), ea.getTargetId());
						}
						matchTime +=(System.currentTimeMillis()-restMatchTime);
						long selStart = System.currentTimeMillis();
						restAnno  =groupSelection.select(restAnno, ees1, umlsEnc, formProperties, umlsProperties, 0.35f, 0,
										avgEntitySize, rep);
						reuseAnno = graphBasedSelection.selectAnnotationMapping(unionGraph, reuseAnno, ees1,
								umlsEnc, formProperties, gp, 0.35f, 0,avgEntitySize, rep);
						selectionTime +=(System.currentTimeMillis()-selStart);
						//TODO only reuse
						AnnotationMapping overallMapping = SetAnnotationOperator.union(AggregationFunction.MAX, reuseAnno, restAnno);
						annoMap.put(ees1.getStructureId(), overallMapping);
					}
				}
			}
			
			long endTime = System.currentTimeMillis();
			System.out.println(((endTime-startTime)/1000/60));
		
			/*
			 * evaluation
			 */
			MappingEvaluation eval = new MappingEvaluation();
			for (Entry<Integer,AnnotationMapping> entry :annoMap.entrySet()){
				if (onlySelectedForms){
					if (selForms.contains(entry.getKey())){
						AnnotationMapping am = entry.getValue();
						for (EntityAnnotation ea: am.getAnnotations()){
							calculatedMapping.addAnnotation(ea);
						}
						EntityStructureVersion esv = map.get(entry.getKey());
						String name= esv.getMetadata().getName()+"["+esv.getMetadata().getTopic()+"]-"
								+ontology.getName()+"["+ontology.getTopic()+"]_odm";
						AnnotationMapping am1 = rep.getMappingManager().getAnnotationMapping(esv.getMetadata(),
								ontology, name);
						for (GenericEntity ge : esv.getEntities()){
							Set<Integer> targetIds = calculatedMapping.getCorrespondingTargetIds(ge.getId());
							if (am1.getCorrespondingTargetIds(ge.getId()).isEmpty()&&!targetIds.isEmpty()){
								
								for (int tid : targetIds){
									am.removeAnnotation(ge.getId(),tid);
									calculatedMapping.removeAnnotation(ge.getId(), tid);
								}
							}
						}
						if (filterGeneralConcepts){
							Set<EntityAnnotation> remAnno = new HashSet<EntityAnnotation>();
							for (EntityAnnotation ea: am1.getAnnotations()){
								if (toFilteringConceptIds.contains(ea.getTargetId())){
									remAnno.add(ea);
								}
							}
							for(EntityAnnotation ea: remAnno){
								am1.removeAnnotation(ea.getSrcId(), ea.getTargetId());
							}
						}
						
						EvaluationResult er = eval.getResult(am, am1, "eligibility forms", ontology.getName());
						EvaluationResultWriter erw = new EvaluationResultWriter();
						Set<GenericProperty> showProp = esv.getAvailableProperties("question", "EN", null);
						Set<GenericProperty> umlsProps = umls.getAvailableProperties("synonym", "EN", null);
						String fileName = esv.getMetadata().getName().replaceAll("/", "_");
						//erw.writeEvaluationDetail("evalresults/QA/"+fileName, esv, idClusterMap, showProp.iterator().next(), gp.iterator().next(), er);
						//erw.writeEvaluationDetail("evalresults/QA/"+esv.getMetadata().getName(), esv,umls,showProp.iterator().next(), umlsProps.iterator().next(), er);
						log.debug("precision: " +er.getMeasures().get("precision"));
						log.debug("recall: " +er.getMeasures().get("recall"));
						log.debug("f-measure: " +er.getMeasures().get("fmeasure"));
					}
				}
			}
			/*
			 * retrieve reference mapping
			 */
			Set<Integer> questionIds = new HashSet<Integer>();
			AnnotationMapping referenceMapping = new AnnotationMapping();
			 set = rep.getFormManager().getStructureVersionsByType(types);
			
			 int notAnnotated =0;
			 EntitySet<GenericEntity> eset = new GenericEntitySet();
			 for (EntityStructureVersion esv : set){
				if (onlySelectedForms){
					if (selForms.contains(esv.getStructureId())){
						String name= esv.getMetadata().getName()+"["+esv.getMetadata().getTopic()+"]-"
								+ontology.getName()+"["+ontology.getTopic()+"]_odm";
						AnnotationMapping am1 = rep.getMappingManager().getAnnotationMapping(esv.getMetadata(),
								ontology, name);
						for (GenericEntity ge : esv.getEntities()){
							Set<Integer> targetIds = calculatedMapping.getCorrespondingTargetIds(ge.getId());
							questionIds.add(ge.getId());
							if (am1.getCorrespondingTargetIds(ge.getId()).isEmpty()&&!targetIds.isEmpty()){
								questionIds.remove(ge.getId());
								for (int tid : targetIds){
									calculatedMapping.removeAnnotation(ge.getId(), tid);
									notAnnotated ++;
								}
							}
							eset.addEntity(ge);
						}
						if (filterGeneralConcepts){
							Set<EntityAnnotation> remAnno = new HashSet<EntityAnnotation>();
							for (EntityAnnotation ea: am1.getAnnotations()){
								if (toFilteringConceptIds.contains(ea.getTargetId())){
									remAnno.add(ea);
								}
							}
							for(EntityAnnotation ea: remAnno){
								am1.removeAnnotation(ea.getSrcId(), ea.getTargetId());
							}
						}
						referenceMapping = SetAnnotationOperator.union(AggregationFunction.MAX, am1, referenceMapping);
					}
				}
			}
			 log.info("not annotated items: "+notAnnotated);
			 log.info("reference annoations: "+referenceMapping.getNumberOfAnnotations());
			 log.info(calculatedMapping.getNumberOfAnnotations());
			EvaluationResult er = eval.getResult(calculatedMapping, referenceMapping,
					"eligibility forms", ontology.getName(),questionIds);
			System.out.println(er.getMeasures().get("precision"));
			System.out.println(er.getMeasures().get("recall"));
			System.out.println(er.getMeasures().get("fmeasure"));
			log.info(er.getMeasures().get("minimalCoverage"));
			log.info("preprocessing\t"+preprocessingTime);
			log.info("identification\t"+matchTime);
			log.info("selection\t"+selectionTime);
			
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
		} catch (GraphAPIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();	
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClusterAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClusterNotExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MatchingExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (EntityAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PreprocessingException e) {
			e.printStackTrace();
		}


	}
	
	public static DirectedGraph<Node,Edge> getGraph(String clusterName,
			Map<GenericEntity,AnnotationCluster> clusterMap,Set<String> semTypes,
			FormRepository rep){
		Map<GenericEntity,AnnotationCluster> filterMap = new HashMap<>();
			for (GenericEntity ge:clusterMap.keySet()){
				List<String> values = ge.getPropertyValues("sem_type", null, null);
				boolean found =false;
				for (String v :values){
					if (semTypes.contains(v)){
						found =true;
						break;
					}
				}
				if (!found){
					filterMap.put(ge, clusterMap.get(ge));
				}
			}
			
			CooccurenceCalculator calc = new CooccurenceCalculator();
			DirectedGraph<Node, Edge> graph = calc.calculateCoocurence(filterMap, clusterName,3, rep);
			return graph;
	}
	
	
	
	public static DirectedGraph <Node,Edge> getSemanticTypeGraph(Map<Integer,AnnotationMapping> annoMap,FormRepository rep) throws EntityAPIException,
	VersionNotExistsException, StructureBuildException, GraphAPIException{
		EntitySet <GenericEntity> nodes = new GenericEntitySet();
		Set<Integer> ids =new HashSet<Integer>();
		for (AnnotationMapping am : annoMap.values()){
			for (EntityAnnotation ea : am.getAnnotations()){
				ids.add(ea.getTargetId());
			}
		}
		nodes= rep.getFormManager().getEntitiesByIdWithProperties(ids);
		
		EntityStructureVersion semTypeGraph = rep.getFormManager().getStructureVersion("semanticNetwork", "ontology", "2014-01-01");
		Set<GenericProperty> propSource = rep.getFormManager().getAvailableProperties("umls2014AB", "2014-01-01", "ontology");
		Set<GenericProperty> joinAtt = new HashSet<>();
		for (GenericProperty joinGp: propSource){
			if (joinGp.getName().equals("sem_type"))
				joinAtt.add(joinGp);
		}
		
		Set<GenericProperty> propTarget = semTypeGraph.getAvailableProperties("name", null, null);
		VersionMetadata vm = rep.getFormManager().getMetadata("semanticNetwork", "ontology", "2014-01-01");
		DirectedGraph<Node,Edge> g2 = rep.getGraphManager().getSubgraphFromExternalStructure(nodes, vm, joinAtt.iterator().next(),
				propTarget, 3);
		return g2;
	}
	
	public static DirectedGraph <Node,Edge> getOntologyGraph (Map<Integer,AnnotationMapping> am,VersionMetadata structure, int depth, EntityStructureVersion esv,
			FormRepository rep) throws GraphAPIException{
		EntitySet<GenericEntity> roots = new GenericEntitySet();
		for (AnnotationMapping m : am.values())
			for (EntityAnnotation ea: m.getAnnotations()){
				GenericEntity ge = esv.getEntity(ea.getTargetId());
				roots.addEntity(ge);
			}
		log.info("number of annotated concepts: "+roots.getSize());
		return rep.getGraphManager().getGraphFromStructure(roots, structure, depth);
		
	}
	
	static AnnotationMapping getRestAnnoations (AnnotationMapping am, EncodedEntityStructure umlsEncoded,
			EntityStructureVersion umls ,EncodedEntityStructure ees,EntityStructureVersion esv,Set<GenericProperty> formProperties,
			Set<GenericProperty> targetProperties, int size,FormRepository rep){
		Set<Integer> restEntities = new HashSet<Integer>();
		MatchOperator mop = new MatchOperator (RegisteredMatcher.SOFT_TFIDF_WND_MATCHER,AggregationFunction.MAX, formProperties, targetProperties, 0.8f);
		Map<String,Object> externalMap = new HashMap<String,Object>();
		externalMap.put(TFIDFWindowMatcher.WND_SIZE, 5);
		externalMap.put(TFIDFWindowMatcher.IS_ADAPTIVE_SIZE, false);
		Int2FloatMap idfMap = TFIDFTokenWeightGenerator.getInstance().generateIDFValuesForAllSources(size);
		externalMap.put(TFIDFWindowMatcher.IDF_MAP_SOURCE, idfMap);
		externalMap.put(TFIDFWindowMatcher.IDF_MAP_TARGET, idfMap);
		externalMap.put(TFIDFWindowMatcher.TFIDF_SOURCE_SEPARATED, false);
		Map <Integer,Set<Integer>> tokenSimLookup = TokenSimilarityLookup.getInstance().getLookup();
		externalMap.put(SoftTFIDFMatcher.LOOKUP, tokenSimLookup);
		mop.setGlobalObjects(externalMap);
		for (int entId :ees.getObjIds().keySet()){
			if (!am.containsCorrespondingTargetIds(entId)){
				restEntities.add(entId);
			}
		}
		log.info("item number"+esv.getTypeCount().get("item")+"not annotated entities"+ restEntities.size());
		ExecutionTree tree = new ExecutionTree();
		tree.addOperator(mop);
		EncodedEntityStructure restEes = EncodingManager.getInstance().getSubset(ees, restEntities);
		AnnotationMapping restMapping = null;
		try {
			restMapping = rep.getMatchManager().match(esv, restEes, umlsEncoded, umls, tree, null);
			log.info("rest mapping:"+ restMapping.getNumberOfAnnotations() +"for" +restEes.getObjIds().size());
		} catch (MatchingExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return restMapping;
	}
	
	static EncodedEntityStructure prepareUMLS(EntityStructureVersion umls){
		Set<GenericProperty> umlsProperties = umls.getAvailableProperties("name", "EN", null);
		umlsProperties.addAll(umls.getAvailableProperties("synonym", "EN", null));
		PreprocessorConfig umlsConfig = new PreprocessorConfig ();
		PreprocessProperty propSyn = new PreprocessProperty("synonym", "EN", null);
		PreprocessProperty propName = new PreprocessProperty("name", "EN", null);
		//formConfig.addPreprocessingStepForProperties(PreprocessingSteps.NUMBER_NORMALIZATION, propForms);
		umlsConfig.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, propSyn,propName);
		umlsConfig.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, propSyn,propName);
		//umlsConfig.addPreprocessingStepForProperties(PreprocessingSteps.NORMALIZE, propSyn,propName);
		PreprocessorExecutor executor = new PreprocessorExecutor();
		try {
			executor.preprocess(umls, umlsConfig);
		} catch (PreprocessingException e) {
			e.printStackTrace();
		}
		EncodedEntityStructure ees = EncodingManager.getInstance().encoding(umls, true);
		TFIDFTokenWeightGenerator.getInstance().initializeGlobalCount(ees, umlsProperties.toArray(new GenericProperty[]{}));
		return ees;
	}	
}
