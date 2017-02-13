package de.uni_leipzig.dbs.formRepository.matching.preprocessing.deepLearning.generation;

import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import java.io.File;
import java.util.*;

/**
 * Created by christen on 03.11.2016.
 */
public class Word2VecGenerator {

  private int minWord;

  private int iterations;

  private int layerSize;

  private int seed;

  private int windowSize;

  public void generateModel(Set<EntityStructureVersion> docs, Set<String> types, Collection<GenericProperty> property,
                            File file){
    Collection <GenericEntity> genericEntities = new HashSet<GenericEntity>();
    for (EntityStructureVersion esv: docs) {
     genericEntities.addAll(esv.getEntities());
    }
    Word2Vec word2Vec = new Word2Vec();
    PropertyIterator pi = new PropertyIterator(genericEntities, types, property);
    TokenizerFactory t = new DefaultTokenizerFactory();
    word2Vec.setSentenceIter(pi);
     word2Vec = new Word2Vec.Builder()
            .minWordFrequency(minWord)
            .iterations(iterations)
            .layerSize(layerSize)
            .seed(seed)
            .windowSize(windowSize)
            .iterate(pi)
            .tokenizerFactory(t)
            .build();
    word2Vec.fit();
    WordVectorSerializer.writeWord2Vec(word2Vec, file);
  }

  public Word2VecGenerator minWordFrequency (int minWord){
    this.minWord = minWord;
    return this;
  }

  public Word2VecGenerator iterations(int iter){
    this.iterations = iter;
    return this;
  }

  public Word2VecGenerator layerSize (int layerSize){
    this.layerSize = layerSize;
    return this;
  }

  public Word2VecGenerator seed (int seed){
    this.seed = seed;
    return this;
  }

  public Word2VecGenerator windowSize (int wndSize){
    this.windowSize = wndSize;
    return this;
  }



  public void generateModelForSentenceCollection(Set<String> data,
                                                 SectionIterator sentenceIterator, File file){
    Word2Vec word2Vec = new Word2Vec();
    sentenceIterator.setData(data);
    TokenizerFactory t = new DefaultTokenizerFactory();
    word2Vec.setSentenceIter(sentenceIterator);
    word2Vec = new Word2Vec.Builder()
            .minWordFrequency(minWord)
            .iterations(iterations)
            .layerSize(layerSize)
            .seed(seed)
            .windowSize(windowSize)
            .iterate(sentenceIterator)
            .tokenizerFactory(t)
            .build();
    word2Vec.fit();
    WordVectorSerializer.writeWord2Vec(word2Vec, file);
  }
}
