package de.uni_leipzig.dbs.formRepository.selection.algorithm;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;

import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 20.04.2017.
 */
public class OptimalSelection implements ISelectionAlgorithm {
  @Override
  public AnnotationMapping computeSelection(AnnotationMapping am, Map<Integer, Set<Set<Integer>>> conflicts,
      Map<Integer, Map<Integer, Double>> scores) {
    return null;
  }
}
