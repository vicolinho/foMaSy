package de.uni_leipzig.dbs.formRepository.matching.preprocessing.string;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import de.uni_leipzig.dbs.formRepository.dataModel.*;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.Preprocessor;

public class NormalizePreprocessor implements Preprocessor {

	Pattern ALPHA_NUMERICAL_PATTERN =Pattern.compile("[^A-Za-z0-9\\s+]");	
	public static final String REMOVE_PATTERN = "tokenPattern";
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
						String value = pv.getValue().replaceAll(ALPHA_NUMERICAL_PATTERN.pattern(), " ");
						value = value.replaceAll("\\s{2,}", " ");
						pv.setValue(value);
					}
					ge.changePropertyValues(gp,values);
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
						String value = pv.getValue().replaceAll(ALPHA_NUMERICAL_PATTERN.pattern(), " ");
						value = value.replaceAll("\\s{2,}", " ");
						pv.setValue(value);
					}
					ge.changePropertyValues(gp, values);
				}
			}
		}
		
		return esv;
	}

}
