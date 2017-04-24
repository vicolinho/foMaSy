package de.uni_leipzig.dbs.formRepository.selection.conflict_generation;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;


import java.util.*;

/**
 * Created by christen on 10.04.2017.
 */
public abstract class AbstractConflictGenerator implements IConflictGenerator{



  protected FormRepository rep;

  public AbstractConflictGenerator(FormRepository rep){
    this.rep = rep;
  }

  protected Map<Integer,Set<EntityAnnotation>> groupBySrcEntity (AnnotationMapping am){
    Map<Integer,Set<EntityAnnotation>> groups = new HashMap<>();
    for (EntityAnnotation cor:am.getAnnotations()){
      Set<EntityAnnotation> umlsCon = groups.get(cor.getSrcId());
      if (umlsCon==null){
        umlsCon = new HashSet<>();
        groups.put(cor.getSrcId(), umlsCon);
      }
      umlsCon.add(cor);
    }
    return groups;
  }

  /**
   * extract the concepts of an encoded entity strcuture depending oh the entitites that are involved in the annotation
   * mapping
   * @param set annotation
   * @param umls encoded entity structrue
   * @param isTarget what should be extracted
   * @return
   */
  protected EncodedEntityStructure getConcepts(Set<EntityAnnotation> set, EncodedEntityStructure umls, boolean isTarget){
    Set<Integer> targetIds  = new HashSet<Integer>();
    for (EntityAnnotation cor:set){
      if (isTarget)
        targetIds.add(cor.getTargetId());
      else {
        targetIds.add(cor.getSrcId());
      }
    }
    EncodedEntityStructure extract = EncodingManager.getInstance().getSubset(umls, targetIds);
    return extract;
  }

}
