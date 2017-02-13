package de.uni_leipzig.dbs.formRepository.modelGeneration;

import de.uni_leipzig.dbs.formRepository.importer.word2VecInput.impl.TrialGovW2VReader;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.deepLearning.generation.SectionIterator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.deepLearning.generation.Word2VecGenerator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.unstructured.NormalizePreprocessing;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.unstructured.SectionSplitter;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by christen on 23.01.2017.
 */
public class Word2VecModelFromFile {

  public static final String[]elements = new String[]{"detailed_description","criteria"};


  public static void main (String[] args){

    TrialGovW2VReader reader = new TrialGovW2VReader();
    Map<String,String> delimiterperType = new HashMap<>();
    Set<String> sentences = new HashSet<>();
    delimiterperType.put("detailed_description","\\s-\\s");
    delimiterperType.put("criteria","\\s-\\s|\\.");

    for (String f: args){
      File folder = new File(f);
      if (folder != null){
        if (folder.isDirectory()){
          for (String filePath: folder.list()){
            try {
              Map<String,Set<String>> blocks = reader.readData(folder+File.separator+filePath, elements);
              sentences.addAll(getSentences(blocks, delimiterperType));
            } catch (ParserConfigurationException e) {
              e.printStackTrace();
            } catch (SAXException e) {
              e.printStackTrace();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }else {
          System.err.println("Specify a folder");
        }
      }
    }
    NormalizePreprocessing np = new NormalizePreprocessing();
    sentences = np.preprocess(sentences);
    Word2VecGenerator generator = new Word2VecGenerator();
    generator = generator.minWordFrequency(5)
            .windowSize(5)
            .iterations(10)
            .layerSize(100)
            .seed(42);
    generator.generateModelForSentenceCollection(sentences, new SectionIterator(),new File("trialGovModel"));
  }

  public static Set<String> getSentences ( Map<String,Set<String>> blocks, Map<String,String> delimiterPerType){
    SectionSplitter splitter = new SectionSplitter();
    Set<String> sentences = new HashSet<>();
    for (Map.Entry<String, Set<String>> elemBlocks: blocks.entrySet()){
      String delimiter = delimiterPerType.get(elemBlocks.getKey());
      if (delimiter!=null) {
        sentences.addAll(splitter.splitSectionsByDelimiter(elemBlocks.getValue(), delimiter));
      }
    }
    return sentences;
  }
}
