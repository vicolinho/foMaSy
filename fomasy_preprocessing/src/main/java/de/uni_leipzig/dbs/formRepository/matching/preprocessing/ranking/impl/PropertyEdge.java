package de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.impl;

/**
 * Created by christen on 15.02.2017.
 */
public class PropertyEdge {

  private int src;

  private int target;

  private float sim;

  public PropertyEdge(int src, int target, float sim) {
    this.src = src;
    this.target = target;
    this.sim = sim;
  }

  public int getSrc() {
    return src;
  }

  public void setSrc(int src) {
    this.src = src;
  }

  public int getTarget() {
    return target;
  }

  public void setTarget(int target) {
    this.target = target;
  }

  public float getSim() {
    return sim;
  }

  public void setSim(float sim) {
    this.sim = sim;
  }
}
