package de.uni_leipzig.dbs.formRepository.importer.annotation.odm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportAnnotationMapping;

public class MDMAnnotationSAXParser extends DefaultHandler {

	private Stack<String> stack;
	private String prefix;
	ImportAnnotationMapping iam;
	String currentItem;
	
	public HashMap<String,List<ImportAnnotation>> annotationMapping;

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		this.stack = new Stack<String>();
		this.iam = new ImportAnnotationMapping();
		this.annotationMapping = new HashMap<String,List<ImportAnnotation>>();
		this.iam.setAnnotations(annotationMapping);
		
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.endDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attrs) throws SAXException {
		// TODO Auto-generated method stub
		stack.push(qName);
	
		if (qName.equalsIgnoreCase("ItemDef")){
			String id = prefix+":"+attrs.getValue("OID").trim();
			this.currentItem = id;
			List<ImportAnnotation> list = annotationMapping.get(id);
			if (list!= null){
				list = new ArrayList<ImportAnnotation>();
				this.annotationMapping.put(id, list);
			}
			
		}else if(qName.equalsIgnoreCase("alias")){
			if (currentItem!=null){
				List <ImportAnnotation> umls = annotationMapping.get(currentItem);
				if (umls ==null){
					umls = new ArrayList<ImportAnnotation>();
					annotationMapping.put(currentItem, umls);
				}
				ImportAnnotation ia = new ImportAnnotation (currentItem, attrs.getValue("Name").trim(), 1, true);
				umls.add(ia);
				
			}
			
		}else if (qName.equalsIgnoreCase("study")){
			prefix = attrs.getValue("OID").trim();
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO Auto-generated method stub
		String element = stack.pop();
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		super.characters(ch, start, length);
	}

	public MDMAnnotationSAXParser() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ImportAnnotationMapping getMapping() {
		// TODO Auto-generated method stub
		return iam;
	}

}
