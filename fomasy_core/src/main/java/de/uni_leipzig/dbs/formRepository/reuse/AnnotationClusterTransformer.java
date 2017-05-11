package de.uni_leipzig.dbs.formRepository.reuse;

import java.util.Map;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationCluster;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;

public class AnnotationClusterTransformer {

  
  public EncodedEntityStructure transformClusters (Map<GenericEntity,AnnotationCluster> clusters
      ){
    EntitySet<GenericEntity> set = new GenericEntitySet();
    for (AnnotationCluster cluster: clusters.values()){
      set.addEntity(cluster);
    }
    EncodedEntityStructure ees = EncodingManager.getInstance().encoding(set, true);
    return ees;
    
  }
  
  public EntityStructureVersion transformClustersToStructure (Map<GenericEntity,AnnotationCluster> clusters
      ){
    EntityStructureVersion esv = new EntityStructureVersion(new VersionMetadata(-1, null, null, null, null));
    for (AnnotationCluster cluster: clusters.values()){
      if (!cluster.getProperties().isEmpty())
        esv.addAvailableProperty(cluster.getProperties().iterator().next());
      esv.addEntity(cluster);
      
    }
    
    return esv;
    
  }
  
}
