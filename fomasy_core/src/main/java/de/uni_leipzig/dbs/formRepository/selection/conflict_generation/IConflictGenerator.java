package de.uni_leipzig.dbs.formRepository.selection.conflict_generation;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.selection.conflict_generation.data.GenerationContext;

import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 10.04.2017.
 */
public interface IConflictGenerator {

  /**
   *
   * @return for each src entity
   */
  Map<Integer, Set<Set<Integer>>> getConflictAnnotations(AnnotationMapping am, EncodedEntityStructure ontology,
                                                         GenerationContext context);


}
