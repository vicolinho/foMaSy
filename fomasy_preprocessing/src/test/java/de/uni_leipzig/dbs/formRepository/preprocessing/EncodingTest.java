package de.uni_leipzig.dbs.formRepository.preprocessing;


import de.uni_leipzig.dbs.formRepository.dataModel.*;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.*;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.exception.PreprocessingException;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction.POSBasedExtractingPreprocessor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.string.ToLowPreprocessor;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created by christen on 25.11.2016.
 */


public class EncodingTest {

  EntityStructureVersion esv;
  GenericProperty gp;
  static String[] t = new String[]{"CD","FW","JJ","JJR","JJS","LS","NN","NNS","NNP","RB","RBS","SYM","IN"};


  @Before
  public void init(){

    VersionMetadata vm = new VersionMetadata(0,new Date(), new Date(), "test", "test");
    esv = new EntityStructureVersion(vm);
    GenericEntity ge = new GenericEntity(0,"e1","testEntity",0);
    gp = new GenericProperty(0,"testProperty","main","EN");
    PropertyValue pv = new PropertyValue(1,"The subject has severe or moderately severe hemophilia" +
            " A as defined by a" +
            " baseline factor VIII level<= 2% of normal, as documented at screening");
    ge.addPropertyValue(gp,pv);
    esv.addEntity(ge);
    esv.addAvailableProperty(gp);


    Set<String> wordTypes = new HashSet<String>();
    for(String i: t){
      wordTypes.add(i);
    }
    PreprocessProperty pp = new PreprocessProperty("testProperty","EN","main");
    PreprocessorConfig formConfig = new PreprocessorConfig ();
    Map<String,Object> preMap = new HashMap<>();
    preMap.put(POSBasedExtractingPreprocessor.FILTER_TYPES, wordTypes);
    formConfig.setExternalSourceMap(preMap);
    formConfig.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW,pp);
    formConfig.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, pp);
    formConfig.addPreprocessingStepForProperties(PreprocessingSteps.KEYWORD_EXTRACTION, pp);
    PreprocessorExecutor executor = new PreprocessorExecutor();
    try {
      esv = executor.preprocess(esv, formConfig);
    } catch (PreprocessingException e) {
      e.printStackTrace();
    }

  }

  @Test
  public void testEncoding (){
    Assert.assertEquals("subject severe moderately severe hemophilia "+
            "baseline factor viii level < = 2 % normal documented screening",esv.getEntity(0).getPropertyValues(gp).get(0));
    Assert.assertEquals(esv.getNumberOfEntities(),1);
    Set <String> entTypes = new HashSet<>();
    entTypes.add("testEntity");
    EncodedEntityStructure ees = EncodingManager.getInstance().encoding(esv,entTypes,true);

    int [][] props = ees.getPropertyValues(0,gp);
    Assert.assertEquals(1,props.length);
    StringBuilder sb = new StringBuilder();
    for (int[] pv:props){
      System.out.println(Arrays.toString(pv));
      for (int t: pv){
        sb.append(EncodingManager.getInstance().getReverseDict().get(t)+" ");
      }
    }

    Assert.assertEquals("subject severe moderately severe hemophilia "+
            "baseline factor viii level 2 normal documented screening", sb.toString().trim());
  }
}
