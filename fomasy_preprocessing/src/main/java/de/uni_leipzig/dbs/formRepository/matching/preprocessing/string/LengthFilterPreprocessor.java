package de.uni_leipzig.dbs.formRepository.matching.preprocessing.string;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.*;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.Preprocessor;

public class LengthFilterPreprocessor implements Preprocessor{

	
	public static final String LENGTH_MAX = "lengthMax";
	@Override
	public EntityStructureVersion preprocess(EntityStructureVersion esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
		int length ;
		if (externalSources!=null){
			length = (externalSources.get(LENGTH_MAX)!=null)?(Integer) externalSources.get(LENGTH_MAX):2;
		}else 
			length =1;
		for (GenericEntity ge: esv.getEntities()){
			for (PreprocessProperty pp: propList){
				Set<GenericProperty> gps = ge.getGenericProperties(pp.getName(), pp.getLang(),pp.getScope());
				for (GenericProperty gp : gps) {
					List<PropertyValue> values = ge.getValues(gp);
					for (PropertyValue pv : values) {
						String value = pv.getValue().replaceAll("(?<=\\s{1,3})[A-Za-z]{1," + length + "}(?=\\s{1,3})", " ");
						value = value.replaceAll("\\s{2,}", " ");
						pv.setValue(value);
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
		int length ;
		if (externalSources!=null){
			length = (externalSources.get(LENGTH_MAX)!=null)?(Integer) externalSources.get(LENGTH_MAX):2;
		}else 
			length =2;
		for (GenericEntity ge: esv){
			for (PreprocessProperty pp: propList){
				Set<GenericProperty> gps = ge.getGenericProperties(pp.getName(), pp.getLang(),pp.getScope());
				for (GenericProperty gp : gps) {
					List<PropertyValue> values = ge.getValues(gp);
					for (PropertyValue pv : values) {
						String value = pv.getValue().replaceAll("(?<=\\s{1,3})[A-Za-z]{1," + length + "}(?=\\s{1,3})", " ");
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
