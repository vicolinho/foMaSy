package de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;













import java.util.Set;

import org.apache.log4j.Logger;

import cern.colt.Arrays;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.PropertyValue;
import de.uni_leipzig.dbs.formRepository.dataModel.StringPropertyValueSet;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;


public class EncodingManager {

	Logger log = Logger.getLogger(getClass());
	
	private static  EncodingManager instance ;
	public static final int NUMBER_OF_UNIGRAMS = 128*128*128;
	public static final int ALPHABET_SIZE = 128;
	int tokenId ;
	private HashMap<String,Integer> dictionary;
	

	private HashMap<Integer, String> reverseDict;
	


	private EncodingManager (){
		dictionary = new HashMap<String,Integer>();
		setReverseDict(new HashMap<Integer,String>());
		tokenId=0;
	}
	
	
	
	public static EncodingManager getInstance(){
		if (instance ==null){
			instance = new EncodingManager();
		}
		return instance;
	}
	
	public EncodedEntityStructure encoding(EntityStructureVersion esv , boolean isTrigramEncoding){
		int entityIndex = 0;
		//int[] objIds = new int[esv.getNumberOfEntities()];
		Map<Integer,String> typeMap = new HashMap<Integer,String> ();
		EncodedEntityStructure ees = new EncodedEntityStructure(esv.getStructureId());
		Int2IntMap objIds = new Int2IntOpenHashMap();
		Map<GenericProperty ,Integer> propertyPosition = this.encodeProperties(esv);
		
		int [][][][] propertyValueIds = new int[esv.getNumberOfEntities()][][][];
		int [][][][] trigramIds = new int[esv.getNumberOfEntities()][][][];
		for (GenericEntity ge : esv.getEntities()) {
			typeMap.put(ge.getId(), ge.getType());
			objIds.put(ge.getId(), entityIndex);
			propertyValueIds = this.encodePropertyValues(propertyValueIds, entityIndex, ge, propertyPosition);
			trigramIds = this.encodeTrigrams(trigramIds, entityIndex, ge, propertyPosition);
			entityIndex++;
		}
		ees.setTypeMap(typeMap);
		ees.setObjIds(objIds);
		ees.setPropertyPosition(propertyPosition);
		ees.setPropertyValueIds(propertyValueIds);
		ees.setTrigramIds(trigramIds);
		return ees;
	}
	
	public EncodedEntityStructure encoding(EntitySet<GenericEntity> esv , boolean isTrigramEncoding){
		int entityIndex = 0;
		//int[] objIds = new int[esv.getNumberOfEntities()];
		Map<Integer,String> typeMap = new HashMap<> ();
		EncodedEntityStructure ees = new EncodedEntityStructure(-1);
		Int2IntMap objIds = new Int2IntOpenHashMap();
		Map<GenericProperty ,Integer> propertyPosition = this.encodeProperties(esv);
		
		int [][][][] propertyValueIds = new int[esv.getSize()][][][];
		int [][][][] trigramIds = new int[esv.getSize()][][][];
		for (GenericEntity ge : esv) {
			typeMap.put(ge.getId(), ge.getType());
			objIds.put(ge.getId(), entityIndex);
			propertyValueIds = this.encodePropertyValues(propertyValueIds, entityIndex, ge, propertyPosition);
			trigramIds = this.encodeTrigrams(trigramIds, entityIndex, ge, propertyPosition);
			entityIndex++;
		}
		ees.setTypeMap(typeMap);
		ees.setObjIds(objIds);
		ees.setPropertyPosition(propertyPosition);
		ees.setPropertyValueIds(propertyValueIds);
		ees.setTrigramIds(trigramIds);
		return ees;
	}

	public EncodedEntityStructure encoding(EntitySet<GenericEntity> esv , Set<String> types){
		int entityIndex = 0;
		//int[] objIds = new int[esv.getNumberOfEntities()];
		Map<Integer,String> typeMap = new HashMap<> ();
		EncodedEntityStructure ees = new EncodedEntityStructure(-1);
		Int2IntMap objIds = new Int2IntOpenHashMap();
		Map<GenericProperty ,Integer> propertyPosition = this.encodeProperties(esv);
		int count = 0;
		for (GenericEntity ge: esv){
			if (types.contains(ge.getType())){
				count++;
			}
		}

		int [][][][] propertyValueIds = new int[count][][][];
		int [][][][] trigramIds = new int[count][][][];
		for (GenericEntity ge : esv) {
			if (types.contains(ge.getType())) {
				typeMap.put(ge.getId(), ge.getType());
				objIds.put(ge.getId(), entityIndex);
				propertyValueIds = this.encodePropertyValues(propertyValueIds, entityIndex, ge, propertyPosition);
				trigramIds = this.encodeTrigrams(trigramIds, entityIndex, ge, propertyPosition);
				entityIndex++;
			}
		}
		ees.setTypeMap(typeMap);
		ees.setObjIds(objIds);
		ees.setPropertyPosition(propertyPosition);
		ees.setPropertyValueIds(propertyValueIds);
		ees.setTrigramIds(trigramIds);
		return ees;
	}
	
	


	public EncodedEntityStructure encoding(EntityStructureVersion esv , Set<String> entTypes, boolean isTrigramEncoding){
		int entityIndex = 0;
		//int[] objIds = new int[esv.getNumberOfEntities()];
		EncodedEntityStructure ees = new EncodedEntityStructure(esv.getStructureId());
		Map<Integer,String> typeMap = new HashMap<Integer,String> ();
		Int2IntMap objIds = new Int2IntOpenHashMap();
		Map<GenericProperty ,Integer> propertyPosition = this.encodeProperties(esv);
		int count =0;
		for (String type : entTypes){
			count+= esv.getTypeCount().get(type);
		}
		int [][][][] propertyValueIds = new int[count][][][];
		int [][][][] trigramIds = new int[count][][][];
		for (GenericEntity ge : esv.getEntities()) {
			if (entTypes.contains(ge.getType())){
				objIds.put(ge.getId(), entityIndex);
				typeMap.put(ge.getId(), ge.getType());
				propertyValueIds = this.encodePropertyValues(propertyValueIds, entityIndex, ge, propertyPosition);
				trigramIds = this.encodeTrigrams(trigramIds, entityIndex, ge, propertyPosition);
				entityIndex++;
			}
		}
		
		ees.setObjIds(objIds);
		ees.setTypeMap(typeMap);
		ees.setPropertyPosition(propertyPosition);
		ees.setPropertyValueIds(propertyValueIds);
		ees.setTrigramIds(trigramIds);
		return ees;
	}
		
	/**
	 * 
	 * @param propertyValueIds [entityId][property pos][property value][token]
	 * @param entityIndex
	 * @param ge
	 * @param propertyMap
	 * @return
	 */
	private int[][][][] encodePropertyValues (int[][][][] propertyValueIds, int entityIndex, GenericEntity ge,
																						Map<GenericProperty,Integer> propertyMap){
		int[][][] propertyValues = new int[propertyMap.size()][][];
		propertyValueIds[entityIndex] = propertyValues;
		for (Entry<GenericProperty,Integer> e: propertyMap.entrySet()){
			List<PropertyValue> propertyValueSet = ge.getValues(e.getKey());
			int propPos = e.getValue();
			Collection <PropertyValue> set = propertyValueSet;
			propertyValues[propPos] = new int[set.size()][];
			int pvIndex =0;
			for (PropertyValue pv: propertyValueSet){
				String [] tokens = this.getTokens(pv.getValue().trim());
				propertyValues[propPos][pvIndex] = new int[tokens.length];
				int tIndex =0;
				IntList stringList = new IntArrayList();
				for (String t : tokens){
					String v = t.trim();
					if (v.length()!=0)
						stringList.add(this.checkToken(v));
				}
				propertyValues[propPos][pvIndex] = stringList.toArray(new int[]{});
				pvIndex++;
			}
		}
		
		return propertyValueIds;
	}
	
	private int[][][][] encodeTrigrams (int[][][][] trigramIds, int entityIndex, GenericEntity ge,
																			Map<GenericProperty,Integer> propertyMap){
		int[][][] propertyValues = new int[propertyMap.size()][][];
		trigramIds[entityIndex] = propertyValues;
		for (Entry<GenericProperty,Integer> e: propertyMap.entrySet()){
			List<PropertyValue> propertyValueSet = ge.getValues(e.getKey());
			int propPos = e.getValue();
			Collection <PropertyValue> set = propertyValueSet;
			propertyValues[propPos] = new int[set.size()][];
			int pvIndex =0;
			for (PropertyValue pv: propertyValueSet){
				String t = pv.getValue().trim();
				IntList triIds = new IntArrayList();
				if (!t.isEmpty()) {
					List<String> trigrams = this.generateTrigrams(t, 3);
					for (String tri : trigrams) {
						triIds.add(this.hashCode(tri));
					}
					Collections.sort(triIds);
				}
				propertyValues[propPos][pvIndex] = triIds.toIntArray();
				pvIndex++;
			}
		}
		return trigramIds;
		
	}
	
	public EncodedEntityStructure  getSubset(EncodedEntityStructure ees, Set <Integer> entIds){
		EncodedEntityStructure extract = new EncodedEntityStructure (ees.getStructureId());
		Int2IntMap objIds = new Int2IntOpenHashMap();
		Map<GenericProperty ,Integer> propertyPosition = ees.getPropertyPosition();
		ees.setTypeMap(ees.getTypeMap());
		int [][][][] propertyValueIds = new int[entIds.size()][][][];
		int [][][][] trigramIds = new int[entIds.size()][][][];
		int entIndex=0;
		for (int id : entIds){
			objIds.put(id, entIndex);
			propertyValueIds [entIndex] = ees.getPropertyValueIds()[ees.getObjIds().get(id)];
			trigramIds [entIndex] = ees.getTrigramIds()[ees.getObjIds().get(id)];
			entIndex++;
		}
		extract.setObjIds(objIds);
		extract.setPropertyPosition(propertyPosition);
		extract.setPropertyValueIds(propertyValueIds);
		extract.setTrigramIds(trigramIds);
		return extract;
		
	}
	
	private int[][] generateNGramId(

			Map<String, Integer> ngrams) {

		int[][] result = new int[2][];
		result[0] = new int[ngrams.size()];
		result[1] = new int[ngrams.size()];
		
		
	    TreeMap<Integer,Integer> tmpResult = new TreeMap<Integer,Integer>();
	    
	    for (String ngram : ngrams.keySet())
	    {
	    	//grams.add(ngram);
	    	//codes.add(grams.hashCode());
	        tmpResult.put(ngram.hashCode(), ngrams.get(ngram));
	    }
	     
	    int i=0;
	    for (Entry<Integer,Integer> entry : tmpResult.entrySet()) {
	    	result[0][i] = entry.getKey();
	    	result[1][i++] = entry.getValue();
	    }
	    
	    return result;
	}
	
	private List<String> generateTrigrams(String t, int gramlength){
		List <String> trigrams = new ArrayList<String>();			
			t = "##"+t+"##";	
		for (int i=0;i<=t.length()-gramlength;i++){
			String trigram = t.substring(i,i+gramlength);
			trigrams.add(trigram);
		}
		return trigrams;
	}
	
	private String[] getTokens (String value){
		String[] tokens = value.split("[^A-Za-z0-9]");
		return tokens;
	}

	private Map<GenericProperty, Integer> encodeProperties(
			EntityStructureVersion esv) {
		int propertyPos =0; 
		HashMap<GenericProperty, Integer> propertyPosMap = new HashMap<GenericProperty,Integer>();
		for (GenericProperty gp: esv.getAvailableProperties()){
			propertyPosMap.put(gp, propertyPos++);
		}
		return propertyPosMap;
	}
	
	private Map<GenericProperty, Integer> encodeProperties(
			EntitySet<GenericEntity> esv) {
		int propertyPos =0;
		HashMap<GenericProperty, Integer> propertyPosMap = new HashMap<GenericProperty,Integer>();
		for (GenericEntity ge : esv){
			for (GenericProperty gp : ge.getProperties()){
				if (!propertyPosMap.containsKey(gp)){
					propertyPosMap.put(gp,propertyPos++);
				}
			}
		}
		return propertyPosMap;
	}

	
	public int checkToken (String t){
		if (dictionary.containsKey(t)){
			return dictionary.get(t);
		}else{
			dictionary.put(t, tokenId);
			this.reverseDict.put(tokenId, t);
			tokenId++;
			return (tokenId-1);
		}
	}
	
	
	public HashMap<String, Integer> getDictionary() {
		return dictionary;
	}

	public void setDictionary(HashMap<String, Integer> dictionary) {
		this.dictionary = dictionary;
	}
	
	public HashMap<Integer, String> getReverseDict() {
		return reverseDict;
	}



	public void setReverseDict(HashMap<Integer, String> reverseDict) {
		this.reverseDict = reverseDict;
	}



	public int hashCode(String trigram) {
		long h = 3;
	    int len = trigram.length();
	    for (int i = 0; i < len; i++) {
	        h += trigram.charAt(i)*Math.pow(ALPHABET_SIZE, i);
	    }
	    h = h%NUMBER_OF_UNIGRAMS;
		return (int) h;
	}

}
