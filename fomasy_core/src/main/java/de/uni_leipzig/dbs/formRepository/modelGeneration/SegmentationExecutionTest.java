package de.uni_leipzig.dbs.formRepository.modelGeneration;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessingSteps;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorConfig;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorExecutor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.deepLearning.generation.Word2VecGenerator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.exception.PreprocessingException;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction.POSBasedExtractingPreprocessor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.segmentation.algorithms.AgglomerativeClustering;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.segmentation.algorithms.GraphBasedSegmentation;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.segmentation.data.WordVector;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by christen on 27.01.2017.
 */
public class SegmentationExecutionTest {

  public static void main(String[] args){
    FormRepository rep = new FormRepositoryImpl();
    try {
      if (args.length >0) {
        rep.initialize(args[0]);
      }
      Set<String> formTypes = new HashSet<>();
      if (args.length>1){

        for (String t: Arrays.asList(args[1].split(","))){
          formTypes.add(t.replaceAll("_"," "));
        }
      }
      String file = "word2VecForm";
      if (args.length>2){
        file = args[2];
      }
      String[] tags = new String[]{"CD","FW","JJ","JJR","JJS","LS","NN","NNS","NNP","RB","RBS","SYM","IN"};
      Map<String,Object> extMap = new HashMap<>();
      Set<String> tagSet = new HashSet<>(Arrays.asList(tags));
      extMap.put(POSBasedExtractingPreprocessor.FILTER_TYPES, tagSet);
      PreprocessorConfig config = new PreprocessorConfig();
      config.setExternalSourceMap(extMap);
      PreprocessProperty[] preProperties = new PreprocessProperty[]{new PreprocessProperty("name", null, null),
              new PreprocessProperty("question","EN",null)};
      config.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, preProperties);
      config.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION,preProperties);
      //config.addPreprocessingStepForProperties(PreprocessingSteps.KEYWORD_EXTRACTION, new PreprocessProperty("question","EN",null));
      PreprocessorExecutor preExec = new PreprocessorExecutor();

      Set<EntityStructureVersion> esvs = rep.getFormManager().getStructureVersionsByType(formTypes);
      Collection<GenericProperty> properties = esvs.iterator().next().getAvailableProperties();
      Iterator<GenericProperty> iter = properties.iterator();
      while(iter.hasNext()){
        GenericProperty gp = iter.next();
        if (!(gp.getName().equals("question")&&gp.getLanguage().equals("EN"))){
          iter.remove();
        }
      }


      List<PreprocessProperty> list = new ArrayList<>();
      list.add(new PreprocessProperty("question","EN",null));
      Word2Vec w2v = WordVectorSerializer.readWord2Vec(new File(args[2]));
      GraphBasedSegmentation segmentation = new GraphBasedSegmentation();
      DistanceMeasure measure = new DistanceMeasure() {
        @Override
        public double compute(double[] a, double[] b) {
          double dotProduct = 0;
          double squareA =0;
          double squareB =0;
          for (int i = 0; i<a.length;i++){
            dotProduct += (a[i]*b[i]);
            squareA += a[i]*a[i];
            squareB += b[i]*b[i];
          }
          return 1-dotProduct/(Math.sqrt(squareA)*Math.sqrt(squareB));

        }
      };
      AgglomerativeClustering agglomerativeClustering = new AgglomerativeClustering();
      for (EntityStructureVersion esv: esvs){
        esv = preExec.preprocess(esv, config);
        for (GenericEntity ge: esv.getEntities()){
          List<String> vs = ge.getPropertyValues("question","EN",null);
          for (String v: vs){
            String[] tokens = v.split("[^A-Za-z0-9]");
            System.out.println(Arrays.toString(tokens));
            int k = Math.round((float)tokens.length/3f);
            KMeansPlusPlusClusterer<WordVector> clusterer = new KMeansPlusPlusClusterer<>(k, -1, measure);
            List<Cluster<WordVector>> clusters =segmentation.clusterPerValue(v, w2v,clusterer);
            Set<Set<WordVector>> clusters2 = agglomerativeClustering.clusterPerValue(v,w2v);
            System.out.println("ORIGINAL:"+v);
            for (Cluster<WordVector> c: clusters){
              System.out.print("mwu:");
              for (WordVector vec: c.getPoints()){
                System.out.print(EncodingManager.getInstance().getReverseDict().get(vec.getId())+" ");
              }
              System.out.println();
            }

            for (Set<WordVector> c: clusters2){
              System.out.print("agglomerative mwu:");
              for (WordVector vec: c){
                System.out.print(EncodingManager.getInstance().getReverseDict().get(vec.getId())+" ");
              }
              System.out.println();
            }
          }
        }

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
    } catch (IOException e) {
      e.printStackTrace();
    } catch (PreprocessingException e) {
      e.printStackTrace();
    }
  }
}
