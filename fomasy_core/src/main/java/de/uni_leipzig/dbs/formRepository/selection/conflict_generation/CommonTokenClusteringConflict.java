package de.uni_leipzig.dbs.formRepository.selection.conflict_generation;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.dataModel.util.CantorDecoder;
import de.uni_leipzig.dbs.formRepository.selection.conflict_generation.data.GenerationContext;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 18.04.2017.
 */
public class CommonTokenClusteringConflict extends AbstractConflictGenerator{


  public CommonTokenClusteringConflict(FormRepository repository){
    super(repository);
  }
  @Override
  public Map<Integer, Set<Set<Integer>>> getConflictAnnotations(AnnotationMapping am, EncodedEntityStructure ontology,
      GenerationContext context) {

    Map<Integer,Set<Set<Integer>>> conflictSetsPerItem = new HashMap<>();
    Map<Integer, Set<EntityAnnotation>> groupPerItem = this.groupBySrcEntity(am);


    for (Map.Entry<Integer, Set<EntityAnnotation>>e : groupPerItem.entrySet()){
      EncodedEntityStructure umlsGroup = this.getConcepts(e.getValue(), ontology, true);
      Map<Integer,Set<Integer>> conceptToEvidenceSet = new HashMap<Integer,Set<Integer>>();
      for (int targetId : umlsGroup.getObjIds().keySet()){
        long anid = CantorDecoder.code(e.getKey(), targetId);
        Set<Integer> evidence = am.getEvidenceMap().get(anid);
        conceptToEvidenceSet.put(targetId, evidence);
      }
      Set<Set<Integer>> conceptTosupersets = getOverlappedConcepts(conceptToEvidenceSet);
      conflictSetsPerItem.put(e.getKey(), conceptTosupersets);
    }
    return conflictSetsPerItem;
  }

  private Set<Set<Integer>> getOverlappedConcepts (Map<Integer,Set<Integer>> conceptEvidenceMap){
    Graph<Integer,Integer> graphOverlap = new UndirectedSparseGraph<>();
    int edgeId = 0;
    for (Map.Entry<Integer,Set<Integer>> entry1 : conceptEvidenceMap.entrySet()) {
      for (Map.Entry<Integer, Set<Integer>> entry2 : conceptEvidenceMap.entrySet()) {
        if (entry1.getKey()!= entry2.getKey()){
          if (!graphOverlap.containsVertex(entry1.getKey())){
            graphOverlap.addVertex(entry1.getKey());
          }
          if (!graphOverlap.containsVertex(entry2.getKey())){
            graphOverlap.addVertex(entry2.getKey());
          }
          if (entry1.getValue() != null && entry2.getValue()!=null) {
            Set<Integer> copy = new HashSet<>(entry1.getValue());
            copy.retainAll(entry2.getValue());
            if (copy.size() != 0) {
              graphOverlap.addEdge(edgeId++, entry1.getKey(), entry2.getKey());
            }
          }
        }
      }
    }
    WeakComponentClusterer<Integer,Integer> componentClusterer = new WeakComponentClusterer<>();
    Set<Set<Integer>>ccs= componentClusterer.transform(graphOverlap);
    return ccs;
  }
}
