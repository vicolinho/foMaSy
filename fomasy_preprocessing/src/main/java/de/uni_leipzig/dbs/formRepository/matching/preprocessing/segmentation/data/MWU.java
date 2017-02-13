package de.uni_leipzig.dbs.formRepository.matching.preprocessing.segmentation.data;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Created by christen on 06.02.2017.
 */
public class MWU {

  private IntSet tokenIds;


  public MWU (){
    this.tokenIds = new IntOpenHashSet();
  }

  public MWU (int tokenId){
    this.tokenIds = new IntOpenHashSet();
    this.tokenIds.add(tokenId);
  }

  public void addMWU (MWU mwu){
    this.tokenIds.addAll(mwu.tokenIds);
  }

  public IntSet getTokenIds(){
    return tokenIds;
  }


}
