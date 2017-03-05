package de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.*;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.Preprocessor;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

public class POSBasedExtractingPreprocessor implements Preprocessor {

	
	
	
	public static final String POS_MODEL = "posModelPath";
	public static final String FILTER_TYPES = "filterTypes";
	
	private  static LexicalizedParser lp;

	public EntityStructureVersion preprocess(EntityStructureVersion esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
			StringBuilder sb = new StringBuilder();
				Set <String> filterTags = (Set<String>) externalSources.get(FILTER_TYPES);
				String grammar = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
			    String[] options = { "-maxLength", "80", "-retainTmpSubcategories" };
			    if (lp==null)
			    	lp = LexicalizedParser.loadModel(grammar, options);
			    for (GenericEntity ge: esv.getEntities()){
			    	for (PreprocessProperty pp: propList){
							Set<GenericProperty> gps = ge.getGenericProperties(pp.getName(), pp.getLang(),pp.getScope());
							for (GenericProperty gp : gps) {

								List<PropertyValue> values = ge.getValues(gp);
								for (PropertyValue pv : values) {
									String value = pv.getValue();
									List<? extends HasWord> list = lp.tokenize(value);
									Tree tree = lp.parse(list);

									for (TaggedWord tw : tree.taggedYield()) {
										if (filterTags.contains(tw.tag())) {
											sb.append(tw.value() + " ");
										}
									}

									if (sb.length() > 0) {
										String newValue = sb.toString().trim();
										newValue = newValue.replaceAll("\\s+", " ");
										pv.setValue(newValue);
										sb.delete(0, sb.length() - 1);
									}
								}
								ge.changePropertyValues(gp, values);
							}
			    	}
			    }
			    return esv;

	}

	public EntitySet<GenericEntity> preprocess(EntitySet<GenericEntity> esv,
			List<PreprocessProperty> propList,
			Map<String, Object> externalSources) {
		StringBuilder sb = new StringBuilder();
		Set <String> filterTags = (Set<String>) externalSources.get(FILTER_TYPES);
		String grammar = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	    String[] options = { "-maxLength", "80", "-retainTmpSubcategories" };
	    if (lp==null)
	    	lp = LexicalizedParser.loadModel(grammar, options);
	    for (GenericEntity ge: esv){
	    	for (PreprocessProperty pp: propList){
					Set<GenericProperty> gps = ge.getGenericProperties(pp.getName(), pp.getLang(),pp.getScope());
					for (GenericProperty gp : gps) {

						List<PropertyValue> values = ge.getValues(gp);
						for (PropertyValue pv : values) {
							String value = pv.getValue();
							List<? extends HasWord> list = lp.tokenize(value);
							Tree tree = lp.parse(list);

							for (TaggedWord tw : tree.taggedYield()) {
								if (filterTags.contains(tw.tag())) {
									sb.append(tw.value() + " ");
								}
							}

							if (sb.length() > 0) {
								String newValue = sb.toString().trim();
								newValue = newValue.replaceAll("\\s+", " ");
								pv.setValue(newValue);
								sb.delete(0, sb.length() - 1);
							}
						}
						ge.changePropertyValues(gp, values);
					}
	    	}
	    }
		return esv;
	}

}
