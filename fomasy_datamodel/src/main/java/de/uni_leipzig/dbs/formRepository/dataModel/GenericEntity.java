package de.uni_leipzig.dbs.formRepository.dataModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

/**
 * This class represents an entity that is characterized with its properties.
 * An entity belongs to a specific EntityStructureVersion, which is defined by the srcVersionStructureId.
 * An entity have an arbitrary set of multi-value properties. A set of {@link PropertyValue} objects is available by 
 * {@code getPropertyValueSet(name,language,scope)} or by  {@code getPropertyValues(name,language,scope)} that return 
 * a list of strings.
 * @author christen
 *
 */
public class GenericEntity implements IEntity{

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenericEntity other = (GenericEntity) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GenericEntity [accession=" + accession + ", type=" + type + "]";
	}


	/**
	 * 
	 */
	private static final long serialVersionUID = 1567173584305850528L;
	private int id ; 
	private String accession;
	private String type;
	private StringPropertyValueMap propertyValueMap;
	private StringPropertyValueSet propertyValues;
	private PropertySet properties;
	private int srcVersionStructureId;
	Logger log = Logger.getLogger(getClass());
	
	
	public GenericEntity (int id, String accession, String type, int srcId){
		this.id =id;
		this.accession = accession ;
		if (type==null){
			this.type = "entity";
		}else{
			this.type = type;
		}
		this.propertyValueMap = new StringPropertyValueMap();
		this.propertyValues = new StringPropertyValueSet();
		this.properties = new PropertySet();
		this.srcVersionStructureId =srcId;
	}
	
	public Collection <GenericProperty> getProperties (){
		return  this.properties.getCollection();
	}
	
	public StringPropertyValueSet getPropertyValueSet (String attName, String lang,String scope){
		StringPropertyValueSet valueSet = new StringPropertyValueSet();
		if (this.properties.contains(attName,lang,scope)){
			Set<GenericProperty> prop = properties.getProperty(attName, scope, lang);
			for (GenericProperty p: prop){
				IntOpenHashSet pvIds = this.propertyValueMap.getPropertyValueIds(p);
				valueSet.addPropertyValues(this.propertyValues.getPropertyValues(pvIds));
			}
		}
		return valueSet;
	}
	
	
	
	public List<String> getPropertyValues (String attName, String lang,String scope){
		List<String> valueSet = new ArrayList<String>();
		if (this.properties.contains(attName,lang,scope)){
			Set<GenericProperty> prop = properties.getProperty(attName, scope, lang);
			for (GenericProperty p: prop){
				IntOpenHashSet pvIds = this.propertyValueMap.getPropertyValueIds(p);
				valueSet.addAll(this.propertyValues.getValues(pvIds));
			}
		}
		return valueSet;
	}
	
	public void changePropertyValues( StringPropertyValueSet newValues){
		
		this.propertyValues.changePropertyValues(newValues);
		
	}
	
	
	public void removeProperty (String attName, String lang,String scope){
		if (this.properties.contains(attName)){
			Set<GenericProperty> prop = properties.getProperty(attName, scope, lang);
			for (GenericProperty p:prop){
				this.properties.removeProperty(p);
				IntOpenHashSet set = this.propertyValueMap.getPropertyValueIds(p);
				for (int id : set){
					this.propertyValues.removePropertyValues(id);
				}
				this.propertyValueMap.removeProperty(p);
			}
		}
	}
	
	public void removePropertyValue (String attName, String lang,String scope,PropertyValue pv){
		if (this.properties.contains(attName)){
			Set<GenericProperty> prop = properties.getProperty(attName, scope, lang);
			for (GenericProperty p :prop){
				this.propertyValueMap.removePropertyValue(p, pv);
				this.propertyValues.removePropertyValues(pv.getId());
				if (this.propertyValueMap.getPropertyValueIds(p)==null){
					this.properties.removeProperty(p);
				}
			}
			
		}
	}
	
	
	
	

	public StringPropertyValueSet getValues(GenericProperty property) {
		StringPropertyValueSet valueSet = new StringPropertyValueSet();
		if (this.properties.contains(property)){
			IntOpenHashSet pvIds = this.propertyValueMap.getPropertyValueIds(property);
			valueSet.addPropertyValues(this.propertyValues.getPropertyValues(pvIds));
			
		}
		return valueSet;
	}
	
	public List<String> getStringValues(GenericProperty property) {
		List<String> valueSet = new ArrayList<String>();
		if (this.properties.contains(property)){
			IntOpenHashSet pvIds = this.propertyValueMap.getPropertyValueIds(property);
			valueSet.addAll(this.propertyValues.getValues(pvIds));
			
		}
		return valueSet;
	}

	public StringPropertyValueSet getValues(GenericProperty... properties) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addPropertyValue(GenericProperty property, PropertyValue pv) {
		this.properties.addProperty(property);
		this.propertyValues.addPropertyValue(pv);
		this.propertyValueMap.addPropertyValue(property, pv);
		
	}

	public void addPropertyValues(GenericProperty property, StringPropertyValueSet pvs) {
		this.properties.addProperty(property);
		this.propertyValues.addPropertyValues(pvs);
		this.propertyValueMap.addPropertyValue(property, pvs);
	}


	public String getAccession() {
		return accession;
	}


	public void setAccession(String accession) {
		this.accession = accession;
	}
	
	public void setId(int id) {
		this.id =id;
	}

	public int getId() {
		return id;
	}




	public String getType() {
		return type;
	}




	public void setType(String type) {
		this.type = type;
	}




	public void removeProperty(GenericProperty property) {
		// TODO Auto-generated method stub
		
	}


	public List<String> getPropertyValues(GenericProperty topicAttribute) {
		List<String> valueSet = new ArrayList<String>();
		if (this.properties.contains(topicAttribute)){
			IntOpenHashSet pvIds = this.propertyValueMap.getPropertyValueIds(topicAttribute);
			valueSet.addAll(this.propertyValues.getValues(pvIds));
			
		}
		return valueSet;
		
	}


	public int getSrcVersionStructureId() {
		return srcVersionStructureId;
	}


	public void setSrcVersionStructureId(int srcVersionStructureId) {
		this.srcVersionStructureId = srcVersionStructureId;
	}

}
