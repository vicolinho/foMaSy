package de.uni_leipzig.dbs.formRepository.matching.preprocessing;

import java.util.List;
import java.util.Map;

import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;

public interface Preprocessor {

	public EntityStructureVersion preprocess(EntityStructureVersion esv, List<PreprocessProperty> propList, Map<String,Object> externalSources);
	public EntitySet<GenericEntity> preprocess(EntitySet<GenericEntity> esv, List<PreprocessProperty> propList, Map<String,Object> externalSources);
}
