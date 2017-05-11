package de.uni_leipzig.dbs.formRepository.importer.contextGraph;

import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Edge;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.EdgeImpl;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.NodeImpl;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by christen on 08.05.2017.
 */
public class CSVGraphReader {

  static Logger log =Logger.getLogger(CSVGraphReader.class);

  public static DirectedGraph<Node, Edge> getGraphFromFile(InputStream inputStream, char delimiter, boolean withHeader,
      Map<String, Integer> cui2idMap)
          throws IOException {
    DirectedGraph<Node, Edge> graph = new DirectedSparseGraph<>();
    InputStreamReader isr = new InputStreamReader(inputStream);
    CSVParser parser;
    if (withHeader)
      parser= new CSVParser(isr, CSVFormat.EXCEL.withDelimiter(delimiter).withHeader("CUI1", "RELA", "CUI2")
              .withSkipHeaderRecord(true));
    else
      parser = new CSVParser(isr, CSVFormat.EXCEL.withDelimiter(delimiter));
    List<CSVRecord> records = parser.getRecords();
    Map<Integer,Node> ids = new HashMap<>();
    for(CSVRecord r: records){
      int id1 = (cui2idMap.get(r.get(0))!=null)?cui2idMap.get(r.get(0)):-2;
      int id2 = (cui2idMap.get(r.get(2))!=null)?cui2idMap.get(r.get(2)):-2;
      if (id1!=-2 && id2!=-2){
        if (!ids.containsKey(id1)) {
          Node n1 = new NodeImpl(id1);
          ids.put(id1, n1);
          graph.addVertex(n1);

        }
        if (!ids.containsKey(id2)) {
          Node n2 = new NodeImpl(id2);
          ids.put(id2, n2);
          graph.addVertex(n2);
        }


        String type = (r.get(1) != null) ? r.get(1) : "N/A";
        Edge ei = new EdgeImpl(id1, id2, type);
        ei.setWeight(0.5f);
        graph.addEdge(ei, ids.get(id1), ids.get(id2));

      }
    }
    return graph;
  }

}
