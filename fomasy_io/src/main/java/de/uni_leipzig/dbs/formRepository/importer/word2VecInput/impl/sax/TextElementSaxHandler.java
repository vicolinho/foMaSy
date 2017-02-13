package de.uni_leipzig.dbs.formRepository.importer.word2VecInput.impl.sax;

import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


import java.util.*;

/**
 * Created by christen on 20.01.2017.
 */


public class TextElementSaxHandler extends DefaultHandler {


  private Map<String, Set<String>> sections;
  private Set<String> elements;

  private StringBuffer sb;

  public TextElementSaxHandler(String[] elements) {
    super();
    this.elements = new HashSet<String>(Arrays.asList(elements));
    this.sections = new HashMap<String, Set<String>>();
    sb = new StringBuffer();
  }

  @Override
  public void startDocument() throws SAXException {
    super.startDocument();

  }

  @Override
  public void endDocument() throws SAXException {

  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (elements.contains(qName)){
      sb.delete(0,sb.length()-1);
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (elements.contains(qName)){
      Set<String> sect = this.sections.get(qName);
      if (sect == null){
        sect = new HashSet<String>();
        this.sections.put(qName, sect);
      }
      String sentence = sb.toString();
      sentence = sentence.replaceAll("(\n|\t)"," ");
      sentence = sentence.replaceAll("\\s+"," ").trim();
      if (sentence.length()!=0)
        sect.add(sentence);

    }
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
   sb.append(new String(ch,start,length));
  }

  public Map<String, Set<String>> getSections() {
    return sections;
  }

  public void setSections(Map<String, Set<String>> sections) {
    this.sections = sections;
  }

  public Set<String> getElements() {
    return elements;
  }

  public void setElements(Set<String> elements) {
    this.elements = elements;
  }
}
