package de.uni_leipzig.dbs.formRepository.matching.selection;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;

public class SelectionOperator {

  public SelectionOperator() {
  }
  
  
  public AnnotationMapping select(AnnotationMapping am, SelectionType type,float threshold, float delta){
    SelectionFactory fac = SelectionFactory.getInstance();
    return fac.getSelectionOperator(type).select(am,threshold,delta);
    
    
  }


}
