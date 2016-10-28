package de.uni_leipzig.dbs.formRepository.manager;

import java.util.HashMap;
import java.util.Map;

import de.uni_leipzig.dbs.formRepository.api.APIFactory;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.exception.ImportAnnotationException;
import de.uni_leipzig.dbs.formRepository.importer.AnnotationImporter;
import de.uni_leipzig.dbs.formRepository.util.MappingImportMetadata;

public class MappingManagerImpl implements MappingManager {

	

	public void importExternalAnnotation(MappingImportMetadata propertyMap) throws ImportAnnotationException {
		AnnotationImporter ai = new AnnotationImporter ();
		ai.importExternalAnnotationMapping(propertyMap);

	}

	public void importAnnotation(AnnotationMapping mapping) {
		AnnotationImporter ai = new AnnotationImporter ();
		ai.importAnnotationMapping(mapping);
		
	}

	public AnnotationMapping getAnnotationMapping(VersionMetadata src,
			VersionMetadata target, String name) {
		return APIFactory.getInstance().getAnnotationAPI().getAnnotationMapping(src, target, name);
		
	}

}
