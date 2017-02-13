package de.uni_leipzig.dbs.formRepository.matching.preprocessing.unstructured;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by christen on 24.01.2017.
 */
public class NormalizePreprocessing {


  public Set<String> preprocess(Set<String> sentences){
    Set<String> normSentences = new HashSet<>();
    for (String s: sentences){
      s = s.replaceAll("[^A-Za-z0-9]"," ");
      s = s.replaceAll("\\s+"," ").trim();
      s = s.toLowerCase();
      normSentences.add(s);
    }
    return normSentences;
  }
}
