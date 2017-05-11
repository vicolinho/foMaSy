package de.uni_leipzig.dbs.entity_resolution.io;

import de.uni_leipzig.dbs.formRepository.dataModel.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 02.05.2017.
 */
public class CSVReader implements Reader{


  static int id =0;

  static int propertyID = 0;
  private  String entityId;
  private String srcID;
  private  String type;

  public CSVReader(String entityId, String type, String srcId){
    this.entityId = entityId;
    this.type = type;
    this.srcID = srcId;
  }
  @Override
  public Map<Integer, EntityStructureVersion> read(InputStream is) throws IOException {
    CSVParser parser = new CSVParser(new InputStreamReader(is),CSVFormat.EXCEL);
    Map<String, Integer> header = parser.getHeaderMap();

    Map<String, GenericProperty> gps = new HashMap<>();
    Map<Integer, Set<GenericEntity>>entityMap = new HashMap<>();
    for (Map.Entry<String, Integer> e: header.entrySet()){
      GenericProperty gp = new GenericProperty(e.getValue(), e.getKey(), null, null);
      gps.put(gp.getName(), gp);
    }
    for (CSVRecord record: parser.getRecords()){

      GenericEntity ge = new GenericEntity(Integer.parseInt(record.get(this.entityId)),
              record.get(entityId),
              type, Integer.parseInt(record.get(srcID)));
      for (Map.Entry<String, Integer>e :header.entrySet()){
        if (!e.equals(entityId)){
          PropertyValue pv = new PropertyValue(propertyID++,record.get(e.getKey()));
          ge.addPropertyValue(gps.get(e.getKey()), pv);
        }
      }
      Set<GenericEntity> entities = entityMap.get(ge.getSrcVersionStructureId());
      if (entities == null){
        entities = new HashSet<>();
        entityMap.put(ge.getSrcVersionStructureId(),entities);
      }
      entities.add(ge);
    }

    Map<Integer,EntityStructureVersion> map = new HashMap<>();
    for (Map.Entry<Integer, Set<GenericEntity>> e:entityMap.entrySet()){
      VersionMetadata vm = new VersionMetadata(e.getKey(),null, null,"testBrainz","test");
      EntityStructureVersion esv = new EntityStructureVersion(vm);
      for (GenericEntity ge: e.getValue()){
        esv.addEntity(ge);
      }
      for (GenericProperty property : gps.values()) {
        esv.addAvailableProperty(property);
      }

    }
    return map;
  }
}
