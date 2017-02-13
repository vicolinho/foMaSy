package de.uni_leipzig.dbs.formRepository.matching.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Edge;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.NodeImpl;
import de.uni_leipzig.dbs.formRepository.matching.graph.export.GraphExport;
import de.uni_leipzig.dbs.formRepository.matching.graph.scorer.ComponentsClosnessCentrality;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter.EdgeType;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.graph.DirectedGraph;

public class CooccurrenceAnnotationSelection implements GraphBasedSelection {

	
	public static final float COOCUR_WEIGHT = 0.5f;

	public static final float SIM_WEIGHT = 0.5f;

	Logger log = Logger.getLogger(getClass());

	private Map<Integer,Integer> questionSizeMap;
	public AnnotationMapping selectAnnotationMapping(
			DirectedGraph<Node, Edge> graph, AnnotationMapping am,
			EncodedEntityStructure src, EncodedEntityStructure target,
			Set<GenericProperty> preDomAtts, Set<GenericProperty> preRanAtts,
			float threshold, float delta,float avgEntitySize, FormRepository rep) {
		
		this.questionSizeMap = this.getQuestionSize(src);
		Map<Integer,List<EntityAnnotation>> annoPerItem = GroupFunctions.groupByItem(am);
		Set<Integer> formConceptNodes = new HashSet<Integer>();
		for (Entry<Integer,List<EntityAnnotation>> e:annoPerItem.entrySet()){
			if (e.getValue().size()>1){
				EncodedEntityStructure umlSubset = this.getUMLSConcepts(e.getValue(), target);
				Map<Integer,Set<Integer>> simGroups = GroupFunctions.groupSimilarUMLSConsByCommonToken(umlSubset, e.getKey(), am);
				Set <Long> keptAnnotations = new HashSet<Long>();
				EntityAnnotation currentCor;
				Map<Integer,Set<Integer>> conflictNodesPerNode = new HashMap<Integer,Set<Integer>>();
				for (Set<Integer> list:simGroups.values()){
					for (int g: list){
						Set<Integer> set = conflictNodesPerNode.get(g);
						if (set ==null){
							set = new HashSet<Integer>();
							conflictNodesPerNode.put(g, set);
						}
						set.addAll(list);
					}
				}
				Map<Integer,Float> ranking = this.calculateScores(graph, am, umlSubset, e.getKey(), conflictNodesPerNode, formConceptNodes);


				for (Set<Integer> umlsGroup:simGroups.values()){
					TreeMap<Float,List<EntityAnnotation>> maxCorrs = new TreeMap<Float,List<EntityAnnotation>>();
					for (Integer u : umlsGroup){
						currentCor = am.getAnnotation(e.getKey(), u);
						if (currentCor !=null){						
							float conf =0;
							conf = ranking.get(u);
							List<EntityAnnotation> list= maxCorrs.get(conf);
							if (list==null){
								list = new ArrayList<EntityAnnotation>();
								maxCorrs.put(conf, list);
							}
							currentCor.setSim(conf);
							list.add(currentCor);
						}
		
					}//build treeMap with confidence as key
					
					//select the correspondences with the maximum concept for the current item of the group
					Float lastKey = null;
					try {
						lastKey= maxCorrs.lastKey();
					}catch(NoSuchElementException ex){}
					if (lastKey!=null){
						for (EntityAnnotation ea: maxCorrs.get(lastKey)){
							keptAnnotations.add(ea.getId());
						}
					}
				}//each umls group
				for (EntityAnnotation ea: e.getValue()){
					if (!keptAnnotations.contains(ea.getId())){
						am.removeAnnotation(ea.getSrcId(), ea.getTargetId());
					}
				}
				int questionSize = this.questionSizeMap.get(e.getKey());
				int estimatedEntityCount = (int) Math.floor(questionSize/(avgEntitySize));
				
				if (keptAnnotations.size()>estimatedEntityCount){
					List <EntityAnnotation> list = new ArrayList<EntityAnnotation> ();
					for (long eaId : keptAnnotations){
						list.add(am.getAnnotation(eaId));
						
					}
					Collections.sort(list, Collections.reverseOrder());
					
					for (int i=estimatedEntityCount+1;i<list.size();i++){
						am.removeAnnotation(list.get(i).getSrcId(),list.get(i).getTargetId());
					}
				}
			}//complex annotation
			
		}
		return am;
	}
	
	private Map<Integer,Integer> getQuestionSize (EncodedEntityStructure src){
		Map <Integer,Integer> srcToSizeMap = new HashMap<Integer,Integer>();
		for (Entry<Integer,Integer> ePos :src.getObjIds().entrySet()){
			int pos = ePos.getValue();
			int maxSize =0;
			for (int [][] prop : src.getPropertyValueIds()[pos]){
				for (int [] pvs: prop){
					if (maxSize< pvs.length){
						maxSize = pvs.length;
					}
				}
			}
			srcToSizeMap.put(ePos.getKey(), maxSize);
		}
		return srcToSizeMap;
	}
	
	public Map<Integer,Float> calculateScores (DirectedGraph<Node, Edge> graph,AnnotationMapping am,
			EncodedEntityStructure umlSubset,List<Integer> conflGroup, int srcEntity){
		Map<Integer,Float> ranking = new HashMap<Integer,Float> ();
		Set<Integer> confSet = new HashSet<Integer>(conflGroup);
		Set<Node> roots = new HashSet<Node> ();
		Set<Node> relevantNodes = new HashSet<Node>();
		Set<Node> conflictNodes = new HashSet<Node>();
		for (Integer c:umlSubset.getObjIds().keySet()){
			Node n = new NodeImpl(c);
			if (graph.containsVertex(n)){
				roots.add(n);
				if (!confSet.contains(c)){
					relevantNodes.add(n);
				}else {
					conflictNodes.add(n);
				}
			}
		}
		KNeighborhoodFilter<Node,Edge> filter= new KNeighborhoodFilter<Node,Edge>(roots, 3, EdgeType.IN_OUT);
		DirectedGraph<Node,Edge> subGraph = (DirectedGraph<Node, Edge>) filter.transform(graph);
		
		for (Edge e: subGraph.getEdges()){
			if (e.getType().equals("co_annotates")){
				if (!roots.contains(e.getTargetId())){
					//remNodes.add(e)
				}
			}
		}
		//GraphExport exporter = new GraphExport();
		//exporter.writeGraphCSV("graphs/"+srcEntity, subGraph);
		Transformer<Edge, Float> transformer = new Transformer<Edge,Float>(){
			public Float transform(Edge input) {
				float distance =0;
				if (input.getType().equals("co_form_annotates"))
					distance = 1/(input.getWeight()*0.5f);
				else
					distance = 1/input.getWeight();
				return distance;
			}
		};
		
		ComponentsClosnessCentrality<Node,Edge> cc = new ComponentsClosnessCentrality<Node,Edge> (subGraph, transformer,relevantNodes);
		for (Node n :conflictNodes){
			Double score = cc.getVertexScore(n);
			if (score ==null)
				score =0d;
			float overallScore = (float) (am.getAnnotation(srcEntity, n.getId()).getSim()*SIM_WEIGHT+score*COOCUR_WEIGHT);
			ranking.put(n.getId(), overallScore);
		}
		for (Integer c:umlSubset.getObjIds().keySet()){
			if (!ranking.containsKey(c)){
				ranking.put(c, am.getAnnotation(srcEntity, c).getSim()*SIM_WEIGHT);
			}
		}
		return ranking;
	}
	
	public Map<Integer,Float> calculateScores (DirectedGraph<Node, Edge> graph,AnnotationMapping am,
			EncodedEntityStructure umlSubset,int srcEntity,Map<Integer,Set<Integer>> conflictNodesPerNode, Set<Integer> formConceptNodes){
		//TODO check if it achieves better quality
		Map<Integer,Float> ranking = new HashMap<Integer,Float> ();
		//Set<Integer> confSet = new HashSet<Integer>(conflGroup);
		Set<Node> roots = new HashSet<Node> ();
		Map<Integer,Node> relevantNodes = new HashMap<Integer,Node>();
		for (Integer c:umlSubset.getObjIds().keySet()){
			Node n = new NodeImpl(c);
			if (graph.containsVertex(n)){
				roots.add(n);
				relevantNodes.put(c,n);	
			}
		}
		//TODO check if it achieves better quality
		/*
		for (int id :formConceptNodes){
			Node n = new NodeImpl(id);
			if (graph.containsVertex(n)){
				relevantNodes.put(id,n);	
			}
		}*/
		
		KNeighborhoodFilter<Node,Edge> filter= new KNeighborhoodFilter<Node,Edge>(roots, 3, EdgeType.IN_OUT);
		
		DirectedGraph<Node,Edge> subGraph = (DirectedGraph<Node, Edge>) filter.transform(graph);
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
//			if (e.getType().equals("co_form_annotates")){
//				if (!formConceptNodes.contains(e.getSrcId())){
//					nodes.add(relevantNodes.get(e.getSrcId()));
//					edges.add(e);
//				}else{
//					nodes.remove(relevantNodes.get(e.getSrcId()));
//					edges.remove(e);
//				}
//				if (!formConceptNodes.contains(e.getTargetId())){
//					nodes.add(relevantNodes.get(e.getTargetId()));
//					edges.add(e);
//				}else{
//					nodes.remove(relevantNodes.get(e.getTargetId()));
//					edges.remove(e);
//				}
//			}
		}
		
		for (Edge e: edges){
			subGraph.removeEdge(e);
		}
		for (Node n: nodes){
			subGraph.removeVertex(n);
		}
		
//		GraphExport exporter = new GraphExport();
//		exporter.writeGraphCSV("graphs/"+srcEntity, subGraph);
		Transformer<Edge, Float> transformer = new Transformer<Edge,Float>(){
			public Float transform(Edge input) {
				float distance = 0;
				if (input.getType().equals("co_form_annotates")){
					distance = 1/(input.getWeight()*0.2f);
				}else{
					distance = 1/(input.getWeight());
					if (input.getType().equals("co_annotates"))
						log.debug(input.toString());
				}
				return distance;
			}
		};
		
		ComponentsClosnessCentrality<Node,Edge> cc = new ComponentsClosnessCentrality<Node,Edge> (subGraph, transformer,relevantNodes.values());
		
		for (Node n  :roots){
//			Set<Node> nodeSet =new HashSet<Node> ();
//			Set <Integer> conflictSet = conflictNodesPerNode.get(n.getId());
//			if (conflictSet!=null){
//				for (int id : conflictSet){
//					nodeSet.add(new NodeImpl(id));
//				}
//				cc.setRelevantNodes(nodeSet);
//			}
			Double score = cc.getVertexScore(n);
			if (score ==null)
				score =0d;
			float overallScore = (float) (am.getAnnotation(srcEntity, n.getId()).getSim()*SIM_WEIGHT+score*COOCUR_WEIGHT);
			ranking.put(n.getId(), overallScore);
		}
		//log.info("source: "+srcEntity+" targets: "+roots.toString()+" ranking:"+ranking.toString());
		//log.info("end");
		for (Integer c:umlSubset.getObjIds().keySet()){
			if (!ranking.containsKey(c)){
				ranking.put(c, am.getAnnotation(srcEntity, c).getSim()*SIM_WEIGHT);
			}
		}
		return ranking;
	}
	
	public EncodedEntityStructure getUMLSConcepts(List<EntityAnnotation> list, EncodedEntityStructure umls){
		Set<Integer> targetIds  = new HashSet<Integer>();
		for (EntityAnnotation cor:list){
			targetIds.add(cor.getTargetId());
		}
		
		EncodedEntityStructure set = EncodingManager.getInstance().getSubset(umls, targetIds);
		return set;
	}

	

}
