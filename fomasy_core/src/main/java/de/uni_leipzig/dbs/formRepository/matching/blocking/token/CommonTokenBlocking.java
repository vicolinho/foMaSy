package de.uni_leipzig.dbs.formRepository.matching.blocking.token;

import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.blocking.Blocking;
import de.uni_leipzig.dbs.formRepository.matching.blocking.data.BlockSet;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by christen on 04.11.2016.
 */
public class CommonTokenBlocking implements Blocking{

  Logger log = Logger.getLogger(CommonTokenBlocking.class);

  public BlockSet computeBlocks(EncodedEntityStructure src, EncodedEntityStructure target) {
    return null;
  }

  public BlockSet computeBlocks(EncodedEntityStructure src, EncodedEntityStructure target, Map<Integer, Set<Integer>> lookup) {

    Map<Integer, Set<Integer>> token2Entities = new HashMap<>();
    for (Map.Entry<Integer,Integer> e: src.getObjIds().entrySet()){
      for (int[][] props : src.getPropertyValues(e.getValue())){
        for (int[]pv:props){
          for (int t : pv) {
            Set<Integer> entities = token2Entities.get(t);
            if (entities ==null){
              entities = new HashSet<>();
              token2Entities.put(t, entities);
            }
            entities.add(e.getValue());
          }
        }
      }
    }
    BlockSet bitSet = new BlockSet(src.getObjIds().size(),target.getObjIds().size());
    int maxLength = target.getObjIds().size();
    for (Map.Entry<Integer,Integer> e: target.getObjIds().entrySet()){
      for (int[][] props : target.getPropertyValues(e.getValue())){
        for (int[]pv:props){
          for (int t : pv) {
            if (token2Entities.containsKey(t)){
             for (int srcId : token2Entities.get(t)){
               bitSet.setToComparePair(srcId,e.getValue());
             }
            }else {
              if (lookup.get(t)!=null) {
                for (int simT : lookup.get(t)) {
                  if (token2Entities.containsKey(simT)) {
                    for (int srcId : token2Entities.get(simT)){
                      bitSet.setToComparePair(srcId,e.getValue());
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    token2Entities.clear();
    token2Entities = null;
    return bitSet;
  }
}
