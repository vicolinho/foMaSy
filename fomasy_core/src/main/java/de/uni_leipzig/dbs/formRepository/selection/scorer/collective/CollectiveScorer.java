package de.uni_leipzig.dbs.formRepository.selection.scorer.collective;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.selection.scorer.data.CollectiveScoreContext;

import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 11.04.2017.
 */
public interface CollectiveScorer {

  Map<Integer, Map<Integer,Double>> computeScore(AnnotationMapping am, Map<Integer, Set<Set<Integer>>> conflictSet,
                                                 CollectiveScoreContext scContext);
}
