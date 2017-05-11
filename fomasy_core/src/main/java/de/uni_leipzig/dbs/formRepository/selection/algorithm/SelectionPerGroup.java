package de.uni_leipzig.dbs.formRepository.selection.algorithm;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Created by christen on 20.04.2017.
 */
public class SelectionPerGroup extends AbstractSelectionAlgorithm {

  Logger log = Logger.getLogger(getClass());
  @Override
  public AnnotationMapping computeSelection(AnnotationMapping am, Map<Integer, Set<Set<Integer>>> conflicts,
                                            Map<Integer, Map<Integer, Double>> scores) {
    PriorityQueue<ScorePair> scorePairs = new PriorityQueue<>();
    for (Map.Entry<Integer, Set<Set<Integer>>> itemCandidates : conflicts.entrySet()){
      Map<Integer,Double> scoreItem = scores.get(itemCandidates.getKey());
      for (Set<Integer> conflictCons: itemCandidates.getValue()){
        scorePairs.clear();
        for (Integer conceptId: conflictCons){
          scorePairs.add(new ScorePair(conceptId, scoreItem.get(conceptId)));
        }
        if (scorePairs.size()>1) {
          double max = scorePairs.poll().score;
          double currentMax = 0;
          do {
            ScorePair sp = scorePairs.peek();
            if (sp != null) {
              currentMax = sp.score;
              if (currentMax == max) {
                scorePairs.poll();
              }
            }else{
              currentMax=-1;
            }
          } while (currentMax > max);
          for (ScorePair sp : scorePairs) {
            am.removeAnnotation(itemCandidates.getKey(), sp.conceptId);
          }
        }

      }
    }
    return am;
  }
}
