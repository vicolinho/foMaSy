package de.uni_leipzig.dbs.formRepository.matching.preprocessing;

import java.util.List;
import java.util.Map;

import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.exception.PreprocessingException;

public interface Preprocessor {

	EntityStructureVersion preprocess(EntityStructureVersion esv, List<PreprocessProperty> propList,
																		Map<String, Object> externalSources) throws PreprocessingException;
	EntitySet<GenericEntity> preprocess(EntitySet<GenericEntity> esv, List<PreprocessProperty> propList,
																			Map<String, Object> externalSources);
}
