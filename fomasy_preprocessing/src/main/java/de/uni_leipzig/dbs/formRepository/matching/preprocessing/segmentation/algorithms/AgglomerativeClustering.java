package de.uni_leipzig.dbs.formRepository.matching.preprocessing.segmentation.algorithms;

import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.segmentation.data.MWU;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.segmentation.data.WordVector;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.bytedeco.javacpp.opencv_core;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 06.02.2017.
 */
public class AgglomerativeClustering implements SegmentationAlgo{




  public Int2ObjectMap<MWU> mwuMap;
  float sim_threshold = 0.25f;

  public Int2ObjectMap<MWU> computeCluster (Int2ObjectMap<MWU> mwuMap, double[][] similarities){
    boolean isChange;
    do{
      int[] position = getIndexForBestSim(mwuMap, similarities);
      if (position[0]!=-1){
        if (similarities[position[0]][position[1]]>=sim_threshold) {
          mwuMap = this.merge(mwuMap, position);
          similarities = this.update(similarities, position);
          isChange = true;
        }else {
          isChange = false;
        }
      }else{
        isChange = false;
      }

    }while (isChange);
    return mwuMap;
  }



  public int[] getIndexForBestSim (Int2ObjectMap<MWU> mwuMap, double [][] sim){
    double bestSim = 0;
    int [] position = new int[]{-1,-1};
    for (int i = 0; i<sim.length;i++){
      for (int j = i+1; j<sim.length; j++){
        if (sim[i][j]!=0) {
          int sizeA = mwuMap.get(i).getTokenIds().size();
          int sizeB = mwuMap.get(j).getTokenIds().size();
          double sv = sim[i][j] / (double)(sizeA + sizeB);
          if (sv > bestSim) {
            bestSim = sv;
            position[0] = i;
            position[1] = j;

          }
        }
      }
    }
    return position;
  }


  public Int2ObjectMap<MWU> merge (Int2ObjectMap<MWU> mwuMap, int[] position){
    int minID = Math.min(position[0],position[1]);
    int maxID = Math.max(position[0],position[1]);
    MWU firstMWU = mwuMap.get(minID);
    MWU secondMWU = mwuMap.get(maxID);

    firstMWU.addMWU(secondMWU);

    mwuMap.put(minID, firstMWU);
    mwuMap.remove(maxID);
    return mwuMap;
  }

  public double[][] update (double[][] simMatrix, int []position){
    double [][] newMatrix = new double[simMatrix.length][simMatrix.length];
    int maxID = Math.max(position[0],position[1]);
    int minID = Math.min(position[0],position[1]);
    for (int i = 0; i<simMatrix.length; i++){
      for (int j = i+1; j< simMatrix.length;j++){
        if (i ==minID && j!= maxID){
          int firstId = Math.min(j, maxID);
          int secId = Math.max(j, maxID);
          double val = simMatrix[i][j]+simMatrix[firstId][secId];
          newMatrix[i][j] =val;
        }else if (j == minID && i!=maxID){
          int firstId = Math.min(i, maxID);
          int secId = Math.max(i, maxID);
          double val = simMatrix[i][j]+simMatrix[firstId][secId];
          newMatrix[i][j] =val;
        }else if (i==maxID || j == maxID){
          newMatrix[i][j] =0;
        }else {
          newMatrix[i][j] = simMatrix[i][j];
        }
      }
    }
    return newMatrix;
  }



  @Override
  public Set<Set<WordVector>> identifyMWU(EntityStructureVersion esv, List<PreprocessProperty> selectedProps, Word2Vec word2Vec) {
    return null;
  }

  public Set<Set<WordVector>> clusterPerValue (String v, Word2Vec word2Vec){
    String[] tokens = tokenize(v);
    double[][] simMatrix = this.computeSimMatrix(tokens, word2Vec);
    mwuMap = this.computeCluster(mwuMap, simMatrix);
    Set<Set<WordVector>> clusters = new HashSet<>();
    for (MWU mwu: mwuMap.values()){
      Set<WordVector> vectorSet = new HashSet<>();
      clusters.add(vectorSet);
      for (int t : mwu.getTokenIds()){
        WordVector vec = new WordVector(null, t);
        vectorSet.add(vec);
      }

    }
    return clusters;
  }

  private String[] tokenize( String property){
    return property.split("[^A-Za-z0-9]");
  }




  private double[][] computeSimMatrix(String[] tokens, Word2Vec word2Vec){
    mwuMap = new Int2ObjectOpenHashMap<>();
    int index =0;
    double[][] simMatrix = new double[tokens.length][tokens.length];
    for (int i =0; i<tokens.length;i++){
      MWU mwu = new MWU(EncodingManager.getInstance().checkToken(tokens[i]));
      mwuMap.put(i,mwu);
      for (int j = i+1; j<tokens.length;j++){
        double sim = 0;
        try {
          sim = word2Vec.similarity(tokens[i], tokens[j]);
          int diff = j-i;
         // sim *= 1d/(double) diff;
        }catch (Exception e){}
        simMatrix[i][j] =sim;
      }

    }
    return simMatrix;
  }

}
