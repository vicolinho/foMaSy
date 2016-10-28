package de.uni_leipzig.dbs.formRepository.api.annontationCluster;

import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationCluster;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.exception.ClusterAPIException;
import de.uni_leipzig.dbs.formRepository.exception.ClusterNotExistsException;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;
/**
 * defines the access operations to to the clusters
 * @author christen
 *
 */
public interface ClusterAPI {


	/**
	 * retrieve for each target concept a set of entities that are annotated by the target concept for specified source Structure
	 * and a target structure
	 * @param src 
	 * @param target
	 * @return map of target entities as key and a  set of annotated source entities as value
	 * @throws EntityAPIException
	 * @throws ClusterAPIException
	 */
	Map<GenericEntity, EntitySet<GenericEntity>> getCluster(
			Set<VersionMetadata> src, VersionMetadata target) throws EntityAPIException, ClusterAPIException;

	/**
	 * import a set of clusters 
	 * @param clusters
	 * @param name
	 * @throws ClusterAPIException
	 */
	void importClusters(Map<GenericEntity, AnnotationCluster> clusters,
			String name) throws ClusterAPIException;

	/**
	 * retrieve a set of clusters
	 * @param clusterConfigName
	 * @return
	 * @throws ClusterAPIException
	 * @throws ClusterNotExistsException
	 */
	Map<GenericEntity, AnnotationCluster> getDeterminedClusters(
			String clusterConfigName) throws ClusterAPIException, ClusterNotExistsException;
	
	Map<Integer,Map<Integer,Float>> getCooccurences (String name, int common);

	Map<Integer, Map<Integer, Float>> getFormCooccurrences(String name,
			int common);
}
