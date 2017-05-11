package de.uni_leipzig.dbs.formRepository.matching.holistic.clustering;

public class SimilarCluster implements Comparable<SimilarCluster>{

  private float sim;
  
  public SimilarCluster(float sim, int clusterId, int clusterId2) {
    super();
    this.sim = sim;
    this.clusterId = clusterId;
    this.correspondingCluster = clusterId2;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    int min = Math.min(clusterId, correspondingCluster);
    int max = (min ==clusterId)?correspondingCluster:clusterId;
    result = prime * result + min;
    result = prime * result + max;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SimilarCluster other = (SimilarCluster) obj;
    int min = Math.min(clusterId, correspondingCluster);
    int max = (min ==clusterId)?correspondingCluster:clusterId;
    int min2 =Math.min(other.clusterId, other.correspondingCluster);
    int max2 =(min ==other.clusterId)?other.correspondingCluster:other.clusterId;
    if (min != min2)
      return false;
    return max == max2;
  }

  private int clusterId;

  private int correspondingCluster;
  public float getSim() {
    return sim;
  }

  public void setSim(float sim) {
    this.sim = sim;
  }

  public int getClusterId() {
    return clusterId;
  }

  public void setClusterId(int clusterId) {
    this.clusterId = clusterId;
  }

  public int getCorrespondingCluster() {
    return correspondingCluster;
  }

  public void setCorrespondingCluster(int correspondingCluster) {
    this.correspondingCluster = correspondingCluster;
  }

  @Override
  public int compareTo(SimilarCluster o) {
    Float f1= this.sim;
    Float f2 = o.sim;
    return f1.compareTo(f2);
  }
}
