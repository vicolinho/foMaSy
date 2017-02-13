package de.uni_leipzig.dbs.formRepository.matching.preprocessing.segmentation;

import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.IPreprocessingStep;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.Preprocessor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.exception.PreprocessingException;
import edu.stanford.nlp.ling.Word;
import org.apache.log4j.Logger;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by christen on 24.01.2017.
 */
public class Word2VecSegmentation implements Preprocessor {

  Logger log = Logger.getLogger(Word2VecSegmentation.class);

  public static final String WORD2VEC_RESOURCE = "word2VecModelPath";

  private Word2Vec word2Vec;

  public enum algorithm  {GRAPH, SEQUENCE}

  @Override
  public EntityStructureVersion preprocess(EntityStructureVersion esv, List<PreprocessProperty> propList,
      Map<String, Object> externalSources) throws PreprocessingException {

    if (externalSources.get(WORD2VEC_RESOURCE)!=null){
      File f = new File((String)externalSources.get(WORD2VEC_RESOURCE));
      try {
        Word2Vec word2VecModel = WordVectorSerializer.readWord2Vec(f);

      } catch (IOException e) {
        throw new PreprocessingException("model not available",e.getCause());
      }
    }else {
      return esv;
    }


    return null;
  }

  @Override
  public EntitySet<GenericEntity> preprocess(EntitySet<GenericEntity> esv, List<PreprocessProperty> propList,
      Map<String, Object> externalSources) {
    return null;
  }
}
