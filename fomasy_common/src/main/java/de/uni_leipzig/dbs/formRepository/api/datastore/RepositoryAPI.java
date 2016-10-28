package de.uni_leipzig.dbs.formRepository.api.datastore;

import de.uni_leipzig.dbs.formRepository.exception.InstallationException;


/**
 * defines the install and deinstall functions
 * @author christen
 *
 */
public interface RepositoryAPI {

	/**
	 * create the schema in the current database
	 * @throws InstallationException
	 */
	public void installRepository() throws InstallationException;
	public void deleteRepository();
}
