package de.uni_leipzig.dbs.formRepository.matching.token;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.matching.Matcher;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;

public class LCSMatcher extends Matcher{

  @Override
  public Long2FloatMap computeSimilarity(EncodedEntityStructure source,
      EncodedEntityStructure target, AggregationFunction function,
      float threshold, Pruning pruning) throws MatchingExecutionException {
    int numberOfProcessros = Runtime.getRuntime().availableProcessors();
    int threadNumber;
    if (source.getObjIds().size()<1000&&target.getObjIds().size()<1000) {
      threadNumber = 4;
    } else {
      threadNumber = Math.max(numberOfProcessros, 16);
    }
    List<LCSThread> threadList = new ArrayList<LCSThread>();
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
        LCSThread tmpThread = new LCSThread (source, target, partObjIds, targetSet, function, threshold, this.getPropertyIds1(), this.getPropertyIds2(),
             pruning);
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
        LCSThread tmpThread = new LCSThread (source, target, sourceSet, partObjIds, function, threshold, this.getPropertyIds1(), this.getPropertyIds2(),
           pruning);
        threadList.add(tmpThread);
      }
    }
    
    for (LCSThread thread : threadList){
      thread.start();
    }
    
    for (LCSThread thread: threadList){
      try {
        thread.join();
        
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    for (LCSThread thread: threadList){
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
