package de.uni_leipzig.dbs.formRepository.evaluation.tool.dictionary.execution;

import de.uni_leipzig.dbs.formRepository.evaluation.tool.dictionary.io.ConceptMapperDictBuilder;
import de.uni_leipzig.dbs.formRepository.evaluation.tool.dictionary.io.UMLSSourceReader;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Created by christen on 10.02.2017.
 */
public class ConceptMapperBuilder {

  public static void main (String[] args){
    if (args.length ==2){
      Properties p = new Properties();
      try {
        URL url = ClassLoader.getSystemResource(args[0]);
        p.load(new FileReader(url.getPath()));
        String fileName = args[1];
        File f = new File(fileName);
        ConceptMapperDictBuilder builder = new ConceptMapperDictBuilder();
        UMLSSourceReader reader = new UMLSSourceReader();
        builder.writeDictionary(f, reader, p);

      } catch (IOException e) {
        e.printStackTrace();
      } catch (XMLStreamException e) {
        e.printStackTrace();
      }
    }
  }
}
