package de.uni_leipzig.dbs.formRepository;

import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;

public interface RepositoryServer {

  
  void initialize(String prop) throws InstantiationException, IllegalAccessException, ClassNotFoundException, InitializationException;
  
}
