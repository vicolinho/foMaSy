package de.uni_leipzig.dbs.formRepository.reuse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationCluster;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.api.graph.build.GraphBuilder;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Edge;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.EdgeImpl;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.NodeImpl;
import edu.uci.ics.jung.graph.DirectedGraph;

public class CooccurenceCalculator {

	public DirectedGraph<Node,Edge> calculateSemanticCoocurence (Map <GenericEntity, AnnotationCluster> cluster,
			String clusterName,int common, FormRepository rep){
		Map <String,Set<GenericEntity>> semTypeToConcept = new HashMap<String,Set<GenericEntity>>();
		Map<String,Map<String,Float>> semTypeToSemType = new HashMap<String,Map<String,Float>>();
		Map<String,Set<Integer>> semTypeToAnnotations = new HashMap<String,Set<Integer>>();
		Map<Integer,Node> nodeMap = new HashMap<Integer,Node> ();
	
		for (Entry<GenericEntity,AnnotationCluster> ge: cluster.entrySet()){
			List<String> semType1 = ge.getKey().getPropertyValues("sem_type", null, null);
			Node n = new NodeImpl(ge.getValue().getId());
			nodeMap.put(n.hashCode(), n);
			for (String semType: semType1){
				Set<GenericEntity> concepts = semTypeToConcept.get(semType);
				if (concepts==null){
					concepts = new HashSet<GenericEntity>();
					semTypeToConcept.put(semType, concepts);
				}
				concepts.add(ge.getKey());
				Set<Integer> annQuestions = semTypeToAnnotations.get(semType);
				if (annQuestions==null){
					annQuestions = new HashSet<Integer>();
					semTypeToAnnotations.put(semType, annQuestions);
				}
				for (GenericEntity ann: ge.getValue().getElements())
					annQuestions.add(ann.getId());
			}	
		}
		
		for (Entry <String,Set<Integer>> semToQuestsEntry:semTypeToAnnotations.entrySet()){
			Set<Integer> set1 = semToQuestsEntry.getValue();
			for (Entry <String,Set<Integer>> semToQuestsEntry2:semTypeToAnnotations.entrySet()){
				if (!semToQuestsEntry.getKey().equals(semToQuestsEntry2.getKey())){
					Set<Integer> set2 = semToQuestsEntry2.getValue();
					Set<Integer> copy = new HashSet<Integer>(set1);
					copy.retainAll(set2);
					if (copy.size()>=common){
						Map<String,Float> corMap = semTypeToSemType.get(semToQuestsEntry.getKey());
						if (corMap ==null){
							corMap = new HashMap<String,Float>();
							semTypeToSemType.put(semToQuestsEntry.getKey(), corMap);
						}
						float weight = (float)copy.size()/semToQuestsEntry.getValue().size();
						corMap.put(semToQuestsEntry2.getKey(), weight);
					
					}
				}
			}
		}
			
		Map<String, Map<Integer,List<Edge>>> edgeMap2 = new HashMap<String, Map<Integer,List<Edge>>>();
		for (GenericEntity ge: cluster.keySet()){
			for (String semanticType: ge.getPropertyValues("sem_type", null, null)){
				Map<Integer,List<Edge>> edgeMapPerType = edgeMap2.get("co_"+semanticType);
				if (edgeMapPerType ==null){
					edgeMapPerType = new HashMap<Integer,List<Edge>>();
					edgeMap2.put("co_"+semanticType, edgeMapPerType);
				}
				Map<String,Float> semTypesCor = semTypeToSemType.get(semanticType);
				List<Edge> edgeList = edgeMapPerType.get(ge.getId());
				if (edgeList==null){
					edgeList = new ArrayList<Edge>();
					edgeMapPerType.put(Node.encoding(ge.getId(), null), edgeList);
				}
				if (semTypesCor!=null){
					for (Entry<String,Float> corEntry: semTypesCor.entrySet()){
						Set<GenericEntity> corEntity = semTypeToConcept.get(corEntry.getKey());
						for (GenericEntity ge2: corEntity){
							
							Edge e = new EdgeImpl(Node.encoding(ge.getId(), null),Node.encoding(ge2.getId(), null),
									"co_"+semanticType);
							e.setWeight(corEntry.getValue());
							edgeList.add(e);
						}
					}
				}
			}
		}
		GraphBuilder gb= new GraphBuilder();
		DirectedGraph <Node,Edge> graph = gb.generateGraphFromGraphData(nodeMap, edgeMap2);
		return graph;
	}
	
	
	
	public DirectedGraph<Node,Edge> calculateCoocurence (Map <GenericEntity, AnnotationCluster> cluster,String clusterName,int common, FormRepository rep){
		Map<Integer,Map<Integer,Float>> edgeMap = rep.getClusterManager().getCooccurrences(clusterName,common);
		//Map<Integer,Map<Integer,Float>> formEdgeMap = rep.getClusterManager().getFormCoocccurrences(clusterName, 2*common);
		GraphBuilder gb = new GraphBuilder();
		Map<Integer,AnnotationCluster> acMap = new HashMap<Integer,AnnotationCluster>();
		Map<Integer,Node> nodeMap = new HashMap<Integer,Node>();
		for (Entry<GenericEntity,AnnotationCluster> ac:cluster.entrySet()){
			Node n = new NodeImpl(ac.getValue().getId());
			nodeMap.put(n.hashCode(), n);
			acMap.put(ac.getValue().getId(), ac.getValue());
		}
		Map<String, Map<Integer,List<Edge>>> edgeMap2 = new HashMap<String, Map<Integer,List<Edge>>>();
		for (Entry<Integer, Map<Integer,Float>> e:edgeMap.entrySet()){
			for (Entry<Integer,Float> edge:e.getValue().entrySet()){
				if (nodeMap.containsKey(Node.encoding(e.getKey(), null)) &&
						nodeMap.containsKey(Node.encoding(edge.getKey(),null))){
					Edge ed= new EdgeImpl(Node.encoding(e.getKey(), null),Node.encoding(edge.getKey(),null),
							"co_annotates");
					float w = edge.getValue();
					w = w/(float)acMap.get(e.getKey()).getElements().getSize();
					ed.setWeight(w);
					ed.setTotal(acMap.get(e.getKey()).getElements().getSize());
					ed.setCooccurCount(edge.getValue());
					Map<Integer,List<Edge>> mapPerType = edgeMap2.get("co_annotates");
					if (mapPerType == null){
						mapPerType = new HashMap<Integer,List<Edge>>();
						edgeMap2.put("co_annotates", mapPerType);
					}
					List<Edge> edgeList = mapPerType.get(Node.encoding(e.getKey(), null));
					if (edgeList == null){
						edgeList = new ArrayList<Edge>();
						mapPerType.put(Node.encoding(e.getKey(), null), edgeList);
					}
					edgeList.add(ed);
				}
			}
		}
		//TODO check if it achieves better quality
//		for (Entry<Integer, Map<Integer,Float>> e:formEdgeMap.entrySet()){
//			for (Entry<Integer,Float> edge:e.getValue().entrySet()){
//				if (nodeMap.containsKey(Node.encoding(e.getKey(), null)) &&
//						nodeMap.containsKey(Node.encoding(edge.getKey(),null))){
//					Edge ed= new EdgeImpl(Node.encoding(e.getKey(), null),Node.encoding(edge.getKey(),null),
//							"co_form_annotates");
//					float w = edge.getValue();
//					ed.setWeight(w);
//					Map<Integer,List<Edge>> mapPerType = edgeMap2.get("co_form_annotates");
//					if (mapPerType == null){
//						mapPerType = new HashMap<Integer,List<Edge>>();
//						edgeMap2.put("co_form_annotates", mapPerType);
//					}
//					List<Edge> edgeList = mapPerType.get(Node.encoding(e.getKey(), null));
//					if (edgeList == null){
//						edgeList = new ArrayList<Edge>();
//						mapPerType.put(Node.encoding(e.getKey(), null), edgeList);
//					}
//					edgeList.add(ed);
//				}
//			}
//		}
		DirectedGraph <Node,Edge> graph = gb.generateGraphFromGraphData(nodeMap, edgeMap2);
		return graph;
	}
}
