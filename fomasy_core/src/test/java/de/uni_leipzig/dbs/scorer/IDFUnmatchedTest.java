package de.uni_leipzig.dbs.scorer;

import de.uni_leipzig.dbs.formRepository.selection.scorer.local.IDFUnmatchedScorer;
import de.uni_leipzig.dbs.formRepository.selection.scorer.local.SequenceScorer;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by christen on 30.04.2017.
 */
public class IDFUnmatchedTest {

  static int[][][] pvs = new int[][][]{{{0,1,2,3}}};

  static Set<Integer> evidence= new HashSet<>(Arrays.asList(new Integer[]{1,2}));

  Int2FloatMap idfMap = new Int2FloatOpenHashMap();


  @Before
  public void init(){
    idfMap.put(0,0.5f);
    idfMap.put(1,0.7f);
    idfMap.put(2,0.8f);
    idfMap.put(3,0.3f);
  }


  @Test
  public void test(){

    IDFUnmatchedScorer sc = new IDFUnmatchedScorer();
    double score = sc.score(pvs, evidence, idfMap);
    double expected = 1/(1d+(Math.sqrt(0.5d+0.3d)));
    Assert.assertEquals(expected,score,0.0001);
  }
}
