package de.uni_leipzig.dbs.formRepository.selection.conflict_generation.data;

import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;

import java.util.Set;

/**
 * Created by christen on 10.04.2017.
 */
public class GenerationContext {

  protected Set<GenericProperty> groupingAttributes;

  protected float sim_threshold;

  protected String sim_func;


  public Set<GenericProperty> getGroupingAttributes() {
    return groupingAttributes;
  }

  public float getSim_threshold() {
    return sim_threshold;
  }

  public String getSim_func() {
    return sim_func;
  }

  public static class Builder {

    private Builder instance;

    private Set<GenericProperty> groupingAttributes;

    private float sim_threshold;

    private String sim_func;

    public Builder (){
    }

    public Builder groupAttributes(Set<GenericProperty> groupingAttributes){
      this.groupingAttributes = groupingAttributes;
      return this;
    }

    public Builder simThreshold (float sim){
      this.sim_threshold = sim;
      return this;
    }

    public Builder simFunc (String sim_func){
      this.sim_func = sim_func;
      return this;
    }

    public GenerationContext build(){
      GenerationContext ctx = new GenerationContext();
      if (groupingAttributes!=null)
        ctx.groupingAttributes = groupingAttributes;
      if (sim_threshold !=0){
        ctx.sim_threshold = sim_threshold;
      }
      if (sim_func != null){
        ctx.sim_func = sim_func;
      }
      return ctx;
    }
  }

}


