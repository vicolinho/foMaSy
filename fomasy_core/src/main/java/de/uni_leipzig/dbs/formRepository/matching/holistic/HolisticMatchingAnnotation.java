package de.uni_leipzig.dbs.formRepository.matching.holistic;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.ExecutionTree;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.TokenClusterInitialization;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.ClusterSimilarityFunctions;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.ClusteringAlgorithm;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.EncodedClusterTransformation;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.SimilarCluster;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.ThresholdFunctions;
import de.uni_leipzig.dbs.formRepository.matching.holistic.data.TokenCluster;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorConfig;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorExecutor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;

public class HolisticMatchingAnnotation {
  
  Logger log = Logger.getLogger(getClass());

  private Map<Integer,TokenCluster> clusters;
  
  private Int2ObjectMap<List<SimilarCluster>> simMatrix ;
  
  private Map<Integer,TokenCluster> termGroups;
  
  
  /**
   * structure that consists of term groups, that frequently cooccur 
   */
  private EntityStructureVersion clusterStructure;
  
  
  
  private EncodedEntityStructure encodedCluster;

  private HashSet<EncodedEntityStructure> encodedForms;
  private int minSize=1;
  
  
  
  /**
   * 
   * @param forms set of considered structures
   * @param types specify the entities, which are processed
   * @param formProperties the properties that are considered
   * @param tfidfThresh consider only tokens whose tfidf value is over the specified threshold
   * @param simThresh
   * @param clusterMethod method to identify the termgroups
   */
  public  void computeTermGroups (Set <EntityStructureVersion> forms,Set<String> types,
        Set<GenericProperty> formProperties,float tfidfThresh,float simThresh,ClusteringAlgorithm clusterMethod){
    
    initialization(forms,types, formProperties,tfidfThresh,simThresh);
    log.debug("#clusters "+clusters.size());
    this.computeTermGroups(clusterMethod,formProperties, simThresh);
    this.simMatrix.clear();
    this.clusters.clear();  
  }
  
  
  private void initialization(Set <EntityStructureVersion> forms,  Set<String> types ,Set<GenericProperty> formProperties,
      float tfidfThresh,float simThresh){
    TokenClusterInitialization init = new TokenClusterInitialization();
     encodedForms = new HashSet<EncodedEntityStructure>();
    for (EntityStructureVersion esv: forms){
      EncodedEntityStructure ees = EncodingManager.getInstance().encoding(esv, types, true);
      encodedForms.add(ees);
    }
    clusters = init.initializeCluster(forms,formProperties,types,tfidfThresh);
    simMatrix = init.calculateSimilarityMatrix(clusters, ClusterSimilarityFunctions.DICE, simThresh);  
  }
  
  
  private void computeTermGroups(ClusteringAlgorithm clusterMethod,Set<GenericProperty> formProperties,float simThresh){
    termGroups = clusterMethod.cluster(clusters, simMatrix, encodedForms, formProperties, simThresh);
    HashSet<Integer>notRepresentative = new HashSet<Integer>();
    for (TokenCluster c :termGroups.values()){
      if (c.getTokenIds().size()<minSize)
        notRepresentative.add(c.getClusterId());
    }
    for (int rid : notRepresentative)
      termGroups.remove(rid);
    EncodedClusterTransformation trans = new EncodedClusterTransformation();
    setClusterStructure(trans.transformToEntityStructureVersion(termGroups.values(), "keyword"));
    setEncodedCluster(trans.transformToEncodedStructure(termGroups.values(), "keyword"));
  }

  public Map<Integer,Set<TokenCluster>> getSubsetRelationships(Map<Integer, TokenCluster> termGroups){
    List<TokenCluster> list = new ArrayList<TokenCluster> (termGroups.values());
    Map<Integer,Set<TokenCluster>> supersetMap = new HashMap<Integer,Set<TokenCluster>>();
    for (int i = 0; i<list.size(); i++){
      for (int j = i+1; j<list.size();j++){
        Set<Integer> copy = new HashSet<Integer>(list.get(i).getTokenIds());
        copy.retainAll(list.get(j).getTokenIds());
        if (copy.size()!=0){
          
          boolean isSubset = (copy.size() == list.get(i).getTokenIds().size() || copy.size() == list.get(j).getTokenIds().size());
          if (isSubset){
            TokenCluster subset ;
            TokenCluster superset; 
            if (copy.size() == list.get(i).getTokenIds().size()){
              subset = list.get(i);
              superset = list.get(j);
            }else{
              subset = list.get(j);
              superset = list.get(i);
            }
            
            Set<TokenCluster> subsets = supersetMap.get(superset.getClusterId());
          
            if (subsets ==null){
              subsets = new HashSet<TokenCluster>();
              supersetMap.put(superset.getClusterId(), subsets);
            }  
            subsets.add(superset);subsets.add(subset);
          }
        }
      }
    }
    return supersetMap;
  }
  
  public Map<Integer,Set<TokenCluster>> getSupersetRelationships(){
    List<TokenCluster> list = new ArrayList<TokenCluster> (this.termGroups.values());
    Map<Integer,Set<TokenCluster>> supersetMap = new HashMap<Integer,Set<TokenCluster>>();
    for (int i = 0; i<list.size(); i++){
      for (int j = i+1; j<list.size();j++){
        Set<Integer> copy = new HashSet<Integer>(list.get(i).getTokenIds());
        copy.retainAll(list.get(j).getTokenIds());
        if (copy.size()!=0){
          
          boolean isSubset = (copy.size() == list.get(i).getTokenIds().size() || copy.size() == list.get(j).getTokenIds().size());
          if (isSubset){
            TokenCluster subset ;
            TokenCluster superset; 
            if (copy.size() == list.get(i).getTokenIds().size()){
              subset = list.get(i);
              superset = list.get(j);
            }else{
              subset = list.get(j);
              superset = list.get(i);
            }
            
            Set<TokenCluster> subsets = supersetMap.get(subset.getClusterId());
          
            if (subsets ==null){
              subsets = new HashSet<TokenCluster>();
              supersetMap.put(subset.getClusterId(), subsets);
            }  
            subsets.add(superset);subsets.add(subset);
          }
        }
      }
    }
    return supersetMap;
  }
  

  public Map<Integer, Set<TokenCluster>> getSupersetRelationships(
      Map<Integer, TokenCluster> fiCluster) {
    // TODO Auto-generated method stub
    List<TokenCluster> list = new ArrayList<TokenCluster> (fiCluster.values());
    Map<Integer,Set<TokenCluster>> supersetMap = new HashMap<Integer,Set<TokenCluster>>();
    for (int i = 0; i<list.size(); i++){
      for (int j = i+1; j<list.size();j++){
        Set<Integer> copy = new HashSet<Integer>(list.get(i).getTokenIds());
        copy.retainAll(list.get(j).getTokenIds());
        if (copy.size()!=0){
          
          boolean isSubset = (copy.size() == list.get(i).getTokenIds().size() || copy.size() == list.get(j).getTokenIds().size());
          if (isSubset){
            TokenCluster subset ;
            TokenCluster superset; 
            if (copy.size() == list.get(i).getTokenIds().size()){
              subset = list.get(i);
              superset = list.get(j);
            }else{
              subset = list.get(j);
              superset = list.get(i);
            }
            
            Set<TokenCluster> subsets = supersetMap.get(subset.getClusterId());
          
            if (subsets ==null){
              subsets = new HashSet<TokenCluster>();
              supersetMap.put(subset.getClusterId(), subsets);
            }  
            subsets.add(superset);subsets.add(subset);
          }
        }
      }
    }
    return supersetMap;
  }
  
  public EntityStructureVersion getClusterStructure() {
    return clusterStructure;
  }

  public void setClusterStructure(EntityStructureVersion clusterStructure) {
    this.clusterStructure = clusterStructure;
  }

  public EncodedEntityStructure getEncodedCluster() {
    return encodedCluster;
  }

  public void setEncodedCluster(EncodedEntityStructure encodedCluster) {
    this.encodedCluster = encodedCluster;
  }

  public Map<Integer, TokenCluster> getClusters() {
    return this.termGroups;
  }

  public void setClusters(Map<Integer, TokenCluster> clusters) {
    this.clusters = clusters;
  }


  
}
