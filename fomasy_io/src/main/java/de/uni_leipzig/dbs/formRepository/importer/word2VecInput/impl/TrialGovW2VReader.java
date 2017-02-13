package de.uni_leipzig.dbs.formRepository.importer.word2VecInput.impl;


import de.uni_leipzig.dbs.formRepository.importer.word2VecInput.Word2VecXMLReader;
import de.uni_leipzig.dbs.formRepository.importer.word2VecInput.impl.sax.TextElementSaxHandler;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by christen on 20.01.2017.
 */
public class TrialGovW2VReader implements Word2VecXMLReader {


  public Map<String,Set<String>> readData(String f, String[] elements)
          throws ParserConfigurationException, org.xml.sax.SAXException, IOException {
    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
    TextElementSaxHandler handler = new TextElementSaxHandler(elements);
    parser.parse(new File(f), handler);
    Map<String, Set<String>> textBlocksPerType = handler.getSections();
    return textBlocksPerType;
  }
}
