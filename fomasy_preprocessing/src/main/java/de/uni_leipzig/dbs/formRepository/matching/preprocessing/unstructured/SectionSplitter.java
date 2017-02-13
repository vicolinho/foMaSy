package de.uni_leipzig.dbs.formRepository.matching.preprocessing.unstructured;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by christen on 23.01.2017.
 */
public class SectionSplitter {

  public Set<String> splitSectionsByDelimiter(Set<String> sections, String delimiter){
    Set<String> sentences = new HashSet<>();
    for (String sec: sections){
      String[] array = sec.split(delimiter);
      for (String s: array){
        if (!s.isEmpty())
          sentences.add(s);
      }
    }
    return sentences;
  }
}
