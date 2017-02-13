package de.uni_leipzig.dbs.formRepository.evaluation.tool.wrapper;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;

import java.util.Properties;

/**
 * Created by christen on 09.02.2017.
 */
public interface AnnotationWrapper {


  AnnotationMapping computeMapping(EntityStructureVersion esv, Properties prop);
}
