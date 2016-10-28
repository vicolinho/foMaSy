package de.uni_leipzig.dbs.formRepository.manager;

import java.util.Properties;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EncodedAnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.ExecutionTree;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;

public interface MatchManager {

	
	public AnnotationMapping match(EntityStructureVersion srcVersion, EncodedEntityStructure encodedSrc,
			EncodedEntityStructure encodedtarget, EntityStructureVersion targetVersion, ExecutionTree tree,
			Pruning pruning ) throws MatchingExecutionException;
	
	public AnnotationMapping match(EntityStructureVersion srcVersion, EncodedEntityStructure encodedSrc,
			EncodedEntityStructure encodedtarget, EntityStructureVersion targetVersion
			 ) throws MatchingExecutionException;
	
	
	public AnnotationMapping match(EncodedEntityStructure encodedSrc,
			EncodedEntityStructure encodedtarget,  ExecutionTree tree,
			Pruning pruning) throws MatchingExecutionException;
	
	public AnnotationMapping matchByMetaMap (EntityStructureVersion src, Set<String> entityTypes, Set<GenericProperty> props,
			Properties metaMapProperties) throws EntityAPIException;
	
	public AnnotationMapping match (Set<EntityStructureVersion> srcVersions, EntityStructureVersion ontology,
			ExecutionTree tree, Pruning pruning);

	EncodedAnnotationMapping matchEncoded(EncodedEntityStructure encodedSrc,
			EncodedEntityStructure encodedtarget, ExecutionTree tree,
			Pruning pruning) throws MatchingExecutionException;
	
	
	
	
	
}
