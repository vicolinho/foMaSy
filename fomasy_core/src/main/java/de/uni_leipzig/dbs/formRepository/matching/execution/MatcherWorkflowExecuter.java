package de.uni_leipzig.dbs.formRepository.matching.execution;

import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.exception.UnknownMatcherException;
import de.uni_leipzig.dbs.formRepository.matching.Matcher;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.ExecutionTree;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.MatchGroup;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.MatchOperator;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.Operator;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.SetOperator;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;

public class MatcherWorkflowExecuter {

	Logger log = Logger.getLogger(getClass());
	
	public static final int MATCHING1 = 0;
	
	public static final int MATCHING2 = 1;
	
	public static final int MATCH_GROUPING1 =2;
	
	public static final int MATCH_GROUPING2 =3;
	
	public static final int COMBINING =4;
	
	public static final int START = -1;
	private int state;
	
	private Long2FloatMap result; 
	
	private Long2ObjectMap<Set<Integer>> evidenceMap;
	
	

	private List<Long2FloatMap> intermediateResults;
		
	public MatcherWorkflowExecuter(){
		result = new Long2FloatOpenHashMap();
	}
	
	public Long2FloatMap match(EncodedEntityStructure source, EncodedEntityStructure target, ExecutionTree tree, Pruning pruning) throws MatchingExecutionException{
		this.state = START;
		this.result.clear();
		this.evidenceMap = new Long2ObjectOpenHashMap<Set<Integer>>();
		this.intermediateResults = new ArrayList<Long2FloatMap>();
		while(!tree.isProcessQueueEmpty()){
			Operator op = tree.processOperator();
			switch (state){
			case START:
				if (op instanceof MatchGroup){
					state = MATCH_GROUPING1;
				}else if (op instanceof MatchOperator){
					state = MATCHING1;
				}
				break;
			case MATCHING1:
				if (op instanceof MatchOperator){
					state = MATCHING2;
				}else if (op instanceof MatchGroup){
					state = MATCH_GROUPING2;
				}
				break;
			case MATCHING2:	
				if (op instanceof SetOperator){
					state = COMBINING;
				}
				break;
			case COMBINING:
				if (op instanceof SetOperator){
					state = COMBINING;
				}else if (op instanceof MatchOperator){
					state = MATCHING1;
				}
				break;
			case MATCH_GROUPING1:
				if (op instanceof MatchOperator){
					state = MATCHING2;
				}else if (op instanceof MatchGroup){
					state = MATCH_GROUPING2;
				}
				break;
			case MATCH_GROUPING2:
				if (op instanceof SetOperator){
					state = COMBINING;
				}
			}
			this.processState(state, source, target, pruning, op);
		}//queue is not empty
		if (intermediateResults.size()==1){
			result = intermediateResults.get(0);
		}
		log.debug("calculated annotations: "+result.size());
		Set<Long> removeEvidence = new HashSet<Long>();
		for (Long r: this.evidenceMap.keySet()){
			if (!result.containsKey(r)){
				removeEvidence.add(r);
			}
		}
		for (Long id : removeEvidence){
			this.evidenceMap.remove(id);
		}
		return result;
	}
	
	private void processState (int state, EncodedEntityStructure source, EncodedEntityStructure target,
														 Pruning pr, Operator op) throws MatchingExecutionException{
		if (state ==MATCHING1 ||state == MATCHING2){
			try {
				
				Long2FloatMap result = this.matcherExecution((MatchOperator)op, source, target, pr);
				this.intermediateResults.add(result);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new MatchingExecutionException ("class not found",e.getCause());
			} catch (UnknownMatcherException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new MatchingExecutionException ("matcher is unknown ", e.getCause());
			}
		}else if (state == MATCH_GROUPING1 ||state ==MATCH_GROUPING2){
			try {
				result = this.matcherGroupExecution((MatchGroup) op, source, target, pr);
				this.intermediateResults.add(result);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new MatchingExecutionException (e.getCause());
			} catch (UnknownMatcherException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new MatchingExecutionException (e.getCause());
			}
		}else if (state == COMBINING){
			SetOperator sop = (SetOperator) op;
			if (intermediateResults.size()==1){
				this.result = sop.setOperation(this.result,this.intermediateResults.get(0));
			}else if (intermediateResults.size()>1){
				Long2FloatMap ir = sop.setOperation(intermediateResults.toArray(new Long2FloatMap[]{}));
				this.intermediateResults.clear();
				this.intermediateResults.add(ir);
			}
		}
	}
	
	private Long2FloatMap matcherGroupExecution (MatchGroup mg, EncodedEntityStructure source,
			EncodedEntityStructure target, Pruning pruning) throws ClassNotFoundException, UnknownMatcherException, MatchingExecutionException{
		SetOperator setOp = mg.getOperator();
		Long2FloatMap result = new Long2FloatOpenHashMap();
		Long2FloatMap[] results= new Long2FloatMap[mg.getMatchers().size()];
		int id = 0;
		for (MatchOperator mo : mg.getMatchers()){
			Long2FloatMap matcherResult = this.matcherExecution(mo, source, target, pruning);
			
			
			results[id++] = matcherResult;
		}
		if (setOp == null)
			log.error(setOp);
		result = setOp.setOperation(results);
		
		return result;
		
	}
	
	private Long2FloatMap matcherExecution(MatchOperator mop, EncodedEntityStructure source,
			EncodedEntityStructure target, Pruning pruning) throws ClassNotFoundException, UnknownMatcherException, MatchingExecutionException{
		Long2FloatMap result = new Long2FloatOpenHashMap() ;
		Matcher m = MatcherFactory.getInstance().getRegisteredMatcher(mop.getMachterName(),
				source.getPropertyPosition(), target.getPropertyPosition(), mop.getSrcProps(), mop.getTargetProps());
		
		if (mop.getGlobalObjects()!=null){
			m.setGlobalObjects(mop.getGlobalObjects());
		}
		result = m.computeSimilarity(source, target, mop.getAggFunction(), mop.getThreshold(), pruning);
		log.debug(mop.getMachterName()+":"+result.size());
		if (m.getEvidenceMap().size()!=0){
			this.evidenceMap.putAll(m.getEvidenceMap());
		}
		log.debug("computed mapping with: " + mop.getMachterName());
		return result;
	}
	
	public Long2ObjectMap<Set<Integer>> getEvidenceMap() {
		return evidenceMap;
	}

	public void setEvidenceMap(Long2ObjectMap<Set<Integer>> evidenceMap) {
		this.evidenceMap = evidenceMap;
	}
}
