package de.uni_leipzig.dbs.formRepository.matching.blocking;

import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.blocking.data.BlockSet;

import java.util.BitSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 01.11.2016.
 */
public interface Blocking {



  String BLOCKING_FIELD = "blockingSet";

  BlockSet computeBlocks(EncodedEntityStructure src, EncodedEntityStructure target);

  BlockSet computeBlocks(EncodedEntityStructure src, EncodedEntityStructure target, Map<Integer, Set<Integer>> lookup);
}
