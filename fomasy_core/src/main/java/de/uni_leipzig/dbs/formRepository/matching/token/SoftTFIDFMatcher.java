package de.uni_leipzig.dbs.formRepository.matching.token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.matching.blocking.Blocking;
import de.uni_leipzig.dbs.formRepository.matching.blocking.data.BlockSet;
import org.apache.log4j.Logger;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.matching.Matcher;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;

public class SoftTFIDFMatcher extends Matcher {
  
  private int wndSize;
  
  private boolean isAdaptive; 
  
  public static final String TFIDF_SOURCE_SEPARATED = "sourceSeparated";
  
  public static final String DOCUMENT_NUMBER= "documentNumber";
  
  public static final String IDF_MAP_SOURCE = "idfSourceMap";
  
  public static final String IDF_MAP_TARGET = "idfTargetMap";
  
  public static final String WND_SIZE = "windowSize";
  
  public static final String IS_ADAPTIVE_SIZE = "isAdaptiveSize";
  
  public static final String LOOKUP = "lookup";
  
  Logger log = Logger.getLogger(getClass());

  private Map<Integer, Set<Integer>> lookup;

  @Override
  public Long2FloatMap computeSimilarity(EncodedEntityStructure source,
      EncodedEntityStructure target, AggregationFunction function,
      float threshold, Pruning pruning) throws MatchingExecutionException {  Int2FloatMap idfSourceMap =null;
      Int2FloatMap idfTargetMap =null;
      if (this.getGlobalObjects().get(IDF_MAP_SOURCE)!=null&&this.getGlobalObjects().get(IDF_MAP_TARGET)!= null){
        idfSourceMap = (Int2FloatMap) this.getGlobalObjects().get(IDF_MAP_SOURCE);
        idfTargetMap = (Int2FloatMap) this.getGlobalObjects().get(IDF_MAP_TARGET);
        
      }else {
        log.error("id map for source and target has to be specified");
        throw new MatchingExecutionException(getClass().getName()+": idfmap is not specified");
      }
      if (this.getGlobalObjects().get(WND_SIZE)!=null){
        this.wndSize = (Integer)this.getGlobalObjects().get(WND_SIZE);
      }else  {
        log.error("window size has to be specified");
        throw new MatchingExecutionException(getClass().getName()+": windowSize is not specified");
      }
      
      if(this.getGlobalObjects().get(IS_ADAPTIVE_SIZE)!=null){
        this.isAdaptive = (Boolean) this.getGlobalObjects().get(IS_ADAPTIVE_SIZE);
      }else {
        isAdaptive =false;
      }
      
      if (this.getGlobalObjects().get(LOOKUP)!=null){
        lookup = (Map<Integer,Set<Integer>>)this.getGlobalObjects().get(LOOKUP);
      }
      BlockSet blocks =null;
      if(this.getGlobalObjects().get(Blocking.BLOCKING_FIELD)!=null){
        blocks = (BlockSet)this.getGlobalObjects().get(Blocking.BLOCKING_FIELD);
      }
      int numberOfProcessros = Runtime.getRuntime().availableProcessors();
      int threadNumber;
      if (source.getObjIds().size()<1000&&target.getObjIds().size()<1000) {
        threadNumber = 4;
      } else {
        threadNumber = Math.max(numberOfProcessros, 16);
      }
      List<SoftTFIDFThread> threadList = new ArrayList<SoftTFIDFThread>();
      if (source.getObjIds().size()>target.getObjIds().size()) {
        //Domain splitten
        ArrayList<IntSet> domObjIDsParts = new ArrayList<IntSet>();
        for (int i=0;i<threadNumber;i++) {
          domObjIDsParts.add(new IntOpenHashSet());
        
        }
        
        for (int  domainObjID : source.getObjIds().keySet()) {
          domObjIDsParts.get(domainObjID%threadNumber).add(domainObjID);
        }
        IntSet targetSet = new IntOpenHashSet(target.getObjIds().keySet());
        for (int i=0;i<threadNumber;i++) {
          IntSet partObjIds = domObjIDsParts.get(i);
          SoftTFIDFThread tmpThread = new SoftTFIDFThread (source, target, partObjIds, targetSet, function, threshold, this.getPropertyIds1(), this.getPropertyIds2(),
              idfSourceMap, idfTargetMap, lookup, blocks,wndSize,isAdaptive);
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
          SoftTFIDFThread tmpThread = new SoftTFIDFThread (source, target, sourceSet, partObjIds, function, threshold, this.getPropertyIds1(), this.getPropertyIds2(),
              idfSourceMap, idfTargetMap, lookup, blocks,wndSize,isAdaptive);
          threadList.add(tmpThread);
        }
      }
      
      for (SoftTFIDFThread thread : threadList){
        thread.start();
      }
      
      for (SoftTFIDFThread thread: threadList){
        try {
          thread.join();
          
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      
      for (SoftTFIDFThread thread: threadList){
        this.mergeResult(thread.getResult(),thread.getEvidenceMap());
      }
      
      return this.getResult();
    }

  @Override
  public Long2FloatMap computeSimilarityByReuse(int[][][] propValues1) {
    // TODO Auto-generated method stub
    return null;
  }

}
