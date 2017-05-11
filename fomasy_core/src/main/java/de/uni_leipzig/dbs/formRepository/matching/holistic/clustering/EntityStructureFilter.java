package de.uni_leipzig.dbs.formRepository.matching.holistic.clustering;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.holistic.data.TokenCluster;

public class EntityStructureFilter {

  
  Logger log  = Logger.getLogger(getClass());
  
  public EntityStructureFilter() {
    
    
    
  }
  
  public EncodedEntityStructure filterStructureByClusters (EncodedEntityStructure form, Collection<TokenCluster> clusters,
      EncodedEntityStructure clusterStructure, Set<GenericProperty> filterProps,AnnotationMapping map){
      for (TokenCluster c : clusters){
        if (map.containsCorrespondingTargetIds(c.getClusterId())){
          int clusterEntityPosition = clusterStructure.getObjIds().get(c.getClusterId());
          for (int item : c.getItems()){
            if (form.getObjIds().containsKey(item)){
              int itemPos = form.getObjIds().get(item);
              for (GenericProperty prop :filterProps){
                int propPos = clusterStructure.getPropertyPosition().get(prop);
                for (int [] pvs : clusterStructure.getPropertyValueIds()[clusterEntityPosition][propPos]){
                  IntSet values = new IntOpenHashSet();
                  for (int v: pvs)
                    values.add(v);
                  for (int propFormPos: form.getPropertyPosition().values()){
                    int id =0;
                    for (int[] itemValues: form.getPropertyValueIds()[itemPos][propFormPos]){
                      IntSet removeSet = new IntOpenHashSet(values);
                      IntList reducedValue = new IntArrayList();
                      for (int iv : itemValues){
                        if (removeSet.contains(iv)){
                          removeSet.remove(iv);
                        }else{
                          reducedValue.add(iv);
                        }
                      }
                      if (removeSet.isEmpty()){
                        
                        form.getPropertyValueIds()[itemPos][propFormPos][id] = reducedValue.toArray(new int[]{});
                        //log.debug("filter set: "+values.toString()+"reduced prop Value: "+Arrays.toString(form.getPropertyValueIds()[itemPos][propFormPos][id]));
                      }
                      id++;
                    }
                  }
                }//values
                
              } //properties
            }
          }
        }
      }
      return form;
  }
  
  public EncodedEntityStructure filterStructureByClusters (EncodedEntityStructure form, 
      EncodedEntityStructure clusterStructure, Set<GenericProperty> filterProps,AnnotationMapping map){
      for (int obj :clusterStructure.getObjIds().keySet()){
        if (map.containsCorrespondingSrcIds(obj)){
          int clusterEntityPosition = clusterStructure.getObjIds().get(obj);
          for (int item :map.getCorrespondingSrcIds(obj)){
            if (form.getObjIds().containsKey(item)){
              int itemPos = form.getObjIds().get(item);
              for (GenericProperty prop :filterProps){
                int propPos = clusterStructure.getPropertyPosition().get(prop);
                for (int [] pvs : clusterStructure.getPropertyValueIds()[clusterEntityPosition][propPos]){
                  IntSet values = new IntOpenHashSet();
                  for (int v: pvs)
                    values.add(v);
                  for (int propFormPos: form.getPropertyPosition().values()){
                    int id =0;
                    for (int[] itemValues: form.getPropertyValueIds()[itemPos][propFormPos]){
                      IntSet removeSet = new IntOpenHashSet(values);
                      IntList reducedValue = new IntArrayList();
                      for (int iv : itemValues){
                        if (removeSet.contains(iv)){
                          removeSet.remove(iv);
                        }else{
                          reducedValue.add(iv);
                        }
                      }
                      if (removeSet.isEmpty()){
                        form.getPropertyValueIds()[itemPos][propFormPos][id] = reducedValue.toArray(new int[]{});
                        //log.debug("filter set: "+values.toString()+"reduced prop Value: "+Arrays.toString(form.getPropertyValueIds()[itemPos][propFormPos][id]));
                      }
                      id++;
                    }
                  }
                }//values
                
              } //properties
            }
          }
        }
      }
      return form;
  }

  

}
