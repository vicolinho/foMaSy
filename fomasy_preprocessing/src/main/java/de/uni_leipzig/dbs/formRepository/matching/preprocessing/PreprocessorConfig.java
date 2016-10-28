package de.uni_leipzig.dbs.formRepository.matching.preprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreprocessorConfig {

	Map<PreprocessingSteps,List<PreprocessProperty>> preMap;
	
	Map<String,Object> externalSourceMap;
	List <PreprocessingSteps> order ;
	
	public PreprocessorConfig(){
		this.preMap = new HashMap<PreprocessingSteps,List<PreprocessProperty>>();
		order = new ArrayList<PreprocessingSteps>();
	}
	
	public void addPreprocessingStepForProperties(PreprocessingSteps step, PreprocessProperty...props){
		List<PreprocessProperty> stepList = this.preMap.get(step);
		if (stepList==null){
			stepList = new ArrayList<PreprocessProperty>();
			this.order.add(step);
			this.preMap.put(step, stepList);
		}
		for (PreprocessProperty prop:props){
			stepList.add(prop);
		}	
	}
	
	public Map<PreprocessingSteps,List<PreprocessProperty>> getPreprocessMap (){
		return preMap;
	}

	public Map<String, Object> getExternalSourceMap() {
		return externalSourceMap;
	}

	public void setExternalSourceMap(Map<String, Object> externalSourceMap) {
		this.externalSourceMap = externalSourceMap;
	}

	public List <PreprocessingSteps> getOrder() {
		// TODO Auto-generated method stub
		return this.order;
	}
	
}
