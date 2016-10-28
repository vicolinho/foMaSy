package de.uni_leipzig.dbs.formRepository.importer;

import de.uni_leipzig.dbs.formRepository.api.APIFactory;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;

public class PostRelationImporter {

	
	
	EntityStructureImporter importer ;
	
	private VersionMetadata currentVersion;

	public PostRelationImporter (EntityStructureImporter importer){
		this.importer=importer;
	}
	
	public void importIntoRepository(){
		currentVersion = importer.getCurrentMetadata();
		determineDiffToPreviousVersion();
		this.importRelationships();
		this.cleanTmpTables();
		
	}
	
	private void cleanTmpTables() {
		// TODO Auto-generated method stub
		APIFactory.getInstance().getImportAPI().cleanTmpTables();
	}

	private void determineDiffToPreviousVersion(){
		APIFactory.getInstance().getImportAPI().determineRelDiff(currentVersion);
		
		
	}
	
	private void importRelationships(){
		APIFactory.getInstance().getImportAPI().importRelsForVersion(currentVersion);
	}
}
