package de.uni_leipzig.dbs.formRepository.matching.preprocessing;

import java.util.List;
import java.util.Map.Entry;

import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;

public class PreprocessorExecutor {

	
	public EntityStructureVersion preprocess (EntityStructureVersion esv , PreprocessorConfig config){
		PreprocessorFactory factory = new PreprocessorFactory();
		for (PreprocessingSteps step: config.getOrder()){
			List<PreprocessProperty> list = config.getPreprocessMap().get(step);
			Preprocessor processor = factory.getPreprocessor(step);
			esv = processor.preprocess(esv, list, config.getExternalSourceMap());
		}
		return esv;	
	}
	
	public EntitySet<GenericEntity> preprocess (EntitySet<GenericEntity> esv , PreprocessorConfig config){
		PreprocessorFactory factory = new PreprocessorFactory();
		for (PreprocessingSteps step: config.getOrder()){
			List<PreprocessProperty> list = config.getPreprocessMap().get(step);
			Preprocessor processor = factory.getPreprocessor(step);
			esv = processor.preprocess(esv, list, config.getExternalSourceMap());
		}
		return esv;	
	}
}
