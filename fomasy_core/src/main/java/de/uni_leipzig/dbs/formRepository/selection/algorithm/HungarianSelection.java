package de.uni_leipzig.dbs.formRepository.selection.algorithm;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by christen on 29.04.2017.
 */
public class HungarianSelection extends AbstractSelectionAlgorithm {
  Logger log = Logger.getLogger(getClass());
  @Override
  public AnnotationMapping computeSelection(AnnotationMapping am, Map<Integer, Set<Set<Integer>>> conflicts,
                                            Map<Integer, Map<Integer, Double>> scores) {
    Map<Integer, Set<Set<Integer>>> reverseMapGroups = new HashMap<>();
    for (Map.Entry<Integer, Set<Set<Integer>>> itemCandidates : conflicts.entrySet()){

      Map<Integer,Double> scoreItem = scores.get(itemCandidates.getKey());
      reverseMapGroups = this.generateReverseGroupMap(itemCandidates.getValue(), reverseMapGroups);
      List<ScorePair> scoreSortedConceptList = sortConcepts(scoreItem, itemCandidates.getValue());
      Iterator<ScorePair> scorePairIterator = scoreSortedConceptList.iterator();
      while (scorePairIterator.hasNext()){
        ScorePair sp = scorePairIterator.next();
        if (!reverseMapGroups.isEmpty()){
          int cid = sp.conceptId;
          if (reverseMapGroups.containsKey(cid)){
            scorePairIterator.remove();
            reverseMapGroups = this.updateReverseMap(reverseMapGroups, cid);
          }
        }
      }
      for (ScorePair sp : scoreSortedConceptList) {
        am.removeAnnotation(itemCandidates.getKey(), sp.conceptId);
      }
    }
    return am;
  }



  private Map<Integer, Set<Set<Integer>>> updateReverseMap(Map<Integer, Set<Set<Integer>>> reverseMap, int cid){
    Set<Set<Integer>> groupsForConcept = reverseMap.remove(cid);
    for (Set<Integer> group: groupsForConcept){
      for (Integer c: group){
        reverseMap.remove(c);
      }
    }
    return reverseMap;
  }

  private List<ScorePair> sortConcepts(Map<Integer, Double> scores, Set<Set<Integer>> concepts){
    Set<Integer> allConcepts = new HashSet<>();
    for (Set<Integer> all: concepts){
      allConcepts.addAll(all);
    }
    List<ScorePair> sortList = new ArrayList<>();
    for (int c: allConcepts){
      sortList.add(new ScorePair(c, scores.get(c)));
    }
    Collections.sort(sortList);
    return sortList;
  }

  private Map<Integer, Set<Set<Integer>>> generateReverseGroupMap (Set<Set<Integer>> groupMap,
                                                                   Map<Integer, Set<Set<Integer>>> reverseMapGroups){
    reverseMapGroups.clear();
    for (Set<Integer> group : groupMap){
      for (Integer c: group){
        Set<Set<Integer>> groups=reverseMapGroups.get(c);
        if (groups==null){
          groups=new HashSet<>();
          reverseMapGroups.put(c, groups);
        }
        groups.add(group);
      }
    }
    return reverseMapGroups;
  }

}
