package de.uni_leipzig.dbs.formRepository.evaluation.tool.wrapper;

import de.uni_leipzig.dbs.formRepository.dataModel.*;
import de.uni_leipzig.dbs.formRepository.evaluation.exception.AnnotationException;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.impl.JCasImpl;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by christen on 13.01.2017.
 */
public class CTakeWrapper implements AnnotationWrapper{

  public static final String AE_PATH = "aePath";
  public static final String ID_MAP = "idMap";
  public static final String PROPERTIES = "properties";

  Logger log = Logger.getLogger(getClass());
  private AnalysisEngine ae;

  private Map<String, Integer> idMap;

  public CTakeWrapper (Properties prop) throws ResourceInitializationException, IOException, InvalidXMLException {

    String path = prop.getProperty(AE_PATH);
    idMap = (Map<String, Integer>)prop.get(ID_MAP);
    URL url = ClassLoader.getSystemResource(path);
    AnalysisEngineDescription aed = UIMAFramework.getXMLParser()
            .parseAnalysisEngineDescription(new XMLInputSource(url.getPath()));
    ae = UIMAFramework.produceAnalysisEngine(aed);

  }

  @Override
  public AnnotationMapping computeMapping(EntityStructureVersion esv, Properties prop) throws AnnotationException{
    Set<GenericProperty> usedProperties = (Set<GenericProperty>) prop.get(PROPERTIES);
    AnnotationMapping am = new AnnotationMapping();
    int notInExtract =0;
    try {
      for (GenericEntity ge: esv.getEntities()){
        if (ge.getType().equals("item")) {
          JCas cas = GenericEntity2JCas.getCas(ge, usedProperties);
          ae.process(cas);
          AnnotationIndex<Annotation> annotations = cas.getAnnotationIndex();
          Iterator<Annotation> iterator = annotations.iterator();
          while (iterator.hasNext()) {
            Annotation a = iterator.next();
            if (a instanceof IdentifiedAnnotation) {
              IdentifiedAnnotation ia = (IdentifiedAnnotation) a;
              if (ia.getOntologyConceptArr() !=null) {
                for (int i = 0; i < ia.getOntologyConceptArr().size(); i++) {
                  UmlsConcept concept = (UmlsConcept) ia.getOntologyConceptArr(i);
                  if (idMap.containsKey(concept.getCui())) {
                    EntityAnnotation ea = new EntityAnnotation(ge.getId(), idMap.get(concept.getCui()),
                            ge.getAccession(), concept.getCui(), 1f, false);
                    am.addAnnotation(ea);
                  }else {
                    notInExtract++;
                  }
                }
              }
            }
          }
        }
      }
      log.info("not in ontology:"+notInExtract);
    } catch (InvalidXMLException e) {
      throw new AnnotationException(e);
    } catch (ResourceInitializationException e) {
      throw new AnnotationException(e);
    } catch (UIMAException e) {
      throw new AnnotationException(e);
    }

    return am;
  }


}
