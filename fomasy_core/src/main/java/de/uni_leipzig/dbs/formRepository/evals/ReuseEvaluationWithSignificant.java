package de.uni_leipzig.dbs.formRepository.evals;

import de.uni_leipzig.dbs.formRepository.matching.preprocessing.exception.PreprocessingException;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.execution.RegisteredMatcher;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.ExecutionTree;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.MatchOperator;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.EntityStructureFilter;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessingSteps;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorConfig;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorExecutor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.TokenWeighting.TFIDFTokenWeightGenerator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.lookup.TokenSimilarityLookup;
import de.uni_leipzig.dbs.formRepository.matching.selection.GroupSelection;
import de.uni_leipzig.dbs.formRepository.matching.selection.Selection;
import de.uni_leipzig.dbs.formRepository.matching.selection.SelectionFactory;
import de.uni_leipzig.dbs.formRepository.matching.selection.SelectionType;
import de.uni_leipzig.dbs.formRepository.matching.token.SoftTFIDFMatcher;
import de.uni_leipzig.dbs.formRepository.matching.token.TFIDFWindowMatcher;
import de.uni_leipzig.dbs.formRepository.reuse.AnnotationClusterTransformer;

public class ReuseEvaluationWithSignificant {
  static boolean filterGeneralConcepts =true; 
  static String[] generalConcepts = new String[]{"Qualitative Concept","Quantitative Concept",
    "Functional Concept","Temporal Concept","Conceptual Entity"};
  
  static int[] selectedForms = new int[]{1,2,7,8,9,10,11,12,13,14,15,19,20,75,76,77,78,79,80,81,82,83,84,85,86};
  static Set <Integer> selForms = new HashSet<Integer>();
  
  
  static boolean onlySelectedForms = true;
  
  
  
  public static String usage(){
    return "This class annotate a set of forms by reuse a set of annotation clusters"
        + " The application runs by command: java <rep.ini> cs=<clusterStructureName> ReuseClusterEvaluation";
  }
  static Logger log = Logger.getLogger(ReuseEvaluation.class);
  public static void main (String[] args){
    
    GroupSelection.isCommonToken = true;
    
    Set<String> semTypes = new HashSet<String> ();
    for (String sem: generalConcepts){
      semTypes.add(sem);
    }
    
    for (int f: selectedForms){
      selForms.add(f);
    }
    
    FormRepository rep = new FormRepositoryImpl();
    PropertyConfigurator.configure("log4j.properties");
    String clusterConfigName = null;
    if (args.length ==2){
      clusterConfigName = args[1].replace("cn=", "").trim();
    }
    
    try {
      
      rep.initialize(args[0]);
      VersionMetadata ontology = rep.getFormManager().getMetadata("umls2014AB", "ontology", "2014-01-01");
      Set<String> types = new HashSet<String>();
      
      types.add("eligibility form");
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
      formMatchProperties.addAll(set.iterator().next().getAvailableProperties("name", null, null));
      
      
      PreprocessorConfig formConfig = new PreprocessorConfig ();
      PreprocessProperty propForms = new PreprocessProperty("question", "EN", null);
      PreprocessProperty propFormName = new PreprocessProperty("name", "EN", null);
      
      
      
      
      
      //formConfig.addPreprocessingStepForProperties(PreprocessingSteps.NUMBER_NORMALIZATION, propForms);
      formConfig.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, propForms,propFormName);
      formConfig.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, propForms,propFormName);
      PreprocessorExecutor executor = new PreprocessorExecutor();
      int size = 0;
      
      for (EntityStructureVersion esv: set){
        esv = executor.preprocess(esv, formConfig);
        size += esv.getTypeCount().get("item");
        EncodedEntityStructure ees = EncodingManager.getInstance().encoding(esv, true);
        TFIDFTokenWeightGenerator.getInstance().initializeGlobalCount(ees, formMatchProperties.toArray(
            new GenericProperty[]{}));
        encSet.add(ees);  
      }
      
      
      
      
      
      Map <GenericEntity, AnnotationCluster> clusterMap = rep.getClusterManager().getDeterminedClusters(clusterConfigName);
      
      /*filter */
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
      
      MatchOperator mop = new MatchOperator (RegisteredMatcher.SOFT_TFIDF_WND_MATCHER,AggregationFunction.MAX, formMatchProperties, gp, 0.6f);
      Map<String,Object> externalMap = new HashMap<String,Object>();
      externalMap.put(TFIDFWindowMatcher.WND_SIZE, 4);
      externalMap.put(TFIDFWindowMatcher.IS_ADAPTIVE_SIZE, false);
      Int2FloatMap idfMap = TFIDFTokenWeightGenerator.getInstance().generateIDFValuesForAllSources(size);
      externalMap.put(TFIDFWindowMatcher.IDF_MAP_SOURCE, idfMap);
      externalMap.put(TFIDFWindowMatcher.IDF_MAP_TARGET, idfMap);
      externalMap.put(TFIDFWindowMatcher.TFIDF_SOURCE_SEPARATED, false);
      log.info("calculates token similarity...");
      TokenSimilarityLookup.getInstance().computeTrigramLookup(set, clusterESV, rep);
      log.info("token lookup built");
      Map <Integer,Set<Integer>> tokenSimLookup = TokenSimilarityLookup.getInstance().getLookup();
      externalMap.put(SoftTFIDFMatcher.LOOKUP, tokenSimLookup);
      mop.setGlobalObjects(externalMap);
      Selection sel = SelectionFactory.getInstance().getSelectionOperator(SelectionType.GROUPSELECTION);
      AnnotationMapping calculatedMapping = new AnnotationMapping();
      Map<Integer,AnnotationCluster> idClusterMap = new HashMap<Integer,AnnotationCluster>();
      
      
      for (AnnotationCluster ac: clusterMap.values()){
        idClusterMap.put(ac.getId(), ac);
      }
      
      Map<Integer,AnnotationMapping> annoMap = new HashMap<Integer,AnnotationMapping>();
      
      EntityStructureFilter filter = new EntityStructureFilter ();
      
      for (EncodedEntityStructure ees : encSet){
        if (onlySelectedForms){
          if (selForms.contains(ees.getStructureId())){
            ExecutionTree tree = new ExecutionTree();
            tree.addOperator(mop);
            //EntityStructureVersion esv = map.get(ees.getStructureId());
            AnnotationMapping am = rep.getMatchManager().match(ees, encodedCluster, tree, null);
            annoMap.put(ees.getStructureId(), am);
            ees = filter.filterStructureByClusters(ees,  encodedCluster, gp, am);
          }
        }else {
          ExecutionTree tree = new ExecutionTree();
          tree.addOperator(mop);
          EntityStructureVersion esv = map.get(ees.getStructureId());
          AnnotationMapping am = rep.getMatchManager().match(ees, encodedCluster, tree, null);
          annoMap.put(ees.getStructureId(), am);
          ees = filter.filterStructureByClusters(ees,  encodedCluster, gp, am);
        }
      }
      
      
      size = encodedCluster.getObjIds().size();
      log.debug("start selection");
      for (EncodedEntityStructure ees1 : encSet){
        if (onlySelectedForms){
          if (selForms.contains(ees1.getStructureId())){
            EntityStructureVersion esv = map.get(ees1.getStructureId());
            AnnotationMapping reuseAnno = annoMap.get(ees1.getStructureId());
            reuseAnno = sel.select(reuseAnno, ees1, encodedCluster, formMatchProperties, gp, 0.35f, 0,1, rep);
            annoMap.put(ees1.getStructureId(), reuseAnno);
            log.debug(reuseAnno.getName() +"#annos "+reuseAnno.getNumberOfAnnotations());
          }
        }else{
          EntityStructureVersion esv = map.get(ees1.getStructureId());
          AnnotationMapping reuseAnno = annoMap.get(ees1.getStructureId());
          reuseAnno = sel.select(reuseAnno, ees1, encodedCluster, formMatchProperties, gp, 0.35f, 0,1, rep);
          annoMap.put(ees1.getStructureId(), reuseAnno);
          log.debug(reuseAnno.getName() +"#annos "+reuseAnno.getNumberOfAnnotations());
        }
      }
      
      
      /*
       * evaluation
       */
      
      MappingEvaluation eval = new MappingEvaluation();
      for (Entry<Integer,AnnotationMapping> entry :annoMap.entrySet()){
        if (selForms.contains(entry.getKey())){
          AnnotationMapping am = entry.getValue();
          for (EntityAnnotation ea: am.getAnnotations()){
            calculatedMapping.addAnnotation(ea);
          }
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
          if (filterGeneralConcepts){
            Set<EntityAnnotation> remAnno = new HashSet<EntityAnnotation>();
            for (EntityAnnotation ea: referenceMapping.getAnnotations()){
              if (generalConcepts.contains(ea.getTargetId())){
                remAnno.add(ea);
              }
            }
            for(EntityAnnotation ea: remAnno){
              referenceMapping.removeAnnotation(ea.getSrcId(), ea.getTargetId());
            }
          }
          EvaluationResult er = eval.getResult(am, referenceMapping, "eligibility forms", ontology.getName());
          EvaluationResultWriter erw = new EvaluationResultWriter();
          Set<GenericProperty> showProp = esv.getAvailableProperties("question", "EN", null);
          //erw.writeEvaluationDetail("evalresults/"+esv.getMetadata().getName(), esv, idClusterMap, showProp.iterator().next(), gp.iterator().next(), er);
          log.debug(" form id: "+entry.getKey());
          log.debug("precision: " +er.getMeasures().get("precision"));
          log.debug("recall: " +er.getMeasures().get("recall"));
          log.debug("f-measure: " +er.getMeasures().get("fmeasure"));
        }
      }
      /*
       * retrieve reference mapping
       */
      AnnotationMapping referenceMapping = new AnnotationMapping();
       set = rep.getFormManager().getStructureVersionsByType(types);
      
       int notAnnotated =0;
       EntitySet<GenericEntity> eset = new GenericEntitySet();
       int corrsGeneral=0;
       for (EntityStructureVersion esv : set){
        if (selForms.contains(esv.getStructureId())){
          String name= esv.getMetadata().getName()+"["+esv.getMetadata().getTopic()+"]-"
              +ontology.getName()+"["+ontology.getTopic()+"]_odm";
          AnnotationMapping am1 = rep.getMappingManager().getAnnotationMapping(esv.getMetadata(),
              ontology, name);
          
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
          
          if (filterGeneralConcepts){
            Set<EntityAnnotation> remAnno = new HashSet<EntityAnnotation>();
            for (EntityAnnotation ea: am1.getAnnotations()){
              if (generalConcepts.contains(ea.getTargetId())){
                corrsGeneral++;
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
       System.out.println("general conecpt annotations: "+corrsGeneral);
       System.out.println("not annotated items: "+notAnnotated);
      System.out.println("reference annoations: "+referenceMapping.getNumberOfAnnotations());
      
      
    
      EvaluationResult er = eval.getResult(calculatedMapping, referenceMapping, "eligibility forms", ontology.getName());
      
      System.out.println(" threshold tfidf: "+0.4);
      System.out.println(calculatedMapping.getNumberOfAnnotations());
      System.out.println(er.getMeasures().get("precision"));
      System.out.println(er.getMeasures().get("recall"));
      System.out.println(er.getMeasures().get("fmeasure"));
    
        
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
    } catch (ClusterAPIException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClusterNotExistsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MatchingExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }// catch (IOException e) {
    catch (PreprocessingException e) {
      e.printStackTrace();
    }
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
  }
}
