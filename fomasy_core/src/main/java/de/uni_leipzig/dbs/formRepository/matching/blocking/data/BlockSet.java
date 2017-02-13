package de.uni_leipzig.dbs.formRepository.matching.blocking.data;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.BitSet;
import java.util.Set;

/**
 * Created by christen on 01.11.2016.
 */
public class BlockSet {

  private BitSet toComparingPairs;

  private int targetSize;

  private int srcSize;

  private int maxSize;


  public BlockSet(int srcSize, int targetSize){
    toComparingPairs = new BitSet(srcSize*targetSize);
    maxSize = Math.max(srcSize, targetSize);
  }



  public void setToComparePair (int srcId, int targetId){
    int pairId = srcId*maxSize+targetId;
    toComparingPairs.set(pairId, true);
  }


  public boolean isPairToCompare (int srcId, int targetId){
    int pairId = srcId*maxSize+targetId;
    return toComparingPairs.get(pairId);
  }

  public int cardinality(){
    return toComparingPairs.cardinality();
  }
}
