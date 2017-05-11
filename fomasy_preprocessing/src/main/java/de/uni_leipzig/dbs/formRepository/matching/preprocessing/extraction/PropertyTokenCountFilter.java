package de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction;

import java.util.*;

import de.uni_leipzig.dbs.formRepository.dataModel.*;
import org.apache.log4j.Logger;

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
				Set<GenericProperty> gps = ge.getGenericProperties(pp.getName(),pp.getLang(),pp.getScope());
				int totalValues  =0;
				for (GenericProperty gp :gps) {
					List<PropertyValue> pvs = ge.getValues(gp);
					for (PropertyValue pv : pvs) {
						totalValues++;
						String[] tokens = pv.getValue().split("[^A-Za-z0-9]");
						avgCount += tokens.length;
						totalValues++;
					}
				}
				avgCount=avgCount/(float)totalValues;
				for (GenericProperty gp :gps) {
					List<PropertyValue> pvs = ge.getValues(gp);
					Iterator<PropertyValue> pvIter = pvs.iterator();
					while (pvIter.hasNext()) {
						PropertyValue pv = pvIter.next();
						String[] tokens = pv.getValue().split("[^A-Za-z0-9]");
						if (tokens.length<avgCount*0.6){
							log.debug(avgCount);
							pvIter.remove();
						}
					}
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
				Set<GenericProperty> gps = ge.getGenericProperties(pp.getName(),pp.getLang(),pp.getScope());
				int totalValues  =0;
				for (GenericProperty gp :gps) {
					List<PropertyValue> pvs = ge.getValues(gp);
					for (PropertyValue pv : pvs) {
						totalValues++;
						String[] tokens = pv.getValue().split("[^A-Za-z0-9]");
						avgCount += tokens.length;
						totalValues++;
					}
				}
				avgCount=avgCount/(float)totalValues;
				for (GenericProperty gp :gps) {
					List<PropertyValue> pvs = ge.getValues(gp);
					Iterator<PropertyValue> pvIter = pvs.iterator();
					while (pvIter.hasNext()) {
						PropertyValue pv = pvIter.next();
						String[] tokens = pv.getValue().split("[^A-Za-z0-9]");
						if (tokens.length<avgCount*0.6){
							log.debug(avgCount);
							pvIter.remove();
						}
					}
				}
			}
		}
		return esv;
	}

}
