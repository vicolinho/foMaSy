package de.uni_leipzig.dbs.formRepository.matching.preprocessing.segmentation.algorithms;

import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;

import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.segmentation.data.WordVector;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by christen on 26.01.2017.
 */
public class GraphBasedSegmentation implements SegmentationAlgo{

  @Override
  public Set<Set<WordVector>> identifyMWU(EntityStructureVersion esv, List<PreprocessProperty> selectedProps,
                                          Word2Vec word2Vec) {
    Clusterer<WordVector> clusterer = new DBSCANClusterer<WordVector>(50,2);
    Set<Set<WordVector>> mwus = new HashSet<>();
    for (GenericEntity ge: esv.getEntities()){
      for (PreprocessProperty pp: selectedProps){
        List<String> value = ge.getPropertyValues(pp.getName(),pp.getLang(), pp.getScope());
        for (String v: value){
          List<Cluster<WordVector>> clusters = this.clusterPerValue(v, word2Vec, clusterer);
          for (Cluster<WordVector> c : clusters){
            Set<WordVector> cs = new HashSet<>();
            cs.addAll(c.getPoints());
            mwus.add(cs);
          }

        }
      }
    }
    return  mwus;
  }

  public List<Cluster<WordVector>> clusterPerValue (String v, Word2Vec word2Vec, Clusterer<WordVector> clusterer){
    String[] tokens = tokenize(v);
    Set<WordVector> points = this.buildPoints(tokens, word2Vec);
    List<Cluster<WordVector>> clusters = (List<Cluster<WordVector>>) clusterer.cluster(points);
    return clusters;
  }

  private String[] tokenize( String property){
    return property.split("[^A-Za-z0-9]");
  }




  private Set<WordVector> buildPoints(String[] tokens, Word2Vec word2Vec){
    Set<WordVector> points = new HashSet<>();
    for (String t: tokens){
      double[] vec = word2Vec.getWordVector(t);
      if (vec !=null){
        points.add(new WordVector(vec, EncodingManager.getInstance().checkToken(t)));
      }
    }
    return points;
  }
}
