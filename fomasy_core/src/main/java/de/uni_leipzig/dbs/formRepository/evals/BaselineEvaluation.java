package de.uni_leipzig.dbs.formRepository.evals;

import de.uni_leipzig.dbs.formRepository.importer.annotation.csv.AnnotationWriter;
import de.uni_leipzig.dbs.formRepository.matching.blocking.Blocking;
import de.uni_leipzig.dbs.formRepository.matching.blocking.data.BlockSet;
import de.uni_leipzig.dbs.formRepository.matching.blocking.token.CommonTokenBlocking;
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
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessingSteps;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorConfig;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorExecutor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.TokenWeighting.TFIDFTokenWeightGenerator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction.POSBasedExtractingPreprocessor;
import de.uni_leipzig.dbs.formRepository.matching.lookup.TokenSimilarityLookup;
import de.uni_leipzig.dbs.formRepository.matching.selection.Selection;
import de.uni_leipzig.dbs.formRepository.matching.selection.SelectionFactory;
import de.uni_leipzig.dbs.formRepository.matching.selection.SelectionType;
import de.uni_leipzig.dbs.formRepository.matching.token.SoftTFIDFMatcher;
import de.uni_leipzig.dbs.formRepository.matching.token.TFIDFWindowMatcher;
import de.uni_leipzig.dbs.formRepository.reuse.CooccurenceCalculator;
import edu.uci.ics.jung.graph.DirectedGraph;

public class BaselineEvaluation{
  public static String usage(){
    return "This class annotate a set of forms by reuse a set of annotation clusters"
            + " The application runs by command: java <rep.ini> cs=<clusterStructureName> ReuseClusterEvaluation";
  }
  static Logger log = Logger.getLogger(ReuseEvaluation.class);

  static boolean filterGeneralConcepts =true;
  static String[] generalConcepts = new String[]{"Qualitative Concept","Quantitative Concept",
          "Functional Concept","Conceptual Entity","Temporal Concept"};

/*
	static int[] selectedForms = new int[]{3,2,8,9,10,
		11,12,13,14,15,
		16,20,21,76,77,
		78,79,80,81,82,
	83,84,85,86,87};*/

  //static int[] selectedForms = new int[]{76};


  static int[] selectedForms = new int[]{1,2,7,8,9,
          10,11,12,13,14,
          15,19,20,75,76,
          77,78,79,80,81,
          82,83,84,85,86};

  //	439,440
//	461,455,456,457,458,459,464,466,467,468,465,463,462,452,453,454,469,470,460,473,475,476
//	static int[] selectedForms = new int[]{450, 451, 452, 453, 454, 455, 456, 457, 458, 459, 460, 461, 462, 463,
//	 464, 465, 466, 467, 468, 469, 470, 471, 472, 473,
//	 474, 475, 476, 477, 478, 479, 480, 481, 482};
//	static int[] selectedForms = new int[]{461,455,456,457,458,459,464,466,
//		467,468,465,463,462,452,453,454,469,470,460,473,475,476,439,440};
  static Set <Integer> selForms = new HashSet<Integer>();
  static String[] t = new String[]{"CD","FW","JJ","RBS","JJR","JJS","LS","NN","NNS","NNP","RB","RBS","SYM","IN"};
  static float threshold =0.6f;
  static int context_depth = 1;
  static boolean onlySelectedForms = true;
  private static long matchTime;
  private static long selectionTime;
  private static long preprocessingTime ;

  public static void main (String[] args){

    PropertyConfigurator.configure("log4j.properties");
    Properties prop = new Properties ();
    Set<String> wordTypes = new HashSet<String>();
    for(String i: t){
      wordTypes.add(i);
    }
    for (int f: selectedForms){
      selForms.add(f);
    }

    Set<String> semTypes = new HashSet<> ();
    for (String sem : generalConcepts){
      semTypes.add(sem);
    }

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
    try {
      FormRepository rep = new FormRepositoryImpl();
      rep.initialize(args[0]);

      HashMap<Integer,EntityStructureVersion> map = new HashMap<>();
      Set<String> types = new HashSet<String>();
      types.add("eligibility form");
//			types.add("quality assurance");
      Set<EntityStructureVersion> set = rep.getFormManager().getStructureVersionsByType(types);
      for (EntityStructureVersion esv:set){
        map.put(esv.getStructureId(), esv);
      }
      Set <String> entTypes = new HashSet<>();
      entTypes.add("item");
      Set<EncodedEntityStructure> encSet = new HashSet<>();
      Set<GenericProperty> formProperties = set.iterator().next().getAvailableProperties("question", "EN", null);
      formProperties.addAll(set.iterator().next().getAvailableProperties("name", null, null));


      PreprocessorConfig formConfig = new PreprocessorConfig ();
      PreprocessProperty propForms = new PreprocessProperty("question", "EN", null);
      PreprocessProperty propFormName = new PreprocessProperty("name", null, null);
      Map<String,Object> preMap = new HashMap<>();
      preMap.put(POSBasedExtractingPreprocessor.FILTER_TYPES, wordTypes);
      formConfig.setExternalSourceMap(preMap);

      formConfig.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, propForms,propFormName);
      formConfig.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, propForms,propFormName);
      formConfig.addPreprocessingStepForProperties(PreprocessingSteps.KEYWORD_EXTRACTION, propForms);
      formConfig.addPreprocessingStepForProperties(PreprocessingSteps.NORMALIZE, propForms,propFormName);
      //formConfig.addPreprocessingStepForProperties(PreprocessingSteps.NUMBER_NORMALIZATION, propForms,propFormName);
      PreprocessorExecutor executor = new PreprocessorExecutor();
      int size = 0;
      for (EntityStructureVersion esv: set){
        esv = executor.preprocess(esv, formConfig);
        size += esv.getTypeCount().get("item");

        EncodedEntityStructure ees = EncodingManager.getInstance().encoding(esv, entTypes, true);
        TFIDFTokenWeightGenerator.getInstance().initializeGlobalCount(ees, formProperties.toArray(
                new GenericProperty[]{}));
        encSet.add(ees);
      }
      log.info("#items:"+size);



      VersionMetadata ontology = rep.getFormManager().getMetadata("umls2014AB", "ontology", "2014-01-01");
      Set<Integer> toFilteringConceptIds = getGeneralConceptIds(rep, semTypes,ontology);

      AnnotationMapping calculatedMapping = new AnnotationMapping();
      Map<Integer,AnnotationMapping> annoMap = new HashMap<>();
      long umlsPreStart =System.currentTimeMillis();
      System.out.println ("before umls loading memory usage:" + Runtime.getRuntime().totalMemory());
      EntityStructureVersion umls = rep.getFormManager().getStructureVersion("umls2014AB", "ontology", "2014-01-01");
      Set<GenericProperty> umlsProperties = umls.getAvailableProperties("synonym", "EN", null);
      umlsProperties.addAll(umls.getAvailableProperties("name","EN",null));
      EncodedEntityStructure umlsEnc = prepareUMLS(umls, toFilteringConceptIds);
      preprocessingTime += (System.currentTimeMillis()-umlsPreStart);

      System.out.println ("preprocessing memory usage:" + Runtime.getRuntime().totalMemory());
      long restLookup = System.currentTimeMillis();
      TokenSimilarityLookup.getInstance().computeTrigramLookup(set, umls, rep);
      System.out.println("lookup time:"+(System.currentTimeMillis()-restLookup));
      System.out.println ("lookup memory usage:" + Runtime.getRuntime().totalMemory());
      matchTime += (System.currentTimeMillis()-restLookup);
      log.info("calcualte umls lookup ready");

      CommonTokenBlocking ctb = new CommonTokenBlocking();



      log.info("document size:"+(size+umlsEnc.getObjIds().size()));
      int sizeOnt = size+umlsEnc.getObjIds().size();
      Int2FloatMap idfMap = TFIDFTokenWeightGenerator.getInstance().generateIDFValuesForAllSources(sizeOnt);
      for (EncodedEntityStructure ees1 : encSet){
        if (onlySelectedForms){
          if (selForms.contains(ees1.getStructureId())){
            BlockSet blocks = null;
            //blocks = ctb.computeBlocks(ees1, umlsEnc, TokenSimilarityLookup.getLookup());

            long restMatchTime = System.currentTimeMillis();
            AnnotationMapping restAnno = getAnnoations(umlsEnc, umls, ees1,map.get(ees1.getStructureId()),
                    formProperties, umlsProperties, idfMap, blocks, rep);
						/*
						 * filtering general correspondences from mapping
						 */
            Set<EntityAnnotation> remAnno = new HashSet<>();
            for (EntityAnnotation ea: restAnno.getAnnotations()){
              if (toFilteringConceptIds.contains(ea.getTargetId())){
                remAnno.add(ea);
              }
            }
            for(EntityAnnotation ea: remAnno){
              restAnno.removeAnnotation(ea.getSrcId(), ea.getTargetId());
            }
            annoMap.put(ees1.getStructureId(), restAnno);
            matchTime +=(System.currentTimeMillis()-restMatchTime);

            log.info("#annotations before selection: "+restAnno.getNumberOfAnnotations());
          }
        }
      }

      //GraphBasedSelection graphBasedSelection = SelectionFactory.getInstance().getGraphSelectionOperator(SelectionType.COOCCURRENCE_SELECTION);
      //DirectedGraph<Node,Edge> ontologyGraph = ReuseEvaluationWithContext.getOntologyGraph(annoMap, ontology, context_depth, umls, rep);
      //log.info("semantic network count: "+ ontologyGraph.getVertexCount()+"edges: "+ ontologyGraph.getEdgeCount());
      Selection groupSelection = SelectionFactory.getInstance().getSelectionOperator(SelectionType.GROUPSELECTION);
      AnnotationWriter aw = new AnnotationWriter();
      selectionTime =0;
      for (EncodedEntityStructure ees1 : encSet) {
        if (selForms.contains(ees1.getStructureId())) {
          AnnotationMapping restAnno = annoMap.get(ees1.getStructureId());
          if (restAnno == null) {
            log.error(restAnno);
          }
          aw.writeAnnotation(map.get(ees1.getStructureId()),umls, restAnno, formProperties, umlsProperties,
                 "mappings/"+map.get(ees1.getStructureId()).getMetadata().getName()+"_3.csv");
          log.info("before selection:"+restAnno.getNumberOfAnnotations());
          restAnno = groupSelection.select(restAnno,ees1, umlsEnc, formProperties, null, 0.3f, 0, 2f, rep);
          log.info("after selection:"+restAnno.getNumberOfAnnotations());
          long selStart = System.currentTimeMillis();
          annoMap.put(ees1.getStructureId(), restAnno);
          selectionTime += (System.currentTimeMillis() - selStart);
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
            EntityStructureVersion esv = map.get(entry.getKey());
            String name= esv.getMetadata().getName()+"["+esv.getMetadata().getTopic()+"]-"
                    +ontology.getName()+"["+ontology.getTopic()+"]_odm";
            AnnotationMapping referenceMapping = rep.getMappingManager().getAnnotationMapping(esv.getMetadata(),
                    ontology, name);
            for (GenericEntity ge : esv.getEntities()){
              Set<Integer> targetIds = calculatedMapping.getCorrespondingTargetIds(ge.getId());
              if (referenceMapping.getCorrespondingTargetIds(ge.getId()).isEmpty()&&!targetIds.isEmpty()){
                for (int tid : targetIds){
                  am.removeAnnotation(ge.getId(),tid);
                  calculatedMapping.removeAnnotation(ge.getId(), tid);
                }
              }
            }
            for (EntityAnnotation ea: am.getAnnotations()){
              calculatedMapping.addAnnotation(ea);
            }
            aw.writeAnnotation(esv,umls, am, formProperties, umlsProperties, "mappings/"+esv.getMetadata().getName()+"_3_selection.csv");
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

      for (EntityStructureVersion esv : set){
        if (onlySelectedForms){
          if (selForms.contains(esv.getStructureId())){
            String name= esv.getMetadata().getName()+"["+esv.getMetadata().getTopic()+"]-"
                    +ontology.getName()+"["+ontology.getTopic()+"]_odm";
            AnnotationMapping referenceMappingPerForm = rep.getMappingManager().getAnnotationMapping(esv.getMetadata(),
                    ontology, name);
            for (GenericEntity ge : esv.getEntities()){
              Set<Integer> targetIds = calculatedMapping.getCorrespondingTargetIds(ge.getId());
              questionIds.add(ge.getId());
              if (referenceMappingPerForm.getCorrespondingTargetIds(ge.getId()).isEmpty()&&!targetIds.isEmpty()){
                questionIds.remove(ge.getId());
                for (int tid : targetIds){
                  calculatedMapping.removeAnnotation(ge.getId(), tid);
                  notAnnotated ++;
                }
              }
            }
            if (filterGeneralConcepts){
              Set<EntityAnnotation> remAnno = new HashSet<>();
              for (EntityAnnotation ea: referenceMappingPerForm.getAnnotations()){
                if (toFilteringConceptIds.contains(ea.getTargetId())){
                  remAnno.add(ea);
                }
              }
              for(EntityAnnotation ea: remAnno){
                referenceMappingPerForm.removeAnnotation(ea.getSrcId(), ea.getTargetId());
              }
            }
            referenceMapping = SetAnnotationOperator.union(AggregationFunction.MAX, referenceMappingPerForm, referenceMapping);
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
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InitializationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
   } catch (MatchingExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (EntityAPIException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
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

  public static DirectedGraph <Node,Edge> getSemanticTypeGraph(Map<Integer,AnnotationMapping> annoMap,FormRepository rep)
          throws EntityAPIException, VersionNotExistsException, StructureBuildException, GraphAPIException{
    EntitySet <GenericEntity> nodes;
    Set<Integer> ids =new HashSet<Integer>();
    for (AnnotationMapping am : annoMap.values()){
      for (EntityAnnotation ea : am.getAnnotations()){
        ids.add(ea.getTargetId());
      }
    }
    nodes= rep.getFormManager().getEntitiesByIdWithProperties(ids);
    EntityStructureVersion semTypeGraph = rep.getFormManager().getStructureVersion("semanticNetwork", "ontology", "2014-01-01");
    Set<GenericProperty> propSource = rep.getFormManager().getAvailableProperties("umls2014AB", "2014-01-01", "ontology");
    Set<GenericProperty> joinAtt = new HashSet<GenericProperty>();
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

  static Set<Integer> getGeneralConceptIds (FormRepository rep, Set<String> semTypes, VersionMetadata ontology) throws EntityAPIException {
    Set<Integer> toFilteringConceptIds = new HashSet<>();
    Set<GenericProperty> umlsProperty = rep.getFormManager().getAvailableProperties("umls2014AB", "2014-01-01", "ontology");
    Set<GenericProperty> semProperty = new HashSet<>();
    for (GenericProperty gp : umlsProperty){
      if (gp.getName().equals("sem_type"))
        semProperty.add(gp);
    }
    EntitySet<GenericEntity> generalConceptEntities = rep.getFormManager()
            .getEntitiesByPropertyWithProperties(semTypes, ontology, semProperty);

    for (GenericEntity ge:generalConceptEntities){
      toFilteringConceptIds.add(ge.getId());
    }
    log.info("number of general concepts: "+ toFilteringConceptIds.size());
    return toFilteringConceptIds;
  }

  static AnnotationMapping getAnnoations(EncodedEntityStructure umlsEncoded,
                                         EntityStructureVersion umls, EncodedEntityStructure ees, EntityStructureVersion esv, Set<GenericProperty> formProperties,
                                         Set<GenericProperty> targetProperties, Int2FloatMap idfMap, BlockSet blocks, FormRepository rep){
    MatchOperator mop = new MatchOperator (RegisteredMatcher.SOFT_TFIDF_WND_MATCHER,AggregationFunction.MAX,
            formProperties, targetProperties, threshold);
    Map<String,Object> externalMap = new HashMap<String,Object>();
    //externalMap.put(Blocking.BLOCKING_FIELD, blocks);
    externalMap.put(TFIDFWindowMatcher.WND_SIZE, 5);
    externalMap.put(TFIDFWindowMatcher.IS_ADAPTIVE_SIZE, false);
    externalMap.put(TFIDFWindowMatcher.IDF_MAP_SOURCE, idfMap);
    externalMap.put(TFIDFWindowMatcher.IDF_MAP_TARGET, idfMap);
    externalMap.put(TFIDFWindowMatcher.TFIDF_SOURCE_SEPARATED, false);
    Map <Integer,Set<Integer>> tokenSimLookup = TokenSimilarityLookup.getInstance().getLookup();
    externalMap.put(SoftTFIDFMatcher.LOOKUP, tokenSimLookup);
    mop.setGlobalObjects(externalMap);
    ExecutionTree tree = new ExecutionTree();
    tree.addOperator(mop);
    AnnotationMapping restMapping = null;
    try {
      restMapping = rep.getMatchManager().match(esv, ees, umlsEncoded, umls, tree, null);
    } catch (MatchingExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return restMapping;
  }

  static EncodedEntityStructure prepareUMLS(EntityStructureVersion umls, Set<Integer> generalConcepts){
    Set<GenericProperty> umlsProperties = umls.getAvailableProperties("name", "EN", null);
    umlsProperties.addAll(umls.getAvailableProperties("synonym", "EN", null));
    PreprocessorConfig umlsConfig = new PreprocessorConfig ();
    PreprocessProperty propSyn = new PreprocessProperty("synonym", "EN", null);
    PreprocessProperty propName = new PreprocessProperty("name", "EN", null);
    for (Integer gc: generalConcepts){
      umls.removeEntity(gc);
    }
    umlsConfig.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, propSyn,propName);
    umlsConfig.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, propSyn,propName);
    umlsConfig.addPreprocessingStepForProperties(PreprocessingSteps.NORMALIZE, propSyn,propName);
    //umlsConfig.addPreprocessingStepForProperties(PreprocessingSteps.NUMBER_NORMALIZATION, propSyn,propName);
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
