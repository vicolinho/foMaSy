package de.uni_leipzig.dbs.formRepository.selection.conflict_generation;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EncodedAnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.ExecutionTree;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.MatchOperator;
import de.uni_leipzig.dbs.formRepository.selection.conflict_generation.data.GenerationContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 10.04.2017.
 */
public class SimilarConceptConflict extends AbstractConflictGenerator {

  public SimilarConceptConflict (FormRepository rep){
    super(rep);
  }

  @Override
  public Map<Integer, Set<Set<Integer>>> getConflictAnnotations(AnnotationMapping am,
      EncodedEntityStructure ontology, GenerationContext context) {


    Map<Integer,Set<Set<Integer>>> conflictSetsPerItem = new HashMap<>();
    Map<Integer, Set<EntityAnnotation>> groupPerItem = this.groupBySrcEntity(am);


    for (Map.Entry<Integer, Set<EntityAnnotation>>e : groupPerItem.entrySet()){
      EncodedEntityStructure umlsGroup = this.getConcepts(e.getValue(), ontology, true);
      Set<Set<Integer>> cc = this.getSimilarGroups(umlsGroup, context.getGroupingAttributes(),
              context.getSim_threshold(), context.getSim_func());
      conflictSetsPerItem.put(e.getKey(), cc);
    }
    return conflictSetsPerItem;
  }

  private Set<Set<Integer>> getSimilarGroups(EncodedEntityStructure umlsGroup, Set<GenericProperty> preRanAtts,
      float threshold, String sim_func){
          EncodedAnnotationMapping set=new EncodedAnnotationMapping();
  Set<Integer> srcs = new HashSet<Integer>();
  srcs.add(umlsGroup.getStructureId());
  MatchOperator mop = new MatchOperator (sim_func, AggregationFunction.MAX,
          preRanAtts, preRanAtts, threshold);
  ExecutionTree tree = new ExecutionTree();
  tree.addOperator(mop);
  try {

    set = rep.getMatchManager().matchEncoded(umlsGroup, umlsGroup, tree, null);
  } catch (MatchingExecutionException e1) {
    e1.printStackTrace();
  }
  HashSet<EntityAnnotation> toRemoveCorrs = new HashSet<EntityAnnotation>();
  HashMap <Integer,Node> nodeMap= new HashMap<Integer,Node>();
  boolean isChange =true;
  int counter =0;

  for (EntityAnnotation c: set.getAnnotations()){
    if (c.getSrcId()==c.getTargetId()){
      toRemoveCorrs.add(c);
    }
  }
  for(EntityAnnotation c:toRemoveCorrs){
    set.removeAnnotation(c.getSrcId(), c.getTargetId());
  }

  while(isChange&&counter<10000){
    counter++;
    if (set.getNumberOfAnnotations()!=0){
      for (EntityAnnotation cor : set.getAnnotations()){
        if (cor.getSrcId()!= cor.getTargetId()){
          Node n=nodeMap.get(cor.getSrcId());
          if (n==null){
            n = new Node();
            n.ownId= cor.getSrcId();
            n.minId =n.ownId;
            nodeMap.put(n.ownId, n);
          }

          Node n2 = nodeMap.get(cor.getTargetId());
          if (n2==null){
            n2 = new Node();
            n2.ownId= cor.getTargetId();
            n2.minId= cor.getTargetId();
            nodeMap.put(n2.ownId, n2);
          }
          int min = Math.min(n.minId, n2.minId);
          isChange = !(n2.minId == min && n.minId == min);
          n.minId = min;
          n2.minId = min;
        }
      }
    }else {
      for (int id : umlsGroup.getObjIds().keySet()){
        Node n = new Node();
        n.ownId= id;
        n.minId =n.ownId;
        nodeMap.put(id, n);
      }
      isChange =false;
    }
  }

  Map<Integer,Set<Integer>> umlsGroups = new HashMap<>();
  HashSet<Integer> notInGroup =new HashSet<Integer>();
  for (int id : umlsGroup.getObjIds().keySet()){
    notInGroup.add(id);
  }
  notInGroup.removeAll(nodeMap.keySet());
  for (Map.Entry<Integer,Node> e: nodeMap.entrySet()){
    Set<Integer> list = umlsGroups.get(e.getValue().minId);
    if (list==null){
      list = new HashSet<>();
      umlsGroups.put(e.getValue().minId,list);
    }
    list.add(e.getKey());
  }
  Set<Set<Integer>> conflictGroups = new HashSet<>();
  for (Set<Integer> cc : umlsGroups.values()){
    conflictGroups.add(cc);
  }
  for (Integer nig : notInGroup){
    Set<Integer> list = new HashSet<>();
    conflictGroups.add(list);
    list.add(nig);
  }
  return conflictGroups;
  }

  class Node {
    int ownId;
    int minId;
  }

}
