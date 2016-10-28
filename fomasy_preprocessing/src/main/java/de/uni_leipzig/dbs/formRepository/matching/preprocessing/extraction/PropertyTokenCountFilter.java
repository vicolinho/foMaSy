package de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.PropertyValue;
import de.uni_leipzig.dbs.formRepository.dataModel.StringPropertyValueSet;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.Preprocessor;

public class PropertyTokenCountFilter implements Preprocessor{

	Logger log = Logger.getLogger(getClass());
	public PropertyTokenCountFilter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public EntityStructureVersion preprocess(EntityStructureVersion esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
		for (GenericEntity ge: esv.getEntities()){
			
			for (PreprocessProperty pp : propList){
				float avgCount = 0;
				List<String> propValues = ge.getPropertyValues(pp.getName(),pp.getLang(),pp.getScope());
				for (String pv: propValues){
					String[] tokens = pv.split("[^A-Za-z0-9]");
					avgCount+=tokens.length;
				}
				avgCount=avgCount/(float)propValues.size();
				StringPropertyValueSet pvSet = ge.getPropertyValueSet(pp.getName(), pp.getLang(), pp.getScope());
				Set<PropertyValue> remValues = new HashSet<PropertyValue>();
				for (PropertyValue pv: pvSet.getCollection()){
					String[] tokens = pv.getValue().split("[^A-Za-z0-9]");
					
					if (tokens.length<avgCount*0.6){
						log.debug(avgCount);
						remValues.add(pv);
					}
				}
				for (PropertyValue remPv: remValues){
					ge.removePropertyValue(pp.getName(), pp.getLang(), pp.getScope(), remPv);
					log.debug("remove property value : "+remPv.getValue()+" for "+ge.getAccession());
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
			
			for (PreprocessProperty pp : propList){
				float avgCount = 0;
				List<String> propValues = ge.getPropertyValues(pp.getName(),pp.getLang(),pp.getScope());
				for (String pv: propValues){
					String[] tokens = pv.split("[^A-Za-z0-9]");
					avgCount+=tokens.length;
				}
				avgCount=avgCount/(float)propValues.size();
				StringPropertyValueSet pvSet = ge.getPropertyValueSet(pp.getName(), pp.getLang(), pp.getScope());
				Set<PropertyValue> remValues = new HashSet<PropertyValue>();
				for (PropertyValue pv: pvSet.getCollection()){
					String[] tokens = pv.getValue().split("[^A-Za-z0-9]");
					
					if (tokens.length<avgCount*0.6){
						log.debug(avgCount);
						remValues.add(pv);
					}
				}
				for (PropertyValue remPv: remValues){
					ge.removePropertyValue(pp.getName(), pp.getLang(), pp.getScope(), remPv);
					log.debug("remove property value : "+remPv.getValue()+" for "+ge.getAccession());
				}
			}
			
		}
		return esv;
	}

}
