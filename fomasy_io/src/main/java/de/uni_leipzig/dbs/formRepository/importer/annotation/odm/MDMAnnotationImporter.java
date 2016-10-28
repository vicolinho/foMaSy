package de.uni_leipzig.dbs.formRepository.importer.annotation.odm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportAnnotationMapping;
import de.uni_leipzig.dbs.formRepository.exception.ImportAnnotationException;
import de.uni_leipzig.dbs.formRepository.importer.annotation.AnnotationFileParser;
import de.uni_leipzig.dbs.formRepository.importer.AnnotationImporter;
import de.uni_leipzig.dbs.formRepository.util.DateFormatter;
import de.uni_leipzig.dbs.formRepository.util.MappingImportMetadata;

public class MDMAnnotationImporter extends AnnotationFileParser{

	
	public MDMAnnotationImporter(){
		super();
	}
	
	
	@Override
	public ImportAnnotationMapping parseSource(MappingImportMetadata properties ) throws ImportAnnotationException {
		String srcFile = (String) properties.getMappingFile();
		MDMAnnotationSAXParser parser = new MDMAnnotationSAXParser();
		try {
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(srcFile, parser);
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ImportAnnotationException(e);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ImportAnnotationException(e);
		}
		ImportAnnotationMapping iam = parser.getMapping();
		iam.setMethod("manual");
		Date srcFrom = DateFormatter.getDate(properties.getSrcFrom());
		String srcName = (String) properties.getSrcName();
		String srcType = (String) properties.getSrcType();
		VersionMetadata srcVersion = new VersionMetadata(0, srcFrom, null, srcName, srcType);
		
		Date targetFrom = DateFormatter.getDate((String) properties.getTargetFrom());
		String targetName = (String) properties.getTargetName();
		String targetType = (String) properties.getTargetType();
		if (properties.getName()==null){
			String name = this.createMappingName(srcName, targetName, srcType, targetType, properties.getPrefix());
			iam.setName(name);
		}
		
		VersionMetadata targetVersion = new VersionMetadata(0, targetFrom, null, targetName, targetType);
		iam.setSrcStruct(srcVersion);
		iam.setTargetStruct(targetVersion);;
		
		return iam;
	} 

}
