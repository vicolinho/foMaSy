package de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking;

import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;

/**
 * Created by christen on 14.02.2017.
 */
public interface SynonymRanking {

  Int2FloatMap getPropertyValueRanking(EntityStructureVersion esv, OntologyClustering clustering,
                                       String distanceType);
}
