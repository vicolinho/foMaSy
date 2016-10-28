package de.uni_leipzig.dbs.formRepository.util;

public class MappingImportMetadata {

	
	private String name ; 
	
	private String methodName;
	
	private boolean metadataIsInFile;
	
	private String targetName;
	
	private String targetType;
	
	private String targetFrom;
	
	private String srcName;
	
	private String srcType;
	
	private String srcFrom;
	
	private String mappingFile;

	private String parser;

	private String prefix;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public boolean isMetadataIsInFile() {
		return metadataIsInFile;
	}

	public void setMetadataIsInFile(boolean metadataIsInFile) {
		this.metadataIsInFile = metadataIsInFile;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getTargetFrom() {
		return targetFrom;
	}

	public void setTargetFrom(String targetFrom) {
		this.targetFrom = targetFrom;
	}

	public String getSrcName() {
		return srcName;
	}

	public void setSrcName(String srcName) {
		this.srcName = srcName;
	}

	public String getSrcType() {
		return srcType;
	}

	public void setSrcType(String srcType) {
		this.srcType = srcType;
	}

	public String getSrcFrom() {
		return srcFrom;
	}

	public void setSrcFrom(String srcFrom) {
		this.srcFrom = srcFrom;
	}

	public String getMappingFile() {
		return mappingFile;
	}

	public void setMappingFile(String mappingFile) {
		this.mappingFile = mappingFile;
	}

	public void setParser(String string) {
		this.parser = string;
		
	}

	public String getParser() {
		return parser;
	}

	public String getPrefix() {
		// TODO Auto-generated method stub
		return this.prefix;
	}

	public void setSuffix(String prefix) {
		this.prefix = prefix;
	}
}
