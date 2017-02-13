package de.uni_leipzig.dbs.formRepository.ranking;

import de.uni_leipzig.dbs.formRepository.data.ranking.Ranking;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;

/**
 * Created by christen on 19.01.2017.
 */
public interface ConceptRanking {

  Ranking computeRankingForConcepts(EncodedEntityStructure encOntology);
}
