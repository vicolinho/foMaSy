package de.uni_leipzig.dbs.formRepository.manager;

import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;
import de.uni_leipzig.dbs.formRepository.exception.GraphAPIException;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Edge;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;
import edu.uci.ics.jung.graph.DirectedGraph;

public interface GraphManager {

	
	public DirectedGraph <Node,Edge> getSubgraphFromExternalStructure (EntitySet <GenericEntity> nodes,
			VersionMetadata externalStructure,
			GenericProperty joinNodeAttribute,
			Set<GenericProperty> joinExternalAttribute, int depth) throws EntityAPIException, GraphAPIException;
	
	public DirectedGraph <Node,Edge> getGraphFromStructure (EntitySet<GenericEntity> rootNodes,VersionMetadata structure,
			int depth)throws GraphAPIException;
}
