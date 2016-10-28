package de.uni_leipzig.dbs.formRepository.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class AnnotationPropertyReader {

	public static final String SRC_FILE = "srcMappingFile";
	
	public static final String SRC_VERSION_NAME = "srcVersionName";
	
	public static final String TARGET_VERSION_NAME = "targetVersionName";
	
	public static final String SRC_TYPE = "srcType";
	
	public static final String SRC_VERSION_DATE = "srcDate";
	
	public static final String TARGET_VERSION_DATE  = "targetDate";
	
	public static final String TARGET_TYPE = "targetType";
	
	public static final String PARSER_CLASS = "parserClass";
	
	public static final String MAPPING_NAME = "mappingName";
	
	public static final String METHOD_NAME = "methodName";
	
	public static final String METADATA_IS_IN_FILE ="metadataIsInFile";
	
	public static final String PREFIX = "prefix";
	

	
	public static MappingImportMetadata readPropertyFile (String file) throws FileNotFoundException, IOException{
		MappingImportMetadata meta = new MappingImportMetadata();
		Properties props = new Properties();
		
		props.load(new FileReader (file));
		meta.setName(props.getProperty(MAPPING_NAME));
		meta.setMappingFile(props.getProperty(SRC_FILE));
		meta.setName(props.getProperty(MAPPING_NAME));
		meta.setMethodName(props.getProperty(METHOD_NAME));
		meta.setSrcFrom(props.getProperty(SRC_VERSION_DATE));
		meta.setTargetFrom(props.getProperty(TARGET_VERSION_DATE));
		meta.setSrcName(props.getProperty(SRC_VERSION_NAME));
		meta.setSrcType(props.getProperty(SRC_TYPE));
		meta.setTargetName(props.getProperty(TARGET_VERSION_NAME));
		meta.setTargetType(props.getProperty(TARGET_TYPE));
		meta.setSuffix(props.getProperty(PREFIX));
		meta.setParser(props.getProperty(PARSER_CLASS));
		boolean isInFile = false;
		try{
		isInFile  = (boolean) Boolean.parseBoolean((String) props.get(METADATA_IS_IN_FILE));
		}catch (NullPointerException e){};		
		meta.setMetadataIsInFile(isInFile);
		return meta;
	}
}
