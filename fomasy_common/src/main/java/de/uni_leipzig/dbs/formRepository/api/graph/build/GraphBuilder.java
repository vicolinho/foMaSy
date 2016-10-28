package de.uni_leipzig.dbs.formRepository.api.graph.build;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Edge;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.EdgeWeightCostModel;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class GraphBuilder {
	
	private Map<Integer,Node> nodeMap;

	Logger log = Logger.getLogger(getClass());
	/**
	 * includes the edges of the corresponding graph. An edge is stored by the parent node as key
	 *  and is stored in a list of edges with respect to the source node.
	 */
	private Map<String,Map<Integer,List<Edge>>> edgeMap;

	public GraphBuilder() {
		edgeMap = new HashMap<String,Map<Integer,List<Edge>>>();
		nodeMap = new HashMap<Integer,Node>();
	}

	public Map<String,Map<Integer,List<Edge>>> getEdgeMap() {
		return edgeMap;
	}

	
	

	
	public DirectedGraph <Node,Edge> generateGraphFromGraphData(){
		DirectedGraph <Node,Edge> graph = new DirectedSparseMultigraph <Node,Edge>();
		for (Map<Integer,List<Edge>> edgesPerTypes: edgeMap.values()){
			for (Entry<Integer,List<Edge>> edges : edgesPerTypes.entrySet()){
				Node n1 = nodeMap.get(edges.getKey());
				for (Edge e:edges.getValue()){
					Node n2 = nodeMap.get(e.getTargetId());
					graph.addEdge(e, n1, n2, EdgeType.DIRECTED);
				}
			}
		}
		return graph;
	}
	
	
	public DirectedGraph <Node,Edge> generateGraphFromGraphData(Map<Integer,Node> nodeMap,
			Map<String,Map<Integer,List<Edge>>> edgeMap){
		DirectedGraph <Node,Edge> graph = new DirectedSparseMultigraph <Node,Edge>();
		for (Map<Integer,List<Edge>> edgesPerTypes: edgeMap.values()){
			for (Entry<Integer,List<Edge>> edges : edgesPerTypes.entrySet()){
				Node n1 = nodeMap.get(edges.getKey());
				if (n1 ==null){
					log.debug(n1+"n1 is null");
				}
				for (Edge e:edges.getValue()){
					Node n2 = nodeMap.get(e.getTargetId());
					if (n2 ==null){
						log.debug(e.getTargetId());
						log.debug(n2+"n2 is null");
					}
					graph.addEdge(e, n1, n2, EdgeType.DIRECTED);
				}
			}
		}
		return graph;
	}
	
	
	
	public DirectedGraph <Node,Edge> generateGraphFromGraphData(EntityStructureVersion esv, Set<String> types){
		DirectedGraph <Node,Edge> graph = new DirectedSparseMultigraph <Node,Edge>();
		Set<Integer> roots = new HashSet<Integer>();
		for (GenericEntity ge : esv.getEntities()){
			if (types.contains(ge.getType())){
				roots.add(Node.encoding(ge.getId(),ge.getType()));
			}
		}
		Set<Integer> reachableNodes = this.bfs(roots);
		for (Integer n: reachableNodes){
			Node n1 = nodeMap.get(n);
			for (Map<Integer,List<Edge>> edgesPerTypes: edgeMap.values()){
				List<Edge> edges =  edgesPerTypes.get(n);
				if (edges!=null){
					for (Edge e:edges){
						Node n2 = nodeMap.get(e.getTargetId());
						graph.addEdge(e, n1, n2, EdgeType.DIRECTED);
					}
				}
			}
		}
		log.info("#vertices: " +graph.getVertexCount());
		return graph;
	}
	
	private Set<Integer> bfs (Set<Integer> roots){
		Set<Integer> reachableNodes = new HashSet<Integer>();
		for (Integer root: roots){
			Stack <Integer> stack  = new Stack<Integer>();
			stack.add(root);
			reachableNodes.add(root);
			while (!stack.isEmpty()){
				int currentNode = stack.pop();
				for (Map<Integer,List<Edge>> edgePerTypeMap: edgeMap.values()){
					if(edgePerTypeMap.containsKey(currentNode)){
						List<Edge> edges = edgePerTypeMap.get(currentNode);
						for (Edge e: edges){
							if (!reachableNodes.contains(e.getTargetId())){
								reachableNodes.add(e.getTargetId());
								stack.add(currentNode);
							}
						}
					}
				}
			}
		}
		return reachableNodes;
	}
	
	public Map<Edge,Float> buidEdgeWeights(EdgeWeightCostModel model,DirectedGraph<Node,Edge> graph){
		Map<Edge,Float> edgeWeightMap = new HashMap<Edge, Float> ();
		for (Node n : graph.getVertices()){
			float sum =0;
			for (Edge e : graph.getOutEdges(n)){
				float weight = model.getEdgeWeightTypeModel().get(e.getType())*e.getWeight();
				edgeWeightMap.put(e, weight);
				sum+=weight;
			}
			for (Edge e : graph.getOutEdges(n)){
				edgeWeightMap.put(e, edgeWeightMap.get(e)/sum);
			}
		}
		
		
		return edgeWeightMap;
	}
}
