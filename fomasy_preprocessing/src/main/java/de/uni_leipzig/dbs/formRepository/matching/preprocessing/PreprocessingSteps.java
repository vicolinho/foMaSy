package de.uni_leipzig.dbs.formRepository.matching.preprocessing;

public enum PreprocessingSteps implements IPreprocessingStep{
		TO_LOW {
			public String getSuffix (){
				return "to_low";
			}
		},
		NORMALIZE,
		NUMBER_NORMALIZATION,
		ABBREVIATION_NORMALIZATION,
		STEMMING,
		SYNONYM_ENRICHMENT,
		KEYWORD_EXTRACTION,
		STOPWORD_EXTRACTION,
		LENGTH_FILTER,
		ABBREVIATION_FILTER,
		TRANSLATION,
		PROPERTY_TOKEN_COUNT_FILTER,
		DISCRIMINATOR_PROPERTY_ENTITY_FILTER;
		@Override
		public String getPrefix() {
			// TODO Auto-generated method stub
			return null;
		}
}
