package de.uni_leipzig.dbs.scorer;

import de.uni_leipzig.dbs.formRepository.selection.scorer.local.SequenceScorer;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by christen on 30.04.2017.
 */
public class SequenceScorerTest {

  static int[][][] pvs = new int[][][]{{{0,1,2,3,4,1}}};

  static Set<Integer> evidence= new HashSet<>(Arrays.asList(new Integer[]{1,2}));


  @Test
  public void test(){

    SequenceScorer sc = new SequenceScorer();
    List<Integer> value = sc.getMatchedValue(evidence, pvs);
    Assert.assertEquals(6, value.size());
    double score = sc.computeSequenceScore(evidence, pvs);
    Assert.assertEquals(2d/6d,score,0.0001);
  }
}
