package de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.*;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.Preprocessor;

public class NumberFilter implements Preprocessor{

	public NumberFilter() {
		// TODO Auto-generated constructor stub
	}

	public EntityStructureVersion preprocess(EntityStructureVersion esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
		for (GenericEntity ge: esv.getEntities()){
			for (PreprocessProperty pp: propList){
				Set<GenericProperty> gps = ge.getGenericProperties(pp.getName(), pp.getLang(),pp.getScope());
				//StringPropertyValueSet values = ge.getPropertyValueSet(pp.getName(), pp.getLang(),pp.getScope());
				for (GenericProperty gp :gps) {
					List<PropertyValue> pvs = ge.getValues(gp);
					for (PropertyValue pv : pvs) {
						String value = pv.getValue().replaceAll("\\s[0-9]{1,3}(\\.[0-9]{1,3})?", "");
						value = value.replaceAll("\\s+", " ").trim();
						pv.setValue(value);
					}
					//ge.changePropertyValues(values);
					ge.changePropertyValues(gp, pvs);
				}
			}
		}
		return esv;
	}

	public EntitySet<GenericEntity> preprocess(EntitySet<GenericEntity> esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
		// TODO Auto-generated method stub
		for (GenericEntity ge: esv){
			for (PreprocessProperty pp: propList){
				Set<GenericProperty> gps = ge.getGenericProperties(pp.getName(), pp.getLang(),pp.getScope());
				//StringPropertyValueSet values = ge.getPropertyValueSet(pp.getName(), pp.getLang(),pp.getScope());
				for (GenericProperty gp :gps) {
					List<PropertyValue> pvs = ge.getValues(gp);
					for (PropertyValue pv : pvs) {
						String value = pv.getValue().replaceAll("\\s[0-9]{1,3}(\\.[0-9]{1,3})?", "");
						value = value.replaceAll("\\s+", " ").trim();
						pv.setValue(value);
					}
					//ge.changePropertyValues(values);
					ge.changePropertyValues(gp, pvs);
				}
			}
		}
		return esv;
	}
	
	
}

