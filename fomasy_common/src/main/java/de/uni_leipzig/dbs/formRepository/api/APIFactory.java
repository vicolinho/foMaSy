package de.uni_leipzig.dbs.formRepository.api;

import de.uni_leipzig.dbs.formRepository.api.annontationCluster.ClusterAPI;
import de.uni_leipzig.dbs.formRepository.api.annontationCluster.RDBMS_ClusterAPI;
import de.uni_leipzig.dbs.formRepository.api.annotation.AnnotationAPI;
import de.uni_leipzig.dbs.formRepository.api.annotation.RDBMS_AnnotationAPI;
import de.uni_leipzig.dbs.formRepository.api.annotation.entity.EntityAPI;
import de.uni_leipzig.dbs.formRepository.api.annotation.entity.RDBMS_EntityAPI;
import de.uni_leipzig.dbs.formRepository.api.datastore.RDBMS_RepositoryAPI;
import de.uni_leipzig.dbs.formRepository.api.datastore.RepositoryAPI;
import de.uni_leipzig.dbs.formRepository.api.form.EntityStructureAPI;
import de.uni_leipzig.dbs.formRepository.api.form.EntityStructureImportAPI;
import de.uni_leipzig.dbs.formRepository.api.form.RDBMS_EntityStructureImportAPI;
import de.uni_leipzig.dbs.formRepository.api.form.RDBMS_EntityStructureAPI;
import de.uni_leipzig.dbs.formRepository.api.graph.GraphAPI;
import de.uni_leipzig.dbs.formRepository.api.graph.RDBMS_GraphAPI;
import de.uni_leipzig.dbs.formRepository.api.util.DataStoreType;
import de.uni_leipzig.dbs.formRepository.global.GlobalRepositorySettings;

public class APIFactory {

	
	private static APIFactory instance ;
	
	
	
	private APIFactory (){
		
	}
	
	public static APIFactory getInstance(){
		if (instance ==null){
			instance =new APIFactory();
		}
		return instance;
	}
	
	
	public EntityStructureImportAPI getImportAPI (){
		DataStoreType type = GlobalRepositorySettings.getInstance().getType();
		switch (type){
		case RDBMS : return new RDBMS_EntityStructureImportAPI();
		default:return null;
		}
	}
	
	public RepositoryAPI getRepositoryAPI (){
		DataStoreType type = GlobalRepositorySettings.getInstance().getType();
		switch (type){
		case RDBMS : return new RDBMS_RepositoryAPI();
		default:return null;
		}
	}
	
	public EntityStructureAPI getStructureAPI (){
		DataStoreType type = GlobalRepositorySettings.getInstance().getType();
		switch (type){
		case RDBMS : return new RDBMS_EntityStructureAPI();
		default:return null;
		}
	}
	
	public EntityAPI getEntityAPI (){
		DataStoreType type = GlobalRepositorySettings.getInstance().getType();
		switch (type){
		case RDBMS : return new RDBMS_EntityAPI();
		default:return null;
		}
	}
	
	
	public AnnotationAPI getAnnotationAPI (){
		DataStoreType type = GlobalRepositorySettings.getInstance().getType();
		switch (type){
		case RDBMS : return new RDBMS_AnnotationAPI();
		default:return null;
		}
	}

	public ClusterAPI getClusterAPI() {
		DataStoreType type = GlobalRepositorySettings.getInstance().getType();
		switch (type){
		case RDBMS : return new RDBMS_ClusterAPI();
		default:return null;
		}
	}

	public GraphAPI getGraphAPI() {
		DataStoreType type = GlobalRepositorySettings.getInstance().getType();
		switch (type){
		case RDBMS : return new RDBMS_GraphAPI();
		default:return null;
		}
	}
}
