package de.uni_leipzig.dbs.formRepository.importer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import de.uni_leipzig.dbs.formRepository.api.APIFactory;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportAnnotationMapping;
import de.uni_leipzig.dbs.formRepository.exception.ImportAnnotationException;
import de.uni_leipzig.dbs.formRepository.importer.annotation.AnnotationFileParser;
import de.uni_leipzig.dbs.formRepository.importer.odm.MetadataInFileReader;
import de.uni_leipzig.dbs.formRepository.util.MappingImportMetadata;

public class AnnotationImporter {

	public static final String SRC_FILE = "srcMappingFile";
	
	public static final String SRC_VERSION_NAME = "srcVersionName";
	
	public static final String TARGET_VERSION_NAME = "targetVersionName";
	
	public static final String SRC_TYPE = "srcType";
	
	public static final String SRC_VERSION_DATE = "srcDate";
	
	public static final String TARGET_VERSION_DATE  = "targetDate";
	
	public static final String TARGET_TYPE = "targetType";
	
	public static final String PARSER_CLASS = "parserClass";
	
	
	public void importAnnotationMapping( AnnotationMapping iam){
	
		
		List<VersionMetadata> metadata = APIFactory.getInstance().getAnnotationAPI().checkInvolvedVersion (iam.getSrcVersion(),iam.getTargetVersion());
		APIFactory.getInstance().getAnnotationAPI().importMapping(metadata, iam);
	}
	
	
	public void importExternalAnnotationMapping( MappingImportMetadata propertyMap) throws ImportAnnotationException{
		
		AnnotationFileParser parser = null;
		try {
			parser = (AnnotationFileParser) Class.forName((String) propertyMap.getParser()).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (propertyMap.isMetadataIsInFile()){
			MetadataInFileReader mi = new MetadataInFileReader();
			SAXParser saxParser;
			try {
				saxParser = SAXParserFactory.newInstance().newSAXParser();
				saxParser.parse(propertyMap.getMappingFile(), mi);
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				
				throw new ImportAnnotationException(e);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				
				throw new ImportAnnotationException(e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new ImportAnnotationException(e);
			}
			
			propertyMap.setSrcName((String) mi.getPropertyMap().get(EntityStructureImporter.NAME));
			propertyMap.setSrcFrom((String) mi.getPropertyMap().get(EntityStructureImporter.TIMESTAMP));
			
		}
		ImportAnnotationMapping iam = parser.parseSource(propertyMap);
		List <VersionMetadata> guranteedInvolvedStructures = APIFactory.getInstance().getAnnotationAPI().checkInvolvedVersion (iam.getSrcStruct(),iam.getTargetStruct());
		if (guranteedInvolvedStructures.size()==2){
			APIFactory.getInstance().getAnnotationAPI().importMapping(guranteedInvolvedStructures, iam);
		}
	}
	
	
}
