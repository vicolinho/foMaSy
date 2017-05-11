package de.uni_leipzig.dbs.formRepository.evaluation.tool.dictionary.io;

import com.sun.xml.internal.ws.api.streaming.XMLStreamWriterFactory;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportProperty;
import de.uni_leipzig.dbs.formRepository.importer.EntityStructureImporter;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

/**
 * Created by christen on 09.02.2017.
 */
public class ConceptMapperDictBuilder {


  public static final String ENTRY = "token";

  public static final String SYN = "variant";

  public static final String VALUE= "base";

  public static final String POS_FEATURE = "POS";


  private String name;
  public void writeDictionary (File out, SourceReader reader, Properties srcProperties) throws FileNotFoundException, XMLStreamException {

    List<ImportEntity> esv = reader.readSource(srcProperties);
    name = (String)srcProperties.get(EntityStructureImporter.NAME);
    this.writeXMLDict(out, esv);

  }

  private void writeXMLDict (File outXmlFile, List<ImportEntity> esv) throws FileNotFoundException, XMLStreamException {
    FileOutputStream fileOutputStream = new FileOutputStream(outXmlFile);
    javax.xml.stream.XMLStreamWriter writer= XMLStreamWriterFactory.create(fileOutputStream, "UTF-8");

    writer.writeStartDocument();
    writer.writeStartElement("dictionary");
    for (ImportEntity ge: esv){
      Iterator<ImportProperty> iter =ge.getProperties().iterator();
      List<ImportProperty> semProp = new ArrayList<>();
      while (iter.hasNext()){
        ImportProperty gp = iter.next();
        if (gp.getName().contains("sem_type")){
          semProp.add(gp);
          iter.remove();
        }
      }

      writer.writeStartElement(ENTRY);
      writer.writeAttribute("CodeType",name);
      writer.writeAttribute("CodeValue", ge.getAccession());
      if (semProp.size()!=0){
        writer.writeAttribute("SemClass", semProp.get(0).getValue());
      }

      for (ImportProperty gp: ge.getProperties()){
          writer.writeStartElement(SYN);
          writer.writeAttribute(VALUE, gp.getValue());
          writer.writeEndElement();
      }
      writer.writeEndElement();
    }
    writer.writeEndElement();
    writer.writeEndDocument();
    writer.flush();
    writer.close();
  }


}
