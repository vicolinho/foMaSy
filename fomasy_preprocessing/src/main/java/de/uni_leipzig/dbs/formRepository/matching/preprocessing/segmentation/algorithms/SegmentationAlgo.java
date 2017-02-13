package de.uni_leipzig.dbs.formRepository.matching.preprocessing.segmentation.algorithms;

import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.segmentation.data.WordVector;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.util.List;
import java.util.Set;

/**
 * Created by christen on 26.01.2017.
 */
public interface SegmentationAlgo {


  Set<Set<WordVector>> identifyMWU(EntityStructureVersion esv, List<PreprocessProperty> selectedProps,
                                   Word2Vec word2Vec);
}
