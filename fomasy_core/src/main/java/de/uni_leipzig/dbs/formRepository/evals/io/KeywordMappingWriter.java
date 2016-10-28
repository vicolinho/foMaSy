package de.uni_leipzig.dbs.formRepository.evals.io;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;

public class KeywordMappingWriter {

	
	public void writeKeywordMapping(String fileName, AnnotationMapping am, EntityStructureVersion esv){
		try {
			FileWriter fw = new FileWriter(fileName);
			fw.append("keywords\tconcept\tsimilarity"+System.getProperty("line.separator"));
			for (EntityAnnotation a :am.getAnnotations()){
				List <String> keywords = esv.getEntity(a.getSrcId()).getPropertyValues("keyword",
						null, null);
				fw.append(keywords.toString()+"\t"+a.getTargetAccession()+"\t"+a.getSim()+System.getProperty("line.separator"));
				
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
