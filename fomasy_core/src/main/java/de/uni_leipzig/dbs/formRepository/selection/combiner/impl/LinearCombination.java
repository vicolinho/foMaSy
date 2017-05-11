package de.uni_leipzig.dbs.formRepository.selection.combiner.impl;

import de.uni_leipzig.dbs.formRepository.selection.combiner.ICombiner;

import java.nio.DoubleBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by christen on 20.04.2017.
 */
public class LinearCombination implements ICombiner{

  private double[] weightVector;

  public LinearCombination (double[] weightVector){
    this.weightVector = weightVector;
  }


  @Override
  public Map<Integer, Map<Integer, Double>> combine(List<Map<Integer, Map<Integer, Double>>> scores) {
    Map<Integer, Map<Integer, Double>> aggregatedScore = new HashMap<>();
    int index =0;
    for (Map<Integer, Map<Integer,Double>> featureMap: scores){
      for (Map.Entry<Integer, Map<Integer,Double>> coonceptsPerItem: featureMap.entrySet()){
        Map<Integer,Double> cscores = aggregatedScore.get(coonceptsPerItem.getKey());
        if (cscores == null){
          cscores = new HashMap<>();
          aggregatedScore.put(coonceptsPerItem.getKey(), cscores);
        }
        for (Map.Entry<Integer, Double> scoreConcept:coonceptsPerItem.getValue().entrySet()){
          if (cscores.get(scoreConcept.getKey())==null){
            cscores.put(scoreConcept.getKey(), scoreConcept.getValue()*weightVector[index]);
          }else{
            cscores.put(scoreConcept.getKey(),
              cscores.get(scoreConcept.getKey())+ scoreConcept.getValue()*weightVector[index]);
          }
        }
      }
      index++;
    }
    return aggregatedScore;
  }
}
