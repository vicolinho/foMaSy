package de.uni_leipzig.dbs.creation;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 08.12.2016.
 */
public class MappingCreationExample {

  public static void main (String[] args){
    FormRepository rep = new FormRepositoryImpl();
    // initialize with ini file
    try {
      rep.initialize(args[0]);
      EntityStructureVersion umls = rep.getFormManager().getStructureVersion("umls2014AB", "ontology", "2014-01-01");

      Set<String> types = new HashSet<String>();
      types.add("eligibility form");

      Set<EntityStructureVersion> set = rep.getFormManager().getStructureVersionsByType(types);
      // contain all items of all eligibility forms
      Map<Integer,GenericEntity> unifiedItemMap  = new HashMap<Integer, GenericEntity>();

      for (EntityStructureVersion esv: set){
        for (GenericEntity ge: esv.getEntities()){
          unifiedItemMap.put(ge.getId(), ge);
        }
      }
      // your annotation mapping
      AnnotationMapping mapping = new AnnotationMapping();
      //id of your question
      int id =  1319101;

      // get the concept from map
      GenericEntity itemEntity = unifiedItemMap.get(id);

      GenericEntity conceptEntity = umls.getEntity("C0014822");

      //your new annotation for the certain question
      // format<question id, concept id, question accession, concept accession, confidence, is verified>
      EntityAnnotation ea = new EntityAnnotation(itemEntity.getId(), conceptEntity.getId(), itemEntity.getAccession(),
              conceptEntity.getAccession(), 1, true);
      //add annotation
      mapping.addAnnotation(ea);
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InitializationException e) {
      e.printStackTrace();
    } catch (StructureBuildException e) {
      e.printStackTrace();
    } catch (VersionNotExistsException e) {
      e.printStackTrace();
    }

  }
}
