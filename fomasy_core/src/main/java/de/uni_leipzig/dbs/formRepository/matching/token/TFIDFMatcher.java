package de.uni_leipzig.dbs.formRepository.matching.token;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.matching.Matcher;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.TokenWeighting.TFIDFTokenWeightGenerator;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;

public class TFIDFMatcher extends Matcher{

  Logger log = Logger.getLogger(getClass());
  
  public static final String TFIDF_SOURCE_SEPARATED = "sourceSeparated";
  
  public static final String DOCUMENT_NUMBER= "documentNumber";
  
  public static final String IDF_MAP_SOURCE = "idfSourceMap";
  
  public static final String IDF_MAP_TARGET = "idfTargetMap";
  @Override
  public Long2FloatMap computeSimilarity(EncodedEntityStructure source,
      EncodedEntityStructure target, AggregationFunction function,
      float threshold, Pruning pruning) throws MatchingExecutionException {
    //log.debug ("start tfidf matcher");
    Int2FloatMap idfSourceMap =null;
    Int2FloatMap idfTargetMap =null;
    if (this.getGlobalObjects().get(IDF_MAP_SOURCE)!=null&&this.getGlobalObjects().get(IDF_MAP_TARGET)!= null){
      idfSourceMap = (Int2FloatMap) this.getGlobalObjects().get(IDF_MAP_SOURCE);
      idfTargetMap = (Int2FloatMap) this.getGlobalObjects().get(IDF_MAP_TARGET);
      
    }else {
      log.error("id map for source and target has to be specified");
      throw new MatchingExecutionException(getClass().getName()+": idfmap is not specified");
    }
    int numberOfProcessros = Runtime.getRuntime().availableProcessors();
    int threadNumber;
    if (source.getObjIds().size()<100&&target.getObjIds().size()<100) {
      threadNumber = 4;
    } else {
      threadNumber = Math.max(numberOfProcessros, 16);
    }
    List<TFIDFThread> threadList = new ArrayList<TFIDFThread>();
    if (source.getObjIds().size()>target.getObjIds().size()) {
      //Domain splitten
      ArrayList<IntSet> domObjIDsParts = new ArrayList<IntSet>();
      for (int i=0;i<threadNumber;i++) {
        domObjIDsParts.add(new IntOpenHashSet());
      
      }
      int index =0; 
      for (int  domainObjID : source.getObjIds().keySet()) {
        domObjIDsParts.get(domainObjID%threadNumber).add(domainObjID);
      }
      IntSet targetSet = new IntOpenHashSet(target.getObjIds().keySet());
      for (int i=0;i<threadNumber;i++) {
        IntSet partObjIds = domObjIDsParts.get(i);
        TFIDFThread tmpThread = new TFIDFThread (source, target, partObjIds, targetSet, function, threshold, this.getPropertyIds1(), this.getPropertyIds2(),
            idfSourceMap, idfTargetMap, pruning);
        threadList.add(tmpThread);
      }
    } else {
      //Range splitten
      ArrayList<IntSet> domObjIDsParts = new ArrayList<IntSet>();
      for (int i=0;i<threadNumber;i++) {
        domObjIDsParts.add(new IntOpenHashSet());
      
      }
      
      for (int  domainObjID : target.getObjIds().keySet()) {
        domObjIDsParts.get(domainObjID%threadNumber).add(domainObjID);
      }
      IntSet sourceSet = new IntOpenHashSet(source.getObjIds().keySet());
      for (int i=0;i<threadNumber;i++) {
        IntSet partObjIds = domObjIDsParts.get(i);
        TFIDFThread tmpThread = new TFIDFThread (source, target, sourceSet, partObjIds, function, threshold, this.getPropertyIds1(), this.getPropertyIds2(),
            idfSourceMap, idfTargetMap, pruning);
        threadList.add(tmpThread);
      }
    }
    
    for (TFIDFThread thread : threadList){
      thread.start();
    }
    
    for (TFIDFThread thread: threadList){
      try {
        thread.join();
        
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    for (TFIDFThread thread: threadList){
      this.mergeResult(thread.getResult());
    }
    
    return this.getResult();
  }

  

  @Override
  public Long2FloatMap computeSimilarityByReuse(int[][][] propValues1) {
    // TODO Auto-generated method stub
    return null;
  }

}
