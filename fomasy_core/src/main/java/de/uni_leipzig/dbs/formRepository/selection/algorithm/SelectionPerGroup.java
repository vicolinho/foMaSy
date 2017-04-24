package de.uni_leipzig.dbs.formRepository.selection.algorithm;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Created by christen on 20.04.2017.
 */
public class SelectionPerGroup implements ISelectionAlgorithm {
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
        double max =0;
        double currentMax=0;
        do {
          ScorePair sp = scorePairs.poll();
          if (sp!=null) {
            currentMax = sp.score;
            am.removeAnnotation(itemCandidates.getKey(), sp.conceptId);
          }
          else {
            currentMax =-1;
          }
        }while (currentMax >= max);
      }
    }
    return am;
  }


  private class ScorePair implements Comparable<ScorePair>{

    double score;

    int conceptId;


    ScorePair(int conceptId ,double score){
      this.score = score;
      this.conceptId = conceptId;
    }
    @Override
    public int compareTo(ScorePair o) {
      if (this.score <o.score){
        return 1;
      }else if (this.score>o.score){
        return -1;
      }else return 0;
    }
  }
}
