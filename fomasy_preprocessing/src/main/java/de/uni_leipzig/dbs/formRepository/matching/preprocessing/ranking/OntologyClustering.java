package de.uni_leipzig.dbs.formRepository.matching.preprocessing.ranking;

import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 14.02.2017.
 */
public interface OntologyClustering {

  String THRESHOLD = "edgeThreshold";

  Collection<Cluster> cluster(EntityStructureVersion esv, String type, Map<String, Object> props,
                              Set<GenericProperty> genericProperties);
}
