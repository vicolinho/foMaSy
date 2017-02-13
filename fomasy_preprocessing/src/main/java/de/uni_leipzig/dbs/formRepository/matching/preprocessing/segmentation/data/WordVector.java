package de.uni_leipzig.dbs.formRepository.matching.preprocessing.segmentation.data;

import org.apache.commons.math3.ml.clustering.Clusterable;

/**
 * Created by christen on 27.01.2017.
 */
public class WordVector implements Clusterable {


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  int id;

  private double[] vec;

  public WordVector (double[] vec, int id){
    this.vec = vec;
    this.setId(id);
  }
  @Override
  public double[] getPoint() {
    return vec;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    WordVector that = (WordVector) o;

    return id == that.id;

  }

  @Override
  public int hashCode() {
    return id;
  }
}
