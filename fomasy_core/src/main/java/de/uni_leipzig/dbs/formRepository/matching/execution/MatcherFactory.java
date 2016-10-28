package de.uni_leipzig.dbs.formRepository.matching.execution;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.exception.UnknownMatcherException;
import de.uni_leipzig.dbs.formRepository.matching.Matcher;
/**
 * This class implements a factory for a {@linkplain de.uni_leipzig.dbs.formRepository.matching.Matcher} that calculates the Cartesian product
 * between a source and a target {@linkplain de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure}.
 * A matcher have to be registered before it can be used.
 * @author christen
 *
 */
public class MatcherFactory {

	
	private static MatcherFactory instance;
	private Map<String, String> matcherClassMapping;
	
	
	MatcherFactory (){
		this.matcherClassMapping = new HashMap<String,String>();
		this.registerMatcher(RegisteredMatcher.TRIGRAM_MATCHER, "de.uni_leipzig.dbs.formRepository.matching.string.TrigramMatcher");
		this.registerMatcher(RegisteredMatcher.TFIDF_MATCHER, "de.uni_leipzig.dbs.formRepository.matching.token.TFIDFMatcher");
		this.registerMatcher(RegisteredMatcher.TFIDF_WINDOW_MATCHER, "de.uni_leipzig.dbs.formRepository.matching.token.TFIDFWindowMatcher");
		this.registerMatcher(RegisteredMatcher.LCS_MATCHER, "de.uni_leipzig.dbs.formRepository.matching.token.LCSMatcher");
		this.registerMatcher(RegisteredMatcher.SOFT_TFIDF_WND_MATCHER, "de.uni_leipzig.dbs.formRepository.matching.token.SoftTFIDFMatcher");
	}
	
	public void registerMatcher (String name, String classLocation){
		this.matcherClassMapping.put(name, classLocation);
	}
	
	public Matcher getRegisteredMatcher(String name, 
			Map<GenericProperty, Integer> srcProp, Map<GenericProperty, Integer> targetProperties,
			Set<GenericProperty> srcComparingProperties, Set<GenericProperty> targetComparingProps) throws UnknownMatcherException, ClassNotFoundException{
		String className = this.matcherClassMapping.get(name);
		if (className == null){
			throw new UnknownMatcherException("not registered Matcher:"+name);
		}
		try {
			Matcher matcher = (Matcher) Class.forName(className).newInstance();
			matcher.setComparingProperties(srcProp, targetProperties, srcComparingProperties, targetComparingProps);
			return matcher;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	
	public static MatcherFactory getInstance(){
		if (instance ==null){
			instance = new MatcherFactory();
		}
		return instance;
	}
	
}
