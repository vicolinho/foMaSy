package de.uni_leipzig.dbs.formRepository.selection.scorer.local;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.selection.scorer.data.LocalScoreContext;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.util.*;

/**
 * Created by christen on 12.04.2017.
 */
public class GapScorer extends AbstractLocalScorer {
  @Override
  public Map<Integer, Map<Integer, Double>> computeScore(AnnotationMapping am, EncodedEntityStructure form,
                                                         EncodedEntityStructure ontology, LocalScoreContext ctx) {
    Map<Integer, Map<Integer, Double>> ranking = new HashMap<>();
    Long2ObjectMap<Set<Integer>> evidenceMap = am.getEvidenceMap();
    Map<Integer, Set<EntityAnnotation>> groupPerItem = this.groupBySrcEntity(am);
    for (Map.Entry<Integer, Set<EntityAnnotation>> e: groupPerItem.entrySet()){
      for(EntityAnnotation ea: e.getValue()){
        this.computeGapScore(evidenceMap.get(ea.getId()), form.getPropertyValues(ea.getSrcId()));
      }
    }
    return ranking;
  }

  private double computeGapScore(Set<Integer> evidence, int[][][] pv){
    return 0d;
  }


  private List<String> getMatchedValue (Set<Integer> evidence, int[][][] pvs){
    List temp = new ArrayList<>();
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
          temp = Arrays.asList(tokenIds);
        }
      }
    }
    return temp;
  }
}
