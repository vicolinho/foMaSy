package de.uni_leipzig.dbs.formRepository.matching.preprocessing.deepLearning.generation;

import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;


import java.util.Iterator;
import java.util.Set;

/**
 * Created by christen on 21.01.2017.
 */
public class SectionIterator implements SentenceIterator{


  private Set<String> data;

  private Iterator<String> fieldIterator ;



  public void setData(Set<String> data) {
    this.data = data;
    fieldIterator = data.iterator();
  }


  @Override
  public String nextSentence() {

    return fieldIterator.next();
  }

  @Override
  public boolean hasNext() {
    return fieldIterator.hasNext();
  }

  @Override
  public void reset() {
    fieldIterator = data.iterator();
  }

  @Override
  public void finish() {

  }

  @Override
  public SentencePreProcessor getPreProcessor() {
    return null;
  }

  @Override
  public void setPreProcessor(SentencePreProcessor preProcessor) {

  }
}
