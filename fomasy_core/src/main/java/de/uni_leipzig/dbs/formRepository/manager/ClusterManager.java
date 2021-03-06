package de.uni_leipzig.dbs.formRepository.manager;

import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationCluster;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.exception.ClusterAPIException;
import de.uni_leipzig.dbs.formRepository.exception.ClusterNotExistsException;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;

public interface ClusterManager {
  
  Map<GenericEntity,EntitySet<GenericEntity>> getInitialAnnotationClusters(Set<VersionMetadata> srcData, VersionMetadata target) throws EntityAPIException, ClusterAPIException;
  
  void importClusters(Map<GenericEntity, AnnotationCluster> clusters, String name) throws ClusterAPIException;


  Map<GenericEntity,AnnotationCluster> getDeterminedClusters(String clusterConfigName) throws ClusterAPIException,ClusterNotExistsException;

  Map<Integer, Map<Integer, Float>> getCooccurrences(
          String clusterName, int common);

  Map<Integer, Map<Integer, Float>> getFormCoocccurrences(
          String clusterName, int common);
}
