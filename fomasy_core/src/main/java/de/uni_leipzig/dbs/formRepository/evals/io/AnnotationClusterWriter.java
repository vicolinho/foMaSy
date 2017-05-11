package de.uni_leipzig.dbs.formRepository.evals.io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationCluster;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;

public class AnnotationClusterWriter {

  public void writeClusterWithRepresentants (Map<GenericEntity,AnnotationCluster> cluster, String fileName) throws IOException{
    FileWriter fw = new FileWriter(fileName);
    fw.append("cluster_id  concept_syns  representant  questions");
    for (Entry <GenericEntity,AnnotationCluster> e:cluster.entrySet()){
      List<String> syns = e.getKey().getPropertyValues("synonym", "EN", null);
      Set<String> representants = e.getValue().getRepresentants();
      List <String> repList = new ArrayList<String>(representants);
      EntitySet <GenericEntity> elements = e.getValue().getElements();
      int size = (representants.size()<elements.getSize())?elements.getSize():representants.size();
      List<GenericEntity> listEnt = new ArrayList<GenericEntity>(elements.getCollection());
      for (int i =0;i<size;i++){
        fw.append(e.getKey().getAccession()+";");
        if (i<syns.size()){
          fw.append(syns.get(i)+";");
        }else fw.append(";");
        if (i<representants.size()){
          fw.append(repList.get(i)+";");
        }else fw.append(";");
        if (i<elements.getSize()){
          List<String> qu = listEnt.get(i).getPropertyValues("question", "EN", null);
          fw.append(qu.get(0)+System.getProperty("line.separator"));
        }else {
          fw.append(System.getProperty("line.separator"));
        }
      }
      fw.append(System.getProperty("line.separator"));
    }
    fw.close();
  }
}
