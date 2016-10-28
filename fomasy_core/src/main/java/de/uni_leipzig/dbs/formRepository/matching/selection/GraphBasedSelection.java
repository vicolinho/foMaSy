package de.uni_leipzig.dbs.formRepository.matching.selection;

import java.util.Set;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Edge;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;
import edu.uci.ics.jung.graph.DirectedGraph;

public interface GraphBasedSelection {

	
	public AnnotationMapping selectAnnotationMapping(DirectedGraph<Node,Edge>graph,AnnotationMapping am, EncodedEntityStructure src,
			EncodedEntityStructure target, Set<GenericProperty> preDomAtts,
			Set<GenericProperty> preRanAtts, float threshold, float delta,float estimatedSize,
			FormRepository rep);
		
}
