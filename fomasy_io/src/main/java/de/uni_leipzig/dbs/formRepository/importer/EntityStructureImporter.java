package de.uni_leipzig.dbs.formRepository.importer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import de.uni_leipzig.dbs.formRepository.api.APIFactory;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportRelationship;
import de.uni_leipzig.dbs.formRepository.exception.ImportException;
import de.uni_leipzig.dbs.formRepository.util.DateFormatter;

public class EntityStructureImporter {

	
	public static final String NAME = "formName";
	public static final String TIMESTAMP = "timestamp";
	public static final String TOPIC ="topic";
	public static final String SOURCE ="source";
	public static final String SOURCE_TYPE = "sourceType";
	public static final String USER = "user";
	public static final String  PW = "password";
	public static final String IMPORTER_CLASS = "importerClass";
	//public static final String COMPACT_VERSION ="compactVersion";
	public static final String DESCR = "description";
	public static final String IS_RELATIONSHIP_IMPORT = "isRelationshipImport";
	
	private String formName; 
	private String timeStamp;
	private String topic;
	private String source;
	private  List<ImportEntity> entities;
	private  List<ImportRelationship> rels;
	private PreSourceImporter preImporter;
	private PostSourceImporter postImporter;
	private PostRelationImporter postRelationImporter;
	private boolean isRelationImport;
	private String user;
	private String pw;
	private VersionMetadata previousVersion;
	private VersionMetadata currentMetadata;
	private String sourceType;
	private String descr;
	
	
	public void importEntityStructure (Map<String, Object> properties) throws InstantiationException, IllegalAccessException,
	ClassNotFoundException, ImportException{
		
		
		formName = (String) properties.get(NAME);
		timeStamp = (String) properties.get(TIMESTAMP);
		topic = (String) properties.get(TOPIC);
		source = (String) properties.get(SOURCE);
		sourceType = (String) properties.get(SOURCE_TYPE);
		setDescription((String) properties.get(DESCR));
		String isRelImport = (String) properties.get(IS_RELATIONSHIP_IMPORT);
		if (isRelImport!=null){
			setRelationImport(Boolean.parseBoolean(isRelImport));
		}else{
			setRelationImport(false);
		}
		currentMetadata = new VersionMetadata(-1,DateFormatter.getDate(timeStamp),null,formName,topic);
		boolean exists =APIFactory.getInstance().getImportAPI().checkVersion(currentMetadata);
		
		
		if (!exists){
			previousVersion = APIFactory.getInstance().getImportAPI().getPreviousVersion(currentMetadata);
			String className = (String) properties.get(IMPORTER_CLASS);
			preImporter = (PreSourceImporter) Class.forName(className).newInstance();
			this.preImporter.setMainImporter(this);
			if (sourceType.equals("rdbms")){
				user = (String) properties.get(USER);
				pw = (String) properties.get(PW);
			}
			if (sourceType.equals("obj")){
				this.entities = (List<ImportEntity>) properties.get("entities");
				this.rels = (List<ImportRelationship>) properties.get("relationship");
				this.preImporter.setEnts(entities);
				this.preImporter.setRels(rels);
				this.preImporter.importIntoTmpTables();
			}else{
				try{
				this.preImporter.loadSourceData();
				}catch (ImportException e){
					System.out.println("import aborted for:"+formName+" type:"+ topic +" version:"+timeStamp);
					return;
				}
				System.out.println("loaded data for:"+formName+" type:"+ topic +" version:"+timeStamp);
				try{
					this.preImporter.importIntoTmpTables();
				}catch (ImportException e){
					System.out.println("import aborted for:"+formName+" type:"+ topic +" version:"+timeStamp);
					return;
				}
				System.out.println("loaded data in staging area");
			}
			
			postImporter = new PostSourceImporter (this);
			//postImporter.isCompactWithDiff = isCompactVersion;
			postImporter.importIntoRepository();
		}else {
			System.out.println ("version:"+formName+" type:"+ topic +" from:"+timeStamp+" already exists");
		}
		
	}
	
	
	
	
	public void importForm (String file) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, ImportException{
		Properties prop = new Properties();
		prop.load(new BufferedReader(new FileReader(file)));
		Map <String,Object> props = new HashMap<String,Object>();
		for (Entry<Object,Object>e:prop.entrySet()){
			props.put((String)e.getKey(), e.getValue());
		}
		this.importEntityStructure(props);
	}
	public String getEntityStructureName() {
		return formName;
	}
	
	public void importRelsToExistingVersion(Map<String,Object> properties) throws ImportException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		formName = (String) properties.get(NAME);
		timeStamp = (String) properties.get(TIMESTAMP);
		topic = (String) properties.get(TOPIC);
		source = (String) properties.get(SOURCE);
		//target = (String) properties.get(TARGET);
		sourceType = (String) properties.get(SOURCE_TYPE);
		setDescription((String) properties.get(DESCR));
		currentMetadata = new VersionMetadata(-1,DateFormatter.getDate(timeStamp),null,formName,topic);
		String className = (String) properties.get(IMPORTER_CLASS);
		boolean exists =APIFactory.getInstance().getImportAPI().checkVersion(currentMetadata);
		if (exists){
			preImporter = (PreSourceImporter) Class.forName(className).newInstance();
			this.preImporter.setMainImporter(this);
			if (sourceType.equals("rdbms")){
				user = (String) properties.get(USER);
				pw = (String) properties.get(PW);
			}
			if (sourceType.equals("obj")){
				this.rels = (List<ImportRelationship>) properties.get("relationship");
				this.preImporter.setRels(rels);
			}else{
				try{
				this.preImporter.loadSourceData();
				}catch (ImportException e){
					System.out.println("import aborted for:"+formName+" type:"+ topic +" version:"+timeStamp);
					return;
				}
			}
			try{
				this.preImporter.importIntoTmpTables();
			}catch (ImportException e){
				System.out.println("import aborted for:"+formName+" type:"+ topic +" version:"+timeStamp);
				return;
			}
			postRelationImporter = new PostRelationImporter (this);
			//postRelationImporter.isCompactWithDiff = isCompactVersion;
			postRelationImporter.importIntoRepository();
			
		}
	}

	

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getTopic() {
		return topic;
	}

	

	public String getSource() {
		return source;
	}

	public void setSource(String s){
		this.source = s;
	}


	public VersionMetadata getCurrentMetadata() {
		return currentMetadata;
	}



	public void setCurrentMetadata(VersionMetadata currentMetadata) {
		this.currentMetadata = currentMetadata;
	}



	public VersionMetadata getPreviousVersion() {
		return previousVersion;
	}



	public void setPreviousVersion(VersionMetadata previousVersion) {
		this.previousVersion = previousVersion;
	}



//	public boolean isCompactVersion() {
//		return isCompactVersion;
//	}
//
//
//
//	public void setCompactVersion(boolean isCompactVersion) {
//		this.isCompactVersion = isCompactVersion;
//	}



	public String getDescription() {
		return descr;
	}



	public void setDescription(String descr) {
		this.descr = descr;
	}



	public String getUser() {
		return user;
	}



	public void setUser(String user) {
		this.user = user;
	}



	public String getPw() {
		return pw;
	}



	public void setPw(String pw) {
		this.pw = pw;
	}



	public String getSourceType() {
		return sourceType;
	}



	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}



	public boolean isRelationImport() {
		return isRelationImport;
	}



	public void setRelationImport(boolean isRelationImport) {
		this.isRelationImport = isRelationImport;
	}

	

}
