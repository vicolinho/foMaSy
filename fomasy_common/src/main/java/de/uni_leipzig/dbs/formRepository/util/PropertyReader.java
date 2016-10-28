package de.uni_leipzig.dbs.formRepository.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropertyReader {

	
	public static Properties readIniFile (String file) throws IOException{
		Properties properties = new Properties ();
		BufferedReader br = new BufferedReader(new FileReader(file));
		properties.load(br);
		return properties;
	}
}
