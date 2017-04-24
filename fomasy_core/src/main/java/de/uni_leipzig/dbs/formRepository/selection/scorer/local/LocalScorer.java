package de.uni_leipzig.dbs.formRepository.selection.scorer.local;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.selection.scorer.data.LocalScoreContext;

import java.util.Map;

/**
 * Created by christen on 11.04.2017.
 */
public interface LocalScorer {

  Map<Integer,Map<Integer,Double>> computeScore(AnnotationMapping am, EncodedEntityStructure form,
                                                EncodedEntityStructure ontology, LocalScoreContext ctx);

}
