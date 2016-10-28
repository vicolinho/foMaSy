package de.uni_leipzig.dbs.formRepository.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {

	
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	public static Date getDate (String date ){
		try {
			return sdf.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}
	
	public static String getFormattedDate (Date date ){
		return sdf.format(date);
	}
}
