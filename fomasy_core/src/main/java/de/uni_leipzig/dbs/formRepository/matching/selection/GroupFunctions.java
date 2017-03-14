package de.uni_leipzig.dbs.formRepository.matching.selection;


import edu.stanford.nlp.graph.ConnectedComponents;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.*;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;

import java.util.*;
import java.util.Map.Entry;


import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EncodedAnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.execution.RegisteredMatcher;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.ExecutionTree;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.MatchOperator;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.CliqueIdentification;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.TokenWeighting.TFIDFTokenWeightGenerator;
import de.uni_leipzig.dbs.formRepository.matching.token.TFIDFMatcher;
import de.uni_leipzig.dbs.formRepository.util.CantorDecoder;
import edu.ucla.sspace.clustering.Assignments;
import edu.ucla.sspace.clustering.ChineseWhispers;
import edu.ucla.sspace.matrix.ArrayMatrix;
import edu.ucla.sspace.matrix.Matrix;

class GroupFunctions {

	static Logger log = Logger.getLogger(GroupFunctions.class);
public static HashMap<Integer, List<Integer>> groupSimilarUMLSConsByCliques(EncodedEntityStructure umlsGroup,
			Set<GenericProperty> preRanAtts,int size, FormRepository gi) {
		
		
		EncodedAnnotationMapping set =new EncodedAnnotationMapping();
		Set<Integer> srcs = new HashSet<Integer>();
		srcs.add(umlsGroup.getStructureId());
		MatchOperator mop = new MatchOperator (RegisteredMatcher.TRIGRAM_MATCHER, AggregationFunction.MAX, preRanAtts, preRanAtts, 0.35f);
		ExecutionTree tree = new ExecutionTree();
		tree.addOperator(mop);
		try {
			set = gi.getMatchManager().matchEncoded(umlsGroup, umlsGroup, tree, null);
		} catch (MatchingExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HashSet<EntityAnnotation> toRemoveCorrs = new HashSet<EntityAnnotation>();
		Set<Integer> nodes = new HashSet<Integer>();
		HashMap<Integer, List<Integer>> edges = new HashMap<Integer,List<Integer>>();
		for (EntityAnnotation c: set.getAnnotations()){
			if (c.getSrcId()==c.getTargetId()){
				toRemoveCorrs.add(c);
			}else {
				nodes.add(c.getSrcId());
				nodes.add(c.getTargetId());
				List<Integer> srcEdges = edges.get(c.getSrcId());
				List<Integer> targetEdges = edges.get(c.getTargetId());
				if (srcEdges ==null){
					srcEdges = new ArrayList<Integer>();
					edges.put(c.getSrcId(), srcEdges);
				}
				if (targetEdges ==null){
					targetEdges = new ArrayList<Integer>();
					edges.put(c.getTargetId(), targetEdges);
				}
				srcEdges.add(c.getTargetId());
				targetEdges.add(c.getSrcId());
				
			}
		}
		CliqueIdentification clique = new CliqueIdentification();
		Set<Set<Integer>> cliques = clique.simpleCluster(nodes, edges);
		HashMap<Integer,List<Integer>> umlsGroups = new HashMap<Integer,List<Integer>>();
		HashSet<Integer> notInGroup =new HashSet<Integer>(); 
		for (int id : umlsGroup.getObjIds().keySet()){
			notInGroup.add(id);
		}
		notInGroup.removeAll(nodes);
		int id =0;
		for (Set<Integer> c:cliques){
			List<Integer> list = new ArrayList<Integer>(c);
				umlsGroups.put(id++,list);
		}
		for (Integer nig : notInGroup){
			List<Integer> list = new ArrayList<Integer>();
			umlsGroups.put(id++, list);
			list.add(nig);
		}
		
		return umlsGroups;
	} 

public static HashMap<Integer, List<Integer>> groupSimilarUMLSConsByCW(EncodedEntityStructure umlsGroup,Set<GenericProperty> preRanAtts,int size, FormRepository gi) {
	
	
	EncodedAnnotationMapping set =new EncodedAnnotationMapping();
	Set<Integer> srcs = new HashSet<Integer>();
	srcs.add(umlsGroup.getStructureId());
	long time = System.currentTimeMillis();
	MatchOperator mop = new MatchOperator (RegisteredMatcher.TFIDF_MATCHER, AggregationFunction.MAX,
					preRanAtts, preRanAtts, 0.35f);
	ExecutionTree tree = new ExecutionTree();
	tree.addOperator(mop);
	try {
		set = gi.getMatchManager().matchEncoded(umlsGroup, umlsGroup, tree, null);
	} catch (MatchingExecutionException e1) {
		e1.printStackTrace();
	}

	Map<Integer,Integer> positionMapping = new HashMap<Integer,Integer>();
	Map<Integer,Integer> revMapping = new HashMap<Integer,Integer>();
	HashSet<EntityAnnotation> toRemoveCorrs = new HashSet<EntityAnnotation>();
	int id =0;

	for (EntityAnnotation c: set.getAnnotations()){
		if (c.getSrcId()==c.getTargetId()){
			toRemoveCorrs.add(c);
		}else {
			if (!positionMapping.containsKey(c.getSrcId())){
				revMapping.put(id, c.getSrcId());
				positionMapping.put(c.getSrcId(), id++);
			}
			if (!positionMapping.containsKey(c.getTargetId())){
				revMapping.put(id, c.getTargetId());
				positionMapping.put(c.getTargetId(), id++);
			}
		}
	}
	
	Matrix adMatrix = new ArrayMatrix(positionMapping.size(),positionMapping.size());
	for (EntityAnnotation c: set.getAnnotations()){
		if (positionMapping.containsKey(c.getSrcId()) && positionMapping.containsKey(c.getTargetId())){
			int posX = positionMapping.get(c.getSrcId());
			int posY = positionMapping.get(c.getTargetId());
			if (c.getSrcId()==c.getTargetId()){
				adMatrix.set(posX, posY, 0);
			}else {
				adMatrix.set(posX, posY, c.getSim());
			}
		}
		
	}
	ChineseWhispers w = new ChineseWhispers(1000);
	Assignments assig = w.cluster(adMatrix, null);
	HashMap<Integer,List<Integer>> umlsGroups = new HashMap<Integer,List<Integer>>();
	HashSet<Integer> notInGroup =new HashSet<Integer>(); 
	for (int node : umlsGroup.getObjIds().keySet()){
		notInGroup.add(node);
	}
	notInGroup.removeAll(positionMapping.keySet());
	id =0;
	for (Set<Integer> c :assig.clusters()){
		List<Integer> list = new ArrayList<Integer>();
		for (int i : c){
			list.add(revMapping.get(i));
		}
		umlsGroups.put(id++,list);
	}
	for (Integer nig : notInGroup){
		List<Integer> list = new ArrayList<Integer>();
		umlsGroups.put(id++, list);
		list.add(nig);
	}
	
	return umlsGroups;
} 

public  static Map<Integer, Set<Integer>> groupSimilarUMLSConsByConnectedComponent(EncodedEntityStructure umlsGroup,
	 	Set<GenericProperty> preRanAtts,int size, FormRepository gi, float threshold){
	EncodedAnnotationMapping set =new EncodedAnnotationMapping();
	Set<Integer> srcs = new HashSet<Integer>();
	srcs.add(umlsGroup.getStructureId());
	MatchOperator mop = new MatchOperator (RegisteredMatcher.TRIGRAM_MATCHER, AggregationFunction.MAX,
					preRanAtts, preRanAtts, threshold);
	ExecutionTree tree = new ExecutionTree();
	tree.addOperator(mop);
	try {
		log.debug(umlsGroup.getObjIds().size());
		set = gi.getMatchManager().matchEncoded(umlsGroup, umlsGroup, tree, null);
		log.debug("matched group");
	} catch (MatchingExecutionException e1) {
		// TODO Auto-generated catch block
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
/*
	Iterator <EntityAnnotation> iter = set.getAnnotations().iterator();
	while (iter.hasNext()){
		EntityAnnotation c = iter.next();
		if (c.getSrcId()==c.getTargetId()){
			iter.remove();
		}
	}*/
	
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
	for (Entry<Integer,Node> e: nodeMap.entrySet()){
		Set<Integer> list = umlsGroups.get(e.getValue().minId);
		if (list==null){
			list = new HashSet<Integer>();
			umlsGroups.put(e.getValue().minId,list);
		}
		list.add(e.getKey());
	}
	for (Integer nig : notInGroup){
		Set<Integer> list = new HashSet<>();
		umlsGroups.put(nig, list);
		list.add(nig);
	}
	return umlsGroups;
}
	
public static Map<Integer, Set<Integer>> groupSimilarUMLSConsByCommonToken(EncodedEntityStructure umlsGroup,
		int srcId, AnnotationMapping am) {
	Map<Integer,List<Integer>> umlsGroups = new HashMap<>();
	Map<Integer,Set<Integer>> conceptToEvidenceSet = new HashMap<Integer,Set<Integer>>();
	for (int targetId : umlsGroup.getObjIds().keySet()){
		long anid = CantorDecoder.code(srcId, targetId);
		Set<Integer> evidence = am.getEvidenceMap().get(anid);
		conceptToEvidenceSet.put(targetId, evidence);
	}

	Map<Integer, Set<Integer>> conceptTosupersets = getOverlappedConcepts(conceptToEvidenceSet);
	return conceptTosupersets;
}



	public static Map<Integer, Set<Integer>> getOverlappedConcepts (Map<Integer,Set<Integer>> conceptEvidenceMap){
		Graph<Integer,Integer> graphOverlap = new UndirectedSparseGraph<>();
		int edgeId = 0;
		for (Entry<Integer,Set<Integer>> entry1 : conceptEvidenceMap.entrySet()) {
			for (Entry<Integer, Set<Integer>> entry2 : conceptEvidenceMap.entrySet()) {
				if (entry1.getKey()!= entry2.getKey()){
					if (!graphOverlap.containsVertex(entry1.getKey())){
						graphOverlap.addVertex(entry1.getKey());
					}
					if (!graphOverlap.containsVertex(entry2.getKey())){
						graphOverlap.addVertex(entry2.getKey());
					}
					if (entry1.getValue() != null && entry2.getValue()!=null) {
						Set<Integer> copy = new HashSet<>(entry1.getValue());
						copy.retainAll(entry2.getValue());
						if (copy.size() != 0) {
							graphOverlap.addEdge(edgeId++, entry1.getKey(), entry2.getKey());
						}
					}
				}
			}
		}
		Map<Integer,Set<Integer>> groupTerm = new HashMap<>();
		WeakComponentClusterer<Integer,Integer> componentClusterer = new WeakComponentClusterer<>();
		Set<Set<Integer>>ccs= componentClusterer.transform(graphOverlap);
		int ccId = 0;
		for (Set<Integer> cc: ccs){
			groupTerm.put(ccId++, cc);
		}
		return groupTerm;
	}

public static Map<Integer,Set<Integer>> getSupersetConceptRelationships (Map<Integer,Set<Integer>> conceptEvidenceMap){
	Map <Integer,Set<Integer>> supersetConceptMap = new HashMap<Integer,Set<Integer>>();
	for (Entry<Integer,Set<Integer>> entry1 : conceptEvidenceMap.entrySet()){
		for (Entry<Integer,Set<Integer>> entry2 : conceptEvidenceMap.entrySet()){
			if (entry1.getKey()!= entry2.getKey()&& entry1.getValue().size()!=entry2.getValue().size()){
				Set<Integer> copy = new HashSet<>(entry1.getValue());
				copy.retainAll(entry2.getValue());
				if (copy.size()!=0){
					boolean isSubset = (copy.size() == entry1.getValue().size() || copy.size() == entry2.getValue().size());
					if (isSubset){
						int subsetConceptId ;
						int superSetConceptId; 
						
						if (copy.size() == entry1.getValue().size()){
							//log.info("concept: "+entry1.getKey()+"set: "+ entry1.getValue()+"----"+entry2.getValue()+"concept: "+entry2.getKey());
							subsetConceptId = entry1.getKey();
							superSetConceptId = entry2.getKey();
						}else{
							subsetConceptId = entry2.getKey();
							superSetConceptId = entry1.getKey();
						}
						
						Set<Integer> supersets = supersetConceptMap.get(subsetConceptId);
					
						if (supersets ==null){
							supersets = new HashSet<Integer>();
							supersetConceptMap.put(subsetConceptId, supersets);
						}	
						supersets.add(superSetConceptId);
					}
				}
			}
		}
	}
	return supersetConceptMap;
}
	
	public static Map<Integer,List<EntityAnnotation>> groupByItem(AnnotationMapping corSet){
		HashMap<Integer,List<EntityAnnotation>> groups = new HashMap<>();
		for (EntityAnnotation cor:corSet.getAnnotations()){
			List<EntityAnnotation> umlsCon = groups.get(cor.getSrcId());
			if (umlsCon==null){
				umlsCon = new ArrayList<>();
				groups.put(cor.getSrcId(), umlsCon);
			}
			umlsCon.add(cor);
		}
		return groups;
	}
	
	
	
	public static HashMap<Integer, List<Integer>> groupSimilarUMLSCons(EncodedEntityStructure umlsGroup,Set<GenericProperty> preRanAtts,
																																		 int size,
																																		 FormRepository gi) {
		
		
		EncodedAnnotationMapping set =new EncodedAnnotationMapping();
		Set<Integer> srcs = new HashSet<Integer>();
		srcs.add(umlsGroup.getStructureId());
		long time = System.currentTimeMillis();
		MatchOperator mop = new MatchOperator (RegisteredMatcher.TRIGRAM_MATCHER, AggregationFunction.MAX, preRanAtts, preRanAtts, 0.35f);
//		mop.setGlobalObjects(externalObjects);
		ExecutionTree tree = new ExecutionTree();
		tree.addOperator(mop);
		try {
			set = gi.getMatchManager().matchEncoded(umlsGroup, umlsGroup, tree, null);
			log.debug("matched group");
		} catch (MatchingExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HashMap <Integer,Node> nodeMap= new HashMap<Integer,Node>();
		boolean isChange =true;
		int counter =0;
		/*
		HashSet<EntityAnnotation> toRemoveCorrs = new HashSet<EntityAnnotation>();
		for (EntityAnnotation c: set.getAnnotations()){
			if (c.getSrcId()==c.getTargetId()){
				toRemoveCorrs.add(c);
			}
		}
		for(EntityAnnotation c:toRemoveCorrs){
			set.removeAnnotation(c.getSrcId(), c.getTargetId());
		}*/

		Iterator <EntityAnnotation> iter = set.getAnnotations().iterator();
		while (iter.hasNext()){
			EntityAnnotation c = iter.next();
			if (c.getSrcId()==c.getTargetId()){
				iter.remove();
			}
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
				for (int id  : umlsGroup.getObjIds().keySet()){
					Node n = new Node();
					n.ownId= id;
					n.minId =n.ownId;
					nodeMap.put(id, n);
				}
				isChange =false;
			}
		}
		
		HashMap<Integer,List<Integer>> umlsGroups = new HashMap<Integer,List<Integer>>();
		HashSet<Integer> notInGroup =new HashSet<Integer>(); 
		for (int id : umlsGroup.getObjIds().keySet()){
			notInGroup.add(id);
		}
		notInGroup.removeAll(nodeMap.keySet());
		for (Entry<Integer,Node> e: nodeMap.entrySet()){
			List<Integer> list = umlsGroups.get(e.getValue().minId);
			if (list==null){
				list = new ArrayList<Integer>();
				umlsGroups.put(e.getValue().minId,list);
			}
			list.add(e.getKey());
		}
		for (Integer nig : notInGroup){
			List<Integer> list = new ArrayList<Integer>();
			umlsGroups.put(nig, list);
			list.add(nig);
		}
		
		return umlsGroups;
	}
}
 class Node {
	int ownId;
	int minId;
}

class Edge {
	int srcId ;
	int targetId;
}
