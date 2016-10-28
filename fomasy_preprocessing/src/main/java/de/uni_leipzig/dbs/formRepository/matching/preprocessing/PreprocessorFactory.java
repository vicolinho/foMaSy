package de.uni_leipzig.dbs.formRepository.matching.preprocessing;

import de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction.AbbreviationFilterPreprocessor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction.EntityFilterPreprocessor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction.NumberFilter;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction.POSBasedExtractingPreprocessor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction.PropertyTokenCountFilter;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction.StopwordPreprocessor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.string.*;


public class PreprocessorFactory {

	
	public PreprocessorFactory (){
		
	}
	
	public Preprocessor getPreprocessor(PreprocessingSteps step){
		switch (step){
		case TO_LOW: return new ToLowPreprocessor();
		case NORMALIZE: return new NormalizePreprocessor();
		case NUMBER_NORMALIZATION: return new NumberFilter();
		case STEMMING : return new StemmingPreprocessor();
		case KEYWORD_EXTRACTION: return new POSBasedExtractingPreprocessor();
		case STOPWORD_EXTRACTION: return new StopwordPreprocessor();
		case LENGTH_FILTER : return new LengthFilterPreprocessor();
		case ABBREVIATION_FILTER: return new AbbreviationFilterPreprocessor();
		case PROPERTY_TOKEN_COUNT_FILTER: return new PropertyTokenCountFilter(); 	
		case DISCRIMINATOR_PROPERTY_ENTITY_FILTER: return new EntityFilterPreprocessor();
		default: return null;
		}
	}
}
