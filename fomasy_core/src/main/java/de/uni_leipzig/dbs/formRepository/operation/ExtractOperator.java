package de.uni_leipzig.dbs.formRepository.operation;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Created by christen on 22.02.2017.
 */
public class ExtractOperator {

  public static EncodedEntityStructure extractUnannotatedEntities(EncodedEntityStructure ees, AnnotationMapping am){
    IntSet srcIds = new IntOpenHashSet();
    for (EntityAnnotation ea: am.getAnnotations()){
      srcIds.add(ea.getSrcId());
    }
    IntSet notInMapping = new IntOpenHashSet();
    for (int entId: ees.getObjIds().keySet()){
      if (!srcIds.contains(entId)){
        notInMapping.add(entId);
      }
    }
    EncodedEntityStructure eesExtract = EncodingManager.getInstance().getSubset(ees, notInMapping);
    return eesExtract;
  }

}
