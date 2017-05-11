package de.uni_leipzig.dbs.formRepository.selection.algorithm;

/**
 * Created by christen on 29.04.2017.
 */
public abstract class AbstractSelectionAlgorithm implements ISelectionAlgorithm{

  public AbstractSelectionAlgorithm(){
  }

  protected class ScorePair implements Comparable<ScorePair>{

    double score;

    int conceptId;


    ScorePair(int conceptId ,double score){
      this.score = score;
      this.conceptId = conceptId;
    }
    @Override
    public int compareTo(ScorePair o) {
      if (this.score <o.score){
        return 1;
      }else if (this.score>o.score){
        return -1;
      }else return 0;
    }

    @Override
    public String toString() {
      return "ScorePair{" +
              "score=" + score +
              ", conceptId=" + conceptId +
              '}';
    }
  }
}
