package de.uni_leipzig.dbs.formRepository.selection.scorer.data;

import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.Set;

/**
 * Created by christen on 11.04.2017.
 */
public class LocalScoreContext {


  private LocalScoreContext (Int2FloatMap idfMap, Int2ObjectMap<String> posTags){
    this.idfMap = idfMap;
    this.posTags = posTags;
  }

  public Int2FloatMap getIdfMap() {
    return idfMap;
  }

  public void setIdfMap(Int2FloatMap idfMap) {
    this.idfMap = idfMap;
  }

  public Int2ObjectMap<String> getPosTags() {
    return posTags;
  }



  public void setPosTags(Int2ObjectMap<String> posTags) {
    this.posTags = posTags;
  }

  private Int2FloatMap idfMap;

  private Int2ObjectMap<String> posTags;


  public static class Builder{
    Int2FloatMap idfMap;
    Int2ObjectMap<String> posTags;

    public Builder idfMap(Int2FloatMap idfMap){
      this.idfMap = idfMap;
      return this;
    }

    public Builder posTags(Int2ObjectMap<String> posTags){
      this.posTags = posTags;
      return this;
    }

    public LocalScoreContext build(){
      LocalScoreContext lsc = new LocalScoreContext(idfMap, posTags);
      return lsc;
    }
  }
}
