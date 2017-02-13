package de.uni_leipzig.dbs.formRepository.evals.blocking;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.matching.blocking.data.BlockSet;
import de.uni_leipzig.dbs.formRepository.matching.blocking.token.CommonTokenBlocking;
import de.uni_leipzig.dbs.formRepository.matching.lookup.TokenSimilarityLookup;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessingSteps;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorConfig;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorExecutor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.TokenWeighting.TFIDFTokenWeightGenerator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.exception.PreprocessingException;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction.POSBasedExtractingPreprocessor;
import org.apache.log4j.PropertyConfigurator;

import java.util.*;

/**
 * Created by christen on 04.11.2016.
 */
public class TokenBlockingEvaluation {

  static String[] generalConcepts = new String[]{"Qualitative Concept","Quantitative Concept",
          "Functional Concept","Conceptual Entity","Temporal Concept"};

  static String[] t = new String[]{"CD","FW","JJ","JJR","JJS","LS","NN","NNS","NNP","RB","RBS","SYM","IN"};

  public static void main(String[] args){

    PropertyConfigurator.configure("log4j.properties");

    FormRepository rep = new FormRepositoryImpl();
    try {
      rep.initialize(args[0]);
      Set<String> types = new HashSet<>();
      types.add("eligibility form");
      rep.getFormManager().getStructureVersionsByType(types);
      Set<EntityStructureVersion> set = rep.getFormManager().getStructureVersionsByType(types);
      HashMap<Integer,EntityStructureVersion> map = new HashMap<Integer,EntityStructureVersion>();
      for (EntityStructureVersion esv:set){
        map.put(esv.getStructureId(), esv);
      }


      Set <String> entTypes = new HashSet<String>();
      entTypes.add("item");
      Set<GenericProperty> formProperties = set.iterator().next().getAvailableProperties("question", "EN", null);
      Set<GenericProperty> formMatchProperties = new HashSet<GenericProperty>(formProperties);
      formMatchProperties.addAll(set.iterator().next().getAvailableProperties("name", "EN", null));


      PreprocessorConfig formConfig = new PreprocessorConfig ();
      PreprocessProperty propForms = new PreprocessProperty("question", "EN", null);
      PreprocessProperty propFormName = new PreprocessProperty("name", "EN", null);
      Map<String,Object> preMap = new HashMap<>();
      Set<String> wordTypes = new HashSet<>();
      for (String wt:t){
        wordTypes.add(wt);
      }

      preMap.put(POSBasedExtractingPreprocessor.FILTER_TYPES, wordTypes);
      formConfig.setExternalSourceMap(preMap);
      Set<String> semTypes = new HashSet<String> ();
      for (String sem: generalConcepts){
        semTypes.add(sem);
      }

      formConfig.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, propForms,propFormName);
      formConfig.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, propForms,propFormName);
      //formConfig.addPreprocessingStepForProperties(PreprocessingSteps.KEYWORD_EXTRACTION, propForms);
      PreprocessorExecutor executor = new PreprocessorExecutor();
      Set<EncodedEntityStructure> encSet = new HashSet<>();
      for (EntityStructureVersion esv: set){
        esv = executor.preprocess(esv, formConfig);
        EncodedEntityStructure ees = EncodingManager.getInstance().encoding(esv, entTypes, true);
        encSet.add(ees);
      }

      EntityStructureVersion umls = rep.getFormManager().getStructureVersion("umls2014AB", "ontology", "2014-01-01");
      Set<GenericProperty> umlsProperties = umls.getAvailableProperties("name", "EN", null);
      umlsProperties.addAll(umls.getAvailableProperties("synonym", "EN", null));
      EncodedEntityStructure umlsEnc = prepareUMLS(umls);
      TokenSimilarityLookup.getInstance().computeTrigramLookup(set, umls, rep);
      CommonTokenBlocking ctb = new CommonTokenBlocking();
      for (EncodedEntityStructure ees : encSet){
        BlockSet pairs= ctb.computeBlocks(ees,umlsEnc,TokenSimilarityLookup.getLookup());
        int comparisons =pairs.cardinality();
        System.out.println("comparisons: "+comparisons+" quadratic: "+(ees.getObjIds().size()*umlsEnc.getObjIds().size()));
      }

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
    } catch (MatchingExecutionException e) {
      e.printStackTrace();
    } catch (PreprocessingException e) {
      e.printStackTrace();
    }
  }

  static EncodedEntityStructure prepareUMLS(EntityStructureVersion umls){
    Set<GenericProperty> umlsProperties = umls.getAvailableProperties("name", "EN", null);
    umlsProperties.addAll(umls.getAvailableProperties("synonym", "EN", null));
    PreprocessorConfig umlsConfig = new PreprocessorConfig ();
    PreprocessProperty propSyn = new PreprocessProperty("synonym", "EN", null);
    PreprocessProperty propName = new PreprocessProperty("name", "EN", null);
    umlsConfig.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, propSyn,propName);
    umlsConfig.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, propSyn,propName);
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
