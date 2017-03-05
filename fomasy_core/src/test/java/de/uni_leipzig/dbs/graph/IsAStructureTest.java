package de.uni_leipzig.dbs.graph;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Edge;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;
import de.uni_leipzig.dbs.formRepository.exception.GraphAPIException;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import edu.uci.ics.jung.graph.DirectedGraph;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by christen on 21.02.2017.
 */
public class IsAStructureTest {


  String date = "2014-01-01";
  String name="umls2014AB";
  String type ="ontology";
  DirectedGraph <Node, Edge> isAGraph;
  EntityStructureVersion umls;
  Logger log;


  @Before
  public void init(){
    PropertyConfigurator.configure("log4j.properties");
    log = Logger.getLogger(IsAStructureTest.class);
    FormRepository rep = new FormRepositoryImpl();
    try {
      rep.initialize("fms.ini");
      umls = rep.getFormManager().getStructureVersion(name, type, date);
      EntitySet<GenericEntity> roots = new GenericEntitySet();
      roots.addEntity(umls.getEntity("C0005961"));
      isAGraph = rep.getGraphManager().getIsAConcepts(roots, umls.getMetadata(), 2);
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
    } catch (GraphAPIException e) {
      e.printStackTrace();
    }

  }

  @Test
  public void testIsAStructure(){
    Assert.assertEquals(16, isAGraph.getVertexCount());
    boolean found =false;
    for (Node n: isAGraph.getVertices()){
      if (n.getDepth() ==1){
        if (umls.getEntity(n.getId()).getAccession().equals("C0029216")){
          found =true;
        }
      }
    }
    Assert.assertTrue(found);
  }
}
