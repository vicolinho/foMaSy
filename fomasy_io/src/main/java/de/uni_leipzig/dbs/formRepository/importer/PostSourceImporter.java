package de.uni_leipzig.dbs.formRepository.importer;

import de.uni_leipzig.dbs.formRepository.api.APIFactory;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;

public class PostSourceImporter {

	//boolean isCompactWithDiff;
	EntityStructureImporter importer ;
	private VersionMetadata ancestorVersion;
	private VersionMetadata currentVersion;
	
	public PostSourceImporter (EntityStructureImporter importer){
		this.importer=importer;
	}
	
	public void importIntoRepository(){
		ancestorVersion = importer.getPreviousVersion();
		currentVersion = importer.getCurrentMetadata();
		determineDiffToPreviousVersion();
		this.importStructure();
		this.cleanTmpTables();
		
	}
	
	private void cleanTmpTables() {
		// TODO Auto-generated method stub
		APIFactory.getInstance().getImportAPI().cleanTmpTables();
	}

	private void determineDiffToPreviousVersion(){
		APIFactory.getInstance().getImportAPI().determineDiff(ancestorVersion, currentVersion);
		
		
	}
	
	private void importStructure (){
		APIFactory.getInstance().getImportAPI().importVersion(ancestorVersion, currentVersion);
	}
	
}
