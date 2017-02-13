package de.uni_leipzig.dbs.sax_parsing;

import de.uni_leipzig.dbs.formRepository.importer.word2VecInput.impl.TrialGovW2VReader;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.unstructured.NormalizePreprocessing;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.unstructured.SectionSplitter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 24.01.2017.
 */
public class SaxParserTest {

  public static final String[]elements = new String[]{"detailed_description","criteria"};
  public String[] args = new String[]{
          "E:/data/trialsGov/leukemia",
          "E:/data/trialsGov/diabetes",
          "E:/data/trialsGov/cancer",
          "E:/data/trialsGov/heart_attack"
  };
  private  Set<String> sentences;
  @Before
  public void init(){
    TrialGovW2VReader reader = new TrialGovW2VReader();
    Map<String,String> delimiterperType = new HashMap<>();

    delimiterperType.put("detailed_description","\\s-\\s|\\.");
    delimiterperType.put("criteria","\\s-\\s|\\.");
    sentences = new HashSet<>();
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
  }

  private Set<String> getSentences ( Map<String,Set<String>> blocks, Map<String,String> delimiterPerType){
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

  @Ignore
  @Test
  public void testSentenceLength(){
    System.out.println("test:" +sentences.size() +"sentences");
    for (String s : sentences){
      String[] tokens = s.split("\\s");
      Assert.assertTrue(s, tokens.length<50);
      if (tokens.length<3){
        System.out.println(s);
      }
      //Assert.assertTrue(s, tokens.length>3);
    }
  }

  @Test
  public void testNormalization(){
    for (String s : sentences){
      Assert.assertFalse(s, s.matches(".*([^A-Za-z0-9\\s]).*"));
    }
  }
}
