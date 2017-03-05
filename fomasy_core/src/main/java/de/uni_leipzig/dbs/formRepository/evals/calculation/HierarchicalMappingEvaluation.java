package de.uni_leipzig.dbs.formRepository.evals.calculation;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.dataModel.*;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Edge;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;
import de.uni_leipzig.dbs.formRepository.exception.GraphAPIException;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.operation.SetAnnotationOperator;
import edu.uci.ics.jung.graph.DirectedGraph;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.Set;

/**
 * Created by christen on 21.02.2017.
 */
public class HierarchicalMappingEvaluation {


  public static final int DEPTH =2;
  public EvaluationResult getResult (AnnotationMapping result, AnnotationMapping referenceMapping,
                                     VersionMetadata src, VersionMetadata target, FormRepository rep)
          throws GraphAPIException {

    EvaluationResult er = new EvaluationResult(src.getName(), target.getName());

    EntitySet<GenericEntity> set = new GenericEntitySet();
    Int2ObjectMap<IntSet> hierarchyConcepts = new Int2ObjectOpenHashMap<>();

    AnnotationMapping correct = new AnnotationMapping(src, target);

    AnnotationMapping falseNegatives = new AnnotationMapping(src, target);

    for (EntityAnnotation ea: referenceMapping.getAnnotations()){
      set.addEntity(new GenericEntity(ea.getTargetId(),ea.getTargetAccession(),"concept",target.getId()));
      DirectedGraph<Node,Edge> graph = rep.getGraphManager().getIsAConcepts(set, target, DEPTH);
      IntSet superConcepts = hierarchyConcepts.get(ea.getTargetId());
      if (superConcepts==null){
        superConcepts = new IntOpenHashSet();
        hierarchyConcepts.put(ea.getTargetId(), superConcepts);
        for (Node n: graph.getVertices()){
          superConcepts.add(n.getId());
        }
      }
      set.clear();
    }
    for (EntityAnnotation ea: referenceMapping.getAnnotations()){
      if (result.contains(ea)){
        correct.addAnnotation(ea);
      }else if (hierarchyConcepts.containsKey(ea.getTargetId())){
        IntSet set1 = hierarchyConcepts.get(ea.getTargetId());
        Set<Integer> set2 = result.getCorrespondingTargetIds(ea.getSrcId());
        boolean found =false;
        if (set2!=null) {
          for (int hc : set1) {
            if (set2.contains(hc)) {
              correct.addAnnotation(result.getAnnotation(ea.getSrcId(),hc));
              found = true;
            }
          }
        }
        if (!found) {
          falseNegatives.addAnnotation(ea);
        }
      }
    }
    AnnotationMapping falsePositives = SetAnnotationOperator.diff( result, correct);

    er.setFalseNegative(falseNegatives, correct.getNumberOfAnnotations());
    er.setFalsePositives(falsePositives, correct.getNumberOfAnnotations());
    return er;

  }
}
