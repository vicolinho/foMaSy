package de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.PropertyValue;
import de.uni_leipzig.dbs.formRepository.dataModel.StringPropertyValueSet;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.Preprocessor;

public class StopwordPreprocessor implements Preprocessor {

	public static final String STOP_LIST_PATH ="stopListPath";
	
	public static final String DEFAULT_STOP_LIST = "stop-word-list.csv";
	
	 public Pattern WORD_PATTERN = Pattern.compile("\\b[A-Za-z]{1,}\\b");

	public EntityStructureVersion preprocess(EntityStructureVersion esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
		InputStream path;
		if (externalSources!=null){
			if (externalSources.containsKey(STOP_LIST_PATH)){
				path  = this.getClass().getClassLoader().getResourceAsStream((String) externalSources.get(STOP_LIST_PATH));
			}else{
				path = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_STOP_LIST);
			}
		}else{
			path = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_STOP_LIST);
		}
		try {
			Set<String> stopWords = this.readFile(path);
			StringBuffer sb=new StringBuffer();
			
			int nextStart;
			int currentEnd = 0;
			for (GenericEntity ge: esv.getEntities()){
				
				for (PreprocessProperty pp: propList){
					
					StringPropertyValueSet values = ge.getPropertyValueSet(pp.getName(), pp.getLang(),pp.getScope());
					for (PropertyValue pv: values.getCollection()){
						nextStart = 0;
						String value = pv.getValue();
						Matcher m = WORD_PATTERN.matcher(value);
						/*for (String stopWord: stopWords){
							value = value.replaceAll("\\b"+stopWord+"\\b", "");
						}*/
						
						while (m.find()){
							String sw = m.group();
							if (stopWords.contains(sw)){
								//currentEnd = m.start();
								//sb.append(value.substring(nextStart, currentEnd));
								value= value.replaceAll("\\b"+sw+"\\b", "");
								//nextStart = m.end();
							}
						}
						//if (currentEnd!=0){
						//	sb.append(value.substring(nextStart,value.length()));
						//}
						pv.setValue(value.replaceAll("\\s{2,}", " "));
						//if (sb.length()!=0)
						//	sb.delete(0, sb.length());
						
					}
					ge.changePropertyValues(values);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		return esv;
	}
	
	
	private Set<String> readFile(InputStream file) throws IOException{
		InputStreamReader fr = new InputStreamReader ((file));
		BufferedReader br= new BufferedReader(fr);
		Set<String> stopWords = new HashSet<String>();
		String line ;
		while(br.ready()){
			line = br.readLine();
			for (String sw:line.split(",")){
				stopWords.add(sw.trim());
			}
		}
		br.close();
		return stopWords;
		
	}

	public EntitySet<GenericEntity> preprocess(EntitySet<GenericEntity> esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
		// TODO Auto-generated method stub
		InputStream path;
		if (externalSources!=null){
			if (externalSources.containsKey(STOP_LIST_PATH)){
				path  = this.getClass().getClassLoader().getResourceAsStream((String) externalSources.get(STOP_LIST_PATH));
			}else{
				path = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_STOP_LIST);
			}
		}else{
			path = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_STOP_LIST);
		}
		try {
			Set<String> stopWords = this.readFile(path);
			StringBuffer sb=new StringBuffer();
			
			int nextStart;
			int currentEnd = 0;
			for (GenericEntity ge: esv){
				
				for (PreprocessProperty pp: propList){
					
					StringPropertyValueSet values = ge.getPropertyValueSet(pp.getName(), pp.getLang(),pp.getScope());
					for (PropertyValue pv: values.getCollection()){
						nextStart = 0;
						String value = pv.getValue();
						Matcher m = WORD_PATTERN.matcher(value);
						/*for (String stopWord: stopWords){
							value = value.replaceAll("\\b"+stopWord+"\\b", "");
						}*/
						
						while (m.find()){
							String sw = m.group();
							if (stopWords.contains(sw)){
								//currentEnd = m.start();
								//sb.append(value.substring(nextStart, currentEnd));
								value= value.replaceAll("\\b"+sw+"\\b", "");
								//nextStart = m.end();
							}
						}
						//if (currentEnd!=0){
						//	sb.append(value.substring(nextStart,value.length()));
						//}
						pv.setValue(value.replaceAll("\\s{2,}", " "));
						//if (sb.length()!=0)
						//	sb.delete(0, sb.length());
						
					}
					ge.changePropertyValues(values);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		return esv;
	}

}
