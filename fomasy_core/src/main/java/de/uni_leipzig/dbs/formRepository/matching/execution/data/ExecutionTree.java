package de.uni_leipzig.dbs.formRepository.matching.execution.data;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class ExecutionTree {

  
  private Queue<Operator> processingQueue ;
  
  
  
  
  public ExecutionTree (){
    this.processingQueue = new LinkedList<Operator>();
  }
  
  
  public void addOperator (Operator op){
    this.processingQueue.add(op);
  }
  
  public Operator processOperator (){
    return this.processingQueue.poll();
  }
  
  public boolean isProcessQueueEmpty(){
    return this.processingQueue.isEmpty();
  }
}
