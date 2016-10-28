package de.uni_leipzig.dbs.formRepository.importer;

import java.util.List;

import de.uni_leipzig.dbs.formRepository.api.APIFactory;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportRelationship;
import de.uni_leipzig.dbs.formRepository.exception.ImportException;

public abstract class PreSourceImporter {

	private EntityStructureImporter mainImporter ;
	
	




	protected List<ImportEntity> ents;
	protected List<ImportRelationship> rels;

	public EntityStructureImporter getMainImporter() {
		return mainImporter;
	}

	public void setMainImporter(EntityStructureImporter mainImporter) {
		this.mainImporter = mainImporter;	
	}

	protected  void importIntoTmpTables() throws ImportException{
		if (ents!=null)
			APIFactory.getInstance().getImportAPI().importTmpEntities(ents);
		if (rels!=null)
			APIFactory.getInstance().getImportAPI().importTmpRelationships(rels);
	}

	protected abstract void loadSourceData() throws ImportException ;

	public List<ImportEntity> getEnts() {
		return ents;
	}

	public void setEnts(List<ImportEntity> ents) {
		this.ents = ents;
	}

	public List<ImportRelationship> getRels() {
		return rels;
	}

	public void setRels(List<ImportRelationship> rels) {
		this.rels = rels;
	}


}
