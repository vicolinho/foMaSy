package de.uni_leipzig.dbs.formRepository.evaluation.selection.util;

import de.uni_leipzig.dbs.formRepository.selection.scorer.data.LocalScoreContext;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;

/**
 * Created by christen on 26.04.2017.
 */
public class ScorerFactory {

  public static LocalScoreContext getSimilarityContext(){
    return new LocalScoreContext.Builder().build();
  }

  public static LocalScoreContext getIDFUnmatchedContext(Int2FloatMap idfMap){
    return new LocalScoreContext.Builder()
            .idfMap(idfMap)
            .build();
  }

  public static LocalScoreContext getGapScorer(){
    return new LocalScoreContext.Builder().build();
  }


}
