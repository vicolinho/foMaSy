package de.uni_leipzig.dbs.formRepository.matching.string;

import it.unimi.dsi.fastutil.longs.Long2FloatMap;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.Matcher;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;

public class TrigramMatcher extends Matcher{

	Logger log = Logger.getLogger(getClass());
	@Override
	public Long2FloatMap computeSimilarity(EncodedEntityStructure source,
			EncodedEntityStructure target, AggregationFunction function,
			float threshold, Pruning pruning) {
		int numberOfProcessros = Runtime.getRuntime().availableProcessors();
		int threadNumber;
		if (source.getObjIds().size()<150&&target.getObjIds().size()<150) {
			threadNumber = 4;
		} else {
			threadNumber = Math.max(numberOfProcessros, 16);
		}
		
		int[] propertiesSrc =  new int[this.getPropertyIds1().size()];
		int id=0;
		for (int  p:this.getPropertyIds1())
			propertiesSrc[id++] = p;
		int[] propertiesTarget =  new int[this.getPropertyIds2().size()];
		id =0;
		for (int  p:this.getPropertyIds2())
			propertiesTarget [id++] = p;
		
		List<TrigramThread> threadList = new ArrayList<TrigramThread>();
		if (source.getObjIds().size()>target.getObjIds().size()) {
			//Domain splitten
			ArrayList<List<Integer>> domObjIDsParts = new ArrayList<List<Integer>>();
			ArrayList<List<int[][][]>> trigrams  =new ArrayList<List<int[][][]>> ();
			for (int i=0;i<threadNumber;i++) {
				domObjIDsParts.add(new ArrayList<Integer>());
				trigrams.add(new ArrayList<int[][][]> ());
			
			}
			
			int srcIndex =0;
			for (int  domainObjID : source.getObjIds().keySet()) {
				domObjIDsParts.get(srcIndex%threadNumber).add(domainObjID);
				trigrams.get(srcIndex%threadNumber).add(source.getTrigramIds()[source.getObjIds().get(domainObjID)]);
				srcIndex++;
			}
			
			Integer [] targetIds = new Integer[target.getObjIds().size()]; 
			int index =0;
			int [][][][] trigramIds = new int [target.getObjIds().size()][][][];
			
			for (int sid : target.getObjIds().keySet()){
				targetIds[index]=sid;
				trigramIds[index] = target.getTrigramIds()[target.getObjIds().get(sid)];
				index++;
			}
			for (int i=0;i<threadNumber;i++) {
				Integer[] partObjIds = domObjIDsParts.get(i).toArray(new Integer[]{});
				int[][][][] trigramParts = trigrams.get(i).toArray(new int[][][][]{});
				TrigramThread tmpThread = new TrigramThread (source, target, partObjIds, targetIds, 
						trigramParts,trigramIds, function, threshold, propertiesSrc, propertiesTarget, pruning);
				threadList.add(tmpThread);
			}
		} else {
			//Range splitten
			ArrayList<List<Integer>> domObjIDsParts = new ArrayList<List<Integer>>();
			ArrayList<List<int[][][]>> trigrams  =new ArrayList<List<int[][][]>> ();
			for (int i=0;i<threadNumber;i++) {
				domObjIDsParts.add(new ArrayList<Integer>());
				trigrams.add(new ArrayList<int[][][]> ());
			
			}
			int indexTarget =0;
			for (int  domainObjID : target.getObjIds().keySet()) {
				domObjIDsParts.get(indexTarget%threadNumber).add(domainObjID);
				trigrams.get(indexTarget%threadNumber).add(target.getTrigramIds()[target.getObjIds().get(domainObjID)]);
				indexTarget++;
				
			}
			Integer [] sourceIds = new Integer[source.getObjIds().size()]; 
			int index =0;
			int [][][][] trigramIds = new int [source.getObjIds().size()][][][];
			for (int sid : source.getObjIds().keySet()){
				sourceIds[index]=sid;
				trigramIds[index] = source.getTrigramIds()[source.getObjIds().get(sid)];  
						index++;
			}
			for (int i=0;i<threadNumber;i++) {
				Integer[] partObjIds = domObjIDsParts.get(i).toArray(new Integer[]{});
				int[][][][] trigramParts = trigrams.get(i).toArray(new int[][][][]{});
				TrigramThread tmpThread = new TrigramThread (source, target, sourceIds, partObjIds, 
						trigramIds,trigramParts, function, threshold, propertiesSrc, propertiesTarget, pruning);
				threadList.add(tmpThread);
			}
		}
		
		for (TrigramThread thread : threadList){
			thread.start();
		}
		
		for (TrigramThread thread: threadList){
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (TrigramThread thread: threadList){
			this.mergeResult(thread.getResult());
		}
		log.debug("all threads ready");
		return this.getResult();
	}

	

	

	@Override
	public Long2FloatMap computeSimilarityByReuse(int[][][] propValues1) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	private float computeSimilarity (int[] trigramSrc, int[] triTarget){
		return 0;
		
	}

}
