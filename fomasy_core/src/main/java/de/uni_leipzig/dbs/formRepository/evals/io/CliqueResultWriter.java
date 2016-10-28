package de.uni_leipzig.dbs.formRepository.evals.io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.matching.holistic.data.TokenCluster;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;

public class CliqueResultWriter {

	
	public void writeResult (String fileName, Collection<TokenCluster> collection, Set<EntityStructureVersion> forms ){
		try {
			FileWriter fw = new FileWriter(fileName+".csv");
			TreeMap<Integer,Integer> coCount = new TreeMap<Integer,Integer>();
			fw.append("#cooccurrence\tcount"+System.getProperty("line.separator"));
			for (TokenCluster c: collection){
				Integer count = coCount.get(c.getTokenIds().size());
				if(count ==null){
					coCount.put(c.getTokenIds().size(), 1);
				}else {
					coCount.put(c.getTokenIds().size(), count+1);
				}
			}
			for (Entry<Integer,Integer> coC:coCount.entrySet()){
				fw.append(coC.getKey()+"\t"+coC.getValue()+System.getProperty("line.separator"));
			}
			fw.close();
			
			fw = new FileWriter(fileName+"detail.csv");
			fw.append("tokens\tnumber of items"+System.getProperty("line.separator"));
			int count=0;
			for (TokenCluster c: collection){
				count =0;
				if (c.getClusterId() ==313){
				System.out.println(" "+EncodingManager.getInstance().getDictionary().get("measurable")+c.getTokenIds().size());
				}
				for(int tid: c.getTokenIds()){
					
					if (count!=c.getTokenIds().size()-1)
						fw.append(EncodingManager.getInstance().getReverseDict().get(tid)+", ");
					else
						fw.append(EncodingManager.getInstance().getReverseDict().get(tid)+"\t");
					
					count++;
				}
				fw.append(c.getAggregateTFIDF()+System.getProperty("line.separator"));
			}
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
