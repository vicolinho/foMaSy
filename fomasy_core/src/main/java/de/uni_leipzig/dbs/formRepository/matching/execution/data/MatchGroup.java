package de.uni_leipzig.dbs.formRepository.matching.execution.data;

import java.util.ArrayList;
import java.util.List;

public class MatchGroup extends Operator{

	private int id ;
	
	
	public MatchGroup ( SetOperator setOp, MatchOperator...matchers){
		this.matchers = new ArrayList<MatchOperator> ();
		for (MatchOperator mo : matchers){
			this.matchers.add(mo);
		}
		this.setOperator(setOp);
		this.type = GROUP;
	}
	
	/**
	 * the results of the matchers in a matchers group 
	 */
	private List <MatchOperator> matchers;
	
	/**
	 * set operation which combines the results of the matcher group
	 */
	private SetOperator operator;

	
	public MatchGroup(){
		this.matchers = new ArrayList<MatchOperator>();
	}
	
	public SetOperator getOperator() {
		return operator;
	}

	public void setOperator(SetOperator operator) {
		this.operator = operator;
	}

	public List <MatchOperator> getMatchers() {
		return matchers;
	}

	public void setMatchers(List <MatchOperator> matchers) {
		this.matchers = matchers;
	}
	
	public void addMatcher(MatchOperator mop){
		this.matchers.add(mop);
	}
}
