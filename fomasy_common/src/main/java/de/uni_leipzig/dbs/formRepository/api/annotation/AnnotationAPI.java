package de.uni_leipzig.dbs.formRepository.api.annotation;

import java.util.List;

import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportAnnotationMapping;

public interface AnnotationAPI {

	/**
	 * retrieve the annotation mapping for a calculated mapping map 
	 * @param mapping encoded long value that encodes the source and target id as key and the similarity as value
	 * @param sourceStructureId 
	 * @param targetStructureId
	 * @return
	 */
	public AnnotationMapping getAnnotationMapping (Long2FloatMap mapping, int sourceStructureId, int targetStructureId);

	public List<VersionMetadata> checkInvolvedVersion(VersionMetadata srcStruct,
			VersionMetadata targetStruct);

	/**
	 * import an annotation mapping for the specified sources in the list
	 * @param guranteedInvolvedStructures associated sources
	 * @param iam
	 */
	public void importMapping(List<VersionMetadata> guranteedInvolvedStructures, AnnotationMapping iam);

	public void importMapping(
			List<VersionMetadata> guranteedInvolvedStructures,
			ImportAnnotationMapping iam);
	
	
	public AnnotationMapping getAnnotationMapping(VersionMetadata src, VersionMetadata target, String name);
}
