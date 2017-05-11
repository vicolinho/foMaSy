package de.uni_leipzig.dbs.formRepository.selection.scorer.local;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.selection.scorer.data.LocalScoreContext;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by christen on 12.04.2017.
 */
public class SequenceScorer extends AbstractLocalScorer {


  Logger log = Logger.getLogger(getClass());

  @Override
  public Map<Integer, Map<Integer, Double>> computeScore(AnnotationMapping am, EncodedEntityStructure form,
                                                         EncodedEntityStructure ontology, LocalScoreContext ctx) {
    Map<Integer, Map<Integer, Double>> ranking = new HashMap<>();
    Long2ObjectMap<Set<Integer>> evidenceMap = am.getEvidenceMap();
    Map<Integer, Set<EntityAnnotation>> groupPerItem = this.groupBySrcEntity(am);
    for (Map.Entry<Integer, Set<EntityAnnotation>> e: groupPerItem.entrySet()){
      Map<Integer,Double> rankPerItem = new HashMap<>();
      for(EntityAnnotation ea: e.getValue()){
        Set<Integer> evidence = evidenceMap.get(ea.getId());
        if (evidence != null) {
          double score = this.computeSequenceScore(evidence, form.getPropertyValues(ea.getSrcId()));
          rankPerItem.put(ea.getTargetId(), score);
        }else {
          rankPerItem.put(ea.getTargetId(), 0d);
        }
      }
      ranking.put(e.getKey(), rankPerItem);
    }
    return ranking;
  }

  public double computeSequenceScore(Set<Integer> evidence, int[][][] pv){

    List<Integer> matchedValue = this.getMatchedValue(evidence, pv);
    int maxSequence = 0;
    int currentSequence = 0;
    int oldMatchedIndex =0;
    for(int i = 0; i<matchedValue.size();i++){
      int t = matchedValue.get(i);
      if (evidence.contains(t)){
        if ((oldMatchedIndex+1) ==i){
          currentSequence++;
        }else {
          if (currentSequence>maxSequence)
            maxSequence =currentSequence;
          currentSequence =0;
        }
        oldMatchedIndex = i;
      }
    }
    return maxSequence/(double)matchedValue.size();
  }


  public List<Integer> getMatchedValue (Set<Integer> evidence, int[][][] pvs){
    List<Integer> temp = new ArrayList<>();
    int match;
    double max = 0;
    for (int[][] pv : pvs){
      for (int[] tokenIds : pv){
        match =0;
        for (int t: tokenIds){
          if (evidence.contains(t)){
            match ++;
          }
        }
        if ((match/(double)tokenIds.length)>max){
          max = match/(double)tokenIds.length;
          temp.clear();
          for (int t: tokenIds)
            temp.add(t);
        }
      }
    }
    return temp;
  }
}
