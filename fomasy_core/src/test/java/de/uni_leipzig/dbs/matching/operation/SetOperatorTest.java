package de.uni_leipzig.dbs.matching.operation;

import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.SetOperator;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by christen on 27.01.2017.
 */
public class SetOperatorTest {

  private Long2FloatMap map1;

  private Long2FloatMap map2;

  private Long2FloatMap map3;

  private Long2FloatMap result;

  @Before
  public void init (){
    map1 = new Long2FloatOpenHashMap();
    map1.put(1l,0.6f);
    map1.put(2l,0.7f);
    map1.put(3l,0.6f);
    map2 = new Long2FloatOpenHashMap();
    map2.put(3l,0.8f);
    map2.put(4l,0.7f);
    map2.put(5l,0.6f);
    map3 = new Long2FloatOpenHashMap();
    map3.put(5l,0.8f);
    map3.put(6l,0.7f);
    map3.put(7l,0.6f);
    SetOperator so = new SetOperator(AggregationFunction.MAX, SetOperator.UNION);
    result = so.setOperation(map1, map2, map3);
  }

  @Test
  public void testResultSize(){
    Assert.assertEquals(7, result.size());
  }

  @Test
  public void testElementSims(){
    Assert.assertEquals(0.8f,result.get(3l),0f);
    Assert.assertEquals(0.8f,result.get(5l),0f);
    Assert.assertEquals(0.6f,result.get(1l),0f);

  }


}
