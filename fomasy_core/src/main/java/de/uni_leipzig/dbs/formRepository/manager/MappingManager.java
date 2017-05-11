package de.uni_leipzig.dbs.formRepository.manager;

import java.util.Map;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.exception.ImportAnnotationException;
import de.uni_leipzig.dbs.formRepository.util.MappingImportMetadata;

public interface MappingManager {

  void importExternalAnnotation(MappingImportMetadata metadata) throws ImportAnnotationException;

  void importAnnotation(AnnotationMapping mapping);
  
  AnnotationMapping getAnnotationMapping(VersionMetadata src, VersionMetadata target,String name);
}
