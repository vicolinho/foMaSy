package de.uni_leipzig.dbs.formRepository.evaluation.tool.wrapper;

import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.PropertyValue;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

import java.util.List;
import java.util.Set;

/**
 * Created by christen on 28.02.2017.
 */
public class GenericEntity2JCas {



  public static JCas getCas (GenericEntity ge, Set<GenericProperty> usedProperties) throws UIMAException {
    StringBuilder sb = new StringBuilder();
      List<PropertyValue> pvs = ge.getValues(usedProperties.toArray(new GenericProperty[]{}));
    for (PropertyValue pv : pvs){
      sb.append(pv.getValue()+". ");
    }
    JCas cas = JCasFactory.createJCas();
    cas.setDocumentText(sb.toString());
    return cas;
  }

}
