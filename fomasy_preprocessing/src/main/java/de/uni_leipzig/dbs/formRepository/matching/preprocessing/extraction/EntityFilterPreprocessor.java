package de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.Preprocessor;

public class EntityFilterPreprocessor implements Preprocessor{

	/**
	 * identifier for the {@code Set} of values in the {@code externalSources} Map, which determine if an entity is removed from the entity structure
	 */
	public static final String DISCRIMINATOR_VALUE_SET = "discriminatorValueSet";
	
	public EntityFilterPreprocessor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public EntityStructureVersion preprocess(EntityStructureVersion esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
		@SuppressWarnings("unchecked")
		Set <String> discrimValues = (Set<String>) externalSources.get(DISCRIMINATOR_VALUE_SET);
		Set<Integer> removeEntities = new HashSet<Integer>();
		for (GenericEntity ge: esv.getEntities()){
			for (PreprocessProperty pp: propList){
				List<String> props = ge.getPropertyValues(pp.getName(), pp.getLang(),
						pp.getScope());
				for (String p: props){
					if (discrimValues.contains(p)){
						removeEntities.add(ge.getId());
					}
				}
			}	
		}
		for (Integer geId: removeEntities){
			esv.removeEntity(geId);
		}
		return esv;
	}

	@Override
	public EntitySet<GenericEntity> preprocess(EntitySet<GenericEntity> esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
		@SuppressWarnings("unchecked")
		Set <String> discrimValues = (Set<String>) externalSources.get(DISCRIMINATOR_VALUE_SET);
		Set<Integer> removeEntities = new HashSet<Integer>();
		for (GenericEntity ge: esv){
			for (PreprocessProperty pp: propList){
				List<String> props = ge.getPropertyValues(pp.getName(), pp.getLang(),
						pp.getScope());
				for (String p: props){
					if (discrimValues.contains(p)){
						removeEntities.add(ge.getId());
					}
				}
			}	
		}
		for (Integer geId: removeEntities){
			esv.removeEntity(geId);
		}
		return esv;
	}

}
