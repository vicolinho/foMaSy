package de.uni_leipzig.dbs.formRepository.api.util;



public class RDBMSDataStoreInitializer implements DataStoreInitializer {

	
	DatabaseConnectionData prop ;
	public RDBMSDataStoreInitializer (DatabaseConnectionData prop){
		this.prop = prop;
	}
	public void initialize() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		DBConHandler dch = DBConHandler.getInstance ();
		dch.setData(prop);
		dch.createConnection();
	}

}
