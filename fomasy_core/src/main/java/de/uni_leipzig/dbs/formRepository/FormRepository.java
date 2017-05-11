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

  FormManager getFormManager();
  MatchManager getMatchManager();
  MappingManager getMappingManager();
  ClusterManager getClusterManager();
  GraphManager getGraphManager();
  

  
}
