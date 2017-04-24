package de.uni_leipzig.dbs.formRepository.selection.scorer.local;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.selection.scorer.data.LocalScoreContext;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This scorer rewards candidates consisting of a synonym that match completely or has only uninformative unmatched
 * words
 */

public class IDFUnmatchedScorer extends AbstractLocalScorer{

  @Override
  public Map<Integer, Map<Integer, Double>> computeScore(AnnotationMapping am,EncodedEntityStructure form,
                                                         EncodedEntityStructure ontology,
     LocalScoreContext ctx) {
    Map<Integer, Map<Integer, Double>> ranking = new HashMap<>();
    Map<Integer, Set<EntityAnnotation>> groupPerItem = this.groupBySrcEntity(am);
    for (Map.Entry<Integer, Set<EntityAnnotation>> e: groupPerItem.entrySet()){
      Map<Integer,Double> rankPerItem = new HashMap<>();
      for (EntityAnnotation ea: e.getValue()){
        int cid = ea.getTargetId();
        Set<Integer> evidence = am.getEvidenceMap().get(ea.getId());
        Set<Integer> uw = this.getUnmatchedWords(ontology.getPropertyValues(cid),evidence);
        rankPerItem.put(ea.getTargetId(), this.score(uw, ctx.getIdfMap()));
      }
    }
    return ranking;
  }


  private double score(Set<Integer> unmatchedTokens, Int2FloatMap idfMap){
    double score =0;
    for (int t :unmatchedTokens){
      score+=idfMap.get(t);
    }
    score = 1d/1d+score;
    return score;
  }

  private Set<Integer> getUnmatchedWords (int[][][] pvs, Set<Integer> evidence){
    Set<Integer> temp = new HashSet<>();
    int match = 0;
    int max = 0;
    for (int[][] pv : pvs){
      for (int[] tokenIds : pv){
        for (int t: tokenIds){
          if (evidence.contains(t)){
            match ++;
          }
        }
        if (max> match){
          temp.clear();
          for (int t:tokenIds){
            temp.add(t);
          }
        }
      }
    }
    temp.removeAll(evidence);
    return temp;
  }
}
