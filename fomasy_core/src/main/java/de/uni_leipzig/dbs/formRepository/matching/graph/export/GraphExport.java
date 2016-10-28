package de.uni_leipzig.dbs.formRepository.matching.graph.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Edge;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;

import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.io.GraphMLWriter;

public class GraphExport {

	public GraphExport() {
		// TODO Auto-generated constructor stub
	}

	
	public void writeGraphML(String file, DirectedGraph<Node,Edge> graph,
			final Map<Edge,Float> edgeWeight,final EntityStructureVersion source,final EntityStructureVersion target){
		GraphMLWriter<Node,Edge> writer = new GraphMLWriter<Node,Edge>();
		
		writer.setVertexIDs(new Transformer<Node,String>(){


			public String transform(Node input) {
				// TODO Auto-generated method stub
				return input.hashCode()+"";
			}
			
		});
		writer.addVertexData("label", "label", "", new Transformer<Node,String>() {
			public String transform(Node input) {
				if (input.getType().equals("token")){
					return EncodingManager.getInstance().getReverseDict().get(input.getId());
				}else if (input.getType().equals("item")){
					GenericEntity ge = source.getEntity(input.getId());
					return ge.getAccession();
				}else {
					GenericEntity ge = target.getEntity(input.getId());
					return ge.getAccession();
				}
			}
			
		});
		
		writer.addEdgeData("weight", "", "0", new Transformer<Edge,String>(){

			public String transform(Edge input) {
				// TODO Auto-generated method stub
				return edgeWeight.get(input)+"";
			}
		});
		
		try {
			writer.save(graph, new FileWriter(file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeGraphCSV(String file, DirectedGraph<Node,Edge> graph, Map<Integer,GenericEntity> nodes, String string){
		try {
			FileWriter fw = new FileWriter("edge."+file);
			fw.append("source\ttarget\tweight\tabsoluteCooccurrence\ttotalAnnotations"+System.getProperty("line.separator"));
			for (Edge e:graph.getEdges()){
				Node src = graph.getSource(e);
				Node target = graph.getDest(e);
				fw.append(nodes.get(src.getId()).getAccession()+"\t"+nodes.get(target.getId()).getAccession()
				+"\t"+e.getWeight()+"\t"+e.getCooccurCount()+"\t"+
						e.getTotal()+System.getProperty("line.separator"));	
			}
			fw.close();
			
			FileWriter fw2 = new FileWriter("node."+file);
			fw2.append("id\trepresentative"+System.getProperty("line.separator"));
			for (GenericEntity ge: nodes.values()){
				List<String> values = ge.getPropertyValues(string, null, null);
				String v ="";
				if (values.size()!=0)
					v = values.get(0);
				fw2.append(ge.getAccession()+"\t"+v+System.getProperty("line.separator"));
			}
			fw2.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
	}
	
	public void writeGraphCSV(String file, DirectedGraph<Node,Edge> graph){
		try {
			FileWriter fw = new FileWriter(file+"edge.csv");
			fw.append("source\ttarget\tweight\tabsoluteCooccurrence\ttotalAnnotations\tedgeType"+System.getProperty("line.separator"));
			for (Edge e:graph.getEdges()){
				Node src = graph.getSource(e);
				Node target = graph.getDest(e);
				fw.append(src.getId()+"\t"+target.getId()+"\t"+e.getWeight()+"\t"+e.getCooccurCount()+"\t"+
						e.getTotal()+"\t"+e.getType()+System.getProperty("line.separator"));	
			}
			fw.close();
			
			FileWriter fw2 = new FileWriter(file+"node.csv");
			fw2.append("id\trepresentative"+System.getProperty("line.separator"));
			for (Node  n: graph.getVertices()){
				fw2.append(n.getId()+System.getProperty("line.separator"));
			}
			fw2.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	

}
