package de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.impl;

import java.util.Map;

/**
 * Created by christen on 15.02.2017.
 */
public class SimMeasure {

  public static final String DICE ="dice";

  public static final String TFIDF="tfidf";

  public float computeSim(int[] tokens1, int[] tokens2, Map<String, Object> propertyMap, String type){
    if (type.equals(DICE)){
      return this.computeDice(tokens1, tokens2);
    }else if (type.equals(TFIDF)){

    }else{

    }
    return 0f;
  }


  private float computeDice(int[] tokens1, int[] tokens2){
    int common=0;
    int index1 = 0;
    int index2 = 0;
    while (index1<tokens1.length&&index2<tokens2.length){
      if (tokens1[index1]<tokens2[index2]){
        index1++;
      }else if (tokens1[index1]>tokens2[index2]){
        index2++;
      }else{
        common++;
        index1++;
        index2++;
      }
    }
    return  2*common/(float)(tokens1.length+tokens2.length);
  }
}
