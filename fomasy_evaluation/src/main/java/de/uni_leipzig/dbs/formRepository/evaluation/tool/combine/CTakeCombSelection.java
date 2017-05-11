package de.uni_leipzig.dbs.formRepository.evaluation.tool.combine;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.dataModel.*;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.evaluation.selection.util.ConflictFactory;
import de.uni_leipzig.dbs.formRepository.evaluation.selection.util.ScorerFactory;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.exception.SelectionBuildException;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.execution.RegisteredMatcher;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.ExecutionTree;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.MatchOperator;
import de.uni_leipzig.dbs.formRepository.matching.lookup.TokenSimilarityLookup;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.*;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.TokenWeighting.TFIDFTokenWeightGenerator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.selection.GraphBasedSelection;
import de.uni_leipzig.dbs.formRepository.matching.selection.GroupSelection;
import de.uni_leipzig.dbs.formRepository.matching.token.SoftTFIDFMatcher;
import de.uni_leipzig.dbs.formRepository.matching.token.TFIDFWindowMatcher;
import de.uni_leipzig.dbs.formRepository.selection.Selection;
import de.uni_leipzig.dbs.formRepository.selection.algorithm.ISelectionAlgorithm;
import de.uni_leipzig.dbs.formRepository.selection.combiner.ICombiner;
import de.uni_leipzig.dbs.formRepository.selection.conflict_generation.IConflictGenerator;
import de.uni_leipzig.dbs.formRepository.selection.conflict_generation.data.GenerationContext;
import de.uni_leipzig.dbs.formRepository.selection.scorer.collective.CollectiveScorer;
import de.uni_leipzig.dbs.formRepository.selection.scorer.data.CollectiveScoreContext;
import de.uni_leipzig.dbs.formRepository.selection.scorer.data.LocalScoreContext;
import de.uni_leipzig.dbs.formRepository.selection.scorer.local.LocalScorer;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import org.apache.log4j.Logger;
import org.hibernate.sql.Select;

import javax.print.attribute.HashPrintJobAttributeSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Created by christen on 02.03.2017.
 */
public class CTakeCombSelection {

Logger log = Logger.getLogger(getClass());
  private float threshold =0.3f;
  private EncodedEntityStructure eesSrc;
  private EncodedEntityStructure eesTarget;
  private Int2FloatMap idfMap;
  private Set<GenericProperty> targetProps;

  public AnnotationMapping selectAnnoation(FormRepository rep, AnnotationMapping am,
                                           GroupSelection sel, Set<GenericProperty> srcProps, Set<GenericProperty> targetProps,  float threshold){
    Set<Integer> questions = this.getIds(am, true);
    Set<Integer> concepts = this.getIds(am, false);
    this.threshold = threshold;
    this.targetProps = targetProps;

    try {
      EntitySet<GenericEntity> srcGe =
              rep.getFormManager().getEntitiesByIdWithProperties(questions);
      EntitySet<GenericEntity> targetGe =
              rep.getFormManager().getEntitiesByIdWithProperties(concepts);
      EncodedAnnotationMapping eam = this.getSimilarities(srcGe, targetGe, srcProps, targetProps, rep);
      int count =0;

      for (EntityAnnotation ea : am.getAnnotations()){
        EntityAnnotation ean = eam.getAnnotation(ea.getId());
        if (ean != null){
          ea.setSim(ean.getSim());
        }else{
          ea.setSim(0);
          count++;
        }
      }
      am.setEvidenceMap(eam.getEvidenceMap());
      am = sel.select(am, eesSrc, eesTarget, srcProps, targetProps, 0.3f, 0f, 2, rep);
    } catch (EntityAPIException e) {
      e.printStackTrace();
    } catch (MatchingExecutionException e) {
      e.printStackTrace();
    }
    return am;
  }

  public AnnotationMapping selectAnnoation(FormRepository rep, AnnotationMapping am,
      LocalScorer[] localScorers, CollectiveScorer[] collectiveScorers,
      IConflictGenerator generator, CollectiveScoreContext collectiveScoreContext, ICombiner combiner,
      ISelectionAlgorithm algorithm, Set<GenericProperty> srcProps, Set<GenericProperty> targetProps, float threshold){


    Set<Integer> questions = this.getIds(am, true);
    Set<Integer> concepts = this.getIds(am, false);
    this.threshold = threshold;
    this.targetProps = targetProps;

    try {
      EntitySet<GenericEntity> srcGe =
              rep.getFormManager().getEntitiesByIdWithProperties(questions);
      EntitySet<GenericEntity> targetGe =
              rep.getFormManager().getEntitiesByIdWithProperties(concepts);
      EncodedAnnotationMapping eam = this.getSimilarities(srcGe, targetGe, srcProps, targetProps, rep);
      int count =0;
      for (EntityAnnotation ea : am.getAnnotations()){
        EntityAnnotation ean = eam.getAnnotation(ea.getId());
        if (ean != null){
          ea.setSim(ean.getSim());
        }else{
          count++;
        }
      }
      am.setEvidenceMap(eam.getEvidenceMap());
      LocalScoreContext lsc = this.getLocalScorerContext();
      GenerationContext gc = this.getGenerationContext();
      Selection sel = new Selection.Builder().collectiveScorers(collectiveScorers)
              .localScorers(localScorers)
              .csc(collectiveScoreContext)
              .lsc(lsc)
              .generator(generator)
              .generationContext(gc)
              .combiner(combiner)
              .selectionAlogrithm(algorithm)
              .build();
      am = sel.select(am, eesSrc, eesTarget);
    } catch (EntityAPIException e) {
      e.printStackTrace();
    } catch (MatchingExecutionException e) {
      e.printStackTrace();
    } catch (SelectionBuildException e) {
      e.printStackTrace();
    }
    return am;
  }


  private Set<Integer> getIds (AnnotationMapping am, boolean isSource){
    Set<Integer> srcIds = new HashSet<>();
    for (EntityAnnotation ea: am.getAnnotations()){
      if (isSource)
        srcIds.add(ea.getSrcId());
      else
        srcIds.add(ea.getTargetId());
    }
    return srcIds;
  }

  private EncodedAnnotationMapping getSimilarities(EntitySet<GenericEntity> src, EntitySet<GenericEntity> target,
    Set<GenericProperty> srcProps, Set<GenericProperty> targetProps, FormRepository rep) throws MatchingExecutionException {

    EncodedAnnotationMapping restMapping = null;
    PreprocessorConfig config = new PreprocessorConfig();
    Set<PreprocessProperty> preprocessProperties = new HashSet<>();
    for (GenericProperty gp : srcProps){
      preprocessProperties.add(new PreprocessProperty(gp.getName(),gp.getLanguage(), gp.getScope()));
    }
    config.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW,
            preprocessProperties.toArray(new PreprocessProperty[]{}));
    config.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION,
            preprocessProperties.toArray(new PreprocessProperty[]{}));
    PreprocessorExecutor pExec = new PreprocessorExecutor();
    src = pExec.preprocess(src, config);
    Set<String> itemTypes = new HashSet<>();
    itemTypes.add("item");
    eesSrc = EncodingManager.getInstance().encoding(src, itemTypes);


   preprocessProperties.clear();
    for (GenericProperty gp : targetProps){
      preprocessProperties.add(new PreprocessProperty(gp.getName(),gp.getLanguage(), gp.getScope()));
    }
    PreprocessProperty[] targetProperties = preprocessProperties.toArray(new PreprocessProperty[]{});
    PreprocessorConfig configTarget = new PreprocessorConfig();
    configTarget.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, targetProperties);
    configTarget.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION,targetProperties);
    target = pExec.preprocess(target, configTarget);

    eesTarget = EncodingManager.getInstance().encoding(target,true);

    TFIDFTokenWeightGenerator.getInstance().removeAll();
    TFIDFTokenWeightGenerator.getInstance().initializeGlobalCount(eesSrc, srcProps.toArray(new GenericProperty[]{}));
    TFIDFTokenWeightGenerator.getInstance().initializeGlobalCount(eesTarget, targetProps.toArray(new GenericProperty[]{}));
    idfMap = TFIDFTokenWeightGenerator.getInstance().generateIDFValuesForAllSources(eesTarget.getObjIds().size()
            +eesSrc.getObjIds().size());

    TokenSimilarityLookup.getInstance().computeTrigramLookup(src, target, srcProps, targetProps, rep);
    MatchOperator mop = new MatchOperator (RegisteredMatcher.SOFT_TFIDF_WND_MATCHER, AggregationFunction.MAX,
            srcProps, targetProps, threshold);
    Map<String,Object> externalMap = new HashMap<String,Object>();
    externalMap.put(TFIDFWindowMatcher.WND_SIZE, 5);
    externalMap.put(TFIDFWindowMatcher.IS_ADAPTIVE_SIZE, false);
    externalMap.put(TFIDFWindowMatcher.IDF_MAP_SOURCE, idfMap);
    externalMap.put(TFIDFWindowMatcher.IDF_MAP_TARGET, idfMap);
    externalMap.put(TFIDFWindowMatcher.TFIDF_SOURCE_SEPARATED, false);
    Map <Integer,Set<Integer>> tokenSimLookup = TokenSimilarityLookup.getInstance().getLookup();

    externalMap.put(SoftTFIDFMatcher.LOOKUP, tokenSimLookup);
    mop.setGlobalObjects(externalMap);
    ExecutionTree tree = new ExecutionTree();
    tree.addOperator(mop);
    try {
      restMapping = rep.getMatchManager().matchEncoded(eesSrc, eesTarget, tree, null);
    } catch (MatchingExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return restMapping;
  }


  private LocalScoreContext getLocalScorerContext(){
    return ScorerFactory.getIDFUnmatchedContext(idfMap);
  }

  private GenerationContext getGenerationContext(){
    return ConflictFactory.getTokenSimilarGenerator(targetProps);
  }
}
