package de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.impl;

import de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking.ClusterElement;

/**
 * Created by christen on 14.02.2017.
 */
public class PropertyElement implements ClusterElement {


  private int entityId;

  private int propId;

  private int[] tokens;


  public PropertyElement(int entityId, int propId, int[] tokens){
    this.entityId = entityId;
    this.propId = propId;
    this.tokens = tokens;
  }


  @Override
  public int getPropertyId() {
    return propId;
  }

  @Override
  public int getEntityId() {
    return entityId;
  }

  @Override
  public int[] getTokens() {
    return tokens;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PropertyElement that = (PropertyElement) o;

    return propId == that.propId;

  }

  @Override
  public int hashCode() {
    return propId;
  }

  @Override
  public String toString() {
    return "PropertyElement{" +
            "propId=" + propId +
            ", entityId=" + entityId +
            '}';
  }
}
