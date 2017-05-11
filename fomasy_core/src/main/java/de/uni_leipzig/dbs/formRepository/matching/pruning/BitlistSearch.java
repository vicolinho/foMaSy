package de.uni_leipzig.dbs.formRepository.matching.pruning;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class BitlistSearch extends Thread {

  private Integer[][] objIdArray;
  private List<BitSet> partList;
  private BitSet itemBitList;
  private Set<Integer>  result;
  private float minOverlapRatio;
  private int bitListSize;
  private int  maxOverlap;
  private TreeMap<Integer, HashSet<Integer>> topMap;
  
  public BitlistSearch (List<BitSet> partList, Integer [][] objIdArray, BitSet itemBits,float minOverlapRatio,int size){
    this.partList = partList ;
    this.objIdArray = objIdArray;
    this.itemBitList = itemBits;
    this.minOverlapRatio = minOverlapRatio;
    this.bitListSize = size;
  }
  
  public BitlistSearch(ArrayList<BitSet> arrayList, Integer[][] objPart,
      BitSet objBitList, float f, int bitListLength) {
    result = new HashSet<Integer>();
    int minOverlap = (int) Math.ceil(itemBitList.cardinality()*minOverlapRatio);
    //log.info(minOverlap);
    maxOverlap =0;
    //topMap = new TreeMap<Integer,HashSet<Integer>> ();
    
    if (minOverlap!=0){
      for (int i= 0;i<this.partList.size();i++){
        BitSet intersect= new BitSet(bitListSize);
        intersect.or(itemBitList);
        intersect.and(this.partList.get(i));
        if (intersect.cardinality()>=minOverlap
            ){
          //maxOverlap = intersect.cardinality();
          Integer[] objArray = this.objIdArray[i];
          //HashSet <Integer> set = new HashSet<Integer> ();
          for (int id : objArray){
            result.add(id);
          }
          //topMap.put(maxOverlap, set);
        }
      }
    }
  }

  public Set<Integer> getResult() {
    return result;
  }

}
