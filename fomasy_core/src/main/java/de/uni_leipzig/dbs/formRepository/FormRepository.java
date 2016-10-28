package de.uni_leipzig.dbs.formRepository;

import de.uni_leipzig.dbs.formRepository.manager.FormManager;
import de.uni_leipzig.dbs.formRepository.manager.GraphManager;
import de.uni_leipzig.dbs.formRepository.manager.MappingManager;
import de.uni_leipzig.dbs.formRepository.manager.MatchManager;
import de.uni_leipzig.dbs.formRepository.manager.ClusterManager;


/**
 * interface that defines the access to the different subcomponents
 * @author christen
 *
 */
public interface FormRepository extends RepositoryServer{

	public FormManager getFormManager();
	public MatchManager getMatchManager();
	public MappingManager getMappingManager();
	public ClusterManager getClusterManager();
	public GraphManager getGraphManager();
	

	
}
