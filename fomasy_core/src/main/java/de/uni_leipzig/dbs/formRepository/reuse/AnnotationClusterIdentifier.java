package de.uni_leipzig.dbs.formRepository.reuse;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationCluster;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.ClusterAPIException;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;
import de.uni_leipzig.dbs.formRepository.matching.holistic.HolisticMatchingAnnotation;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.ClusterSimilarityFunctions;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.ClusteringAlgorithm;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.FINAlgorithm;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.SimilarCluster;
import de.uni_leipzig.dbs.formRepository.matching.holistic.clustering.TokenClusterInitialization;
import de.uni_leipzig.dbs.formRepository.matching.holistic.data.TokenCluster;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorConfig;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorExecutor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;

public class AnnotationClusterIdentifier {

  Logger log = Logger.getLogger(getClass());
  
  public AnnotationClusterIdentifier() {
    // TODO Auto-generated constructor stub
  }

  
  private float ridfThreshold;
  private Map <Integer,Float> idfMap ;
  
  private Map <Integer,Float> globalMap;
  String lang= "EN";
  private int totalAnnotatedQuestions;
  private Map<Integer, Set<Integer>> lookup;
  
  public Map<GenericEntity ,EntitySet<GenericEntity>>  detemermineCluster (Set<VersionMetadata>src, VersionMetadata ontology, FormRepository rep) throws EntityAPIException, ClusterAPIException{
    Map<GenericEntity, EntitySet<GenericEntity>> clusterMap = rep.getClusterManager().getInitialAnnotationClusters(src, ontology);
    log.debug("#Cluster: " +clusterMap.size());
    return clusterMap;
    
  }
  
  public Map<GenericEntity ,AnnotationCluster> identifyRepresentantsOnlyCommon(Set<GenericProperty> srcProperties, Set<GenericProperty> targetProperties,
      Map<GenericEntity, EntitySet<GenericEntity>> clusterMap ,PreprocessorConfig srcConfig,PreprocessorConfig targetConfig,
      Map<Integer, Set<Integer>> lookup2, Map<GenericEntity ,AnnotationCluster> resultCluster){
    this.idfMap = new HashMap<Integer,Float> ();
    this.globalMap = new HashMap<Integer,Float>();
    this.lookup = lookup2;
    for (Entry<GenericEntity,EntitySet<GenericEntity>> entry: clusterMap.entrySet()){
      //log.debug("process annotation:"+ entry.getKey().getAccession());
      Map <Integer, TokenCluster> commonCluster = this.determineRepresentantsByCommon(entry.getValue(), srcConfig, targetConfig, entry.getKey(), srcProperties, targetProperties);
      AnnotationCluster ac = resultCluster.get(entry.getKey());
      if (ac ==null){
        ac = new AnnotationCluster(entry.getKey());
        resultCluster.put(entry.getKey(),ac);
        ac.setElements(entry.getValue());
      }
      Set <String> representatives = new HashSet<String> ();
      if (commonCluster.size()!=0){
        for (TokenCluster tc : commonCluster.values()){
          StringBuffer sb = new StringBuffer ();
          boolean onlyNumeric =true;
          for (int tid: tc.getTokenIds()){
            if (!EncodingManager.getInstance().getReverseDict().get(tid).matches("[0-9]")){
              onlyNumeric =false;
            }
            sb.append(EncodingManager.getInstance().getReverseDict().get(tid)+" ");
          }
          if (sb.toString().trim().equals("")){
            //log.warn("empty string");
          }else {
            if (sb.toString().trim().length()>2&&!onlyNumeric)
              representatives.add(sb.toString().trim());
          }
        }
      }
      
      if (!representatives.isEmpty()){
        for (String r:representatives){
          ac.addRepresentant(r, lang);
        }
        resultCluster.put(entry.getKey(), ac);
      }
    }
    return resultCluster;
  }
  
  public Map<GenericEntity ,AnnotationCluster> identifyRepresentants(Set<GenericProperty> srcProperties, Set<GenericProperty> targetProperties,
      Map<GenericEntity, EntitySet<GenericEntity>> clusterMap ,PreprocessorConfig srcConfig,PreprocessorConfig targetConfig,
      Map<Integer, Set<Integer>> lookup, Map<GenericEntity ,AnnotationCluster> resultCluster){
    
    this.idfMap = new HashMap<Integer,Float> ();
    this.globalMap = new HashMap<Integer,Float>();
    this.lookup = lookup;
    Set <Integer> set = new HashSet<Integer>();
    for  (Entry<GenericEntity,EntitySet<GenericEntity>> e: clusterMap.entrySet()){
      for (GenericEntity ge: e.getValue()){
        set.add(ge.getId());
      }
    }
    totalAnnotatedQuestions = set.size();
    int avg_annotationPerConcept = (int) Math.ceil((float)set.size()*0.001f);
    log.info("average annotation per concept:"+avg_annotationPerConcept);
    Map<GenericEntity,Collection<TokenCluster>> entityTokenCluster = new HashMap<GenericEntity,Collection<TokenCluster>>();
    Map<GenericEntity,Collection<TokenCluster>> commonTokenCluster = new HashMap<GenericEntity,Collection<TokenCluster>>();
    
    
    for (Entry<GenericEntity,EntitySet<GenericEntity>> entry: clusterMap.entrySet()){
      //log.debug("process annotation:"+ entry.getKey().getAccession());
      Map<Integer,TokenCluster> fiCluster = this.determineRepresentantByFI(entry.getValue(),srcConfig, srcProperties,avg_annotationPerConcept);
      entityTokenCluster.put(entry.getKey(), fiCluster.values());
      Map <Integer, TokenCluster> commonCluster = this.determineRepresentantsByCommon(entry.getValue(), srcConfig, targetConfig, entry.getKey(), srcProperties, targetProperties);
      commonTokenCluster.put(entry.getKey(), commonCluster.values());
      AnnotationCluster ac = resultCluster.get(entry.getKey());
      if (ac ==null){
        ac = new AnnotationCluster(entry.getKey());
        resultCluster.put(entry.getKey(),ac);
        ac.setElements(entry.getValue());
      }
      Set <String> representatives = new HashSet<String> ();
      if (commonCluster.size()!=0){
        for (TokenCluster tc : commonCluster.values()){
          StringBuffer sb = new StringBuffer ();
          boolean onlyNumeric =true;
          for (int tid: tc.getTokenIds()){
            if (!EncodingManager.getInstance().getReverseDict().get(tid).matches("[0-9]")){
              onlyNumeric =false;
            }
            sb.append(EncodingManager.getInstance().getReverseDict().get(tid)+" ");
          }
          if (sb.toString().trim().equals("")){
            //log.warn("empty string");
          }else {
            if (sb.toString().trim().length()>2&&!onlyNumeric)
              representatives.add(sb.toString().trim());
          }
        }
      }
      
      if (!representatives.isEmpty()){
        for (String r:representatives){
          ac.addRepresentant(r, lang);
        }
        resultCluster.put(entry.getKey(), ac);
      }
      
      
      //log.debug("number of representants for "+entry.getKey().getAccession()+" "+ac.getRepresentants().size());
    }
    
    entityTokenCluster = this.filterFrequentTokensByIDF (entityTokenCluster,commonTokenCluster);
    
    
    for (Entry <GenericEntity,Collection<TokenCluster>> e: entityTokenCluster.entrySet()){
      Set<String> representatives = new HashSet<String> ();
      for (TokenCluster tc : e.getValue()){
        StringBuffer sb = new StringBuffer ();
        boolean onlyNumeric =true;
        for (int tid: tc.getTokenIds()){
          if (!EncodingManager.getInstance().getReverseDict().get(tid).matches("[0-9]")){
            onlyNumeric =false;
          }
          sb.append(EncodingManager.getInstance().getReverseDict().get(tid)+" ");
        }
        if (sb.toString().trim().equals("")){
          //log.warn("empty string");
        }else {
          if (sb.toString().trim().length()>2&&!onlyNumeric)
            representatives.add(sb.toString().trim());
        }
      }
      if (!representatives.isEmpty()){
        AnnotationCluster ac = resultCluster.get(e.getKey());
        if (ac==null){
          ac = new AnnotationCluster(e.getKey());
          resultCluster.put(e.getKey(),ac);
          ac.setElements(clusterMap.get(e.getKey()));
        }
        for (String r:representatives){
          ac.addRepresentant(r, lang);
        }
        resultCluster.put(e.getKey(), ac);
      }
    }
    return resultCluster;
    
  }
  
  

  
  private Map<GenericEntity, Collection<TokenCluster>> filterFrequentTokensByIDF(
      Map<GenericEntity, Collection<TokenCluster>> entityTokenCluster,Map <GenericEntity,Collection<TokenCluster>> commonTokenClusters) {
    //Map<Integer,Float> idfMap = new HashMap<Integer, Float> ();
    //Map<Integer,Float> corpusMap = new HashMap<Integer,Float>(); 
    
    Map<Integer,Float> ridfMap = new HashMap<Integer,Float>();
  
    //Set<Integer> tokenSet =new HashSet<Integer>();
    //Set <Collection<TokenCluster>> setCluster = new HashSet<Collection<TokenCluster>>();
    //setCluster.addAll(entityTokenCluster.values());  setCluster.addAll(commonTokenClusters.values());
    Set<Integer> docs = new HashSet<Integer>();
    for (GenericEntity ge: entityTokenCluster.keySet()){
      docs.add(ge.getId());
    }
    for (GenericEntity ge: commonTokenClusters.keySet()){
      docs.add(ge.getId());
    }
    ridfMap = this.computeResidualIDF(docs.size());
    float avg = 0;
    for (float f :ridfMap.values()){
      avg+=f;
    }
    avg/=ridfMap.size();
    float ratio = 0.022f;
    log.debug("ratio"+ratio);
//    for (Collection<TokenCluster> tokens : setCluster){
//      tokenSet.clear();
//      for (TokenCluster tc : tokens){
//        for (int t : tc.getTokenIds()){
//          tokenSet.add(t);
//          if (!corpusMap.containsKey(t)){
//            corpusMap.put(t, 1f);
//          }else{
//            corpusMap.put(t, corpusMap.get(t)+1f);
//          }
//        }
//      }
//      for (int t: tokenSet){
//        if (idfMap.containsKey(t)){
//          idfMap.put(t, idfMap.get(t)+1f);
//        }else{
//          idfMap.put(t, 1f);
//        }
//      }
//    }
//    
//    for (Integer t: idfMap.keySet()){
//      float count = idfMap.get(t);
//      float idf = (float) Math.log((float)docs.size()/count)/(float)Math.log(2);
//      
//      float cf = corpusMap.get(t);
//      float p =  1f-(float)Math.exp(-(cf/(float)docs.size()));
//      float logExp = (float) (Math.log(p)/Math.log(2));
//      if (Math.abs(logExp)>idf){
//        log.info(idf +"### "+logExp+"####"+docs.size()+"####"+count);
//        log.info(EncodingManager.getInstance().getReverseDict().get(t));
//      }
//      float ridf = (float) (idf+(Math.log(p)/Math.log(2)));
//      ridfMap.put(t, ridf);
//    }
    Set<Integer> badTokens = new HashSet<Integer>();
    for (Entry <Integer,Float> e:ridfMap.entrySet()){
      if (e.getValue()<ratio){
        badTokens.add(e.getKey());
      }
    }
    
    float agg =0;
    log.info("avg idf"+avg);
    for (Entry<GenericEntity,Collection<TokenCluster>> entry: entityTokenCluster.entrySet()){
      Collection <TokenCluster> col = entry.getValue();
      
      Set <TokenCluster> remCluster = new HashSet<TokenCluster>();
      
      for (TokenCluster tc : col){
        agg =0;
        for (int t : tc.getTokenIds()){
          agg +=ridfMap.get(t);
          if (badTokens.contains(t)){
            //remCluster.add(tc);
            //break;
          }
        }
        if (agg<avg){
          remCluster.add(tc);
        }
      }
      
      
      if (!remCluster.isEmpty()){
        boolean rem = col.removeAll(remCluster);
      }
      remCluster.clear();
      HolisticMatchingAnnotation hol = new HolisticMatchingAnnotation ();
      Map<Integer,TokenCluster> fiCluster = new HashMap<Integer,TokenCluster>();
      for(TokenCluster tc: col){
        fiCluster.put(tc.getClusterId(), tc);
      }
      Map<Integer,Set<TokenCluster>> subsetMap = hol.getSupersetRelationships(fiCluster);
      //Map<Integer,Set<TokenCluster>> supersetMap = hol.getSubsetRelationships(fiCluster);
      for (Integer key :fiCluster.keySet()){
        if (subsetMap.containsKey(key)){
          remCluster.add(fiCluster.get(key));
        }
      }
      col.removeAll(remCluster);
    }
    return entityTokenCluster;
  }

  int id ;
  
  private void testWrite(Map<Integer,Float> ridfMap){
    try {
      FileWriter fw = new FileWriter ("ridfDistribution");
      fw.append("token  ridf"+System.getProperty("line.separator"));
      for (Entry<Integer,Float> e:ridfMap.entrySet()){
        fw.append(EncodingManager.getInstance().getReverseDict().get(e.getKey())+"  "+e.getValue()+System.getProperty("line.separator"));
      }
      fw.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  private Map<Integer, TokenCluster> filterTokenSets(Map<Integer, TokenCluster> fiCluster,
      Map<Integer, TokenCluster> commonCluster,  Set<Integer> remove, float size,float highSupport,boolean isFrequentAllowed) {
    Map<Integer, TokenCluster> filterMap =new HashMap<Integer,TokenCluster>();
    Set<Set<Integer>> set= new HashSet<Set<Integer>>();
    if (fiCluster.size()>0){
      TreeMap<Float,List<TokenCluster>> commonMap = new TreeMap<Float,List<TokenCluster>>();
      for (TokenCluster tc: fiCluster.values()){
        
        float ratio = (float)tc.getItems().size()/ size;
        
        if (ratio>=highSupport&&!remove.contains(tc.getClusterId())&&isFrequentAllowed){
          filterMap.put(tc.getClusterId(), tc);
        }else {
          for (TokenCluster ctc: commonCluster.values()){
            Set<Integer> common = new HashSet<Integer>(tc.getTokenIds());
            common.retainAll(ctc.getTokenIds());
            
            Float dice = 2*common.size()/(float)(ctc.getTokenIds().size()+tc.getTokenIds().size());
            List <TokenCluster> list = commonMap.get(dice);
            if (dice >0.5){ // check if the fi overlaps with the concept description
              if (list ==null){
                list = new ArrayList<TokenCluster>();
                commonMap.put(dice, list);
              }
              list.add(tc);  
            }
          }
        }
      }// for each frequent item set
      
      Float key = null;
      try{
        key = commonMap.lastKey();
      }catch (NoSuchElementException e){}
      if (key!=null){
        for (List<TokenCluster> highCommon:commonMap.values()){
          //List<TokenCluster> highCommon = commonMap.get(key);
          for (TokenCluster highTC:highCommon){
            set.add(highTC.getTokenIds());
            filterMap.put(highTC.getClusterId(), highTC);
          }
        }
      }
    }
    for (TokenCluster tc: commonCluster.values()){
      if (!set.contains(tc.getTokenIds()))
        filterMap.put(tc.getClusterId(), tc);
    }
    
    return filterMap;
  }

  private  Map<Integer,TokenCluster> determineRepresentantByFI(EntitySet<GenericEntity> set,PreprocessorConfig config,Set<GenericProperty> props, int minElements){
    Map<Integer,TokenCluster> fiCluster = new HashMap<Integer,TokenCluster>();
    if (set.getSize()>minElements){
      PreprocessorExecutor exec = new PreprocessorExecutor();
      
      set = this.deduplicateEntities(set, props);
      set = exec.preprocess(set, config);
      EncodedEntityStructure ees = EncodingManager.getInstance().encoding(set, true);
      TokenClusterInitialization init = new TokenClusterInitialization();
      Set<EncodedEntityStructure> encodedSet = new HashSet<EncodedEntityStructure>();
      encodedSet.add(ees);
      
      
      
      Map<Integer,TokenCluster> clusters = init.initializeClusterByEncodedStructures(encodedSet, props, 0);
      ClusteringAlgorithm algorithm = new FINAlgorithm();
      Int2ObjectMap<List<SimilarCluster>> simMatrix  =init.calculateSimilarityMatrix(clusters, ClusterSimilarityFunctions.DICE, 0.1f);
      int absoluteSupport = (int) Math.ceil(set.getSize()*0.3f);
      
      absoluteSupport = (absoluteSupport<3)?3:absoluteSupport;
      fiCluster = algorithm.cluster(clusters, simMatrix, encodedSet, props, absoluteSupport);
      
      
    }
    return fiCluster;
  }
  
  private EntitySet<GenericEntity> deduplicateEntities(EntitySet<GenericEntity> set, Set<GenericProperty> srcProperty){
    Map<String, GenericEntity> map = new HashMap<String,GenericEntity> ();
    Set<GenericEntity> duplicates = new HashSet<GenericEntity>();
    for (GenericProperty gp :srcProperty){
      for (GenericEntity ge : set){
        try{
          String value = ge.getPropertyValues(gp).get(0);
          if (!map.containsKey(value)){
            map.put(value, ge);
          }else {
            duplicates.add(ge);
          }
        }catch (IndexOutOfBoundsException e){}
      }
    }
    for (GenericEntity ge: duplicates)
      set.removeEntity(ge);
    return set;
    
  }
  
  private Map<Integer,TokenCluster>  determineRepresentantsByCommon(EntitySet<GenericEntity> set ,PreprocessorConfig config,
      PreprocessorConfig targetConfig ,GenericEntity target, Set<GenericProperty> srcProps, Set<GenericProperty> targetProps){
    
    PreprocessorExecutor exec = new PreprocessorExecutor();
    set = exec.preprocess(set, config);
    EncodedEntityStructure esv = EncodingManager.getInstance().encoding(set, true);
    Set <Integer> tokenInQuestionPerEntity = new HashSet<Integer> ();
    for (int[][][] values:esv.getPropertyValueIds()){
      for (GenericProperty gp: srcProps){
        int pos = (esv.getPropertyPosition().get(gp)!=null)?esv.getPropertyPosition().get(gp):-1;
        if (pos!=-1){
          for (int[] pv: values[pos]){
            for (int t : pv){
              tokenInQuestionPerEntity.add(t);
              if (this.globalMap.containsKey(t)){
                this.globalMap.put(t,this.globalMap.get(t)+1);
              }else{
                this.globalMap.put(t,1f);
              }
            }
          }
        }
      }
    }
    for (int t : tokenInQuestionPerEntity){
      if (this.idfMap.containsKey(t))
        this.idfMap.put(t, this.idfMap.get(t)+1f);
      else 
        this.idfMap.put(t, 1f);
    }
    
    EntitySet<GenericEntity> targetEntitySet = new  GenericEntitySet ();
    targetEntitySet.addEntity(target);
    targetEntitySet = exec.preprocess(targetEntitySet, targetConfig);
    
    CommonTokenIdentifier tokenIdentifier = new CommonTokenIdentifier();
    EncodedEntityStructure targetEnc = EncodingManager.getInstance().encoding(targetEntitySet, true);
    Map<Integer, TokenCluster> tc = tokenIdentifier.getCommonTokens(esv, targetEnc, srcProps, targetProps,lookup);
    
    return tc;
  }
  
  
  private Set<Integer> getTokensOfGeneralAnnotations(Map<GenericEntity,EntitySet<GenericEntity>> clusterMap){
    Map <GenericEntity,Float> idfAnnotation = new HashMap<GenericEntity,Float>();
    Map<GenericEntity,EntitySet<GenericEntity>> revMap = new HashMap<GenericEntity,EntitySet<GenericEntity>> ();
    EntitySet<GenericEntity> singleAnnotations = new GenericEntitySet ();
    for (Entry<GenericEntity,EntitySet<GenericEntity>>e:clusterMap.entrySet()){
      for (GenericEntity ge: e.getValue()){
        EntitySet<GenericEntity> set = revMap.get(ge);
        if (set==null){
          set = new GenericEntitySet();
          revMap.put(ge, set);
        }
        set.addEntity(e.getKey());
      }
    }
    for (EntitySet<GenericEntity> ge: revMap.values()){
      if (ge.getSize()==1){
        singleAnnotations.addEntity(ge.getCollection().iterator().next());
      }
    }
    
    for (Entry<GenericEntity,EntitySet<GenericEntity>> e: clusterMap.entrySet()){
      if (!singleAnnotations.contains(e.getKey()))
        idfAnnotation.put(e.getKey(), (float)Math.log((float)totalAnnotatedQuestions/(float)e.getValue().getSize()));
    }
    FileWriter fw;
    try {
      fw = new FileWriter ("idfConcept");
      fw.append("accession  idf"+System.getProperty("line.separator"));
      for (Entry<GenericEntity,Float>e:idfAnnotation.entrySet()){
        
        fw.append(e.getKey().getAccession()+"  "+e.getValue()+System.getProperty("line.separator"));
      }
      fw.close();
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    
    return null;
  }
  
  
  private Map<Integer,Float> computeResidualIDF(int docNumber){
    Map<Integer, Float> ridfMap = new HashMap<Integer,Float>();
    for (Integer t: idfMap.keySet()){
      float count = idfMap.get(t);
      float idf = (float) Math.log((float)docNumber)/count;
      
      float cf = this.globalMap.get(t);
      float p =  1f-(float)Math.exp(-(cf/(float)docNumber));
      float logExp = (float) (Math.log(p));
      float ridf = idf+logExp;
      
      ridfMap.put(t, idf);
    }
    return ridfMap;
    
  }
}
