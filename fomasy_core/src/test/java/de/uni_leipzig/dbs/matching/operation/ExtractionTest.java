package de.uni_leipzig.dbs.matching.operation;

import de.uni_leipzig.dbs.formRepository.dataModel.*;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.manager.MatchManager;
import de.uni_leipzig.dbs.formRepository.manager.MatchManagerImpl;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.execution.RegisteredMatcher;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.ExecutionTree;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.MatchOperator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessingSteps;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorConfig;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorExecutor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.exception.PreprocessingException;
import de.uni_leipzig.dbs.formRepository.operation.ExtractOperator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created by christen on 22.02.2017.
 */
public class ExtractionTest {

  EntityStructureVersion esv;
  EntityStructureVersion esv2;
  EncodedEntityStructure ees =null;
  EncodedEntityStructure ees2;
  AnnotationMapping am;
  GenericProperty gp;
  static String[] t = new String[]{"CD","FW","JJ","JJR","JJS","LS","NN","NNS","NNP","RB","RBS","SYM","IN"};


  @Before
  public void init() throws MatchingExecutionException{

    VersionMetadata vm = new VersionMetadata(0,new Date(), new Date(), "test", "test");
    esv = new EntityStructureVersion(vm);

    GenericEntity ge = new GenericEntity(0,"e1","testEntity",0);
    gp = new GenericProperty(0,"testProperty","main","EN");
    PropertyValue pv = new PropertyValue(1,"severe hemophilia");

    ge.addPropertyValue(gp,pv);

    GenericEntity ge3 = new GenericEntity(2,"e3","testEntity",0);
    gp = new GenericProperty(0,"testProperty","main","EN");
    PropertyValue pv3 = new PropertyValue(3,"not");
    ge3.addPropertyValue(gp,pv3);

    esv.addEntity(ge);
    esv.addEntity(ge3);
    esv.addAvailableProperty(gp);

    VersionMetadata vm2= new VersionMetadata(1,new Date(), new Date(), "target", "target");
    esv2 = new EntityStructureVersion(vm2);
    GenericEntity ge2 = new GenericEntity(0,"e2","testEntity",0);
    PropertyValue pv2 = new PropertyValue(1,"severe hemophilia");
    ge2.addPropertyValue(gp, pv2);
    esv2.addEntity(ge2);
    esv2.addAvailableProperty(gp);
    PreprocessProperty pp = new PreprocessProperty("testProperty","EN","main");
    PreprocessorConfig formConfig = new PreprocessorConfig ();
    Map<String,Object> preMap = new HashMap<>();
    formConfig.setExternalSourceMap(preMap);
    formConfig.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW,pp);
    formConfig.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, pp);
    //formConfig.addPreprocessingStepForProperties(PreprocessingSteps.KEYWORD_EXTRACTION, pp);
    PreprocessorExecutor executor = new PreprocessorExecutor();
    try {
      esv = executor.preprocess(esv, formConfig);
    } catch (PreprocessingException e) {
      e.printStackTrace();
    }
    Set<String> entTypes = new HashSet<>();
    entTypes.add("testEntity");
    ees = EncodingManager.getInstance().encoding(esv,entTypes,true);
    ees2 = EncodingManager.getInstance().encoding(esv2,entTypes,true);
    Set <GenericProperty> propsSrc = new HashSet<>();
    propsSrc.add(gp);
    MatchOperator mop = new MatchOperator(RegisteredMatcher.EXACT_MATCHER,
            AggregationFunction.MAX, propsSrc, propsSrc, 1f);
    ExecutionTree tree = new ExecutionTree();
    tree.addOperator(mop);
    MatchManager mm = new MatchManagerImpl();
    am = mm.match(esv, ees, ees2,esv2,tree, null);
  }

  @Test
  public void testMatching () throws MatchingExecutionException {
    EncodedEntityStructure extract = ExtractOperator.extractUnannotatedEntities(ees,am);
    Assert.assertEquals(1,extract.getObjIds().size());

  }
}
