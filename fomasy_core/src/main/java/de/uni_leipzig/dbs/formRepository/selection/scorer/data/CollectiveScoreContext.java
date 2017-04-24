package de.uni_leipzig.dbs.formRepository.selection.scorer.data;

import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Edge;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;
import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * Created by christen on 11.04.2017.
 */
public class CollectiveScoreContext {

  private DirectedGraph<Node, Edge> graph;

  private int depth;


  public DirectedGraph<Node, Edge> getGraph() {
    return graph;
  }

  public int getDepth() {
    return depth;
  }

  public class Builder {
    DirectedGraph<Node, Edge> g;
    int depth;

    public Builder graph(DirectedGraph<Node, Edge> g){
      this.g= g;
      return this;
    }

    public Builder depth(int depth){
      this.depth = depth;
      return this;
    }

    public CollectiveScoreContext build(){
      CollectiveScoreContext csc = new CollectiveScoreContext();
      csc.graph = g;
      csc.depth = depth;
      return csc;
    }
  }
}
