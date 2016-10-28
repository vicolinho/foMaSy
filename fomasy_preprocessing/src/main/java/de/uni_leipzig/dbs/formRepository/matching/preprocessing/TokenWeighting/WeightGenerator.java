package de.uni_leipzig.dbs.formRepository.matching.preprocessing.TokenWeighting;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;

public interface WeightGenerator {

	
	public Int2FloatMap getWeightMap();
}
