package de.uni_leipzig.dbs.formRepository.selection.combiner.impl;

import de.uni_leipzig.dbs.formRepository.selection.combiner.ICombiner;

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
    return null;
  }
}
