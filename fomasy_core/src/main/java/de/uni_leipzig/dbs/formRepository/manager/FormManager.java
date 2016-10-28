package de.uni_leipzig.dbs.formRepository.manager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;
import de.uni_leipzig.dbs.formRepository.exception.ImportException;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;

public interface FormManager {

	public void importForm (String file) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, ImportException;
	
	public void importForm(Map<String,Object> properties) throws InstantiationException, IllegalAccessException,
	ClassNotFoundException, ImportException;
	
	
	public void importRelationshipsForVersion (Map<String, Object> properties) throws InstantiationException, 
	IllegalAccessException, ClassNotFoundException, ImportException;
	
	/**
	 * 
	 * @param name of the entity structure
	 * @param type e.g. ontology, eligibility criteria form, quality assurance form
	 * @param version release date as string yyyy-mm-dd
	 * @return 
	 * @throws VersionNotExistsException
	 * @throws StructureBuildException
	 */
	public EntityStructureVersion getStructureVersion (String name,String type, String version) throws VersionNotExistsException, StructureBuildException;
	/**
	 * 
	 * @param name of the entity structure
	 * @param type e.g. ontology, eligibility criteria form, quality assurance form
	 * @return the latest structure
	 * @throws VersionNotExistsException
	 * @throws StructureBuildException
	 */
	public EntityStructureVersion getLatestStructureVersion (String name,String type);
	
	/**
	 * 
	 * @param types
	 * @return a set of entityStructreVersions, which are from the specified type
	 * @throws VersionNotExistsException
	 * @throws StructureBuildException
	 */
	public Set<EntityStructureVersion> getStructureVersionsByType (Set<String> types) throws VersionNotExistsException, StructureBuildException;
	
	/**
	 * 
	 * @param ids
	 * @return an set of Entities 
	 * @throws EntityAPIException
	 */
	public EntitySet<GenericEntity> getEntitiesById (Set<Integer> ids) throws EntityAPIException;
	
	
	
	public VersionMetadata getMetadata(String name,String type, String version) throws VersionNotExistsException;

	public Set<GenericProperty> getAvailableProperties (String name, String from, String type);

	EntitySet<GenericEntity> getEntitiesByIdWithProperties(Set<Integer> ids)
			throws EntityAPIException;

	public EntitySet<GenericEntity> getEntityWithPropertiesByAccession(Set<String> accs) throws EntityAPIException;

	EntitySet<GenericEntity> getEntitiesByPropertyWithProperties(
			Set<String> values, VersionMetadata vm, Set<GenericProperty> gps)
			throws EntityAPIException;
}
