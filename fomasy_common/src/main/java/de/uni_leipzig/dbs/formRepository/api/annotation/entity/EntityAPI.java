package de.uni_leipzig.dbs.formRepository.api.annotation.entity;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;

/**
 * defines the access for entities 
 * This interface defines an independent access to entities of different sources
 * @author christen
 *
 */
public interface EntityAPI {

	/**
	 * get GenericEntities WITHOUT properties by their ids 
	 * @param ids
	 * @return
	 * @throws EntityAPIException
	 */
	public EntitySet<GenericEntity> getEntitiesById(Set<Integer> ids) throws EntityAPIException;
	
	
	public EntitySet<GenericEntity> getEntityWithPropertiesByAccession(Set<String> accs) throws EntityAPIException;
	/**
	 * get GenericEntities WITH properties by their ids 
	 * @param ids
	 * @return
	 * @throws EntityAPIException
	 */
	public EntitySet<GenericEntity> getEntityWithProperties(Set<Integer> ids) throws EntityAPIException;
	

	EntitySet<GenericEntity> getEntityWithPropertiesByProperty(
			Collection<String> values, VersionMetadata version,
			Set<GenericProperty> filterProperties) throws EntityAPIException;
}
