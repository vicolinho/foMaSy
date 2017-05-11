package de.uni_leipzig.dbs.formRepository.selection.scorer.collective;

import com.google.common.base.Function;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Edge;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.NodeImpl;
import de.uni_leipzig.dbs.formRepository.matching.graph.scorer.ComponentsClosnessCentrality;
import de.uni_leipzig.dbs.formRepository.selection.scorer.data.CollectiveScoreContext;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import edu.uci.ics.jung.graph.DirectedGraph;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 11.04.2017.
 */
public class CCItemGraphScorer implements CollectiveScorer {

  Logger log = Logger.getLogger(getClass());

  @Override
  public Map<Integer, Map<Integer, Double>> computeScore(AnnotationMapping am, Map<Integer, Set<Set<Integer>>> conflictSet,
      CollectiveScoreContext scContext) {

    log.info("#vertices:" +scContext.getGraph().getVertexCount()+", #edges:"+scContext.getGraph().getEdgeCount());
    Map<Integer, Map<Integer, Double>> rankingPerItem = new HashMap<>();
    int depth = scContext.getDepth();
    Set<Integer> formConceptNodes = new HashSet<>();
    for (EntityAnnotation ea:am.getAnnotations()){
      formConceptNodes.add(ea.getTargetId());
    }

    Set<Integer> relevantNodes = new HashSet<>();
    DirectedGraph<Node, Edge> graph = scContext.getGraph();
    for (Map.Entry<Integer, Set<Set<Integer>>> e: conflictSet.entrySet()){
      relevantNodes.clear();
      for (Set<Integer> cc: e.getValue()){
        relevantNodes.addAll(cc);
      }
      Map<Integer,Double> ranking = this.calculateScores(graph, depth, am, relevantNodes,
              e.getKey(), e.getValue(), formConceptNodes);
      rankingPerItem.put(e.getKey(), ranking);
    }
    return rankingPerItem;
  }



  public Map<Integer,Double> calculateScores (DirectedGraph<Node, Edge> graph, int depth, AnnotationMapping am,
      Set<Integer> umlSubset, int srcEntity, Set<Set<Integer>> conflictNodesPerNode, Set<Integer> formConceptNodes){

    Map<Integer,Double> ranking = new HashMap<>();
    Set<Node> roots = new HashSet<Node>();
    Map<Integer,Node> relevantNodes = new HashMap<Integer,Node>();
    for (Integer c:umlSubset){
      Node n = new NodeImpl(c);
      if (graph.containsVertex(n)){
        roots.add(n);
        relevantNodes.put(c,n);
      }
    }

    KNeighborhoodFilter<Node,Edge> filter= new KNeighborhoodFilter<Node,Edge>(roots, depth, KNeighborhoodFilter.EdgeType.IN_OUT);
    DirectedGraph<Node,Edge> subGraph = (DirectedGraph<Node, Edge>) filter.apply(graph);
    log.info("#vertices:" +subGraph.getVertexCount()+", #edges:"+subGraph.getEdgeCount());
    Set <Node> nodes = new HashSet<Node>();
    Set <Edge> edges = new HashSet<Edge>();
    for (Edge e :subGraph.getEdges()){
      if (e.getType().equals("co_annotates")){
        if (!relevantNodes.containsKey(e.getSrcId())){
          nodes.add(relevantNodes.get(e.getSrcId()));
          edges.add(e);
        }
        if (!relevantNodes.containsKey(e.getTargetId())){
          nodes.add(relevantNodes.get(e.getTargetId()));
          edges.add(e);
        }
      }
    }

    for (Edge e: edges){
      subGraph.removeEdge(e);
    }
    for (Node n: nodes){
      subGraph.removeVertex(n);
    }

//		GraphExport exporter = new GraphExport();
//		exporter.writeGraphCSV("graphs/"+srcEntity, subGraph);
    Function<Edge, Float> transformer = new Function<Edge, Float>(){
      @Override
      public Float apply(@Nullable Edge edge) {
        float distance = 0;
        if (edge.getType().equals("co_form_annotates")){
          distance = 1/(edge.getWeight()*0.2f);
        }else{
          distance = 1/(edge.getWeight());
          if (edge.getType().equals("co_annotates"))
            log.debug(edge.toString());
        }
        return distance;
      }
    };

    ComponentsClosnessCentrality<Node,Edge> cc = new ComponentsClosnessCentrality<Node,Edge> (subGraph, transformer,relevantNodes.values());
    double totalScore =0;
    for (Node n  :roots){
      Double score = cc.getVertexScore(n);
      if (score ==null)
        score =0d;
      totalScore+=score;
      ranking.put(n.getId(), score);
    }

    //TODO check if normalization generates better results
    for (Map.Entry<Integer, Double> n: ranking.entrySet()){
      ranking.put(n.getKey(), n.getValue()/totalScore);
    }
    return ranking;
  }
}
