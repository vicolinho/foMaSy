package de.uni_leipzig.dbs.formRepository.importer.annotation;

import java.util.Map;

import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportAnnotationMapping;
import de.uni_leipzig.dbs.formRepository.exception.ImportAnnotationException;
import de.uni_leipzig.dbs.formRepository.util.MappingImportMetadata;

public abstract class AnnotationFileParser {

	
	public abstract ImportAnnotationMapping parseSource(MappingImportMetadata propertyMap) throws ImportAnnotationException;

	public String createMappingName(String srcName, String targetName,String srcType, String targetType, String prefix){
		StringBuffer sb = new StringBuffer();
		sb.append(srcName+"["+srcType+"]-"+targetName+"["+targetType+"]_"+prefix);
		return sb.toString();
		
		
	}
}
