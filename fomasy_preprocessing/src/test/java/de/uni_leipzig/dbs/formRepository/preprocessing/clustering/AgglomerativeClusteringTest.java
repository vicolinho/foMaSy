package de.uni_leipzig.dbs.formRepository.preprocessing.clustering;

import de.uni_leipzig.dbs.formRepository.matching.preprocessing.segmentation.algorithms.AgglomerativeClustering;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.segmentation.data.MWU;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by christen on 07.02.2017.
 */
public class AgglomerativeClusteringTest {

  private double[][] simMatrix;
  private double[][] simMatrix2;
  public Int2ObjectMap<MWU> mwuMap;
  AgglomerativeClustering clustering = new AgglomerativeClustering();

  @Before
  public void init (){
    simMatrix = new double[][]
            {{0d,0.5d,0.3d,0.6d},
             {0d,0d  ,0.8d,0.4d},
             {0d,0d  ,0d  ,0.1d},
             {0d,0d  ,0d  ,0d  }};
    simMatrix2 = new double[][]
            {{0d,0.5d,0.3d,0.8d},
              {0d,0d  ,0.6d,0.4d},
              {0d,0d  ,0d  ,0.1d},
              {0d,0d  ,0d  ,0d  }};
    MWU mwu1 = new MWU(1);
    MWU mwu2 = new MWU(2);
    MWU mwu3 = new MWU(3);
    MWU mwu4 = new MWU(4);
    mwuMap = new Int2ObjectOpenHashMap();
    mwuMap.put(0,mwu1);
    mwuMap.put(1,mwu2);
    mwuMap.put(2,mwu3);
    mwuMap.put(3,mwu4);
  }


  @Test
  public void testBest(){
    int[] position=clustering.getIndexForBestSim(mwuMap,simMatrix);
    Assert.assertEquals(1,position[0]);
    Assert.assertEquals(2,position[1]);
  }

  @Test
  public void testMerge(){
    mwuMap = clustering.merge(mwuMap, new int[]{1,2});
    Assert.assertEquals(3,mwuMap.size());
    Assert.assertEquals(2, mwuMap.get(1).getTokenIds().size());
  }

  @Test
  public void testUpate(){
    double[][]newMatrix = clustering.update(simMatrix,  new int[]{1,2});
    double[][]expectedMatrix = new double[][]
            {{0d,0.8d, 0d,0.6d},
              {0d,0d,  0d,0.5d},
              {0d,0d,0d,0d},
              {0d,0d,0d,0d}};
    for (int i =0;i<newMatrix.length;i++) {
      double[] v = newMatrix[i];
      System.out.println(Arrays.toString(v));
      Assert.assertEquals(Arrays.toString(expectedMatrix[i]),Arrays.toString(v));
    }
    double[][]expectedMatrix2 = new double[][]
    {{0d,0.9d, 0.4d,0.0d},
      {0d,0d,  0.6d,0.0d},
      {0d,0d,0d,0d},
      {0d,0d,0d,0d}};
    double[][]newMatrix2 = clustering.update(simMatrix2,  new int[]{0,3});

    for (int i =0;i<newMatrix2.length;i++) {
      double[] v = newMatrix2[i];
      System.out.println(Arrays.toString(v));
      Assert.assertEquals(Arrays.toString(expectedMatrix2[i]),Arrays.toString(v));
    }

  }
}
