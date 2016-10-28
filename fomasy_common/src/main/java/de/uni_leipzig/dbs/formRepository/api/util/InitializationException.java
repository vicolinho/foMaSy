package de.uni_leipzig.dbs.formRepository.api.util;

public class InitializationException extends Exception {

	public InitializationException (String object){
		super ("initialization error caused by: " +object);
	}
}
