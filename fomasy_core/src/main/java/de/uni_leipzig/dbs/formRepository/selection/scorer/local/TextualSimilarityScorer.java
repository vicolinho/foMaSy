package de.uni_leipzig.dbs.formRepository.selection.scorer.local;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.selection.scorer.data.LocalScoreContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 11.04.2017.
 */
public class TextualSimilarityScorer extends AbstractLocalScorer {

  @Override
  public Map<Integer, Map<Integer, Double>> computeScore(AnnotationMapping am, EncodedEntityStructure form,
                                                         EncodedEntityStructure ontology, LocalScoreContext ctx) {

    Map<Integer, Map<Integer,Double>> ranking = new HashMap<>();
    for (Map.Entry<Integer, Set<EntityAnnotation>>e: this.groupBySrcEntity(am).entrySet()){
      Map<Integer, Double> rankPerItem = new HashMap<>();
      for (EntityAnnotation ea: e.getValue()){
        double sim = (double)ea.getSim();
        rankPerItem.put(ea.getTargetId(), sim);
      }
      ranking.put(e.getKey(), rankPerItem);
    }
    return ranking;
  }


}
