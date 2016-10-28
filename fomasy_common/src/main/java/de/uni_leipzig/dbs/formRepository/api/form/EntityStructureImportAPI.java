package de.uni_leipzig.dbs.formRepository.api.form;

import java.util.List;

import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportRelationship;
import de.uni_leipzig.dbs.formRepository.exception.ImportException;

/**
 * import api
 * @author christen
 *
 */
public interface EntityStructureImportAPI {

	/**
	 * import a list of entities in temporal tables
	 * @param importEntities
	 * @throws ImportException
	 */
	public void importTmpEntities (List <ImportEntity> importEntities) throws  ImportException;
	
	/**
	 * import a list of relationships temporal
	 * @param importRelationships
	 * @throws ImportException
	 */
	public void importTmpRelationships (List<ImportRelationship> importRelationships)throws ImportException; 
	
	
	/**
	 * determine the differences between the current entities in the temporal table and the previous version
	 * @param previousMeta
	 * @param current
	 */
	public void determineDiff(VersionMetadata previousMeta,VersionMetadata current) ;
	
	
	/**
	 * register the current version in the repository
	 * @param previous
	 * @param current
	 */
	public void importVersion (VersionMetadata previous, VersionMetadata current);
	
	/**
	 * get the previous version w.r.t the current version, to determine the differences
	 * @param m
	 * @return
	 * @throws ImportException
	 */
	public VersionMetadata getPreviousVersion (VersionMetadata m)throws ImportException;
	
	/**
	 * check if the version already exists in the repository
	 * @param m
	 * @return
	 * @throws ImportException
	 */
	public boolean checkVersion(VersionMetadata m) throws ImportException;

	public void cleanTmpTables();

	

	void importRelsForVersion(VersionMetadata currentVersion);

	void determineRelDiff(VersionMetadata current); 
	
}
