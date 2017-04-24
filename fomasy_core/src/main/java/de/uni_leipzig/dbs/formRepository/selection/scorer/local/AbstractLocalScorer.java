package de.uni_leipzig.dbs.formRepository.selection.scorer.local;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 11.04.2017.
 */
public abstract class AbstractLocalScorer implements LocalScorer{

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
}
