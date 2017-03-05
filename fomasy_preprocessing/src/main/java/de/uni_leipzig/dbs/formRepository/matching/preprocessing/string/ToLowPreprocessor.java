package de.uni_leipzig.dbs.formRepository.matching.preprocessing.string;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.*;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.Preprocessor;

public class ToLowPreprocessor implements Preprocessor {

	@Override
	public EntityStructureVersion preprocess(EntityStructureVersion esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
				
		for (GenericEntity ge: esv.getEntities()){
			for (PreprocessProperty pp: propList){
				Set<GenericProperty> gps = ge.getGenericProperties(pp.getName(), pp.getLang(),pp.getScope());
				for (GenericProperty gp : gps) {
					List<PropertyValue> values = ge.getValues(gp);
					for (PropertyValue pv : values) {
						pv.setValue(pv.getValue().toLowerCase());
					}
					ge.changePropertyValues(gp, values);
				}
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
				Set<GenericProperty> gps = ge.getGenericProperties(pp.getName(), pp.getLang(),pp.getScope());
				for (GenericProperty gp : gps) {
					List<PropertyValue> values = ge.getValues(gp);
					for (PropertyValue pv : values) {
						pv.setValue(pv.getValue().toLowerCase());
					}
					ge.changePropertyValues(gp, values);
				}
			}
		}
		return esv;
	}

}
