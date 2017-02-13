package de.uni_leipzig.dbs.formRepository.matching.token;

import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.AbstractPartMatcher;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.apache.log4j.Logger;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.util.*;

/**
 * Created by christen on 03.01.2017.
 */
public class RankingBasedThread extends AbstractPartMatcher {

  Logger log = Logger.getLogger(getClass());

  EncodedEntityStructure srcStructure;

  EncodedEntityStructure targetStructure;

  AggregationFunction aggFunc;

  Set<Integer> srcIds;

  Set<Integer> targetIds;

  private Set<Integer> propertyIds1;

  private Set<Integer> propertyIds2;

  private Int2FloatMap idfTargetMap;

  private Int2FloatMap idfSourceMap;

  private int windowSize;

  private Map<Integer, Set<Integer>> lookup;

  private Word2Vec w2vec;

  private HashMap<Float, Set<Integer>> evidenceConfMap;


  public RankingBasedThread(EncodedEntityStructure src, EncodedEntityStructure target, IntSet srcIds, IntSet targetIds,
                            AggregationFunction aggFunc,
                            float threshold, Set<Integer> set, Set<Integer> set2, Int2FloatMap idfMap,
                            Int2FloatMap idfTargetMap, Map<Integer, Set<Integer>> lookup, Word2Vec word2Vec, int wndSize) {
    super(threshold);
    this.srcStructure = src;
    this.targetStructure = target;
    this.aggFunc = aggFunc;
    this.propertyIds1 = set;
    this.propertyIds2 = set2;
    this.srcIds = srcIds;
    this.targetIds = targetIds;
    this.windowSize = wndSize;
    this.idfTargetMap = idfTargetMap;
    this.idfSourceMap = idfMap;
    this.lookup = lookup;
    this.w2vec = word2Vec;
  }

  @Override
  public void run() {
    evidenceConfMap = new HashMap<>();
    List<Float> confidenceList = new ArrayList<>();
    for (int srcId : srcIds) {
      int entitySrcId = srcId;
      int entitySrcPos = srcStructure.getObjIds().get(srcId);
      Set<Integer> targetEntities;
      targetEntities = targetIds;
      int maximalSize = 0;
      for (Integer te :targetEntities){
        evidenceConfMap.clear();
        int targetPos = targetStructure.getObjIds().get(te);
        for (int srcPropertyPos : propertyIds1){
          for (int[] trigramSrc:srcStructure.getPropertyValueIds()[entitySrcPos][srcPropertyPos]){
            int start = 0;
            if (trigramSrc.length>maximalSize){
              maximalSize = trigramSrc.length;
            }
            //for (int currentWindow =2; currentWindow<windowSize;currentWindow++){
            int end = (windowSize<trigramSrc.length)?windowSize: trigramSrc.length;
            do{
              Int2IntMap countMapSrc = this.getFrequencies(trigramSrc, start, end);
              //Set<Integer> srcTerms = this.getTermSet(trigramSrc, start, end);
              for (int targetPropertyPos :propertyIds2){
                for (int [] trigramTarget: targetStructure.getPropertyValueIds()[targetPos][targetPropertyPos]){
                  Int2IntMap countMapTarget = this.getFrequencies(trigramTarget, 0,trigramTarget.length);
                  //Set<Integer> targetTerms = this.getTermSet(trigramTarget, 0,trigramTarget.length);
                  confidenceList.add(this.computeSimilarity(countMapSrc , countMapTarget));

                  //confidenceList.add(this.computeSimilarity(srcTerms, targetTerms, srcId, te));
                }
              }
              start++;
              end++;
            }while(end<=trigramSrc.length);
            //}
          }
        }


        float sim = aggFunc.aggregateFloatList(confidenceList);
        Set<Integer> evidence = evidenceConfMap.get(sim);
        this.addResultWithEvidence(srcStructure.getStructureId(), targetStructure.getStructureId(), entitySrcId, te,evidence, sim);
        confidenceList.clear();
      }
    }
  }

  private  Set <Integer> intersect;
  private  Set <Integer> onlyInSrc;
  private Set<Integer> onlyInTarget;

  private float  computeSimilarity (Int2IntMap frequencySrc, Int2IntMap frequencyTarget){
    if (intersect ==null){
      intersect = new HashSet<>();
    }else {
      intersect.clear();
    }
    if (onlyInSrc ==null){
      onlyInSrc = new HashSet<>();
    }else {
      onlyInSrc.clear();
    }
    if (onlyInTarget ==null){
      onlyInTarget = new HashSet<>();
    }else{
      onlyInTarget.clear();
    }
    intersect.addAll(frequencySrc.keySet());
    intersect.retainAll(frequencyTarget.keySet());

    float sim =0;
    float dotProduct = 0;
    float lengthSrc =0;
    float lengthTarget=0;
    //terms that are not exact in target
    onlyInSrc.addAll(frequencySrc.keySet());
    onlyInSrc.removeAll(intersect);
    //terms that are not exact in src
    onlyInTarget.addAll(frequencyTarget.keySet());
    onlyInTarget.removeAll(intersect);

    Int2IntMap srcToTarget = new Int2IntOpenHashMap();
    Set<Integer> removeSrc = new HashSet<Integer>();
    Set<Integer> removeTarget = new HashSet<Integer>();
    for (int s: onlyInSrc){
      Set <Integer> simWords = lookup.get(s);
      if (simWords!=null){
        for (int tt: onlyInTarget){
          if (simWords.contains(tt)){
            intersect.add(s);
            srcToTarget.put(s, tt);
            removeSrc.add(s);
            removeTarget.add(tt);
          }
        }
      }
    }
    onlyInTarget.removeAll(removeTarget);
    onlyInSrc.removeAll(removeSrc);

    if (intersect.size() ==0){
      return 0;
    }

    for (Integer t: intersect){
      float tfidfSrc = frequencySrc.get(t)*idfSourceMap.get(t);
      if (idfSourceMap.get(t)<0)
        log.warn(idfSourceMap.get(t));

      float tfidfTarget;
      if (srcToTarget.containsKey(t)){
        int targetToken = srcToTarget.get(t);
        float idf = idfTargetMap.get(targetToken);
        tfidfTarget = frequencyTarget.get(targetToken)* idf;
      }else{
        if (!frequencyTarget.containsKey(t)){
          log.error("not in map"+t);
        }
        if (idfTargetMap.get(t)<0)
          log.warn(idfTargetMap.get(t));
        float idf = idfTargetMap.get(t);
        tfidfTarget = frequencyTarget.get(t)* idf;
      }

      dotProduct +=tfidfSrc*tfidfTarget;
      lengthSrc+= tfidfSrc*tfidfSrc;
      lengthTarget += tfidfTarget*tfidfTarget;
    }

    for (Integer i :onlyInSrc){
      float tfidfSrc = frequencySrc.get(i)*idfSourceMap.get(i);
      lengthSrc+= tfidfSrc*tfidfSrc;
    }

    for (Integer i :onlyInTarget){
      if (!frequencyTarget.containsKey(i)){
        log.error("not in map"+i);
      }
      float tfidfTarget = frequencyTarget.get(i)*idfTargetMap.get(i);
      lengthTarget+= tfidfTarget*tfidfTarget;
    }

    if (lengthSrc !=0 &&lengthTarget!=0){
      sim = (float) (dotProduct/(Math.sqrt(lengthSrc)*Math.sqrt(lengthTarget)));
      evidenceConfMap.put(sim, intersect);
      if (sim >1){
        log.error(sim+" dotProduct: "+ dotProduct +" length src: "+lengthSrc +" length target: " + lengthTarget);
      }
    }
    return sim;
  }

  private Int2IntMap getFrequencies (int [] tokens, int offset, int limit){
    Int2IntMap frequencies = new Int2IntOpenHashMap();
    for (int i=offset; i<limit; i++){
      int t = tokens[i];
      if (!frequencies.containsKey(t)){
        frequencies.put(t, 1);
      }else{
        frequencies.put(t, frequencies.get(t)+1);
      }
    }
    return frequencies;
  }
}
