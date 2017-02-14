package de.uni_leipzig.dbs.formRepository.matching.token.util;

import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.Set;

/**
 * Determine the encoded term frequencies for each encoded concept
 */
public class TermFrequenceCounter {

  public Int2ObjectMap<Int2IntMap> getTermFrequencies (Set<Integer> srcIds, Set<Integer> targetIds,
      EncodedEntityStructure src, EncodedEntityStructure target,
      Set<Integer> srcProperties, Set<Integer> targetProperties){
    Int2ObjectMap map = new Int2ObjectLinkedOpenHashMap();
    for (int sId : srcIds){
      Int2IntMap counts = new Int2IntLinkedOpenHashMap();
      counts.defaultReturnValue(-1);
      map.put(sId, counts);
      int entitySrcPos = src.getObjIds().get(sId);
      for (int sp : srcProperties) {
        for (int[] value : src.getPropertyValueIds()[entitySrcPos][sp]){
          for (int t: value){
            int c = counts.get(t);
            if (c == counts.defaultReturnValue()){
              counts.put(t,1);
            }else{
              counts.put(t,1+c);
            }
          }
        }
      }
    }
    for (int tid : targetIds){
      Int2IntMap counts = new Int2IntLinkedOpenHashMap();
      counts.defaultReturnValue(-1);
      map.put(tid, counts);
      int entityTargetPos = target.getObjIds().get(tid);
      for (int sp : targetProperties) {
        for (int[] value : target.getPropertyValueIds()[entityTargetPos][sp]){
          for (int t: value){
            int c = counts.get(t);
            if (c == counts.defaultReturnValue()){
              counts.put(t,1);
            }else{
              counts.put(t,1+c);
            }
          }
        }
      }
    }
    return map;
  }
}
