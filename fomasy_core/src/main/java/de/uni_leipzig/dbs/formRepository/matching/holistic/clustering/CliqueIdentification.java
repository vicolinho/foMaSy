package de.uni_leipzig.dbs.formRepository.matching.holistic.clustering;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.holistic.data.TokenCluster;

public class CliqueIdentification implements ClusteringAlgorithm {
Logger log = Logger.getLogger(getClass());
  private Set<Set<Integer>> clusters;
  private float threshold;
  
  
  @Override
  public Map<Integer, TokenCluster> cluster(Map<Integer,TokenCluster> initialCluster,
      Int2ObjectMap<List<SimilarCluster>> simMatrix,Set<EncodedEntityStructure> ees , Set<GenericProperty> props,float minSim){
    Set <Integer> r = new HashSet<Integer>();
    Set <Integer> x = new HashSet<Integer>();
    Set <Integer> p = new HashSet<Integer>();
    this.clusters = new HashSet<Set<Integer>> ();
    for (int i : simMatrix.keySet()){
      p.add(i);
    }
    log.debug("nodes: "+p.size());
    this.kerbosch(r, p, x, simMatrix);
    return this.updateCluster(clusters, initialCluster);
  }
  
  
  private TreeMap<Integer,Set<Integer>> getOrdering (Int2ObjectMap<List<SimilarCluster>> simMatrix){
    TreeMap<Integer, Set<Integer>> order = new TreeMap<Integer,Set<Integer>>();
    for (Entry<Integer,List<SimilarCluster>> e:simMatrix.entrySet()){
      Set<Integer> nodes= order.get(e.getValue().size());
      if (nodes ==null){
        nodes = new HashSet<Integer>();
        order.put(e.getValue().size(), nodes);
      }
      nodes.add(e.getKey());
      
    }
    return order;
  }
  
  public Set<Set<Integer>> getClusters() {
    return clusters;
  }

  public void setClusters(Set<Set<Integer>> clusters) {
    this.clusters = clusters;
  }

  
  
  /**
   * 
   * @param r current clique
   * @param p current node set
   * @param x 
   * @param simMatrix
   * @param initialCluster
   */
  private void kerbosch(Set<Integer> r,Set<Integer> p, Set<Integer> x,Int2ObjectMap<List<SimilarCluster>>simMatrix
      ){
    if (p.isEmpty()&&x.isEmpty()){
      clusters.add(r);
      return ;
    }
    
    while (!p.isEmpty()){
      int n = p.iterator().next();
      Set<Integer> r2 = new HashSet<Integer>(r);
      r2.add(n);
      Set<Integer> p2 = new HashSet<Integer>(p);
      Set<Integer> x2 = new HashSet<Integer>(x);
      Set<Integer> neighbors = new HashSet<Integer>();
      for (SimilarCluster sc: simMatrix.get(n)){
        int neigh =(sc.getClusterId()==n)?sc.getCorrespondingCluster():sc.getClusterId();
        neighbors.add(neigh);
      }
      p2.retainAll(neighbors);
      x2.retainAll(neighbors);
      
      kerbosch (r2,p2,x2,simMatrix);
      p.remove(n);
      x.add(n);
    }
  }
  
  public Set<Set<Integer>> simpleCluster(Set<Integer> nodes,
      HashMap<Integer,List<Integer>> edges){
    Set <Integer> r = new HashSet<Integer>();
    Set <Integer> x = new HashSet<Integer>();
    Set <Integer> p = new HashSet<Integer>();
    Set<Set<Integer>> clusters = new HashSet<Set<Integer>> ();
    for (int i : nodes){
      p.add(i);
    }
    this.simpleKerbosch(r, p, x, edges,clusters);
    return clusters;
  }
  
  private void simpleKerbosch(Set<Integer> r,Set<Integer> p, Set<Integer> x,HashMap<Integer,List<Integer>>  edges, Set<Set<Integer>> clusters){
    if (p.isEmpty()&&x.isEmpty()){
      clusters.add(r);
      return ;
    }
    
    while (!p.isEmpty()){
      int n = p.iterator().next();
      Set<Integer> r2 = new HashSet<Integer>(r);
      r2.add(n);
      Set<Integer> p2 = new HashSet<Integer>(p);
      Set<Integer> x2 = new HashSet<Integer>(x);
      Set<Integer> neighbors = new HashSet<Integer>();
      for (int corNode: edges.get(n)){
        int neigh =(corNode);
        neighbors.add(neigh);
      }
      p2.retainAll(neighbors);
      x2.retainAll(neighbors);
      
      simpleKerbosch (r2,p2,x2,edges,clusters);
      p.remove(n);
      x.add(n);
    }
  }
  
  private void tomita(Set<Integer> r,Set<Integer> p, Set<Integer> x,Int2ObjectMap<List<SimilarCluster>>simMatrix,
      Map<Integer,TokenCluster> initialCluster){
    if (p.isEmpty()&&x.isEmpty()){
      clusters.add(r);
      return ;
    }
    int u=-1;
    int maxCount = -1;
    int count = 0;
    for (int n: p){
      count=0;
      for (SimilarCluster sc: simMatrix.get(n)){
        int neigh =(sc.getClusterId()==n)?sc.getCorrespondingCluster():sc.getClusterId();
        if (p.contains(neigh)){
          count++;
        }
      }
      if (count >maxCount){
        maxCount = count;
        u = n;
      }
    }
    for (int n: x){
      for (SimilarCluster sc: simMatrix.get(n)){
        int neigh =(sc.getClusterId()==n)?sc.getCorrespondingCluster():sc.getClusterId();
        if (p.contains(neigh)){
          count++;
        }
      }
      if (count >maxCount){
        maxCount = count;
        u = n;
      }
    }
    Set<Integer> neighborU = new HashSet<Integer>();
    for (SimilarCluster sc: simMatrix.get(u)){
      int neigh =(sc.getClusterId()==u)?sc.getCorrespondingCluster():sc.getClusterId();
      neighborU.add(neigh);
    }
    Set<Integer> pmod = new HashSet<Integer>(p);
    pmod.removeAll(neighborU);
    while (!pmod.isEmpty()){
      int n = pmod.iterator().next();
      Set<Integer> r2 = new HashSet<Integer>(r);
      r2.add(n);
      Set<Integer> p2 = new HashSet<Integer>(p);
      Set<Integer> x2 = new HashSet<Integer>(x);
      Set<Integer> neighbors = new HashSet<Integer>();
      for (SimilarCluster sc: simMatrix.get(n)){
        int neigh =(sc.getClusterId()==n)?sc.getCorrespondingCluster():sc.getClusterId();
        neighbors.add(neigh);
      }
      p2.retainAll(neighbors);
      x2.retainAll(neighbors);
      
      tomita (r2,p2,x2,simMatrix,initialCluster);
      p.remove(n);
      x.add(n);
    }
  }
  
  private Map<Integer, TokenCluster> updateCluster(Set<Set<Integer>> clusters, Map<Integer, TokenCluster> initialCluster){
    Map<Integer,TokenCluster> refreshCluster = new HashMap<Integer,TokenCluster>();
    int count =0;
    for (Set<Integer> c:clusters){
      TokenCluster newCluster = new TokenCluster();
      
      for (int cid: c){
        newCluster.addToken(cid);
        
        if (newCluster.getItems().size()==0)
          newCluster.addItems(initialCluster.get(cid).getItems());
        else {
          newCluster.intersectItems(initialCluster.get(cid).getItems());
          if (newCluster.getItems().size()<2){
            log.warn(newCluster.getItems().size()+"token count:"+newCluster.getTokenIds().size());
            count++;
          }
          
        }
      }
      refreshCluster.put(newCluster.getClusterId(),newCluster);
    }
    log.info("only one item "+count);
    
    return refreshCluster;
  }
  
  public float getThreshold() {
    return threshold;
  }
  public void setThreshold(float threshold) {
    this.threshold = threshold;
  }


  


  

  

}
