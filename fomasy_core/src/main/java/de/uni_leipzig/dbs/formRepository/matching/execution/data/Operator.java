package de.uni_leipzig.dbs.formRepository.matching.execution.data;

public abstract class Operator {

  public static final int MATCH = 0;
  
  public static final int SET =1;
  
  public static final int GROUP =2;
  
  public static final int RESULT = 3;
  
  protected int type;

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }
  
  
}
