package de.uni_leipzig.dbs.formRepository.evals.clustering;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessingSteps;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorConfig;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorExecutor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.TokenWeighting.TFIDFTokenWeightGenerator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.exception.PreprocessingException;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction.POSBasedExtractingPreprocessor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.Cluster;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.OntologyClustering;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.impl.SimMeasure;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.impl.TokenbasedClustering;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


/**
 * Created by christen on 15.02.2017.
 */
public class OntologyClusteringEvaluation {
  static String[] t = new String[]{"CD","FW","JJ","JJR","JJS","LS","NN","NNS","NNP","RB","RBS","SYM","IN"};

  public static void main(String[] args) throws VersionNotExistsException, StructureBuildException {
    PropertyConfigurator.configure("log4j.properties");
    Logger log = Logger.getLogger(OntologyClusteringEvaluation.class);
    FormRepository rep = new FormRepositoryImpl();


    try {
      rep.initialize(args[0]);
      EntityStructureVersion umls = rep.getFormManager().getStructureVersion("umls2014AB", "ontology", "2014-01-01");
      Set<String> taggs= new HashSet<>(Arrays.asList(t));
      Map<String,Object> ext = new HashMap<>();
      ext.put(POSBasedExtractingPreprocessor.FILTER_TYPES, taggs);
      umls = prepareUMLS(umls, ext);
      Set<GenericProperty> umlsProperties = umls.getAvailableProperties("name", "EN", null);
      umlsProperties.addAll(umls.getAvailableProperties("synonym", "EN", null));
      EncodedEntityStructure encUmls = EncodingManager.getInstance().encoding(umls,true);
      TFIDFTokenWeightGenerator.getInstance().initializeGlobalCount(encUmls,
              umlsProperties.toArray(new GenericProperty[]{}));
      writeTokenDistribution(encUmls);
      OntologyClustering clustering = new TokenbasedClustering();
      Map<String,Object> props = new HashMap<>();
      props.put(OntologyClustering.THRESHOLD, 0.5f);
      Collection<Cluster> clusters = clustering.cluster(umls, SimMeasure.DICE, props, umlsProperties);
      //log.info("size: "+clusters.size());
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InitializationException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  static void writeTokenDistribution (EncodedEntityStructure ees) throws IOException {
    Int2FloatMap map = TFIDFTokenWeightGenerator.getInstance().generateIDFValuesForAllSources(ees.getObjIds().size());
    FileWriter fw = new FileWriter("tokenDistribution.csv");
    for (Map.Entry<Integer,Float> e: map.entrySet()){
      fw.append(EncodingManager.getInstance().getReverseDict().get(e.getKey())+"  "+e.getValue()+
              System.getProperty("line.separator"));
    }

  }

  static EntityStructureVersion prepareUMLS(EntityStructureVersion umls, Map<String, Object> external){
    Set<GenericProperty> umlsProperties = umls.getAvailableProperties("name", "EN", null);
    umlsProperties.addAll(umls.getAvailableProperties("synonym", "EN", null));
    PreprocessorConfig umlsConfig = new PreprocessorConfig ();
    umlsConfig.setExternalSourceMap(external);
    PreprocessProperty propSyn = new PreprocessProperty("synonym", "EN", null);
    PreprocessProperty propName = new PreprocessProperty("name", "EN", null);
    umlsConfig.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, propSyn,propName);
    umlsConfig.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, propSyn,propName);
    //umlsConfig.addPreprocessingStepForProperties(PreprocessingSteps.KEYWORD_EXTRACTION, propSyn,propName);
    PreprocessorExecutor executor = new PreprocessorExecutor();
    try {
      executor.preprocess(umls, umlsConfig);
    } catch (PreprocessingException e) {
      e.printStackTrace();
    }
    return umls;
  }
}
