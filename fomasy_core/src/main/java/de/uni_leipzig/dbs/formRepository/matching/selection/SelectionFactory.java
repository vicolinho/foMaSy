package de.uni_leipzig.dbs.formRepository.matching.selection;

public class SelectionFactory {

  private static SelectionFactory instance;


  public SelectionFactory() {
    // TODO Auto-generated constructor stub
  }
  
  
  public Selection getSelectionOperator(SelectionType type){
    Selection selection = null;
    switch (type){
    case MAX_DELTA_ONE_DIRECTION: 
       selection = new MaxDeltaOneDirection();
      break;
    case THRESHOLD:
       selection = new ThresholdSelection();
      break;
    case GROUPSELECTION:  
      selection = new GroupSelection();
      break;
    default:return null;
      
    }
    return selection;
  }
  
  public GraphBasedSelection getGraphSelectionOperator(SelectionType type){
    GraphBasedSelection selection = null;
    switch (type){
    case COOCCURRENCE_SELECTION: 
       selection = new CooccurrenceAnnotationSelection();
      break;
    default:return null;
      
    }
    return selection;
  }

  
  public static SelectionFactory getInstance(){
    if (instance ==null){
      instance = new SelectionFactory();
    }
    return instance;
  }
}
