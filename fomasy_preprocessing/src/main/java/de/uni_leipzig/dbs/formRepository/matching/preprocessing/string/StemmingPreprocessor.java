package de.uni_leipzig.dbs.formRepository.matching.preprocessing.string;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.PropertyValue;
import de.uni_leipzig.dbs.formRepository.dataModel.StringPropertyValueSet;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.Preprocessor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.string.stemmer.PorterStemmer;

public class StemmingPreprocessor implements Preprocessor {

	Logger log = Logger.getLogger(getClass());
	Pattern wordPatter = Pattern.compile("([A-Za-z])+");
	@Override
	public EntityStructureVersion preprocess(EntityStructureVersion esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
		PorterStemmer stemmer = new PorterStemmer();
		
		for (GenericEntity ge: esv.getEntities()){
			for (PreprocessProperty pp: propList){
				StringPropertyValueSet values = ge.getPropertyValueSet(pp.getName(), pp.getLang(),pp.getScope());
				for (PropertyValue pv: values.getCollection()){
					StringBuffer sb = new StringBuffer();
					String value =pv.getValue();
					String stemmedValue =pv.getValue();
					Matcher m = wordPatter.matcher(value);
					while (m.find()){
						String word = m.group();
						stemmer = new PorterStemmer();
						stemmer.add(word.toCharArray(), word.length());
						stemmer.stem();
						stemmedValue = stemmedValue.replace(word, stemmer.toString());
					}
					pv.setValue(stemmedValue);
					
				}
				ge.changePropertyValues(values);
			}
		}
		
				return esv;
		// TODO Auto-generated method stub

	}
	@Override
	public EntitySet<GenericEntity> preprocess(EntitySet<GenericEntity> esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
		// TODO Auto-generated method stub
PorterStemmer stemmer = new PorterStemmer();
		
		for (GenericEntity ge: esv){
			for (PreprocessProperty pp: propList){
				StringPropertyValueSet values = ge.getPropertyValueSet(pp.getName(), pp.getLang(),pp.getScope());
				for (PropertyValue pv: values.getCollection()){
					StringBuffer sb = new StringBuffer();
					String value =pv.getValue();
					String stemmedValue =pv.getValue();
					Matcher m = wordPatter.matcher(value);
					while (m.find()){
						String word = m.group();
						stemmer = new PorterStemmer();
						stemmer.add(word.toCharArray(), word.length());
						stemmer.stem();
						stemmedValue = stemmedValue.replace(word, stemmer.toString());
					}
					pv.setValue(stemmedValue);
					
				}
				ge.changePropertyValues(values);
			}
		}
		
				return esv;
	}

}
