package de.uni_leipzig.dbs.matching.token;

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
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created by christen on 10.01.2017.
 */
public class LCSTest {

  EntityStructureVersion esv;
  EntityStructureVersion esv2;
  EncodedEntityStructure ees;
  EncodedEntityStructure ees2;
  GenericProperty gp;
  static String[] t = new String[]{"CD","FW","JJ","JJR","JJS","LS","NN","NNS","NNP","RB","RBS","SYM","IN"};


  @Before
  public void init(){

    VersionMetadata vm = new VersionMetadata(0,new Date(), new Date(), "test", "test");
    esv = new EntityStructureVersion(vm);

    GenericEntity ge = new GenericEntity(0,"e1","testEntity",0);
    gp = new GenericProperty(0,"testProperty","main","EN");
    PropertyValue pv = new PropertyValue(1,"subject severe moderately "+
            "baseline factor viii level < = 2 % normal documented screening severe hemophilia");
    ge.addPropertyValue(gp,pv);
    esv.addEntity(ge);
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

  }

  @Test
  public void testMatching () throws MatchingExecutionException {
    Set <GenericProperty> propsSrc = new HashSet<>();
    propsSrc.add(gp);
    MatchOperator mop = new MatchOperator(RegisteredMatcher.LCS_MATCHER,
            AggregationFunction.MAX, propsSrc, propsSrc, 0f);
    ExecutionTree tree = new ExecutionTree();
    tree.addOperator(mop);
    MatchManager mm = new MatchManagerImpl();
    AnnotationMapping am = mm.match(esv, ees, ees2,esv2,tree, null);
    Assert.assertEquals(1, am.getNumberOfAnnotations());
    for (EntityAnnotation ea: am.getAnnotations()){
      Assert.assertEquals(1.25*(2)/ (2+0.25f*13f),ea.getSim(), 0.001);
    }
  }
}
