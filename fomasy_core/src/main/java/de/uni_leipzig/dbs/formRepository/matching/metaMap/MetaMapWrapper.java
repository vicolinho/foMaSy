package de.uni_leipzig.dbs.formRepository.matching.metaMap;

import gov.nih.nlm.nls.metamap.AcronymsAbbrevs;
import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.api.APIFactory;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;

public class MetaMapWrapper {

  Logger log = Logger.getLogger(getClass());
  
  public static final String HOST = "host";
  
  public static final String PORT = "port";

  public static final Object THRESHOLD = "threshold";

  public static final Object OPTIONS = "options";
  
  public AnnotationMapping match (EntityStructureVersion esv,Set<String> entityTypes,
      Set<GenericProperty> props, Properties metaMapProperties ) throws EntityAPIException{
    MetaMapApiImpl impl = new MetaMapApiImpl();
    Integer threshold =0;
    if (metaMapProperties.get(HOST)!=null){
      impl.setHost((String) metaMapProperties.get(HOST));
    }
    if (metaMapProperties.get(PORT)!=null){
      impl.setPort((Integer)metaMapProperties.get(PORT));
    }
    
    if (metaMapProperties.get(THRESHOLD)!= null){
      threshold = (Integer) metaMapProperties.get(THRESHOLD);
    }
    if (metaMapProperties.get(OPTIONS)!=null){
      impl.setOptions((String) metaMapProperties.get(OPTIONS));
    }
    
    Map<GenericEntity,Set<Pair<String,Float>>> map = new HashMap<GenericEntity,Set<Pair<String,Float>>>();
    for (GenericEntity ge : esv.getEntities()){
      if (entityTypes.contains(ge.getType())){
        for (GenericProperty p : props){
          
          List<String> pvs = ge.getPropertyValues(p);
          for (String pv : pvs){
            List<Result> result = impl.processCitationsFromString(pv);
            
            for (Result r: result){
              try {
                if (!r.getAcronymsAbbrevs().isEmpty()){
                  for (AcronymsAbbrevs abbrev: r.getAcronymsAbbrevs()){
                    for (String cui : abbrev.getCUIList()){
                       Set<Pair<String,Float>> cset = map.get(ge);
                       if (cset ==null){  
                            cset = new HashSet<Pair<String,Float>>();
                            map.put(ge, cset);
                          }
                       Pair<String,Float> pair = new Pair<String,Float>(cui.trim(), 
                              Math.abs(1f));
                       cset.add(pair);
                       
                    }
                  }
                }
                TreeMap<Integer,List<Mapping>> mappingMap = new TreeMap <Integer,List<Mapping>>(); 
                for (Utterance u: r.getUtteranceList()){
                  for (PCM pcm: u.getPCMList()) {
                    for (Mapping ev: pcm.getMappingList()) {
                         int score = Math.abs(ev.getScore());
                         List<Mapping> mappings =mappingMap.get(score);
                         if (mappings ==null){
                           mappings =new ArrayList<Mapping> ();
                           mappingMap.put(score, mappings);
                         }
                         mappings.add(ev);
                    }
                  }
                }
            
                if (mappingMap.lastKey()!=null){
                  for (Mapping ev : mappingMap.get(mappingMap.lastKey())){
                    for (Ev v :ev.getEvList()){
                        if (Math.abs(v.getScore())>=threshold){
                          Set<Pair<String,Float>> cset = map.get(ge);
                          
                          if (cset ==null){  
                            cset = new HashSet<Pair<String,Float>>();
                            map.put(ge, cset);
                          }
                          Pair<String,Float> pair = new Pair<String,Float>(v.getConceptId().trim(), 
                              Math.abs(v.getScore()/1000f));
                          cset.add(pair);
                         }
                      }
                  }
                }
              } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            }
          }  
        }
      }
    }
    
    Map<String,GenericEntity> entityMap =new HashMap<String,GenericEntity>();
    Set< String> accs = new HashSet< String> ();
    for (Set< Pair<String,Float>> s : map.values()){
      for ( Pair<String,Float> t: s)accs.add(t.con);
    }
    
    AnnotationMapping am = new AnnotationMapping ();
    EntitySet <GenericEntity> entitySet = APIFactory.getInstance().getEntityAPI().getEntityWithPropertiesByAccession(accs);
    for (GenericEntity ge: entitySet){
      entityMap.put(ge.getAccession(), ge);
    }
    for (Entry <GenericEntity,Set<Pair<String,Float>>> e: map.entrySet()){
      for (Pair<String,Float> acc : e.getValue()){
        if (entityMap.containsKey(acc.con)){
          EntityAnnotation ea = new EntityAnnotation(e.getKey().getId(),
              entityMap.get(acc.con).getId(), e.getKey().getAccession(),
              acc.con, acc.weight, false);
          am.addAnnotation(ea);
        }
      }
    }
    return am;
  }
  
  private class Pair <T,S> {
     T con;
    
    S weight;
    
    Pair (T c, S w){
      this.con = c;
      this.weight = w;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getOuterType().hashCode();
      result = prime * result + ((con == null) ? 0 : con.hashCode());
      result = prime * result
          + ((weight == null) ? 0 : weight.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Pair other = (Pair) obj;
      if (!getOuterType().equals(other.getOuterType()))
        return false;
      if (con == null) {
        if (other.con != null)
          return false;
      } else if (!con.equals(other.con))
        return false;
      if (weight == null) {
        if (other.weight != null)
          return false;
      } else if (!weight.equals(other.weight))
        return false;
      return true;
    }

    private MetaMapWrapper getOuterType() {
      return MetaMapWrapper.this;
    }
    
    
  }
}
