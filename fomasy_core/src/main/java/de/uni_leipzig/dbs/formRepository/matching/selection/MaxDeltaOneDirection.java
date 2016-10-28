package de.uni_leipzig.dbs.formRepository.matching.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;

public class MaxDeltaOneDirection implements Selection{

	
	Logger log = Logger.getLogger(getClass());
	public MaxDeltaOneDirection() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public AnnotationMapping select(AnnotationMapping am,float threshold, float delta) {
		AnnotationMapping filter = new AnnotationMapping ();
		filter.setMethod(am.getMethod());filter.setSrcVersion(am.getSrcVersion());filter.setTargetVersion(am.getTargetVersion());
		HashMap<Integer,List<EntityAnnotation>> srcAnnotationList = new HashMap<Integer,List<EntityAnnotation>>();
		for (EntityAnnotation ea :am.getAnnotations()){
			List <EntityAnnotation> list = srcAnnotationList.get(ea.getSrcId());
			if (list ==null){
				list = new ArrayList<EntityAnnotation>();
				srcAnnotationList.put(ea.getSrcId(), list);
			}
			list.add(ea);
		}
		
		for (List<EntityAnnotation> srcList : srcAnnotationList.values()){
			Collections.sort(srcList,Collections.reverseOrder());
			float maxSim = srcList.get(0).getSim();
			for (int i =0; i<srcList.size();i++){
				if (srcList.get(i).getSim()>=(maxSim-delta)){
					filter.addAnnotation(srcList.get(i));
				}else {
					break;
				}
			}
			
		}
		return filter;
	}

	@Override
	public AnnotationMapping select(AnnotationMapping am,
			EncodedEntityStructure src, EncodedEntityStructure target,
			Set<GenericProperty> preDomAtts, Set<GenericProperty> preRanAtts,
			float threshold, float delta, float avgEntitySize,
			FormRepository rep) {
		// TODO Auto-generated method stub
		return null;
	}

}
