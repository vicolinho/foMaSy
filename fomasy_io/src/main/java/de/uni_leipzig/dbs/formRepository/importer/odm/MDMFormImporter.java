package de.uni_leipzig.dbs.formRepository.importer.odm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportRelationship;
import de.uni_leipzig.dbs.formRepository.exception.ImportException;
import de.uni_leipzig.dbs.formRepository.importer.EntityStructureImporter;
import de.uni_leipzig.dbs.formRepository.importer.PreSourceImporter;

public class MDMFormImporter extends PreSourceImporter{


	@Override
	protected void loadSourceData() throws ImportException{
		
		EntityStructureImporter importer = this.getMainImporter();
		String source = importer.getSource();
		this.ents = new ArrayList<ImportEntity>();
		rels = new ArrayList<ImportRelationship>();
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			MDMSaxParser mdmHandler = new MDMSaxParser();
			File f = new File(source);
				try {
					parser.parse(f, mdmHandler);
					this.ents.addAll(mdmHandler.getEntities().values());
					this.ents.addAll(mdmHandler.getGroups().values());
					this.ents.addAll(mdmHandler.getAnswers().values());
					rels.addAll(mdmHandler.getRels());
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new ImportException ("errory by parsing source");
				}catch (Exception e ){
					e.printStackTrace();
					throw new ImportException ("errory by parsing source");
				}
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				
				e.printStackTrace();
				throw new ImportException ("errory by reading source");
			}catch (Exception e ){
				e.printStackTrace();
				throw new ImportException ("errory by reading source");
			}
	}

	
	
	
	

	
	
}
