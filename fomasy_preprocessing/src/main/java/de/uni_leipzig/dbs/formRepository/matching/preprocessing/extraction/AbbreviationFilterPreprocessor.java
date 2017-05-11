package de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.*;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.Preprocessor;

public class AbbreviationFilterPreprocessor implements Preprocessor {


	public EntityStructureVersion preprocess(EntityStructureVersion esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
		for (GenericEntity ge: esv.getEntities()){
			for (PreprocessProperty pp: propList){
				Set<GenericProperty> gps = ge.getGenericProperties(pp.getName(),pp.getLang(),pp.getScope());
				for (GenericProperty gp : gps) {
					List<PropertyValue> values = ge.getPropertyValueSet(pp.getName(), pp.getLang(), pp.getScope());
					for (PropertyValue pv : values) {
						String value = pv.getValue().replaceAll("\\s{1,3}[A-Z][A-Z]{1,3}\\s{1,3}", " ");
						pv.setValue(value);
					}
					ge.changePropertyValues(gp,values);
				}
			}
		}
		return esv;

	}


	public EntitySet<GenericEntity> preprocess(EntitySet<GenericEntity> esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
		for (GenericEntity ge: esv){
			for (PreprocessProperty pp: propList){
				Set<GenericProperty> gps = ge.getGenericProperties(pp.getName(),pp.getLang(),pp.getScope());
				for (GenericProperty gp : gps) {
					List<PropertyValue> values = ge.getPropertyValueSet(pp.getName(), pp.getLang(), pp.getScope());
					for (PropertyValue pv : values) {
						String value = pv.getValue().replaceAll("\\s{1,3}[A-Z][A-Z]{1,3}\\s{1,3}", " ");
						pv.setValue(value);
					}
					ge.changePropertyValues(gp,values);
				}
			}
		}
		return esv;
	}

}
