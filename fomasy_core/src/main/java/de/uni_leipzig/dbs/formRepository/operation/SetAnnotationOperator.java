package de.uni_leipzig.dbs.formRepository.operation;

import java.util.ArrayList;
import java.util.List;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;

/**
 * This class implements the set operations (union, intersection, difference) for two annotation mappings
 * @author christen
 *
 */
public class SetAnnotationOperator {

  
  public static AnnotationMapping union(AggregationFunction func,
      AnnotationMapping mapping1, AnnotationMapping mapping2){
    
    AnnotationMapping unionMapping = new AnnotationMapping(mapping1.getSrcVersion(),mapping1.getTargetVersion());
    
    for (EntityAnnotation ea:mapping1.getAnnotations()){
      if (!mapping2.contains(ea)){
        EntityAnnotation copy = new EntityAnnotation(ea.getSrcId(), ea.getTargetId(),
          ea.getSrcAccession(), ea.getTargetAccession(), ea.getSim(), ea.isVerfified());
        unionMapping.addAnnotation(copy);
      }else {
        List<Float> sims = new ArrayList<Float>();
        sims.add(ea.getSim());
        sims.add(mapping2.getAnnotation(ea.getId()).getSim());
        float aggSim = func.aggregateFloatList(sims);
        EntityAnnotation copy = new EntityAnnotation(ea.getSrcId(), ea.getTargetId(),
            ea.getSrcAccession(), ea.getTargetAccession(), aggSim, ea.isVerfified());
          unionMapping.addAnnotation(copy);
      }
    }
    for (EntityAnnotation ea:mapping2.getAnnotations()){
      if (!unionMapping.contains(ea))
        if (!mapping1.contains(ea)){
          EntityAnnotation copy = new EntityAnnotation(ea.getSrcId(), ea.getTargetId(),
            ea.getSrcAccession(), ea.getTargetAccession(), ea.getSim(), ea.isVerfified());
          unionMapping.addAnnotation(copy);
        }
    }
    return unionMapping;
    
  }
  
  
  public static AnnotationMapping intersect(AggregationFunction func,
      AnnotationMapping mapping1, AnnotationMapping mapping2){
    AnnotationMapping unionMapping = new AnnotationMapping(mapping1.getSrcVersion(),mapping1.getTargetVersion());
    for (EntityAnnotation ea:mapping1.getAnnotations()){
      if (mapping2.contains(ea)){
        List<Float> sims = new ArrayList<Float>();
        sims.add(ea.getSim());
        sims.add(mapping2.getAnnotation(ea.getId()).getSim());
        float aggSim = func.aggregateFloatList(sims);
        EntityAnnotation copy = new EntityAnnotation(ea.getSrcId(), ea.getTargetId(),
          ea.getSrcAccession(), ea.getTargetAccession(),aggSim, ea.isVerfified());
        unionMapping.addAnnotation(copy);
      }
    }
    return unionMapping;
  }
  
  
  public static AnnotationMapping diff(
      AnnotationMapping mapping1, AnnotationMapping mapping2){
    AnnotationMapping unionMapping = new AnnotationMapping(mapping1.getSrcVersion(),mapping1.getTargetVersion());
    for (EntityAnnotation ea:mapping1.getAnnotations()){
      if (!mapping2.contains(ea)) {
        EntityAnnotation copy = new EntityAnnotation(ea.getSrcId(), ea.getTargetId(),
            ea.getSrcAccession(), ea.getTargetAccession(), ea.getSim(), ea.isVerfified());
          unionMapping.addAnnotation(copy);
      }
    }
    return unionMapping;
  }
  
  
  
  
  
  
}
