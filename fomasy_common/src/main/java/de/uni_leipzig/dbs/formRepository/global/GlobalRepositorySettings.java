package de.uni_leipzig.dbs.formRepository.global;

import de.uni_leipzig.dbs.formRepository.api.util.DataStoreType;

public class GlobalRepositorySettings {

	private static GlobalRepositorySettings instance;
	private DataStoreType type ;
	
	private GlobalRepositorySettings(){
		
	}
	
	public static GlobalRepositorySettings getInstance(){
		if (instance == null){
			instance = new GlobalRepositorySettings();
		}
		return instance;
	}

	public DataStoreType getType() {
		return type;
	}

	public void setType(DataStoreType type) {
		this.type = type;
	}
	
	
}
