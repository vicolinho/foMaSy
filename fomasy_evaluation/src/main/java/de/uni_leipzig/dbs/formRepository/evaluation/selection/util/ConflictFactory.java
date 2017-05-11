package de.uni_leipzig.dbs.formRepository.evaluation.selection.util;

import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.matching.execution.RegisteredMatcher;
import de.uni_leipzig.dbs.formRepository.selection.conflict_generation.CommonTokenConflict;
import de.uni_leipzig.dbs.formRepository.selection.conflict_generation.IConflictGenerator;
import de.uni_leipzig.dbs.formRepository.selection.conflict_generation.data.GenerationContext;

import java.util.Set;

/**
 * Created by christen on 26.04.2017.
 */
public class ConflictFactory {


  public static String sim_func = RegisteredMatcher.TRIGRAM_MATCHER;
  public static float threshold = 0.4f;
  public static GenerationContext getCommonTokenGenerator (){
    GenerationContext.Builder b = new GenerationContext.Builder();
    return b.build();
  }


  public static GenerationContext getTokenSimilarGenerator (Set<GenericProperty> props){
    GenerationContext.Builder b = new GenerationContext.Builder()
            .groupAttributes(props)
            .simFunc(sim_func)
            .simThreshold(threshold);
    return b.build();
  }
}
