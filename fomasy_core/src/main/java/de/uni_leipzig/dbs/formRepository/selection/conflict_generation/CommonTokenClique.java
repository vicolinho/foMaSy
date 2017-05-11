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
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by christen on 29.04.2017.
 */
public class CommonTokenClique extends AbstractConflictGenerator {

  Logger log = Logger.getLogger(getClass());

  public CommonTokenClique(FormRepository rep){
    super(rep);
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
      Set<Set<Integer>> conceptTosupersets = getConceptsInClique(conceptToEvidenceSet);
      conflictSetsPerItem.put(e.getKey(), conceptTosupersets);
    }
    return conflictSetsPerItem;
  }

  private Set<Set<Integer>> getConceptsInClique(Map<Integer,Set<Integer>> conceptEvidenceMap){
    Set<Integer> nodes = new HashSet<>();
    HashMap<Integer,List<Integer>> edges = new HashMap<>();
    for (Map.Entry<Integer,Set<Integer>> entry1 : conceptEvidenceMap.entrySet()) {
      for (Map.Entry<Integer, Set<Integer>> entry2 : conceptEvidenceMap.entrySet()) {
        if (entry1.getKey()!= entry2.getKey()){
          if (!nodes.contains(entry1.getKey())){
            nodes.add(entry1.getKey());
          }
          if (!nodes.contains(entry2.getKey())){
            nodes.add(entry2.getKey());
          }
          if (entry1.getValue() != null && entry2.getValue()!=null) {
            Set<Integer> copy = new HashSet<>(entry1.getValue());
            copy.retainAll(entry2.getValue());
            if (copy.size() != 0) {
              List<Integer> e1 = edges.get(entry1.getKey());
              if (e1 ==null){
                e1 = new ArrayList<>();
                edges.put(entry1.getKey(),e1);
              }
              e1.add(entry2.getKey());

              List<Integer> e2 = edges.get(entry2.getKey());
              if (e2 ==null){
                e2 = new ArrayList<>();
                edges.put(entry2.getKey(),e2);
              }
              e2.add(entry1.getKey());
            }
          }
        }
      }
    }
    Set<Integer> singletons = new HashSet<>(nodes);
    singletons.removeAll(edges.keySet());
    nodes.removeAll(singletons);
    Set<Set<Integer>> cliques = simpleCluster(nodes, edges);
    for (Integer i : singletons){
      Set<Integer> set= new HashSet<>();
      set.add(i);
      cliques.add(set);
    }
    return cliques;
  }

  private Set<Set<Integer>> simpleCluster(Set<Integer> nodes,
                                         HashMap<Integer,List<Integer>> edges){
    Set <Integer> r = new HashSet<Integer>();
    Set <Integer> x = new HashSet<Integer>();
    Set <Integer> p = new HashSet<Integer>();
    Set<Set<Integer>> clusters = new HashSet<Set<Integer>> ();
    for (int i : nodes){
      p.add(i);
    }
    this.simpleKerbosch(r, p, x, edges,clusters);
    return clusters;
  }

  private void simpleKerbosch(Set<Integer> r,Set<Integer> p, Set<Integer> x,HashMap<Integer,
          List<Integer>> edges, Set<Set<Integer>> clusters){
    if (p.isEmpty()&&x.isEmpty()){
      clusters.add(r);
      return ;
    }

    while (!p.isEmpty()){
      int n = p.iterator().next();
      Set<Integer> r2 = new HashSet<Integer>(r);
      r2.add(n);
      Set<Integer> p2 = new HashSet<Integer>(p);
      Set<Integer> x2 = new HashSet<Integer>(x);
      Set<Integer> neighbors = new HashSet<Integer>();
      List<Integer> list = edges.get(n);
      if (list ==null){
        log.error(n);
      }
      for (int corNode: list){
        int neigh =(corNode);
        neighbors.add(neigh);
      }
      p2.retainAll(neighbors);
      x2.retainAll(neighbors);

      simpleKerbosch (r2,p2,x2,edges,clusters);
      p.remove(n);
      x.add(n);
    }
  }
}
