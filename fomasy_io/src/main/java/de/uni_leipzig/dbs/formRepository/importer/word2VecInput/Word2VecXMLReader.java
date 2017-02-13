package de.uni_leipzig.dbs.formRepository.importer.word2VecInput;



import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 20.01.2017.
 */
public interface Word2VecXMLReader {

  Map<String,Set<String>> readData(String file, String[] elements)
          throws ParserConfigurationException, SAXException, IOException;
}
