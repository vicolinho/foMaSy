package de.uni_leipzig.dbs.formRepository.selection.scorer.collective;

import com.google.common.base.Function;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Edge;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;
import de.uni_leipzig.dbs.formRepository.selection.scorer.data.CollectiveScoreContext;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 28.04.2017.
 */
public class FormPageRankScorer implements CollectiveScorer {

  Logger log = Logger.getLogger(FormPageRankScorer.class);

  @Override
  public Map<Integer, Map<Integer, Double>> computeScore(AnnotationMapping am, Map<Integer, Set<Set<Integer>>> conflictSet,
                                                         final CollectiveScoreContext scContext) {

    final Map<Integer, Double> priors = new HashMap<>();
    Set<Integer> concepts  = new HashSet<>();
    for (Set<Set<Integer>> set: conflictSet.values()){
      for (Set<Integer> s: set){
        concepts.addAll(s);
      }
    }
    log.info("concepts:"+concepts.size());
    int stCount = 0;
    for (Node n: scContext.getGraph().getVertices()){
      if (!concepts.contains(n.getId())) {
        stCount++;
        if (priors.get(n.getId()) == null) {
          priors.put(n.getId(), (double)scContext.getGraph().getIncidentEdges(n).size());
        }
      }else {
        priors.put(n.getId(), 1d/concepts.size());
      }
    }
    for (Integer k : priors.keySet()){
      priors.put(k, priors.get(k)/(double)stCount);
    }

    Function<Node, Double> transformer = new Function<Node, Double>() {
      @Override
      public Double apply(@Nullable Node node) {
        if (priors.get(node.getId())!=null){
          return priors.get(node.getId());
        }else {
          return 1d;
        }
      }
    };
    PageRankWithPriors<Node, Edge> pageRank = new PageRankWithPriors<>(scContext.getGraph(), transformer, 0.15);
    pageRank.evaluate();
    Map<Integer, Double> scores = new HashMap<>();
    double sumScore = 0;
    for (Node n: scContext.getGraph().getVertices()){
      if (concepts.contains(n.getId())) {
        if (pageRank.getVertexScore(n) != null) {
          scores.put(n.getId(), pageRank.getVertexScore(n));
          sumScore+= pageRank.getVertexScore(n);
        } else {
          scores.put(n.getId(), 0d);
        }
      }
    }

    Map<Integer,Map<Integer, Double>> ranking = new HashMap<>();
    for (Map.Entry<Integer, Set<Set<Integer>>> e: conflictSet.entrySet()){
      Map<Integer,Double> rankPerItem = new HashMap<>();
      ranking.put(e.getKey(), rankPerItem);
      for (Set<Integer> set: e.getValue()){
        for (Integer cc: set){
          rankPerItem.put(cc, scores.get(cc)/sumScore);
        }
      }
    }
    return ranking;
  }
}
