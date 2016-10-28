package de.uni_leipzig.dbs.formRepository.matching.preprocessing.string;

import java.util.List;
import java.util.Map;

import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.PropertyValue;
import de.uni_leipzig.dbs.formRepository.dataModel.StringPropertyValueSet;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.Preprocessor;

public class ToLowPreprocessor implements Preprocessor {

	@Override
	public EntityStructureVersion preprocess(EntityStructureVersion esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
				
		for (GenericEntity ge: esv.getEntities()){
			for (PreprocessProperty pp: propList){
				
				StringPropertyValueSet values = ge.getPropertyValueSet(pp.getName(), pp.getLang(),pp.getScope());
				for (PropertyValue pv: values.getCollection()){
					pv.setValue(pv.getValue().toLowerCase());
				}
				ge.changePropertyValues(values);
			}
		}
		return esv;
	}

	@Override
	public EntitySet<GenericEntity> preprocess(EntitySet<GenericEntity> esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
		// TODO Auto-generated method stub
		for (GenericEntity ge: esv){
			for (PreprocessProperty pp: propList){
				
				StringPropertyValueSet values = ge.getPropertyValueSet(pp.getName(), pp.getLang(),pp.getScope());
				for (PropertyValue pv: values.getCollection()){
					pv.setValue(pv.getValue().toLowerCase());
				}
				ge.changePropertyValues(values);
			}
		}
		return esv;
	}

}
