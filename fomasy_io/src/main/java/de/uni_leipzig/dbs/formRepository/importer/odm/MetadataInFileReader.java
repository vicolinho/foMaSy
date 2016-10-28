package de.uni_leipzig.dbs.formRepository.importer.odm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.uni_leipzig.dbs.formRepository.importer.EntityStructureImporter;
import de.uni_leipzig.dbs.formRepository.util.DateFormatter;

public class MetadataInFileReader extends DefaultHandler{
	
	private Map<String, Object> propertyMap;

	Stack <String> stack = new Stack<String>();
	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		propertyMap = new HashMap<String,Object>();
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.endDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		stack.add(qName);
		if (qName.equals("ODM")){
			String date;
			try{
				date =  attributes.getValue("CreationDateTime").split("T")[0];
			}catch(NullPointerException e){
				date  = DateFormatter.getFormattedDate(new Date());
			}catch (IndexOutOfBoundsException e ){
				date  = DateFormatter.getFormattedDate(new Date());
			}
			propertyMap.put(EntityStructureImporter.TIMESTAMP,date);
		}else if (qName.equals("Study")){
			propertyMap.put(EntityStructureImporter.NAME,attributes.getValue("OID"));
		}
		
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		this.stack.pop();
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (stack.lastElement().equals("StudyDescription")){
			propertyMap.put(EntityStructureImporter.DESCR, new String(ch,start,length));
		}
	}

	public Map<String, Object> getPropertyMap() {
		return propertyMap;
	}

	public void setPropertyMap(Map<String, Object> propertyMap) {
		this.propertyMap = propertyMap;
	}

}
