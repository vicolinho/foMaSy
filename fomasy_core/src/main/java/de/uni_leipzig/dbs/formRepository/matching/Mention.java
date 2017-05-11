package de.uni_leipzig.dbs.formRepository.matching;

import java.util.Set;

public class Mention {

  private Set<Integer> mention;
  
  private float sim;

  public Set<Integer> getMention() {
    return mention;
  }

  public void setMention(Set<Integer> mention) {
    this.mention = mention;
  }

  public float getSim() {
    return sim;
  }

  public void setSim(float sim) {
    this.sim = sim;
  }
}
