package de.uni_leipzig.dbs.formRepository.evals;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;



import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationCluster;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;

import de.uni_leipzig.dbs.formRepository.exception.ClusterAPIException;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessingSteps;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorConfig;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorExecutor;
import de.uni_leipzig.dbs.formRepository.matching.lookup.TokenSimilarityLookup;
import de.uni_leipzig.dbs.formRepository.reuse.AnnotationClusterIdentifier;
import de.uni_leipzig.dbs.formRepository.util.DateFormatter;

public class ClusterCalculationMain {
  
  //static float sampleDefaultRatio = 2f/3f;

  public static String usage(){
    return "run the cluster calculation by command: java -<repository.ini> cn=<clusterStructureName> sr=<x.xf> t=<form type>";
  }
  
  public static void main (String[] args){
    Logger log = Logger.getLogger(ClusterCalculationMain.class);
    String type =null;
    String clusterName =null;
    float sampleRatio = 1f/2f;
    if (args.length ==4){
      if (args[1].startsWith("cn=")){
        clusterName = args[1].replace("cn=", "").trim();
      }else {
        System.out.println(usage());
        System.exit(1);
      }
      
      if (args[2].startsWith("sr=")){
        String n  = args[2].replace("sr=", "").trim();
        sampleRatio = Float.parseFloat(n);
      }else {
        System.out.println(usage());
        System.exit(1);
      }
      if (args[3].startsWith("t=")){
        String n  = args[3].replace("t=", "").trim();
        type = n.replaceAll("_", " ");
      }else {
        System.out.println(usage());
        System.exit(1);
      }
      
    }else {
      System.out.println(usage());
      System.exit(1);
    }
    
    
    AnnotationClusterIdentifier identification = new AnnotationClusterIdentifier();
    
    FormRepository rep = new FormRepositoryImpl();
    
    PropertyConfigurator.configure("log4j.properties");
    try {
    rep.initialize(args[0]);
    Set<String> types = new HashSet<String>();
    types.add(type);
    Set<EntityStructureVersion> set = rep.getFormManager().getStructureVersionsByType(types);
    int removeCount = Math.round(set.size()*(1-sampleRatio));
    Set<Integer> evalSet = new HashSet<Integer>();
    for (int i= 0;i<removeCount;i++){
      EntityStructureVersion esv = set.iterator().next();
      set.remove(esv);
      evalSet.add(esv.getStructureId());
    }
    
    log.info(evalSet.toString());
    
    
    HashMap<Integer,EntityStructureVersion> map = new HashMap<Integer,EntityStructureVersion>();
    Set <VersionMetadata> setMeta = new HashSet<VersionMetadata> ();
    for (EntityStructureVersion esv:set){
      map.put(esv.getStructureId(), esv);
      setMeta.add(esv.getMetadata());
    }
    log.info(map.size());
    log.info(map.keySet().toString());
    Set<GenericProperty> formProperties = set.iterator().next().getAvailableProperties("question", "EN", null);
  
    PreprocessorConfig formConfig = new PreprocessorConfig ();
    PreprocessorConfig ontologyConfig = new PreprocessorConfig ();
    PreprocessProperty propForms = new PreprocessProperty("question", "EN", null);
    PreprocessProperty propFormName = new PreprocessProperty("name", "EN", null);
    PreprocessProperty propName = new PreprocessProperty("name","EN",null);
    PreprocessProperty propSyn = new PreprocessProperty ("synonym","EN",null);
    PreprocessProperty propDef = new PreprocessProperty ("definition","EN",null);
    PreprocessProperty propST = new PreprocessProperty ("sem_type","EN",null);
    formConfig.addPreprocessingStepForProperties(PreprocessingSteps.NORMALIZE, propForms,propFormName);
    formConfig.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, propForms,propFormName);
    formConfig.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, propForms,propFormName);
    //formConfig.addPreprocessingStepForProperties(PreprocessingSteps.NUMBER_NORMALIZATION, propForms,propFormName);
    //formConfig.addPreprocessingStepForProperties(PreprocessingSteps.LENGTH_FILTER, propForms,propFormName);
    ontologyConfig.addPreprocessingStepForProperties(PreprocessingSteps.NORMALIZE, propName,propSyn,propDef);
    ontologyConfig.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, propName,propSyn,propDef);
    ontologyConfig.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, propName,propSyn,propDef);
    //ontologyConfig.addPreprocessingStepForProperties(PreprocessingSteps.NUMBER_NORMALIZATION, propName,propSyn,propDef);
    //ontologyConfig.addPreprocessingStepForProperties(PreprocessingSteps.LENGTH_FILTER, propName,propSyn,propDef);
    
    VersionMetadata ontology;
    Set<GenericProperty> props = rep.getFormManager().getAvailableProperties("umls2014AB","2014-01-01","ontology");
    Set<GenericProperty> removeProp = new HashSet<GenericProperty>();
    for (GenericProperty gp : props){
      if (gp.getName().equals("definition")){
        removeProp.add(gp);
      }
    }
    for (GenericProperty gp: removeProp){
      props.remove(gp);
    }
    
      ontology = rep.getFormManager().getMetadata("umls2014AB", "ontology", "2014-01-01");
      Map<GenericEntity, EntitySet<GenericEntity>> annotationCluster = 
          identification.detemermineCluster(setMeta, ontology, rep);
      
      EntitySet<GenericEntity> target = new GenericEntitySet();
      for (GenericEntity ge: annotationCluster.keySet()){
        target.addEntity(ge);
      }
      EntitySet<GenericEntity> src = new GenericEntitySet();
      for (EntitySet<GenericEntity> annSet: annotationCluster.values()){
        for (GenericEntity ge:annSet){
          src.addEntity(ge);
        }
      }
      VersionMetadata formMeta = set.iterator().next().getMetadata();
      Set<GenericProperty> srcProps = rep.getFormManager().getAvailableProperties(formMeta.getName(),
          DateFormatter.getFormattedDate(formMeta.getFrom()),formMeta.getTopic());
      PreprocessorExecutor exec = new PreprocessorExecutor();
      src = exec.preprocess(src, formConfig);
      target = exec.preprocess(target, ontologyConfig);
      TokenSimilarityLookup.getInstance().computeTrigramLookup(src, target, srcProps, props, rep);
      Map<Integer,Set<Integer>> lookup = TokenSimilarityLookup.getInstance().getLookup();
      Map<GenericEntity,AnnotationCluster> resultCluster = new HashMap<GenericEntity,AnnotationCluster>();
      System.out.println("identify cluster");
      resultCluster = identification.identifyRepresentants(formProperties, props, annotationCluster, formConfig, ontologyConfig,
          lookup,resultCluster);
      rep.getClusterManager().importClusters(resultCluster, clusterName);
    } catch (VersionNotExistsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (EntityAPIException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClusterAPIException e) {
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
    
  }
  
   
}
