package de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction;

import java.util.List;
import java.util.Map;

import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.PropertyValue;
import de.uni_leipzig.dbs.formRepository.dataModel.StringPropertyValueSet;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.Preprocessor;

public class AbbreviationFilterPreprocessor implements Preprocessor {


	public EntityStructureVersion preprocess(EntityStructureVersion esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
		for (GenericEntity ge: esv.getEntities()){
			for (PreprocessProperty pp: propList){
				
				StringPropertyValueSet values = ge.getPropertyValueSet(pp.getName(), pp.getLang(),pp.getScope());
				for (PropertyValue pv: values.getCollection()){
					String value = pv.getValue().replaceAll("\\s{1,3}[A-Z][A-Z]{1,3}\\s{1,3}", " ");
					pv.setValue(value);
				}
				ge.changePropertyValues(values);
			}
		}
		return esv;
	}


	public EntitySet<GenericEntity> preprocess(EntitySet<GenericEntity> esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
		for (GenericEntity ge: esv){
			for (PreprocessProperty pp: propList){
				
				StringPropertyValueSet values = ge.getPropertyValueSet(pp.getName(), pp.getLang(),pp.getScope());
				for (PropertyValue pv: values.getCollection()){
					String value = pv.getValue().replaceAll("\\s{1,3}[A-Z][A-Z]{1,3}\\s{1,3}", " ");
					pv.setValue(value);
				}
				ge.changePropertyValues(values);
			}
		}
		return esv;
	}

}
