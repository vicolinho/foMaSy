package de.uni_leipzig.dbs.formRepository.manager;

import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.api.APIFactory;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationCluster;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.exception.ClusterAPIException;
import de.uni_leipzig.dbs.formRepository.exception.ClusterNotExistsException;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;

public class ClusterManagerImpl implements ClusterManager{

	@Override
	public Map<GenericEntity, EntitySet<GenericEntity>> getInitialAnnotationClusters(
			Set<VersionMetadata> srcData, VersionMetadata target) throws EntityAPIException, ClusterAPIException {
		return APIFactory.getInstance().getClusterAPI().getCluster(srcData, target);
	}

	@Override
	public void importClusters(Map<GenericEntity, AnnotationCluster> clusters,
			String name) throws ClusterAPIException {
		APIFactory.getInstance().getClusterAPI().importClusters(clusters, name);
		
	}

	@Override
	public Map<GenericEntity, AnnotationCluster> getDeterminedClusters(
			String clusterConfigName) throws ClusterAPIException,
			ClusterNotExistsException {
		// TODO Auto-generated method stub
		return APIFactory.getInstance().getClusterAPI().getDeterminedClusters(clusterConfigName);
	}

	@Override
	public Map<Integer, Map<Integer, Float>> getCooccurrences(String name,int common) {
		// TODO Auto-generated method stub
		return APIFactory.getInstance().getClusterAPI().getCooccurences(name,common);
	}

	@Override
	public Map<Integer, Map<Integer, Float>> getFormCoocccurrences(
			String clusterName, int common) {
		// TODO Auto-generated method stub
		return APIFactory.getInstance().getClusterAPI().getFormCooccurrences(clusterName,common);
	}

	

}
