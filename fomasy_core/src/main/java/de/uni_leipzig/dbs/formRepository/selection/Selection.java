package de.uni_leipzig.dbs.formRepository.selection;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.SelectionBuildException;
import de.uni_leipzig.dbs.formRepository.selection.algorithm.ISelectionAlgorithm;
import de.uni_leipzig.dbs.formRepository.selection.algorithm.SelectionPerGroup;
import de.uni_leipzig.dbs.formRepository.selection.combiner.ICombiner;
import de.uni_leipzig.dbs.formRepository.selection.conflict_generation.IConflictGenerator;
import de.uni_leipzig.dbs.formRepository.selection.conflict_generation.data.GenerationContext;
import de.uni_leipzig.dbs.formRepository.selection.scorer.collective.CollectiveScorer;
import de.uni_leipzig.dbs.formRepository.selection.scorer.data.CollectiveScoreContext;
import de.uni_leipzig.dbs.formRepository.selection.scorer.data.LocalScoreContext;
import de.uni_leipzig.dbs.formRepository.selection.scorer.local.LocalScorer;
import de.uni_leipzig.dbs.formRepository.selection.scorer.local.TextualSimilarityScorer;

import java.util.*;

/**
 * Created by christen on 20.04.2017.
 */
public class Selection {

  LocalScorer[]localScorers;
  CollectiveScorer[] collectiveScorers;
  IConflictGenerator generator;
  ICombiner combiner;
  GenerationContext gc;
  LocalScoreContext lsc;
  CollectiveScoreContext csc;


  protected Selection(LocalScorer[] localScorers, CollectiveScorer[] collectiveScorers, IConflictGenerator generator,
                   LocalScoreContext lsc, CollectiveScoreContext csc, GenerationContext gc, ICombiner combiner) {
    this.localScorers = localScorers;
    this.collectiveScorers = collectiveScorers;
    this.generator = generator;
    this.lsc = lsc;
    this.gc = gc;
    this.csc = csc;
    this.combiner = combiner;
  }


  /**
   * 1. generate confilct concepts for each item
   * 2. compute scores for each item and its annotations
   * 3. combine scores
   * 4. select concepts with highest score per group and item
   * @param am
   * @param src
   * @param target
   * @return
   */
  public AnnotationMapping select(AnnotationMapping am, EncodedEntityStructure src, EncodedEntityStructure target){
    Map<Integer, Set<Set<Integer>>> conflictSetPerItem = generator.getConflictAnnotations(am, target, gc);
    List<Map<Integer,Map<Integer,Double>>> scores = new ArrayList<>();
    if (localScorers != null) {
      for (LocalScorer ls : localScorers){
        scores.add(ls.computeScore(am, src, target, lsc));
      }
    }
    if (collectiveScorers != null){
      for (CollectiveScorer cs: collectiveScorers){
        scores.add(cs.computeScore(am, conflictSetPerItem, csc));
      }
    }
    Map<Integer,Map<Integer,Double>> combinedScore = combiner.combine(scores);
    ISelectionAlgorithm algorithm = new SelectionPerGroup();
    return algorithm.computeSelection(am, conflictSetPerItem, combinedScore);
  }

  public class Builder{
    LocalScorer[]localScorers;
    CollectiveScorer[] collectiveScorers;
    LocalScoreContext lsc;
    CollectiveScoreContext csc;
    IConflictGenerator generator;
    GenerationContext generationContext;
    ICombiner combiner;

    public Builder localScorers(LocalScorer[]localScorers){
      this.localScorers = localScorers;
      return this;
    }

    public Builder collectiveScorers(CollectiveScorer[]collectiveScorers){
      this.collectiveScorers = collectiveScorers;
      return this;
    }

    public Builder lsc(LocalScoreContext lsc){
      this.lsc = lsc;
      return this;
    }

    public Builder csc(CollectiveScoreContext csc){
      this.csc = csc;
      return this;
    }

    public Builder generator(IConflictGenerator generator){
      this.generator = generator;
      return this;
    }

    public Builder generationContext(GenerationContext generationContext){
      this.generationContext = generationContext;
      return this;
    }

    public Builder combiner(ICombiner combiner){
      this.combiner = combiner;
      return this;
    }

    public Selection build() throws SelectionBuildException {
      if (localScorers == null && collectiveScorers ==null){
        localScorers = new LocalScorer[]{new TextualSimilarityScorer()};
        lsc = new LocalScoreContext();
      }
      if (this.generator == null){
        throw new SelectionBuildException("conflict generator has to be initialized");
      }
      Selection selection = new Selection(localScorers, collectiveScorers, generator, lsc, csc, generationContext,
              combiner);
      return selection;
    }
  }
}


