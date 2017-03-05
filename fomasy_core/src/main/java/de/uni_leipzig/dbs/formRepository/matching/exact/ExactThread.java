package de.uni_leipzig.dbs.formRepository.matching.exact;

import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.AbstractPartMatcher;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;
import de.uni_leipzig.dbs.formRepository.matching.token.LCSThread;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by christen on 22.02.2017.
 */
public class ExactThread extends AbstractPartMatcher{

  Logger log = Logger.getLogger(LCSThread.class);

  EncodedEntityStructure srcStructure;

  EncodedEntityStructure targetStructure;

  AggregationFunction aggFunc;

  IntSet srcIds;

  IntSet targetIds;

  float threshold;

  Pruning pruning;

  private Set<Integer> propertyIds1;

  private Set<Integer> propertyIds2;

  public ExactThread(EncodedEntityStructure source,
                   EncodedEntityStructure target, IntSet partObjIds, IntSet targetSet,
                   AggregationFunction function, float threshold,
                   Set<Integer> propertyIds1, Set<Integer> propertyIds2,
                   Pruning pruning) {
    super(1f);
    this.srcStructure = source;
    this.targetStructure = target;
    this.aggFunc = function;
    this.pruning = pruning;
    this.propertyIds1 = propertyIds1;
    this.propertyIds2 = propertyIds2;
    this.srcIds = partObjIds;
    this.targetIds =targetSet;
  }

  @Override
  public void run(){
    List<Float> sims = new ArrayList<>();
    for (int srcId :srcIds){
      for (int targetId:targetIds){
        sims.clear();
        for (int srcProps : propertyIds1){
          int[][] srcProperty = srcStructure.getPropertyValues(srcId)[srcProps];
          for (int targetProp : propertyIds2){
            int[][] targetProperty = targetStructure.getPropertyValues(targetId)[targetProp];
            for (int[] srcPv : srcProperty){
              for (int[] targetPv: targetProperty){
                boolean equal = Arrays.equals(srcPv, targetPv);
                if (equal)
                  sims.add(1f);
              }
            }
          } // each target property
        }// each source property
        float sim = aggFunc.aggregateFloatList(sims);
        this.addResult(srcStructure.getStructureId(), targetStructure.getStructureId(),
                 srcId, targetId, sim);
      }
    }
  }
}
