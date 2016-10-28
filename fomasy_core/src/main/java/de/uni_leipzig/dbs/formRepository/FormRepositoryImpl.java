package de.uni_leipzig.dbs.formRepository;

import java.io.IOException;
import java.util.Properties;

import de.uni_leipzig.dbs.formRepository.api.util.DataStoreInitializer;
import de.uni_leipzig.dbs.formRepository.api.util.DataStoreInitializerFactory;
import de.uni_leipzig.dbs.formRepository.api.util.DataStoreType;
import de.uni_leipzig.dbs.formRepository.api.util.DatabaseConnectionData;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.global.GlobalRepositorySettings;
import de.uni_leipzig.dbs.formRepository.manager.ClusterManager;
import de.uni_leipzig.dbs.formRepository.manager.FormManager;
import de.uni_leipzig.dbs.formRepository.manager.GraphManager;
import de.uni_leipzig.dbs.formRepository.manager.ManagerFactory;
import de.uni_leipzig.dbs.formRepository.manager.MappingManager;
import de.uni_leipzig.dbs.formRepository.manager.MatchManager;
import de.uni_leipzig.dbs.formRepository.util.PropertyReader;

/**
 * implementation of the {@linkplain FormRepository}
 * @author christen
 *
 */
public class FormRepositoryImpl implements FormRepository{

	
	
	
	/** 
	 * initialize the database configurations
	 */
	public void initialize (String file) throws InstantiationException, IllegalAccessException, ClassNotFoundException, InitializationException{
		
		Properties props =null;
		try {
			props = PropertyReader.readIniFile(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new InitializationException ("missing ini file");
		}
		String stringType = (String) props.get("dataSourceType");	
		DataStoreType type=null;
		if (stringType.equals("rdbms")){
			type = DataStoreType.RDBMS;
		}else{
			throw new InitializationException (" type");
		}
		
		DatabaseConnectionData data = new DatabaseConnectionData();
		data.setDriver(props.getProperty("driver"));
		data.setUser(props.getProperty("user"));
		data.setUrl(props.getProperty("url"));
		data.setPw(props.getProperty("password"));
		GlobalRepositorySettings.getInstance().setType(type);
		DataStoreInitializerFactory.getInitializer(type, data).initialize();
		
		
	}
	
	public void initialize (Properties map){
		
	}
	
	/**
	 * return the form manager manager that is necessary to get entity structures by name or by type
	 */
	public FormManager getFormManager(){
		FormManager fm = ManagerFactory.getFormManager();
		return fm;
	}


	public MappingManager getMappingManager() {
		return ManagerFactory.getMappingManger();
		
	}

	@Override
	public MatchManager getMatchManager() {
		MatchManager mm = ManagerFactory.getMatchManager();
		return mm;
	}

	@Override
	public ClusterManager getClusterManager() {
		ClusterManager cm = ManagerFactory.getClusterManager();
		return cm;
	}

	@Override
	public GraphManager getGraphManager() {
		GraphManager gm = ManagerFactory.getGraphManager();
		return gm;
	}
}
