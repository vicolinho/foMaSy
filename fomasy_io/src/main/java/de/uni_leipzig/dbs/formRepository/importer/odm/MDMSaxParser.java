package de.uni_leipzig.dbs.formRepository.importer.odm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;










import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportRelationship;

public class MDMSaxParser extends DefaultHandler{

	Logger log = Logger.getLogger(getClass());
	Stack<String> stack ;
	StringBuilder sb;
	String currentLang ;
	String prefix;
	HashMap<String,ImportEntity> entities;
	HashMap<String,ImportEntity> answers;
	HashMap<String,ImportEntity> groups;
	HashMap<String,ImportEntity> units;
	List<ImportRelationship> rels;
	ImportEntity currentGroup; 
	ImportEntity currentItem; 
	ImportEntity currentComplexResponse;
	ImportEntity currentUnit;
	ImportEntity currentRange;
	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		stack = new Stack<String>();
		this.entities = new HashMap<String,ImportEntity>();
		this.answers = new HashMap<String,ImportEntity>();
		this.groups = new HashMap<String, ImportEntity>();
		this.units = new HashMap<String, ImportEntity>();
		rels = new ArrayList<ImportRelationship>();
		
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.endDocument();
	}

	public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException{
		stack.push(qName);
		sb = new StringBuilder();
		if (attrs.getValue("xml:lang")!=null){
			if(attrs.getValue("xml:lang").equalsIgnoreCase("en")){
			currentLang = "EN";
			}else if (attrs.getValue("xml:lang").equalsIgnoreCase("de")){
				currentLang = "DE";
			}else {
				currentLang ="EN";
			}
			
		}
		 if (qName.equalsIgnoreCase("study")){
			prefix = attrs.getValue("OID");
		}else if (qName.equalsIgnoreCase("ItemGroupRef") && stack.get(stack.size()-2).equals("FormDef")){
			
			String id = prefix+":"+attrs.getValue("ItemGroupOID");
			ImportEntity ie = new ImportEntity (id, "group");
			if (attrs.getValue("OrderNumber")!=null)
				ie.addProperty("order_number", attrs.getValue("OrderNumber"), "int", "EN", null);
			groups.put(ie.getAccession(), ie);
		}else if (qName.equalsIgnoreCase("ItemGroupDef")){
			String id = prefix+":"+attrs.getValue("OID");
			ImportEntity g = groups.get(id);
			if (g ==null){
				g = new ImportEntity(id,"group");
				groups.put(id, g);
			}
			currentGroup = g;
			g.addProperty("name",attrs.getValue("Name"), "string", null, null);
			
			//g.values.put("lang", attrs.getValue("xml:lang"));
		}else if (qName.equalsIgnoreCase("ItemRef") && 
				stack.get(stack.size()-2).equals("ItemGroupDef")){
			ImportEntity i = new ImportEntity(prefix+":"+attrs.getValue("ItemOID"),"item");
			if (attrs.getValue("OrderNumber")!=null)
				i.addProperty("order_number", attrs.getValue("OrderNumber"), "int", "EN", null);
			entities.put(i.getAccession(), i);
			if (currentGroup!=null){
				ImportRelationship ir = new ImportRelationship(currentGroup.getAccession(),i.getAccession(),"contains",true);
				rels.add(ir);
			}
		}else if (qName.equalsIgnoreCase("ItemDef")){
			String id = prefix+":"+attrs.getValue("OID");
			ImportEntity i = entities.get(id);
			
			if (i ==null){
				i = new ImportEntity(id,"item");
				entities.put(id, i);
			}
			currentItem = i;
			i.addProperty("name", attrs.getValue("Name"), "string", "EN", null);
			//i.values.put("lang", attrs.getValue("xml:lang"));
			if ( attrs.getValue("DataType")!=null)
			
			i.addProperty("datatype", attrs.getValue("DataType"), "string", "EN", null);
		}else if (qName.equalsIgnoreCase("CodeList")){
			String id = prefix+":"+attrs.getValue("OID");
			ImportEntity ie = new ImportEntity(id,"complex_response");
			ie.addProperty("name", attrs.getValue("Name"),"string", null, null);
			currentComplexResponse = ie;
			this.answers.put(id, ie);
		}else if (qName.equalsIgnoreCase("CodeListref")){
			ImportRelationship ir = new ImportRelationship(currentItem.getAccession(),attrs.getValue("CodeListOID"), "has_response",true);
			this.rels.add(ir);
		}else if (qName.equalsIgnoreCase("MeasurementUnit")){
			String id = prefix+":"+attrs.getValue("OID");
			ImportEntity ie = new ImportEntity (id,"response_unit");
			this.currentUnit = ie;
			this.units.put(id, ie);
		}else if (qName.equalsIgnoreCase("MeasurementUnitRef")){
			ImportEntity unit = this.units.get(prefix+":"+attrs.getValue("MeasurementUnitOID"));
			String symbol = unit.getProperties().iterator().next().getValue();
			currentItem.addProperty("response_unit", symbol, "String", "EN", null);
		}else if (qName.equalsIgnoreCase("RangeCheck")){
			currentRange = new ImportEntity(attrs.getValue("Comparator"),"range");
		}
	}
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		String elem = stack.pop();
		if (currentGroup!=null){
			if (stack.get(stack.size()-2).equals("ItemGroupDef")&&
					elem.equals("TranslatedText")){
				this.groups.get(currentGroup.getAccession()).addProperty("description",
						sb.toString(),"string",currentLang, null);
			}
		}else if (currentItem != null){
			String esc = sb.toString();
			if ( stack.get(stack.size()-1).equalsIgnoreCase("question")&&
				elem.equalsIgnoreCase("TranslatedText")){
				this.entities.get(currentItem.getAccession()).addProperty("question",
						esc,"string",currentLang, null);
			}else if ( stack.get(stack.size()-1).equalsIgnoreCase("Description")&&
					elem.equalsIgnoreCase("TranslatedText")){
					this.entities.get(currentItem.getAccession()).addProperty("description",
							sb.toString(),"string",currentLang, null);
			}
		}else if (currentComplexResponse != null){
			if ( stack.get(stack.size()-1).equalsIgnoreCase("decode")&&
					elem.equalsIgnoreCase("TranslatedText")){
				this.currentComplexResponse.addProperty("response_value", sb.toString(), "string", currentLang, null);
			}
		}else if (currentUnit!=null){
			if ( stack.get(stack.size()-1).equalsIgnoreCase("Symbol")&&
					elem.equalsIgnoreCase("TranslatedText")){
				currentUnit.addProperty("symbol", sb.toString(),"String",currentLang,null);
			}
		}else if (currentRange!=null){
			if ( stack.get(stack.size()-1).equalsIgnoreCase("RangeCheck")&&
					elem.equalsIgnoreCase("CheckValue")){
				String range = currentRange.getAccession()+" "+ sb.toString();
				currentItem.addProperty("response_range", range, "string", currentLang, null);
			}
		}
		
		if(qName.equals("ItemGroupDef")){
			currentGroup =null;
		}else if (qName.equals("ItemDef")){
			currentItem = null;
		}else if (qName.equals("TranslatedText")||qName.equals("CheckValue")){
			sb.delete(0, sb.length());
		}else if (qName.equalsIgnoreCase("CodeList")){
			currentComplexResponse = null;
		}else if (qName.equalsIgnoreCase("MeasurementUnit")){
			currentUnit = null;
		}else if (qName.equalsIgnoreCase("RangeCheck")){
			currentRange = null;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		String v =  new String(ch,start,length).trim();
		if (v.length()!=0){
			sb.append(v);
		}
	}

	public HashMap<String, ImportEntity> getEntities() {
		return entities;
	}

	public void setEntities(HashMap<String, ImportEntity> entities) {
		this.entities = entities;
	}

	public HashMap<String, ImportEntity> getAnswers() {
		return answers;
	}

	public void setAnswers(HashMap<String, ImportEntity> answers) {
		this.answers = answers;
	}

	public HashMap<String, ImportEntity> getGroups() {
		return groups;
	}

	public void setGroups(HashMap<String, ImportEntity> groups) {
		this.groups = groups;
	}

	public List<ImportRelationship> getRels() {
		return rels;
	}

	
	
}
