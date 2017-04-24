package de.uni_leipzig.dbs.formRepository.selection.combiner;

import java.util.List;
import java.util.Map;

/**
 * Created by christen on 20.04.2017.
 */
public interface ICombiner {

  Map<Integer, Map<Integer, Double>> combine(List<Map<Integer, Map<Integer, Double>>> scores);
}
