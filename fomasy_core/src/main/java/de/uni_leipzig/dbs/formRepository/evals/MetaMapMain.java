package de.uni_leipzig.dbs.formRepository.evals;

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

import de.uni_leipzig.dbs.formRepository.operation.SetAnnotationOperator;
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
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.evals.calculation.EvaluationResult;
import de.uni_leipzig.dbs.formRepository.evals.calculation.MappingEvaluation;
import de.uni_leipzig.dbs.formRepository.evals.io.EvaluationResultWriter;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;


public class MetaMapMain {

  static Logger log = Logger.getLogger(MetaMapMain.class);
  
  
  public static void main (String[] args) throws IOException, EntityAPIException{
    PropertyConfigurator.configure("log4j.properties");
    FormRepository rep = new FormRepositoryImpl();
    String[] generalConcepts = new String[]{"Qualitative Concept","Quantitative Concept",
      "Functional Concept","Temporal Concept","Conceptual Entity"};
    Set<String> semValues = new HashSet<String>();
    for (String sem: generalConcepts){
      semValues.add(sem);
    }
    
    
    try {
      
      rep.initialize(args[0]);
      Set<GenericProperty> umlsProperty = rep.getFormManager().getAvailableProperties("umls2014AB", "2014-01-01", "ontology");
      Set<GenericProperty> semProperty = new HashSet<GenericProperty>();
      for (GenericProperty gp : umlsProperty){
        semProperty.add(gp);
      }
      VersionMetadata vm = rep.getFormManager().getMetadata("umls2014AB", "ontology", "2014-01-01");
      EntitySet<GenericEntity> generalConceptEntities = rep.getFormManager().getEntitiesByPropertyWithProperties(semValues, vm, semProperty);
      Set<Integer> toFilteringConceptIds = new HashSet<Integer>();
      for (GenericEntity ge:generalConceptEntities){
        toFilteringConceptIds.add(ge.getId());
      }
      log.info("number of general concepts: "+ toFilteringConceptIds.size());
      
//      int[] forms = new int[]{461,455,456,457,458,459,464,466,
//        467,468,465,463,462,452,453,454,469,470,460,473,475,476,439,440};
      Integer [] forms = new Integer[]{1,2,7,8,9,
            10,11,12,13,14,
            15,19,20,75,76,
            77,78,79,80,81,
            82,83,84,85,86};
      Set <Integer> selForms = new HashSet<Integer>();
      for (int i : forms)selForms.add(i);
      Set <String> set = new HashSet<String>();
      set.add("eligibility form");
      Set <EntityStructureVersion> formStructures = rep.getFormManager().getStructureVersionsByType(set);
      Properties metaProp = new Properties ();
      metaProp.put(de.uni_leipzig.dbs.formRepository.matching.metaMap.MetaMapWrapper.HOST, "localhost");
      metaProp.put(de.uni_leipzig.dbs.formRepository.matching.metaMap.MetaMapWrapper.PORT, 8066);
      metaProp.put(de.uni_leipzig.dbs.formRepository.matching.metaMap.MetaMapWrapper.THRESHOLD, 700);
      metaProp.put(de.uni_leipzig.dbs.formRepository.matching.metaMap.MetaMapWrapper.OPTIONS, "-y -D");
      Set<GenericProperty> propertySet = formStructures.iterator().next().getAvailableProperties("question", "EN", null);
      Set<String> entTypes = new HashSet<String>();
      entTypes.add("item");
      AnnotationMapping am = new AnnotationMapping ();
      
      for (EntityStructureVersion esv : formStructures){
        if (selForms.contains(esv.getStructureId())){
          AnnotationMapping iAm = rep.getMatchManager().matchByMetaMap(esv, entTypes, propertySet, metaProp);
          Set<EntityAnnotation> remAnno = new HashSet<EntityAnnotation>();
          /*
           * filtering general concept correspondences from computed mapping
           */
          for (EntityAnnotation ea: iAm.getAnnotations()){
            if (toFilteringConceptIds.contains(ea.getTargetId())){
              remAnno.add(ea);
            }
          }
          for(EntityAnnotation ea: remAnno){
            iAm.removeAnnotation(ea.getSrcId(), ea.getTargetId());
          }
          
          am = SetAnnotationOperator.union(AggregationFunction.MAX, am, iAm);
        }  
      }
      
      AnnotationMapping referenceMapping = new AnnotationMapping();
    
      for (EntityStructureVersion esv: formStructures){
        if (selForms.contains(esv.getStructureId())){
          String name= esv.getMetadata().getName()+"["+esv.getMetadata().getTopic()+"]-"
              +vm.getName()+"["+vm.getTopic()+"]_odm";
          AnnotationMapping am1 = rep.getMappingManager().getAnnotationMapping(esv.getMetadata(),vm , name);
          Set<EntityAnnotation> remAnno = new HashSet<EntityAnnotation>();
          /*
           * filtering general concept correspondences from reference mapping
           */
          for (EntityAnnotation ea: am1.getAnnotations()){
            if (toFilteringConceptIds.contains(ea.getTargetId())){
              remAnno.add(ea);
            }
          }
          for(EntityAnnotation ea: remAnno){
            am1.removeAnnotation(ea.getSrcId(), ea.getTargetId());
          }
          referenceMapping = SetAnnotationOperator.union(AggregationFunction.MAX, referenceMapping, am1);
        }
      }
      
      log.info(referenceMapping.getNumberOfAnnotations());
      Set <EntityAnnotation> remAnns = new HashSet<EntityAnnotation> ();
      for (EntityAnnotation ea: am.getAnnotations()) {
        int srcId = ea.getSrcId();
        if (!referenceMapping.containsCorrespondingTargetIds(srcId)){
          remAnns.add(ea);
        }
      }
      log.info("remove annos:"+ remAnns.size());
      
      for (EntityAnnotation ea: remAnns){
        am.removeAnnotation(ea.getSrcId(), ea.getTargetId());
      }
      
      MappingEvaluation eval = new MappingEvaluation();
      EvaluationResult er = eval.getResult(am, referenceMapping, "eligibility forms", vm.getName());
      EvaluationResultWriter erw = new EvaluationResultWriter();
      //erw.writeEvaluationDetail("evalresults/"+esv.getMetadata().getName(), esv, idClusterMap, showProp.iterator().next(), umlsProps.iterator().next(), er);
    //  erw.writeEvaluationDetail("evalresults/"+esv.getMetadata().getName(), esv,umls,showProp.iterator().next(), umlsProps.iterator().next(), er);
      System.out.println((am.getNumberOfAnnotations()));
      System.out.println(er.getMeasures().get("precision"));
      System.out.println(er.getMeasures().get("recall"));
      System.out.println(er.getMeasures().get("fmeasure"));
      
      
      
    } catch (InstantiationException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (IllegalAccessException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (ClassNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (InitializationException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (VersionNotExistsException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (StructureBuildException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
  }
}
