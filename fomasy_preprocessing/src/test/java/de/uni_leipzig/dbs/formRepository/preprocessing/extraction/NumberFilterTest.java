package de.uni_leipzig.dbs.formRepository.preprocessing.extraction;

import de.uni_leipzig.dbs.formRepository.dataModel.*;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessingSteps;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorConfig;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorExecutor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.exception.PreprocessingException;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction.POSBasedExtractingPreprocessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created by christen on 13.12.2016.
 */
public class NumberFilterTest {

  EntityStructureVersion esv;
  GenericProperty gp;
  static String[] t = new String[]{"CD", "FW", "JJ", "JJR", "JJS", "LS", "NN", "NNS", "NNP", "RB", "RBS", "SYM", "IN"};


  @Before
  public void init() {

    VersionMetadata vm = new VersionMetadata(0, new Date(), new Date(), "test", "test");
    esv = new EntityStructureVersion(vm);
    GenericEntity ge = new GenericEntity(0, "e1", "testEntity", 0);
    gp = new GenericProperty(0, "testProperty", "main", "EN");
    PropertyValue pv = new PropertyValue(1, "HbA1c greater than or equal to 7.5 and less than or equal to 10.5%");
    ge.addPropertyValue(gp, pv);
    esv.addEntity(ge);
    esv.addAvailableProperty(gp);


    Set<String> wordTypes = new HashSet<String>();
    for (String i : t) {
      wordTypes.add(i);
    }
    PreprocessProperty pp = new PreprocessProperty("testProperty", "EN", "main");
    PreprocessorConfig formConfig = new PreprocessorConfig();
    Map<String, Object> preMap = new HashMap<>();
    preMap.put(POSBasedExtractingPreprocessor.FILTER_TYPES, wordTypes);
    formConfig.setExternalSourceMap(preMap);
    formConfig.addPreprocessingStepForProperties(PreprocessingSteps.NUMBER_NORMALIZATION, pp);
    PreprocessorExecutor executor = new PreprocessorExecutor();
    try {
      esv = executor.preprocess(esv, formConfig);
    } catch (PreprocessingException e) {
      e.printStackTrace();
    }

  }

  @Test
  public void testEncoding() {
    Assert.assertEquals("HbA1c greater than or equal to and" +
            " less than or equal to", esv.getEntity(0).getPropertyValues(gp).get(0));

  }


}

