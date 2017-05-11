package de.uni_leipzig.dbs.formRepository.matching.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;

public class ThresholdSelection implements Selection {

  public ThresholdSelection() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public AnnotationMapping select(AnnotationMapping am, float threshold,
      float delta) {
    AnnotationMapping filter = new AnnotationMapping ();
    filter.setMethod(am.getMethod());filter.setSrcVersion(am.getSrcVersion());filter.setTargetVersion(am.getTargetVersion());
    
    for (EntityAnnotation ea :am.getAnnotations()){
      if (ea.getSim() >=threshold){
        filter.addAnnotation(ea);
      }
    }
    
    
    return filter;
  }

  @Override
  public AnnotationMapping select(AnnotationMapping am,
      EncodedEntityStructure src, EncodedEntityStructure target,
      Set<GenericProperty> preDomAtts, Set<GenericProperty> preRanAtts,
      float threshold, float delta, float avgEntitySize,
      FormRepository rep) {
    // TODO Auto-generated method stub
    return null;
  }

  


}
