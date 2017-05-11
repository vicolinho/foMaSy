package de.uni_leipzig.dbs.formRepository.manager;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.api.APIFactory;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;
import de.uni_leipzig.dbs.formRepository.exception.ImportException;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.importer.EntityStructureImporter;

public class FormManagerImpl implements FormManager {

  public void importForm(String file) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, ImportException {
    EntityStructureImporter fm = new EntityStructureImporter();
    fm.importForm(file);

  }

  public void importForm(Map<String, Object> properties) throws InstantiationException, IllegalAccessException, 
  ClassNotFoundException, ImportException {
    EntityStructureImporter fm = new EntityStructureImporter();
    fm.importEntityStructure(properties);

  }

  public void importRelationshipsForVersion (Map<String, Object> properties) throws InstantiationException, 
  IllegalAccessException, ClassNotFoundException, ImportException {
    EntityStructureImporter fm = new EntityStructureImporter();
    fm.importRelsToExistingVersion(properties);
  }

  public EntityStructureVersion getStructureVersion(String name, String type,
      String version) throws VersionNotExistsException, StructureBuildException {
    return APIFactory.getInstance().
        getStructureAPI().getEntityStructureVersion(name, type, version);
    
  }

  public EntityStructureVersion getStructureVersion(String name, String type, String version,
      Set<GenericProperty> usedProps, Set<GenericProperty> optionalProperties)
          throws VersionNotExistsException, StructureBuildException {
    return APIFactory.getInstance().
            getStructureAPI().getEntityStructureVersion(name, type,
            version, usedProps, optionalProperties);

  }

  public EntityStructureVersion getLatestStructureVersion(String name,
      String type) {
    // TODO Auto-generated method stub
    return null;
  }

  public Set<EntityStructureVersion> getStructureVersionsByType(
      Set<String> types) throws VersionNotExistsException,
      StructureBuildException {
    return APIFactory.getInstance().getStructureAPI().getEntityStructureVersionsByType(types);
    
  }

  public EntitySet<GenericEntity> getEntitiesById(Set<Integer> ids) throws EntityAPIException {
    // TODO Auto-generated method stub
    return APIFactory.getInstance().getEntityAPI().getEntitiesById(ids);
  }
  

  public EntitySet<GenericEntity> getEntitiesByIdWithProperties(Set<Integer> ids) throws EntityAPIException {
    // TODO Auto-generated method stub
    return APIFactory.getInstance().getEntityAPI().getEntityWithProperties(ids);
  }
  

  public EntitySet<GenericEntity> getEntitiesByPropertyWithProperties(Set<String> values, VersionMetadata vm,Set<GenericProperty> gps) throws EntityAPIException {
    // TODO Auto-generated method stub
    return APIFactory.getInstance().getEntityAPI().getEntityWithPropertiesByProperty(values, vm, gps);
  }

  @Override
  public Map<String, Integer> getIdMapping(String name, String date, String type) {
    return  APIFactory.getInstance().getStructureAPI().getIdMapping(name, date, type);
  }

  public VersionMetadata getMetadata(String name, String type, String version)
      throws VersionNotExistsException {
    // TODO Auto-generated method stub
    return APIFactory.getInstance().getStructureAPI().getMetadata(name,type,version);
  }
  
  
  public Set<GenericProperty> getAvailableProperties (String name, String from, String type){
    return APIFactory.getInstance().getStructureAPI().getAvailableProperties(name, from, type);
  }

  public EntitySet<GenericEntity> getEntityWithPropertiesByAccession(
      Set<String> accs) throws EntityAPIException {
    // TODO Auto-generated method stub
    return APIFactory.getInstance().getEntityAPI().getEntityWithPropertiesByAccession(accs);
  }
  

  

}
