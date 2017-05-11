package de.uni_leipzig.dbs.formRepository.import_execution;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.exception.ImportException;

public class OntologyMainImporter {

  
  
  public static void main (String[] args){
    String iniFile = args[0];
    String propfile = args[1];
    FormRepository rep = new FormRepositoryImpl();
    try {
      rep.initialize(iniFile);
      rep.getFormManager().importForm(propfile);
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
