package de.uni_leipzig.dbs.formRepository.import_execution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.PropertyConfigurator;
import org.xml.sax.SAXException;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.exception.ImportException;
import de.uni_leipzig.dbs.formRepository.exception.MetadataReadException;
import de.uni_leipzig.dbs.formRepository.importer.odm.MetadataInFileReader;
import de.uni_leipzig.dbs.formRepository.importer.EntityStructureImporter;

public class ODMMainImporter {

  
  
  public void readMetaDataFromProperties(String propertyFile,FormRepository fr) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, ImportException{
    Properties prop = new Properties();
    prop.load(new FileReader(propertyFile));
    
    HashMap<String,Object> propertyMap = new HashMap<String,Object>();
    propertyMap.put(EntityStructureImporter.IMPORTER_CLASS, "de.uni_leipzig.dbs.formRepository.importer.odm.MDMFormImporter");
    propertyMap.put(EntityStructureImporter.TOPIC, prop.get(EntityStructureImporter.TOPIC));
    propertyMap.put(EntityStructureImporter.SOURCE_TYPE, "file");
    propertyMap.put(EntityStructureImporter.SOURCE, prop.get(EntityStructureImporter.SOURCE));
    propertyMap.put(EntityStructureImporter.TIMESTAMP, prop.get(EntityStructureImporter.TIMESTAMP));
    propertyMap.put(EntityStructureImporter.NAME, prop.get(EntityStructureImporter.NAME));
    fr.getFormManager().importForm(propertyMap);
  }
  
  public void readMetaDataFromFile(String folder,String type,boolean recursive,FormRepository fr) throws MetadataReadException {
    File f = new File (folder);
    PropertyConfigurator.configure("log4j.properties");
    SAXParser parser = null;
    try {
      parser = SAXParserFactory.newInstance().newSAXParser();
    } catch (ParserConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
      throw new  MetadataReadException("error by reading metadata");
      
    }
    MetadataInFileReader mr = new MetadataInFileReader();
    
    HashMap<String,Object> propertyMap = new HashMap<String,Object>();
    propertyMap.put(EntityStructureImporter.IMPORTER_CLASS, "de.uni_leipzig.dbs.formRepository.importer.odm.MDMFormImporter");
    propertyMap.put(EntityStructureImporter.TOPIC, type);
    propertyMap.put(EntityStructureImporter.SOURCE_TYPE, "file"); 
    if (f.isDirectory()){
      for (File filePath: f.listFiles()){
        if (filePath.isDirectory()&&recursive){
          
        }else{
          propertyMap.put(EntityStructureImporter.SOURCE, filePath.getAbsolutePath());
          propertyMap = this.readMetadata(filePath.getAbsolutePath(), propertyMap, parser, mr);
          try {
            fr.getFormManager().importForm(propertyMap);
          } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (ImportException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    }
  }
  
  private HashMap<String,Object>  readMetadata (String file,  HashMap<String,Object> pm, SAXParser parser,MetadataInFileReader mr) throws MetadataReadException {
    try {
      parser.parse(file, mr);
      pm.putAll(mr.getPropertyMap());
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new  MetadataReadException("error by reading metadata");
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new  MetadataReadException("error by reading metadata");
    }
  
    return pm;
    
  }
  public static String usage() {
    return "\nusage: MappingImporter <configFileNameLocation> (-folder=<folderOfFileNameLocation> -type=<typeOfSource>"+
        System.getProperty("line.separator")+ "| -propFile=<propertyFile> )\n\n";
  }
  
  public static void main (String[] args){
    
    String fmini = args[0];
    FormRepository fr = new FormRepositoryImpl();
    ODMMainImporter importer = new ODMMainImporter();
    try {
      fr.initialize(fmini);
      if (args[1].startsWith("-folder")){
        String folder = args[1].replace("-folder=", "");
        String type ;
        if (args[2].startsWith("-type")){
          type = args[2].replace("-type=", "");
          type = type.replaceAll("_", " ");
          importer.readMetaDataFromFile(folder, type, false, fr);
        }
      }else if (args[1].startsWith("-propFile")){
        importer.readMetaDataFromProperties(args[1], fr);
      }else {
        System.out.println(usage());
      }
    } catch (InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InitializationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MetadataReadException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ImportException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
  
}
