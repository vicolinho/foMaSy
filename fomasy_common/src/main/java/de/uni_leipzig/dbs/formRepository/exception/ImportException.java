package de.uni_leipzig.dbs.formRepository.exception;

import javax.xml.parsers.ParserConfigurationException;

public class ImportException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3108590655575049434L;
	
	
	public ImportException (String message){
		super ("Faliure by import: "+message);
	}


	public ImportException(Exception e) {
		super(e);
	}

}
