package de.uni_leipzig.dbs.formRepository.dataModel;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.util.CantorDecoder;

/**
 * encoded Annotation Mapping that holds {EntityAnnotation} objects which only store the ids of the entities 
 * @author christen
 *
 */
public class EncodedAnnotationMapping {

private Map <Long, EntityAnnotation> annotations;
	
	private HashMap<Integer,Set<Long>> srcCorrespondenceMap;
	
	private  HashMap<Integer,Set<Long>> targetCorrespondenceMap;
	
	private Long2ObjectMap<Set<Integer>> evidenceMap;
	
	
	public Long2ObjectMap<Set<Integer>> getEvidenceMap() {
		return evidenceMap;
	}


	public void setEvidenceMap(Long2ObjectMap<Set<Integer>> evidenceMap) {
		this.evidenceMap = evidenceMap;
	}


	public EncodedAnnotationMapping (){
		this.annotations = new HashMap<Long,EntityAnnotation>();
		srcCorrespondenceMap = new HashMap<Integer,Set<Long>>();
		targetCorrespondenceMap = new HashMap<Integer,Set<Long>>();
		evidenceMap = new Long2ObjectOpenHashMap<Set<Integer>>();
	}
	
	
	public void addAnnotation (EntityAnnotation a){
		this.annotations.put(a.getId(), a);
		Set<Long> srcAnnos = this.srcCorrespondenceMap.get(a.getSrcId());
		if (srcAnnos==null){
			srcAnnos = new HashSet<Long>();
			this.srcCorrespondenceMap.put(a.getSrcId(), srcAnnos);
		}
		Set<Long> targetAnnos = this.targetCorrespondenceMap.get(a.getTargetId());
		if (targetAnnos==null){
			targetAnnos = new HashSet<Long>();
			this.targetCorrespondenceMap.put(a.getTargetId(), targetAnnos);
		}
		srcAnnos.add(a.getId());
		targetAnnos.add(a.getId());
	}
	
	public void removeAnnotation (int srcId,int targetId){
		long code  = CantorDecoder.code(srcId, targetId);
		this.annotations.remove(code);
		Set<Long> srcAnnos = this.srcCorrespondenceMap.get(srcId);
		if (srcAnnos!=null){
			srcAnnos.remove(code);
		}
		Set<Long> targetAnnos = this.targetCorrespondenceMap.get(targetId);
		if (targetAnnos!=null){
			targetAnnos.remove(code);
		}
		
		
	}


	public EntityAnnotation getAnnotation (long id){
		return this.annotations.get(id);
	}
	
	public Collection<EntityAnnotation> getAnnotations(){
		return this.annotations.values();
	}
	
	public EntityAnnotation getAnnotation (int srcId, int targetId){
		long c = CantorDecoder.code(srcId, targetId);
		return this.annotations.get(c);
	}

	public Set<Integer> getCorrespondingTargetIds (int srcId){
		Set <Integer> set = new HashSet<Integer>();
		Set<Long> corrIds = this.srcCorrespondenceMap.get(srcId);
		if (corrIds!=null){
			for (Long id : corrIds){
				set.add(this.annotations.get(id).getTargetId());
			}
		}
		return set;
	}
	
	public boolean containsCorrespondingTargetIds (int srcId){
		return this.srcCorrespondenceMap.containsKey(srcId);
	}
	
	public Set<Integer> getCorrespondingSrcIds (int targetId){
		Set <Integer> set = new HashSet<Integer>();
		Set<Long> corrIds = this.targetCorrespondenceMap.get(targetId);
		if (corrIds!=null){
			for (Long id : corrIds){
				set.add(this.annotations.get(id).getSrcId());
			}
		}
		return set;
	}
	
	public boolean containsCorrespondingSrcIds (int targetId){
		return this.targetCorrespondenceMap.containsKey(targetId);
	}
	
	public boolean contains(EntityAnnotation am){
		return this.annotations.containsKey(am.getId());
	}
	
	
	
	public int getNumberOfAnnotations(){
		return this.annotations.size();
	}
}
