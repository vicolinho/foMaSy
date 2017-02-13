package de.uni_leipzig.dbs.formRepository.matching.preprocessing.deepLearning.generation;

import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PropertyIterator implements SentenceIterator {



  private Collection<GenericEntity> recordSet;

  private Iterator<GenericEntity> recordIterator;

  private Collection<GenericProperty> propertySet;

  private SentencePreProcessor preProcessor;

  private Set <String> types;


  public PropertyIterator(Collection<GenericEntity> records, Set<String> types, Collection<GenericProperty> properties){
    this.recordSet = records;
    recordIterator = recordSet.iterator();
    this.propertySet = properties;
    this.types = types;
  }

  public String nextSentence() {
    GenericEntity r = recordIterator.next();
    if (types.contains(r.getType())) {
      StringBuilder sb = new StringBuilder();
      for (GenericProperty gp : propertySet) {
        List<String> pvs = r.getPropertyValues(gp);
        for (String pv : pvs) {
          sb.append(pv + "\\s");
        }
      }
      return sb.toString();
    }
    return "";
  }

  public boolean hasNext() {
    return recordIterator.hasNext();
  }

  public void reset() {
    recordIterator = recordSet.iterator();
  }


  public void finish() {
    recordIterator =null;
  }

  public SentencePreProcessor getPreProcessor() {
    return this.preProcessor;
  }

  public void setPreProcessor(SentencePreProcessor preProcessor) {
    this.preProcessor = preProcessor;
  }
}