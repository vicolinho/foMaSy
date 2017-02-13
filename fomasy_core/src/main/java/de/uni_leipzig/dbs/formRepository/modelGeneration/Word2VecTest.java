package de.uni_leipzig.dbs.formRepository.modelGeneration;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by christen on 21.11.2016.
 */
public class Word2VecTest {

  public static void main(String[] args){
    try {
      Word2Vec w2v = WordVectorSerializer.readWord2Vec(new File(args[0]));
      //System.out.println(w2v.wordsNearest("bone",20).toString());
      //System.out.println(w2v.similarity("bone","blast"));
      //System.out.println(w2v.wordsNearest("congestive",20).toString());

      String testQuestion = "bone marrow blasts (leukemic cells) greater than 10%no chronic" +
              " myelogenous leukemia in blast crisis";
      testQuestion = testQuestion.replaceAll("[^A-Za-z0-9]"," ");
      testQuestion = testQuestion.replaceAll("\\s+"," ");

      INDArray terms =null ;
      terms = w2v.getWordVectorsMean(Arrays.asList("bone", "marrow"));

      System.out.println(w2v.wordsNearestSum(terms,5));
      String[] tokens = testQuestion.split("\\s");
      System.out.print(tokens[0]);
      for (int i =0; i<tokens.length -1;i++){
        System.out.print("-"+w2v.similarity(tokens[i],tokens[i+1])+"->"+tokens[i+1]);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
