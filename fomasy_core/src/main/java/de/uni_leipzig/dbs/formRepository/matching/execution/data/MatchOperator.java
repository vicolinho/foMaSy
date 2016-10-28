package de.uni_leipzig.dbs.formRepository.matching.execution.data;

import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;

public class MatchOperator extends Operator {

	private int id;
	
	String operatorName;
	
	/**
	 * minimal similarity for a correspondence
	 */
	private float threshold ;
	/**
	 * aggregation function to summarize the similarity values of the resulting similarities
	 */
	private AggregationFunction aggFunction ;
	
	
	/**
	 * name of the matcher which is executed by the execution tree.
	 * The implemented class have to be registered by the {@linkplain de.uni_leipzig.dbs.formRepository.matching.execution.MatcherFactory}
	 */
	private String machterName;

	
	/**
	 * to comparing source Properties
	 */
	private Set<GenericProperty> srcProps;

	/**
	 * to comparing target properties;
	 */
	private Set<GenericProperty> targetProps;
	
	
	private Map<String, Object> globalObjects;


	/**
	 * 
	 * @param name of the Matcher
	 * @param af aggregationFunction that determines the kind of aggregation for multi value similarities
	 * @param toComparePropsSrc set of properties of the source entities that have to be compared
	 * @param toComparePropsTgt set of properties of the target entities that have to be compared
	 * @param threshold
	 */
	public MatchOperator (String name, AggregationFunction af,Set<GenericProperty> toComparePropsSrc,
			Set<GenericProperty> toComparePropsTgt, float threshold){
		this.threshold = threshold;
		this.machterName = name;
		this.aggFunction = af;
		this.setSrcProps(toComparePropsSrc);
		this.setTargetProps(toComparePropsTgt);
	}
	
	
	public String getMachterName() {
		return machterName;
	}


	public void setMachterName(String machterName) {
		this.machterName = machterName;
	}


	public AggregationFunction getAggFunction() {
		return aggFunction;
	}


	public void setAggFunction(AggregationFunction aggFunction) {
		this.aggFunction = aggFunction;
	}


	public float getThreshold() {
		return threshold;
	}


	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}


	public Set<GenericProperty> getSrcProps() {
		return srcProps;
	}


	public void setSrcProps(Set<GenericProperty> srcProps) {
		this.srcProps = srcProps;
	}


	public Set<GenericProperty> getTargetProps() {
		return targetProps;
	}


	public void setTargetProps(Set<GenericProperty> targetProps) {
		this.targetProps = targetProps;
	}


	public Map<String, Object> getGlobalObjects() {
		return globalObjects;
	}


	public void setGlobalObjects(Map<String, Object> globalObjects) {
		this.globalObjects = globalObjects;
	}
	
}
