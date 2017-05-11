package de.uni_leipzig.dbs.formRepository.evals;

import de.uni_leipzig.dbs.formRepository.importer.annotation.csv.AnnotationWriter;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.exception.PreprocessingException;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction.POSBasedExtractingPreprocessor;
import de.uni_leipzig.dbs.formRepository.operation.ExtractOperator;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;

import java.io.IOException;
import java.util.*;

import org.apache.log4j.PropertyConfigurator;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.operation.SetAnnotationOperator;
import de.uni_leipzig.dbs.formRepository.evals.calculation.EvaluationResult;
import de.uni_leipzig.dbs.formRepository.evals.calculation.MappingEvaluation;
import de.uni_leipzig.dbs.formRepository.evals.io.EvaluationResultWriter;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.manager.MatchManager;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.execution.RegisteredMatcher;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.ExecutionTree;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.MatchGroup;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.MatchOperator;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.SetOperator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessingSteps;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorConfig;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorExecutor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.TokenWeighting.TFIDFTokenWeightGenerator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.lookup.TokenSimilarityLookup;
import de.uni_leipzig.dbs.formRepository.matching.selection.GroupSelection;
import de.uni_leipzig.dbs.formRepository.matching.selection.Selection;
import de.uni_leipzig.dbs.formRepository.matching.token.SoftTFIDFMatcher;
import de.uni_leipzig.dbs.formRepository.matching.token.TFIDFMatcher;
import de.uni_leipzig.dbs.formRepository.matching.token.TFIDFWindowMatcher;
import de.uni_leipzig.dbs.formRepository.util.CantorDecoder;

public class UMLS_SimpleFormMatching {
  static boolean filterGeneralConcepts =true;
  static String[] generalConcepts = new String[]{"Qualitative Concept","Quantitative Concept",
    "Functional Concept","Temporal Concept","Conceptual Entity"};

  public static final GenericProperty[] umlsProperties = new GenericProperty[]{
          new GenericProperty(0, "name", "PN", "EN"),
          new GenericProperty(1, "synonym", "PT", "EN"),
          new GenericProperty(2, "synonym", "SY", "EN"),
          new GenericProperty(3, "synonym", "SCN", "EN"),
          new GenericProperty(4, "synonym", "FN", "EN"),
          new GenericProperty(6, "synonym", "CE", "EN"),
          new GenericProperty(7, "synonym", "NM", "EN"),
          //new GenericProperty(8, "synonym", "OCD", "EN"),
          //new GenericProperty(9, "synonym", null, "EN"),
  };

  public static final GenericProperty[] optionalProperties = new GenericProperty[]{
          new GenericProperty(6, "sem_type", null, null)
  };

  static final String[] tags = new String[]{"CD","FW","JJ","JJR","JJS","LS","NN","NNS","NNP","RB","RBS","SYM","IN"};

  public static final String[] trials = new String[]{
          "NCT00168051","NCT00355849","NCT00175903","NCT00356109","NCT00357227",
          "NCT00359762","NCT00372229","NCT00190047","NCT00373373","NCT00195507",
          "NCT00376337","NCT00384046","NCT00385372","NCT00391287","NCT00391872",
          "NCT00393692","NCT00006045","NCT00048295","NCT00151112","NCT00153062",
          "NCT00156338","NCT00157157","NCT00160524","NCT00160706","NCT00165828"
  };




  //  int[] selectedForms = new int[]{461,455,456,457,458,459,464,466,
  //    467,468,465,463,462,452,453,454,469,470,460,473,475,476,439,440};

  public static void main (String args[]){
    FormRepository rep = new FormRepositoryImpl();
    PropertyConfigurator.configure("log4j.properties");
    String date = "2014-01-01";
    String name="umls2014AB";
    String type ="ontology";
    

    
    Set<String> semTypes = new HashSet<String> ();
    for (String sem: generalConcepts){
      semTypes.add(sem);
    }
    Set<String> selectedFormsByName = new HashSet<>(Arrays.asList(trials));
    Map<String,Object> extMap = new HashMap<>();
    Set<String> tagSet = new HashSet<>(Arrays.asList(tags));
    extMap.put(POSBasedExtractingPreprocessor.FILTER_TYPES, tagSet);
    Set <Integer> selForms = new HashSet<Integer>();
    //for(int i : selectedForms)selForms.add(i);

    
    try {
      rep.initialize("fms.ini");
      Set<GenericProperty> usedProperties = new HashSet<>(Arrays.asList(umlsProperties));
      Set<GenericProperty> optProperties = new HashSet<>(Arrays.asList(optionalProperties));
      EntityStructureVersion umls = rep.getFormManager().getStructureVersion(name, type,
              date);
              //, usedProperties, optProperties);
      System.out.println("umls :"+umls.getNumberOfEntities());

      PreprocessorConfig config = new PreprocessorConfig();
      config.setExternalSourceMap(extMap);
      PreprocessProperty[] properties = new PreprocessProperty[]{new PreprocessProperty("name", null, null),
          new PreprocessProperty("question","EN",null)};
      config.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, properties);
      config.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION,properties);
      //config.addPreprocessingStepForProperties(PreprocessingSteps.KEYWORD_EXTRACTION, properties);
      //config.addPreprocessingStepForProperties(PreprocessingSteps.NORMALIZE,properties);
      Set<String> formTypes = new HashSet<String>();
      //formTypes.add("eligibility form");
      formTypes.add("eligibility criteria");
      Set <EntityStructureVersion> forms = rep.getFormManager().getStructureVersionsByType(formTypes);
      Set<EncodedEntityStructure> encodedStructures= new HashSet<EncodedEntityStructure>();
      
      PreprocessorExecutor preExec = new PreprocessorExecutor();

      Set<String> entTypes = new HashSet<String>();
      entTypes.add("item");
      Set<GenericProperty> propsSrc = null;
      Map <Integer,EntityStructureVersion> metaMap = new HashMap<Integer,EntityStructureVersion>();
      
      
      int size =0;
      for (EntityStructureVersion esv: forms){
        if (selectedFormsByName.contains(esv.getMetadata().getName())){
          selForms.add(esv.getStructureId());
          esv = preExec.preprocess(esv, config);
          EncodedEntityStructure encForm =  EncodingManager.getInstance().encoding(esv,entTypes, true);
          encodedStructures.add(encForm);
          propsSrc = esv.getAvailableProperties("question","EN",null);
          propsSrc.addAll(esv.getAvailableProperties("name", null, null));
          size +=encForm.getObjIds().size();
          //count number of documents where tokens occur
          TFIDFTokenWeightGenerator.getInstance().initializeGlobalCount(encForm, propsSrc.toArray(new GenericProperty[]{}));
          metaMap.put(esv.getStructureId(), esv);
        }
      }
      forms.clear();
      Set <Integer> generalConceptIds = new HashSet<Integer>();
      if (filterGeneralConcepts){
        for (GenericEntity ge: umls.getEntities()){
          List <String> semType = ge.getPropertyValues("sem_type", null, null);
          for (String st :semType){
            if (semTypes.contains(st)){
              generalConceptIds.add(ge.getId());
              break;
            }
          }
        }
        for(int id : generalConceptIds){
          umls.removeEntity(id);
        }
      }
      Set<GenericProperty> propsTarget = umls.getAvailableProperties("name", "EN", null);
      propsTarget.addAll(umls.getAvailableProperties("synonym", "EN",null));

      PreprocessProperty[] propertiesUmls = new PreprocessProperty[]{new PreprocessProperty("name", "EN", null),
          new PreprocessProperty("synonym","EN",null)};
      PreprocessorConfig configUmls = new PreprocessorConfig();
      
      configUmls.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, propertiesUmls);
      configUmls.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, propertiesUmls);
      //configUmls.addPreprocessingStepForProperties(PreprocessingSteps.NORMALIZE, propertiesUmls);

      umls = preExec.preprocess(umls, configUmls);

      //umls.deduplicateProperties(propsTarget);
      System.out.println("deduplicated: "+umls.getNumberOfEntities());

      EncodedEntityStructure eesTarget = EncodingManager.getInstance().encoding(umls, true);
      size+=eesTarget.getObjIds().size();
      umls.clear();



      //TokenSimilarityLookup.getInstance().computeTrigramLookup(forms, umls, rep);
      TFIDFTokenWeightGenerator.getInstance().initializeGlobalCount(eesTarget,
              propsTarget.toArray(new GenericProperty[]{}));
      Int2FloatMap idfMap = TFIDFTokenWeightGenerator.getInstance().generateIDFValuesForAllSources(size);
      MatchManager mm  = rep.getMatchManager();
      Map <String,Object> globalObjects = new HashMap<String,Object>();
      //globalObjects.put(SoftTFIDFMatcher.LOOKUP, TokenSimilarityLookup.getInstance().getLookup());
      long startTime = System.currentTimeMillis();

      System.out.println("preprocessing finished...");
      AnnotationMapping overallCalculatedMapping= new AnnotationMapping();
      AnnotationMapping overallReferenceMapping = new AnnotationMapping();

      VersionMetadata umlsMeta = rep.getFormManager().getMetadata(name, type, date);
      for (EncodedEntityStructure ees:encodedStructures){
        if (selForms.contains(ees.getStructureId())){

          Set<GenericProperty> nameProperty = metaMap.get(ees.getStructureId()).getAvailableProperties("question","EN",null);
          /*
          MatchOperator exact = new MatchOperator(RegisteredMatcher.EXACT_MATCHER, AggregationFunction.MAX, nameProperty,
                  propsTarget, 1f);
          ExecutionTree treeExact = new ExecutionTree();
          treeExact.addOperator(exact);
          AnnotationMapping exactMapping;
          exactMapping = mm.match(metaMap.get(ees.getStructureId()),ees,eesTarget,umls, treeExact, null);
          for (EntityAnnotation ea: exactMapping.getAnnotations()){
            System.out.println(ea);
          }
          System.out.println("exact:" +exactMapping.getNumberOfAnnotations());
          EncodedEntityStructure restEes = ExtractOperator.extractUnannotatedEntities(ees,exactMapping);
          */
          EncodedEntityStructure restEes = ees;
          MatchOperator mop = new MatchOperator(RegisteredMatcher.LCS_MATCHER, AggregationFunction.MAX, propsSrc, propsTarget, 0.6f);
          MatchOperator mop2 = new MatchOperator(RegisteredMatcher.TRIGRAM_MATCHER, AggregationFunction.MAX, propsSrc, propsTarget, 0.65f);
          MatchOperator mop3 = new MatchOperator(RegisteredMatcher.TFIDF_MATCHER, AggregationFunction.MAX, propsSrc, propsTarget, 0.6f);
          MatchGroup group = new MatchGroup();
          group.addMatcher(mop3);
          group.addMatcher(mop);
          //group.addMatcher(mop2);

          SetOperator sop = new SetOperator(AggregationFunction.MAX, SetOperator.UNION);
          group.setOperator(sop);
          globalObjects.put(TFIDFMatcher.IDF_MAP_SOURCE, idfMap);
          globalObjects.put(TFIDFMatcher.IDF_MAP_TARGET, idfMap);
          globalObjects.put(TFIDFMatcher.TFIDF_SOURCE_SEPARATED, false);
          //globalObjects.put(TFIDFWindowMatcher.WND_SIZE, 5);
          mop3.setGlobalObjects(globalObjects);
          ExecutionTree tree = new ExecutionTree();
          tree.addOperator(group);


          AnnotationMapping am = mm.match(restEes, eesTarget, tree, null);

          Selection selection = new GroupSelection ();
          List<String> annos = new ArrayList<>();
          //AnnotationWriter aw = new AnnotationWriter();
          for (EntityAnnotation ea: am.getAnnotations()){
            annos.add(ea.getTargetAccession());
          }

          System.out.println(metaMap.get(ees.getStructureId()).getMetadata().getName()+
                  " before selection: "+am.getNumberOfAnnotations());
          am = selection.select(am, restEes, eesTarget, propsSrc, propsTarget, 0.3f, 0,1f, rep);
          //am = SetAnnotationOperator.union(AggregationFunction.MAX, am, exactMapping);
          VersionMetadata vm = metaMap.get(ees.getStructureId()).getMetadata();
          String mappingName= vm.getName()+"["+vm.getTopic()+"]-"
              +umlsMeta.getName()+"["+umlsMeta.getTopic()+"]_odm";
                AnnotationMapping am1 = rep.getMappingManager().getAnnotationMapping(vm,
                        umlsMeta, mappingName);
          System.out.println("after selection:"+ am.getNumberOfAnnotations());    

          for (int id : ees.getObjIds().keySet()){
            Set<Integer> targetIds = am.getCorrespondingTargetIds(id);
            if (am1.getCorrespondingTargetIds(id).isEmpty()&&!targetIds.isEmpty()){ 
              for (int tid : targetIds){
                am.removeAnnotation(id, tid);
              }
            }
          }
          overallCalculatedMapping = SetAnnotationOperator.union(AggregationFunction.MAX, overallCalculatedMapping, am);
          Set<Long> removeAnnotations = new HashSet<Long>();
          if (filterGeneralConcepts){
            for (EntityAnnotation ea: am1.getAnnotations()){
              if (generalConceptIds.contains(ea.getTargetId())){
                removeAnnotations.add(ea.getId());
              }
            }
            for(long id : removeAnnotations){
              int srcId= (int) CantorDecoder.decode_a(id);
              int targetId=(int) CantorDecoder.decode_b(id);  
              am1.removeAnnotation(srcId, targetId);
            }
          }
          overallReferenceMapping = SetAnnotationOperator.union(AggregationFunction.MAX, overallReferenceMapping, am1);
        }
      }
      MappingEvaluation eval = new MappingEvaluation();
      EvaluationResult er = eval.getResult(overallCalculatedMapping, overallReferenceMapping, "eligibility forms", umlsMeta.getName());
      
      
      System.out.println(er.getMeasures().get("precision"));
      System.out.println(er.getMeasures().get("recall"));      
      System.out.println(er.getMeasures().get("fmeasure"));  
      long elapsedTime = System.currentTimeMillis()-startTime;  
      System.out.println("match time:"+elapsedTime);
      
      
      
            
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
    } catch (PreprocessingException e) {
      e.printStackTrace();
    }
  }
}
