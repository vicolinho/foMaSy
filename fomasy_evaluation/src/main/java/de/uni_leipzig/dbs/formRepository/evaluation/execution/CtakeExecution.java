package de.uni_leipzig.dbs.formRepository.evaluation.execution;

import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.dataModel.*;
import de.uni_leipzig.dbs.formRepository.evals.calculation.EvaluationResult;
import de.uni_leipzig.dbs.formRepository.evals.calculation.MappingEvaluation;
import de.uni_leipzig.dbs.formRepository.evaluation.exception.AnnotationException;
import de.uni_leipzig.dbs.formRepository.evaluation.tool.wrapper.AnnotationWrapper;
import de.uni_leipzig.dbs.formRepository.evaluation.tool.wrapper.CTakeWrapper;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.operation.SetAnnotationOperator;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;


import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by christen on 28.02.2017.
 */
public class CtakeExecution {

  public static final String PATH = "ctakes-clinical-pipeline/desc/analysis_engine";

  public static final String[] trials = new String[]{
    "NCT00168051","NCT00355849","NCT00175903","NCT00356109","NCT00357227",
            "NCT00359762","NCT00372229","NCT00190047","NCT00373373","NCT00195507",
            "NCT00376337","NCT00384046","NCT00385372","NCT00391287","NCT00391872",
            "NCT00393692","NCT00006045","NCT00048295","NCT00151112","NCT00153062",
            "NCT00156338","NCT00157157","NCT00160524","NCT00160706","NCT00165828"
  };

  public static final String[] AE_DESCRIPTIONS = new String[]{
          "AggregatePlaintextFastUMLSProcessor_VOL.xml",
          "AggregatePlaintextFastUMLSProcessor_V--.xml",
          "AggregatePlaintextFastUMLSProcessor_V-L.xml"};

  public static void main (String[] args){

    String date = "2014-01-01";
    //String name = "umls2014AB";
    String name = "umls2014AB_extract";
    String type = "ontology";

    Properties prop = new Properties();
    FormRepository rep = new FormRepositoryImpl();
    Set<String> usedTrials = new HashSet<>(Arrays.asList(trials));
    try {
      rep.initialize(args[0]);
      Set<String> types = new HashSet<>();

      Map<String, Integer> cui2Id = rep.getFormManager().getIdMapping(name, date, type);
      types.add("eligibility criteria");
      Set<EntityStructureVersion> esvSet = rep.getFormManager().getStructureVersionsByType(types);

      VersionMetadata umlsMeta = rep.getFormManager().getMetadata(name, type, date);
      AnnotationMapping referenceMapping = new AnnotationMapping();

      for (EntityStructureVersion es: esvSet) {
        if (usedTrials.contains(es.getMetadata().getName())) {
          VersionMetadata vm = es.getMetadata();
          String mappingName= vm.getName()+"["+vm.getTopic()+"]-"
                  +umlsMeta.getName()+"["+umlsMeta.getTopic()+"]_odm";
          AnnotationMapping esRefMap = rep.getMappingManager().getAnnotationMapping(es.getMetadata(),
                  umlsMeta, mappingName);
          referenceMapping = SetAnnotationOperator.union(AggregationFunction.MAX, referenceMapping, esRefMap);
        }
      }
      List<String[]> results = new ArrayList<>();
      for (String aeDescr : AE_DESCRIPTIONS) {
        prop.put(CTakeWrapper.ID_MAP, cui2Id);
        prop.put(CTakeWrapper.AE_PATH, PATH + "/" + aeDescr);
        AnnotationWrapper wrapper = new CTakeWrapper(prop);
        Set<GenericProperty> gps = esvSet.iterator().next().getAvailableProperties("question", "EN", null);
        gps.addAll(esvSet.iterator().next().getAvailableProperties("name", null, null));
        prop.put(CTakeWrapper.PROPERTIES, gps);
        long start = System.currentTimeMillis();
        AnnotationMapping overallMapping = new AnnotationMapping();
        for (EntityStructureVersion es : esvSet) {
          if (usedTrials.contains(es.getMetadata().getName())) {
            AnnotationMapping am = wrapper.computeMapping(es, prop);
            am = removeConceptsNotInExtract(am, cui2Id);
            overallMapping = SetAnnotationOperator.union(AggregationFunction.MAX, overallMapping, am);
          }
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        MappingEvaluation me = new MappingEvaluation();
        EvaluationResult er = me.getResult(overallMapping, referenceMapping, "eligibility criteria", "umls");
        results.add(er.toStringArray());
      }

      String resTable = writeTable(AE_DESCRIPTIONS, results);
      System.out.println(resTable);

    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InitializationException e) {
      e.printStackTrace();
    } catch (StructureBuildException e) {
      e.printStackTrace();
    } catch (VersionNotExistsException e) {
      e.printStackTrace();
    } catch (AnnotationException e) {
      e.printStackTrace();
    } catch (InvalidXMLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ResourceInitializationException e) {
      e.printStackTrace();
    }
  }

  /**
   *
   * @param am
   * @param cui2Id mapping between CUI and id for each concept from the repository
   * @return
   */
  public static AnnotationMapping removeConceptsNotInExtract(AnnotationMapping am, Map<String,Integer>cui2Id){
    //annotations not in ontology because concept is not in repository;
    Set<Long> removeCorrs = new HashSet<>();
    //all ctakes annotations
    for (EntityAnnotation ea: am.getAnnotations()){
      if (!cui2Id.containsKey(ea.getTargetAccession())){
        removeCorrs.add(ea.getId());
      }
    }
    //remove
    for (long rc: removeCorrs){
      am.removeAnnotation(rc);
    }
    return am;
  }
  public static String writeTable(String[] header, List<String[]> result){
    StringBuilder sb = new StringBuilder();
    sb.append("method"+"\t");
    for (int i = 0; i<header.length;i++){
      if (i!= header.length-1)
        sb.append(header[i]+"\t");
      else{
        sb.append(header[i]+System.getProperty("line.separator"));
      }
    }

    for (int i= 0 ;i<6;i++){
      String col = getColumn(i);
      sb.append(col+"\t");
      for (int j =0; j<result.size(); j++){
        String[] r = result.get(j);
        if (j!= result.size()-1)
          sb.append(r[i]+"\t");
        else{
          sb.append(r[i]+System.getProperty("line.separator"));
        }
      }
    }
    return sb.toString();
  }



  private static String getColumn (int i){
    if (i == 0){
      return "truePositive";
    }else if (i==1){
      return "falsePositive";
    }else if (i==2){
      return "falseNegative";
    }else if (i==3){
      return "precision";
    }else if (i==4){
      return "recall";
    }else if (i==5){
      return "fmeasure";
    }
    return "";
  }

}
