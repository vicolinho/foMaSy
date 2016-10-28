package de.uni_leipzig.dbs.formRepository.importer.main;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.exception.ImportException;

public class OntologyRelationMainImporter {

	public static void main (String[] args){
			String iniFile = args[0];
			String propfile = args[1];
			FormRepository rep = new FormRepositoryImpl();
			try {
				PropertyConfigurator.configure("log4j.properties");
				rep.initialize(iniFile);
				Map <String,Object> props = new HashMap<String,Object>();
				Properties prop = new Properties();
				prop.load(new FileReader(propfile));
				for (Entry<Object, Object> p: prop.entrySet()){
					props.put((String)p.getKey(), p.getValue());
				}
				rep.getFormManager().importRelationshipsForVersion(props);
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
