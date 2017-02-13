package de.uni_leipzig.dbs.formRepository.api.graph;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.api.annotation.entity.RDBMS_EntityAPI;
import de.uni_leipzig.dbs.formRepository.api.util.DBConHandler;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;
import de.uni_leipzig.dbs.formRepository.exception.GraphAPIException;
import de.uni_leipzig.dbs.formRepository.api.graph.build.GraphBuilder;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Edge;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.EdgeImpl;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.NodeImpl;
import edu.uci.ics.jung.graph.DirectedGraph;

public class RDBMS_GraphAPI implements GraphAPI{
	
	Logger log = Logger.getLogger (getClass());

	private HashMap<String, Map<Integer, List<Edge>>> parentChildRelationship;

	private HashMap<Integer, Node> nodeMap;
	private Map <String,Float> simMap ;
	
	int overallEdges =0;
	
	private Map<String,Integer> edgeTypeCount;
	public static final String RELS = "Select  target_id, r.rel_name, e.ent_type from entity_relationship er , rel_type r ,entity e "
			+ "where "
			+"r.rel_type_id = er.rel_type_id_fk AND "
			+ "r.rel_type_id not in (64,95,96,125,143,195) AND "
			+ "e.ent_id = target_id AND "
			+"er.src_id =? ";

	public DirectedGraph<Node, Edge> getSubgraphFromExternalStructure(
			EntitySet<GenericEntity> nodes, VersionMetadata externalStructure,
			GenericProperty joinNodeAttribute,
			Set<GenericProperty> joinExternalAttribute, int depth) throws EntityAPIException, GraphAPIException {
		RDBMS_EntityAPI entApi = new RDBMS_EntityAPI();
		Connection con = null ;
		Map <String,List<GenericEntity>> valueEntityMapping = new HashMap<String,List<GenericEntity>>();
		edgeTypeCount = new HashMap<String,Integer>();
		try {
			con = DBConHandler.getInstance().getConnection();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Set<String> values =new HashSet<String>();
		
		/*-----save mapping between values and entities--*/
		for (GenericEntity ge:nodes){
			List <String> joinValues = ge.getPropertyValues(joinNodeAttribute);
			for (String v:joinValues){
				List<GenericEntity> list = valueEntityMapping.get(v);
				if (list == null){
					list = new ArrayList<GenericEntity>();
					valueEntityMapping.put(v, list);
				}
				list.add(ge);
			}
			values.addAll(joinValues);
		}
		EntitySet<GenericEntity> nodeSet = entApi.getEntityWithPropertiesByProperty(values, externalStructure, joinExternalAttribute);
		nodeMap = new HashMap<Integer,Node>();
		parentChildRelationship = new HashMap<String, Map<Integer,List<Edge>>>();
		
		this.breathFirstSearch(con, nodeSet, valueEntityMapping, joinExternalAttribute, depth);
		GraphBuilder builder = new GraphBuilder();
		DirectedGraph<Node,Edge> graph = builder.generateGraphFromGraphData(nodeMap, parentChildRelationship);
		float maxRidf = 0;
//		for (Edge e : graph.getEdges()){
//			Integer count = this.edgeTypeCount.get(e.getType());
//			if (count!=null){
//				float ridf = (float) Math.log((float)(overallEdges/(float)count));
//				if (ridf>maxRidf){
//					maxRidf = ridf;
//				}
//				if (!e.getType().equals("has_semanticType")){
//					e.setWeight(ridf);
//				}
//			}
//		}
//		for (Edge e : graph.getEdges()){
//			if (!e.getType().equals("has_semanticType"))
//				e.setWeight(e.getWeight()/maxRidf);
//			
//		}
//		
//		
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return graph;
	}

	public DirectedGraph <Node,Edge> getGraphFromStructure (EntitySet<GenericEntity> rootNodes,VersionMetadata structure,
			int depth)throws GraphAPIException{
		nodeMap = new HashMap<Integer,Node>();
		parentChildRelationship = new HashMap<String, Map<Integer,List<Edge>>>();
		edgeTypeCount = new HashMap<String,Integer>();
		Connection con =null;
		try {
			con = DBConHandler.getInstance().getConnection();
			this.breathFirstSearch(con, rootNodes, depth);
			
			
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new GraphAPIException(e);
		}finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		GraphBuilder builder = new GraphBuilder();
		DirectedGraph<Node,Edge> graph = builder.generateGraphFromGraphData(nodeMap, parentChildRelationship);
//		float maxRidf = 0;
//		for (Edge e : graph.getEdges()){
//			Integer count = this.edgeTypeCount.get(e.getType());
//			if (count!=null){
//				float ridf = (float) Math.log((float)(overallEdges/(float)count));
//				if (ridf>maxRidf){
//					maxRidf = ridf;
//				}
//				if (!e.getType().equals("has_semanticType")){
//					e.setWeight(ridf);
//				}
//			}
//		}
//		for (Edge e : graph.getEdges()){
//			if (!e.getType().equals("has_semanticType"))
//				e.setWeight(e.getWeight()/maxRidf);
//		}
		return graph;
		
	}
	
	private void breathFirstSearch(Connection con, EntitySet<GenericEntity> nodeSet,
			Map <String,List<GenericEntity>> valueEntityMapping,Set<GenericProperty> gp,int depth ) throws GraphAPIException{
		try {
			Set<Integer> alreadyVisit = new HashSet<Integer>();
			PreparedStatement relStmt = con.prepareStatement(RELS);
			for (GenericEntity ge : nodeSet){
				
				alreadyVisit.clear();
				this.addEdgeToSourceNode(ge, valueEntityMapping, gp);
				Node n = new NodeImpl(ge.getId());
				n.setDepth(0);
				nodeMap.put(n.getId(), n);
				Stack<Integer> stack = new Stack<Integer>();
				stack.add(ge.getId());
				
				relStmt.setInt(1,ge.getId());
				
				while (!stack.isEmpty()){
					int currentNode = stack.pop();
					relStmt.setInt(1,currentNode);
					
					ResultSet rs = relStmt.executeQuery();
					while (rs.next()){
						int targetId = rs.getInt(1);
						String rel_type = rs.getString(2);
						String ent_type = rs.getString(3);
						
						if (!alreadyVisit.contains(targetId)&&
								(nodeMap.get(currentNode).getDepth()+1)<=depth){
							stack.add(targetId);
							alreadyVisit.add(targetId);
							Node child = new NodeImpl(targetId);
							nodeMap.put(child.getId(), child);
							child.setDepth(nodeMap.get(currentNode).getDepth()+1);				
						}
						if (alreadyVisit.contains(targetId)){
							Edge  e  = new EdgeImpl(currentNode, targetId, rel_type);
							this.overallEdges++;
							e.setWeight(0.5f);
							if (edgeTypeCount.containsKey(e.getType())){
								edgeTypeCount.put(rel_type, 1+edgeTypeCount.get(rel_type));
							}else{
								edgeTypeCount.put(rel_type, 1);
							}
							Edge  revEdge  = new EdgeImpl(targetId, currentNode, rel_type);
							revEdge.setWeight(0.5f);
							Map <Integer,List<Edge>> edgesPerType = parentChildRelationship.get(rel_type);
							if (edgesPerType ==null){
								edgesPerType = new HashMap<Integer,List<Edge>>();
								parentChildRelationship.put(rel_type, edgesPerType);
							}
							List<Edge> children = edgesPerType.get(currentNode);
							if (children ==null){
								children = new ArrayList<Edge>();
								edgesPerType.put(currentNode, children);
							}
							children.add(e);
							
							List<Edge> parents = edgesPerType.get(targetId);
							if (parents ==null){
								parents = new ArrayList<Edge>();
								edgesPerType.put(targetId, parents);
							}
							parents.add(revEdge);
						}
					}
					rs.close();
				}//stack is not empty
			}
			relStmt.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new GraphAPIException(e1);
		}
	}
	
	private void breathFirstSearch (Connection con,EntitySet<GenericEntity> roots,int depth){
		try {
			Set<Integer> alreadyVisit = new HashSet<Integer>();
			PreparedStatement relStmt = con.prepareStatement(RELS);
			for (GenericEntity ge : roots){
				alreadyVisit.clear();
				Node n = new NodeImpl(ge.getId());
				n.setDepth(0);
				nodeMap.put(n.getId(), n);
				Stack<Integer> stack = new Stack<Integer>();
				stack.add(ge.getId());
				relStmt.setInt(1,ge.getId());
				while (!stack.isEmpty()){
					int currentNode = stack.pop();
					relStmt.setInt(1,currentNode);
					
					ResultSet rs = relStmt.executeQuery();
					while (rs.next()){
						int targetId = rs.getInt(1);
						String rel_type = rs.getString(2);
						String ent_type = rs.getString(3);
						
						if (!alreadyVisit.contains(targetId)&&
								(nodeMap.get(currentNode).getDepth()+1)<=depth){
							this.overallEdges++;
							stack.add(targetId);
							alreadyVisit.add(targetId);
							Node child = new NodeImpl(targetId);
							nodeMap.put(child.getId(), child);
							child.setDepth(nodeMap.get(currentNode).getDepth()+1);				
						}
						if (alreadyVisit.contains(targetId)){
							Edge  e  = new EdgeImpl(currentNode, targetId, rel_type);
							this.overallEdges++;
							e.setWeight(0.5f);
							if (edgeTypeCount.containsKey(e.getType())){
								edgeTypeCount.put(rel_type, 1+edgeTypeCount.get(rel_type));
							}else{
								edgeTypeCount.put(rel_type, 1);
							}
							Edge  revEdge  = new EdgeImpl(targetId, currentNode, rel_type);
							revEdge.setWeight(0.5f);
							Map <Integer,List<Edge>> edgesPerType = parentChildRelationship.get(rel_type);
							if (edgesPerType ==null){
								edgesPerType = new HashMap<Integer,List<Edge>>();
								parentChildRelationship.put(rel_type, edgesPerType);
							}
							List<Edge> children = edgesPerType.get(currentNode);
							if (children ==null){
								children = new ArrayList<Edge>();
								edgesPerType.put(currentNode, children);
							}
							children.add(e);
							
							List<Edge> parents = edgesPerType.get(targetId);
							if (parents ==null){
								parents = new ArrayList<Edge>();
								edgesPerType.put(targetId, parents);
							}
							parents.add(revEdge);
						}
						
						
					}
					rs.close();
				}//stack is not empty
			}
			relStmt.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void addEdgeToSourceNode(GenericEntity target,Map <String,List<GenericEntity>> valueEntityMapping,Set<GenericProperty> gp){
		for (GenericProperty p:gp){
			List<String> values = target.getPropertyValues(p);
			for (String v: values){
				List<GenericEntity> sourceEntities = valueEntityMapping.get(v);
				for (GenericEntity ge: sourceEntities){
					Node n = nodeMap.get(ge.getId());
					if (n==null){
						n = new NodeImpl(ge.getId());
						nodeMap.put(ge.getId(), n);
					}
					Edge  e  = new EdgeImpl(ge.getId(), target.getId(), "has_semanticType");
					Edge  revEdge  = new EdgeImpl(target.getId(),ge.getId(), "has_semanticType");
					e.setWeight(1);
					revEdge.setWeight(1);
					Map <Integer,List<Edge>> edgesPerType = parentChildRelationship.get("has_semanticType");
					if (edgesPerType ==null){
						edgesPerType = new HashMap<Integer,List<Edge>>();
						parentChildRelationship.put("has_semanticType", edgesPerType);
					}
					List<Edge> children = edgesPerType.get(ge.getId());
					if (children ==null){
						children = new ArrayList<Edge>();
						edgesPerType.put(ge.getId(), children);
					}
					children.add(e);
					
					List<Edge> parents = edgesPerType.get(target.getId());
					if (parents ==null){
						parents = new ArrayList<Edge>();
						edgesPerType.put(target.getId(), parents);
					}
					parents.add(revEdge);
				}
				
			}
		}
	}
}
