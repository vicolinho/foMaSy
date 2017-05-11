package de.uni_leipzig.dbs.formRepository.matching.graph.scorer;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.Function;
import org.apache.commons.collections15.Transformer;

import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.graph.Hypergraph;

public class ComponentsClosnessCentrality<V,E> extends ComponentDistanceCentralityScorer<V,E>{
   /**
     * Creates an instance using the specified vertex/vertex distance metric.
     * @param graph the input
     * @param distance the vertex/vertex distance metric.
     */
    public ComponentsClosnessCentrality(Hypergraph<V,E> graph, Distance<V> distance)
    {
        super(graph, distance, false);
    }

    /**
     * Creates an instance which measures distance using the specified edge weights.
     * @param graph the input graph
     * @param edge_weights the edge weights to be used to determine vertex/vertex distances
     */
    public ComponentsClosnessCentrality(Hypergraph<V,E> graph, Function<E, ? extends Number> edge_weights, Collection<Node> collection)
    {
        super(graph, edge_weights,false,collection);
    }

    /**
     * Creates an instance which measures distance on the graph without edge weights.
     * @param graph
     */
    public ComponentsClosnessCentrality(Hypergraph<V,E> graph)
    {
        super(graph, false);
    }
}
