package de.uni_leipzig.dbs.formRepository.api.util;
/**
 * Initialize the connection to the data source
 * @author christen
 *
 */
public interface DataStoreInitializer {

	public void initialize() throws InstantiationException, IllegalAccessException, ClassNotFoundException;
}
