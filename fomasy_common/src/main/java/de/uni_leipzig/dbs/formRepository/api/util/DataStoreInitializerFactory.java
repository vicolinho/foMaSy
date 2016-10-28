package de.uni_leipzig.dbs.formRepository.api.util;

public class DataStoreInitializerFactory {

	
	private static DataStoreInitializerFactory factory ;
	

	private DataStoreInitializerFactory (){
	}

	public static DataStoreInitializer getInitializer(DataStoreType type,DatabaseConnectionData data){
		if (factory ==null){
			factory = new DataStoreInitializerFactory();
		}
		switch (type){
		case RDBMS: 
			RDBMSDataStoreInitializer initializer = new RDBMSDataStoreInitializer(data);
			return initializer;
		default:return null;	
		}
	}
}
