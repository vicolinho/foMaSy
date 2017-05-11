package de.uni_leipzig.dbs.formRepository.evaluation.execution;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.dataModel.*;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Edge;
import de.uni_leipzig.dbs.formRepository.dataModel.graph.data.Node;
import de.uni_leipzig.dbs.formRepository.evals.calculation.EvaluationResult;
import de.uni_leipzig.dbs.formRepository.evals.calculation.HierarchicalMappingEvaluation;
import de.uni_leipzig.dbs.formRepository.evals.calculation.MappingEvaluation;
import de.uni_leipzig.dbs.formRepository.evaluation.tool.combine.CTakeCombSelection;
import de.uni_leipzig.dbs.formRepository.evaluation.tool.wrapper.CTakeWrapper;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;
import de.uni_leipzig.dbs.formRepository.exception.GraphAPIException;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.operation.SetAnnotationOperator;
import de.uni_leipzig.dbs.formRepository.selection.algorithm.ISelectionAlgorithm;
import de.uni_leipzig.dbs.formRepository.selection.algorithm.SelectionPerGroup;
import de.uni_leipzig.dbs.formRepository.selection.combiner.ICombiner;
import de.uni_leipzig.dbs.formRepository.selection.combiner.impl.LinearCombination;
import de.uni_leipzig.dbs.formRepository.selection.conflict_generation.CommonTokenClique;
import de.uni_leipzig.dbs.formRepository.selection.conflict_generation.IConflictGenerator;
import de.uni_leipzig.dbs.formRepository.selection.scorer.collective.CCItemGraphScorer;
import de.uni_leipzig.dbs.formRepository.selection.scorer.collective.CollectiveScorer;
import de.uni_leipzig.dbs.formRepository.selection.scorer.data.CollectiveScoreContext;
import de.uni_leipzig.dbs.formRepository.selection.scorer.local.LocalScorer;
import de.uni_leipzig.dbs.formRepository.selection.scorer.local.TextualSimilarityScorer;
import edu.uci.ics.jung.graph.DirectedGraph;
import org.apache.log4j.PropertyConfigurator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;


/**
 * Created by christen on 03.03.2017.
 */
public class CtakeWithSelectionEvalaution {

  private static final String PATH = "ctakes-clinical-pipeline/desc/analysis_engine";

  private static final String GRAPH_PATH = "datasets/contextGraphs/umls_extract_graph.csv";

  private static final boolean IS_TOP2 = false;

  private static final float THRESHOLD = 0.2f;

  private static final char DELIMITER = ',';

  private static final boolean IS_HEADER = true;

  public static final LocalScorer[] localScorers = new LocalScorer[]{
          new TextualSimilarityScorer(),
          //new IDFUnmatchedScorer(),
          //new SequenceScorer(),
  };

  public static final CollectiveScorer[] collectiveScorers = new CollectiveScorer[]{
          new CCItemGraphScorer()
  };
  private static final int DEPTH = 2;

  public static IConflictGenerator generator;

  public static ISelectionAlgorithm algorithm = new SelectionPerGroup();

  static CTakeCombSelection selection = new CTakeCombSelection();

  public static ICombiner combiner = new LinearCombination(new double[]{0.5, 0.5});

  static String date = "2014-01-01";
  //String name = "umls2014AB";
  static String name = "umls2014AB_extract";
  static String type = "ontology";

  public static final HashSet<String> usedTrials = new HashSet<>(Arrays.asList(new String[]{
          "NCT00168051",
          "NCT00355849", "NCT00175903", "NCT00356109", "NCT00357227",
          "NCT00359762", "NCT00372229", "NCT00190047", "NCT00373373", "NCT00195507",
          "NCT00376337", "NCT00384046", "NCT00385372", "NCT00391287", "NCT00391872",
          "NCT00393692", "NCT00006045", "NCT00048295", "NCT00151112", "NCT00153062",
          "NCT00156338", "NCT00157157", "NCT00160524", "NCT00160706", "NCT00165828"
  })
  );

  public static final String[] AE_DESCRIPTIONS = new String[]{
          //   "AggregatePlaintextFastUMLSProcessor_VOL.xml",
          //   "AggregatePlaintextFastUMLSProcessor_V--.xml",
          "AggregatePlaintextFastUMLSProcessor_V-L.xml"
  };

  public static final GenericProperty[] umlsProperties = new GenericProperty[]{
          new GenericProperty(0, "name", "PN", "EN"),
          new GenericProperty(1, "synonym", "PT", "EN"),
          new GenericProperty(2, "synonym", "SY", "EN"),
          new GenericProperty(3, "synonym", "SCN", "EN"),
          new GenericProperty(4, "synonym", "FN", "EN"),
          new GenericProperty(5, "synonym", "MH", "EN")
  };
  private static Set<EntityStructureVersion> esvSet;
  private static Map<Integer, AnnotationMapping> mappings;
  private static FormRepository rep;
  private static AnnotationMapping referenceMapping;
  private static Set<GenericProperty> targetGps;
  private static AnnotationMapping overallMapping;
  private static ArrayList results;
  private static Properties prop;
  private static Map<String, Integer> cui2Id;
  private static VersionMetadata umlsMeta;
  private static CTakeWrapper wrapper;


  public CtakeWithSelectionEvalaution() {
    super();
  }

  public static void main(String[] args) throws Exception {

    PropertyConfigurator.configure("log4j.properties");


    /*
     *initialize
     */
    initialize(args[0]);
    for (String aeDescr : AE_DESCRIPTIONS) {
      long start = System.currentTimeMillis();
      prop.put(CTakeWrapper.ID_MAP, cui2Id);
      prop.put(CTakeWrapper.AE_PATH, PATH + "/" + aeDescr);
      wrapper = new CTakeWrapper(prop);
      overallMapping = new AnnotationMapping();
      for (EntityStructureVersion es : esvSet) {
        if (usedTrials.contains(es.getMetadata().getName())) {
          Set<GenericProperty> gps = es.getAvailableProperties("question", "EN", null);
          gps.addAll(esvSet.iterator().next().getAvailableProperties("name", null, null));
          prop.put(CTakeWrapper.PROPERTIES, gps);

          ctakeGeneration(es);
        /*
        selection
        */
          selection(es);
        }
      }
      long end = System.currentTimeMillis();
      System.out.println(end - start);
      /*
        evaluate
       */
      evaluate();
    }

    String resTable = writeTable(AE_DESCRIPTIONS, results);
    System.out.println(resTable);
  }


  private static void initialize(String ini) throws Exception {
    mappings = new HashMap<>();
    rep = new FormRepositoryImpl();
    rep.initialize(ini);
    generator = new CommonTokenClique(rep);
    Set<String> types = new HashSet<>();
    cui2Id = rep.getFormManager().getIdMapping(name, date, type);
    types.add("eligibility criteria");
    esvSet = rep.getFormManager().getStructureVersionsByType(types);
    umlsMeta = rep.getFormManager().getMetadata(name, type, date);
    referenceMapping = new AnnotationMapping();
    for (EntityStructureVersion es : esvSet) {
      if (usedTrials.contains(es.getMetadata().getName())) {
        VersionMetadata vm = es.getMetadata();
        String mappingName = vm.getName() + "[" + vm.getTopic() + "]-"
                + umlsMeta.getName() + "[" + umlsMeta.getTopic() + "]_odm";
        AnnotationMapping esRefMap = rep.getMappingManager().getAnnotationMapping(es.getMetadata(),
                umlsMeta, mappingName);
        referenceMapping = SetAnnotationOperator.union(AggregationFunction.MAX, referenceMapping, esRefMap);
      }
    }
    targetGps = rep.getFormManager()
            .getAvailableProperties(name, date, type);
    prop = new Properties();
    results = new ArrayList<>();
  }


  private static void ctakeGeneration(EntityStructureVersion es) throws Exception {
    AnnotationMapping am = wrapper.computeMapping(es, prop);
    am = removeConceptsNotInExtract(am, cui2Id);
    mappings.put(es.getStructureId(), am);


  }

  private static void selection(EntityStructureVersion es) throws Exception {
    Set<GenericProperty> gps = es.getAvailableProperties("question", "EN", null);
    gps.addAll(esvSet.iterator().next().getAvailableProperties("name", null, null));
    AnnotationMapping am = mappings.get(es.getStructureId());
//    DirectedGraph<Node,Edge> graph = getSemanticTypeGraph(am,rep);
    DirectedGraph<Node, Edge> graph = getGraphFromFile(GRAPH_PATH, rep);
    CollectiveScoreContext csc = getContext(DEPTH, graph);
    //am = selection.selectAnnoation(rep, am, new GroupSelection(), gps, usedProperties, THRESHOLD);
    am = selection.selectAnnoation(rep, am, localScorers, collectiveScorers, generator,
            csc, combiner, algorithm, gps, targetGps, THRESHOLD);
    overallMapping = SetAnnotationOperator.union(AggregationFunction.MAX, overallMapping, am);
  }

  private static void evaluate() throws Exception {
    if (!IS_TOP2) {
      MappingEvaluation me = new MappingEvaluation();
      EvaluationResult er = me.getResult(overallMapping, referenceMapping, "eligibility criteria", "umls");
      results.add(er.toStringArray());
    } else {
      HierarchicalMappingEvaluation me = new HierarchicalMappingEvaluation();
      VersionMetadata all = new VersionMetadata(-1, new Date(), new Date(), "all_forms", "eligibility criteria");
      EvaluationResult er = me.getResult(overallMapping, referenceMapping, all, umlsMeta, rep);
      results.add(er.toStringArray());
    }
  }

  private static CollectiveScoreContext getContext(int depth, DirectedGraph<Node, Edge> graph) {
    return new CollectiveScoreContext.Builder()
            .depth(depth)
            .graph(graph)
            .build();
  }

  private static AnnotationMapping removeConceptsNotInExtract(AnnotationMapping am, Map<String, Integer> cui2Id) {
    Set<Long> removeCorrs = new HashSet<>();
    for (EntityAnnotation ea : am.getAnnotations()) {
      if (!cui2Id.containsKey(ea.getTargetAccession())) {
        removeCorrs.add(ea.getId());
      }
    }
    for (long rc : removeCorrs) {
      am.removeAnnotation(rc);
    }
    return am;
  }


  private static DirectedGraph<Node, Edge> getGraphFromFile(String file, FormRepository rep) throws FileNotFoundException, GraphAPIException {
    return rep.getGraphManager().getGraphFromCSV(new FileInputStream(file), DELIMITER, IS_HEADER, cui2Id);
  }

  private static DirectedGraph<Node, Edge> getSemanticTypeGraph(AnnotationMapping am, FormRepository rep) throws EntityAPIException,
          VersionNotExistsException, StructureBuildException, GraphAPIException {
    EntitySet<GenericEntity> nodes;
    Set<Integer> ids = new HashSet<Integer>();
    for (EntityAnnotation ea : am.getAnnotations()) {
      ids.add(ea.getTargetId());
    }
    nodes = rep.getFormManager().getEntitiesByIdWithProperties(ids);

    EntityStructureVersion semTypeGraph = rep.getFormManager().getStructureVersion("semanticNetwork", "ontology", "2014-01-01");
    Set<GenericProperty> propSource = rep.getFormManager().getAvailableProperties("umls2014AB_extract", "2014-01-01", "ontology");
    Set<GenericProperty> joinAtt = new HashSet<>();
    for (GenericProperty joinGp : propSource) {
      if (joinGp.getName().equals("sem_type"))
        joinAtt.add(joinGp);
    }
    Set<GenericProperty> propTarget = semTypeGraph.getAvailableProperties("name", null, null);
    VersionMetadata vm = rep.getFormManager().getMetadata("semanticNetwork", "ontology", "2014-01-01");
    return rep.getGraphManager().getSubgraphFromExternalStructure(nodes, vm, joinAtt.iterator().next(),
            propTarget, 5);
  }

  private static DirectedGraph<Node, Edge> getSemanticTypeGraph(Map<Integer, AnnotationMapping> annoMap, FormRepository rep) throws EntityAPIException,
          VersionNotExistsException, StructureBuildException, GraphAPIException {
    EntitySet<GenericEntity> nodes;
    Set<Integer> ids = new HashSet<Integer>();
    for (AnnotationMapping am : annoMap.values()) {
      for (EntityAnnotation ea : am.getAnnotations()) {
        ids.add(ea.getTargetId());
      }
    }
    nodes = rep.getFormManager().getEntitiesByIdWithProperties(ids);

    EntityStructureVersion semTypeGraph = rep.getFormManager().getStructureVersion("semanticNetwork", "ontology", "2014-01-01");
    Set<GenericProperty> propSource = rep.getFormManager().getAvailableProperties("umls2014AB_extract", "2014-01-01", "ontology");
    Set<GenericProperty> joinAtt = new HashSet<>();
    for (GenericProperty joinGp : propSource) {
      if (joinGp.getName().equals("sem_type"))
        joinAtt.add(joinGp);
    }
    Set<GenericProperty> propTarget = semTypeGraph.getAvailableProperties("name", null, null);
    VersionMetadata vm = rep.getFormManager().getMetadata("semanticNetwork", "ontology", "2014-01-01");
    return rep.getGraphManager().getSubgraphFromExternalStructure(nodes, vm, joinAtt.iterator().next(),
            propTarget, 5);
  }

  private static String writeTable(String[] header, List<String[]> result) {
    StringBuilder sb = new StringBuilder();
    sb.append("method" + "\t");
    for (int i = 0; i < header.length; i++) {
      if (i != header.length - 1)
        sb.append(header[i]).append("\t");
      else {
        sb.append(header[i] + System.getProperty("line.separator"));
      }
    }

    for (int i = 0; i < 6; i++) {
      String col = getColumn(i);
      sb.append(col + "\t");
      for (int j = 0; j < result.size(); j++) {
        String[] r = result.get(j);
        if (j != result.size() - 1)
          sb.append(r[i] + "\t");
        else {
          sb.append(r[i] + System.getProperty("line.separator"));
        }
      }
    }
    return sb.toString();
  }

  private static String getColumn(int i) {
    if (i == 0) {
      return "truePositive";
    } else if (i == 1) {
      return "falsePositive";
    } else if (i == 2) {
      return "falseNegative";
    } else if (i == 3) {
      return "precision";
    } else if (i == 4) {
      return "recall";
    } else if (i == 5) {
      return "fmeasure";
    }
    return "";
  }


}
