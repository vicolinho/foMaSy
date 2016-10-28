package de.uni_leipzig.dbs.formRepository.importer.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.exception.ImportAnnotationException;
import de.uni_leipzig.dbs.formRepository.exception.MetadataReadException;
import de.uni_leipzig.dbs.formRepository.util.AnnotationPropertyReader;
import de.uni_leipzig.dbs.formRepository.util.MappingImportMetadata;

public class ODMAnnotationMainImporter {
	
	
	private void readMetaDataFromProperties(String string, FormRepository fr) {
		// TODO Auto-generated method stub
		
	}
	public void readMetaDataFromFile(String folder,String annotationFile,FormRepository fr) throws MetadataReadException, ImportAnnotationException {
		File f = new File (folder);
	MappingImportMetadata metadata= null;
		try {
			metadata = AnnotationPropertyReader.readPropertyFile(annotationFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (f.isDirectory()){
			for (File filePath: f.listFiles()){
				if (filePath.isDirectory()){
					
				}else{
					metadata.setMappingFile(filePath.getAbsolutePath());
					fr.getMappingManager().importExternalAnnotation(metadata);
				}
			}
		}
	}
	
	
	public static void main (String[]args){
		PropertyConfigurator.configure("log4j.properties");
		String fmini = args[0];
		FormRepository fr = new FormRepositoryImpl();
		ODMAnnotationMainImporter importer = new ODMAnnotationMainImporter();
		try {
			fr.initialize(fmini);
			if (args[1].startsWith("-folder")){
				String folder = args[1].replace("-folder=", "");
				if (args[2].startsWith("-metafile")){
					String metaFile = args[2].replace("-metafile=", "");
					importer.readMetaDataFromFile(folder, metaFile,fr);
				}
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
		
		} catch (ImportAnnotationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
