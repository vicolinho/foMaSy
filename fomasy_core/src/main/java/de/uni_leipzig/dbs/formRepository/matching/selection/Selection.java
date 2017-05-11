package de.uni_leipzig.dbs.formRepository.matching.selection;

import java.util.Set;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;

public interface Selection {

  
  AnnotationMapping select(AnnotationMapping am, float threshold, float delta);
  
  


  AnnotationMapping select(AnnotationMapping am, EncodedEntityStructure src,
      EncodedEntityStructure target, Set<GenericProperty> preDomAtts,
      Set<GenericProperty> preRanAtts, float threshold, float delta,
      float avgEntitySize, FormRepository rep);
}
