package de.uni_leipzig.dbs.formRepository.selection.scorer.local;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.selection.scorer.data.LocalScoreContext;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * This scorer rewards candidates consisting of a synonym that match completely or has only uninformative unmatched
 * words
 */

public class IDFUnmatchedScorer extends AbstractLocalScorer{

  Logger log = Logger.getLogger(getClass());

  @Override
  public Map<Integer, Map<Integer, Double>> computeScore(AnnotationMapping am, EncodedEntityStructure form,
                                                         EncodedEntityStructure ontology, LocalScoreContext ctx) {
    Map<Integer, Map<Integer, Double>> ranking = new HashMap<>();
    Map<Integer, Set<EntityAnnotation>> groupPerItem = this.groupBySrcEntity(am);
    for (Map.Entry<Integer, Set<EntityAnnotation>> e: groupPerItem.entrySet()){
      Map<Integer,Double> rankPerItem = new HashMap<>();
      for (EntityAnnotation ea: e.getValue()){
        int cid = ea.getTargetId();
        Set<Integer> evidence = am.getEvidenceMap().get(ea.getId());
        if (evidence!= null) {
          double score = this.score(ontology.getPropertyValues(cid), evidence, ctx.getIdfMap());
          rankPerItem.put(ea.getTargetId(), score);
        }else {
          rankPerItem.put(ea.getTargetId(), 0d);
        }
      }
      ranking.put(e.getKey(),rankPerItem);
    }
    return ranking;
  }


  public double score(int[][][] pvs, Set<Integer> evidence, Int2FloatMap idfMap){
    Set<Integer> unmatchedTokens = this.getUnmatchedWords(pvs, evidence);
    double score =0;
    for (int t :unmatchedTokens){
      score+=idfMap.get(t);
    }
    score = 1d/(1d+Math.sqrt(score));
    return score;
  }

  private Set<Integer> getUnmatchedWords (int[][][] pvs, Set<Integer> evidence){
    Set<Integer> temp = new HashSet<>();
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
    temp.removeAll(evidence);
    return temp;
  }
}
