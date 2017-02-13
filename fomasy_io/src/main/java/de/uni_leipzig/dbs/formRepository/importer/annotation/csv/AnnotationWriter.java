package de.uni_leipzig.dbs.formRepository.importer.annotation.csv;

import de.uni_leipzig.dbs.formRepository.dataModel.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by christen on 12.12.2016.
 */
public class AnnotationWriter {

  public void writeAnnotation (EntityStructureVersion srcStructure, EntityStructureVersion targetStructure,
                               AnnotationMapping am, Set<GenericProperty> srcShownProps,
                               Set<GenericProperty> targetShownProps, String file) throws IOException {
    FileWriter fw = new FileWriter(file);
    Map<Integer, Set<EntityAnnotation>> groupPerItem = this.getAnnosPerItem(am);
    for (Map.Entry<Integer,Set<EntityAnnotation>> e: groupPerItem.entrySet()){
      Set<String> srcValues = this.getProperties(srcShownProps, srcStructure.getEntity(e.getKey()));
      for (EntityAnnotation ea: e.getValue()){
        Set<String> targetValues = this.getProperties(targetShownProps, targetStructure.getEntity(ea.getTargetId()));
        fw.append(this.getAnnotationString(ea.getSrcAccession(), ea.getTargetAccession(), srcValues, targetValues, ea.getSim()));
      }
    }
    fw.close();
  }

  private String getAnnotationString(String srcAcc, String targetAcc, Set<String> srcValues,
      Set<String> targetValues, float sim){
    Iterator<String> srcIter = srcValues.iterator();
    Iterator<String> targetIter = targetValues.iterator();
    StringBuilder sb = new StringBuilder();
    boolean firstLine = true;
    while(true){
      if (firstLine){
        String srcPv = srcIter.next();
        String targetPv = targetIter.next();
        sb.append(srcAcc+"\t"+srcPv+"\t"+targetAcc+"\t"+targetPv+"\t"+sim+System.getProperty("line.separator"));
        firstLine = false;
      }
      if (srcIter.hasNext()&&targetIter.hasNext()){
        String srcPv = srcIter.next();
        String targetPv = targetIter.next();
        sb.append(""+"\t"+srcPv+"\t"+""+"\t"+targetPv+System.getProperty("line.separator"));
      }else if (srcIter.hasNext()){
        String srcPv = srcIter.next();
        sb.append(""+"\t"+srcPv+"\t"+""+"\t"+""+System.getProperty("line.separator"));
      }else if (targetIter.hasNext()){
        String targetPv = targetIter.next();
        sb.append(""+"\t"+""+"\t"+""+"\t"+targetPv+System.getProperty("line.separator"));
      }else{
        break;
      }
    }
    return sb.toString();
  }

  private Set<String> getProperties (Set<GenericProperty> genericPropertySet, GenericEntity ge){
    Set<String> pvs = new HashSet<String>();
    for (GenericProperty p: genericPropertySet){
      if (ge.getPropertyValues(p)!=null)
      pvs.addAll(ge.getPropertyValues(p));
    }
    return pvs;
  }

  private Map<Integer, Set<EntityAnnotation>> getAnnosPerItem(AnnotationMapping am){
    Map<Integer, Set<EntityAnnotation>> map = new HashMap<Integer, Set<EntityAnnotation>>();
    for (EntityAnnotation ea: am.getAnnotations()){
      Set<EntityAnnotation> annos = map.get(ea.getSrcId());
      if (annos == null){
        annos = new HashSet<EntityAnnotation>();
        map.put(ea.getSrcId(),annos);
      }
      annos.add(ea);
    }
    return map;
  }
}
