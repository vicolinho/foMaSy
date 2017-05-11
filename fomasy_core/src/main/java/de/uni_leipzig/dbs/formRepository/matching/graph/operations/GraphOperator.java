package de.uni_leipzig.dbs.formRepository.matching.graph.operations;


import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Edge;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class GraphOperator {

  
  public static DirectedGraph<Node,Edge> union (DirectedGraph <Node,Edge> g1, DirectedGraph<Node,Edge> g2){
    DirectedGraph<Node,Edge> graph = new DirectedSparseMultigraph<Node,Edge>();
    
    for (Node v : g1.getVertices()) {
      graph.addVertex(v);
    }
    for (Node v : g2.getVertices()) {
      graph.addVertex(v);
    }
    for (Edge e : g1.getEdges()) {
      graph.addEdge(e, g1.getEndpoints(e));
    }
    for (Edge e : g2.getEdges()) {
      graph.addEdge(e, g2.getEndpoints(e));
    }
    return graph;
  }
}
