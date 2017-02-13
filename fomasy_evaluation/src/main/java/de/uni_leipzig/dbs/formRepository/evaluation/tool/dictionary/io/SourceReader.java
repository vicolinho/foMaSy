package de.uni_leipzig.dbs.formRepository.evaluation.tool.dictionary.io;

import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportEntity;

import java.util.List;
import java.util.Properties;

/**
 * Created by christen on 09.02.2017.
 */
public interface SourceReader {

  List<ImportEntity> readSource(Properties prop);
}
