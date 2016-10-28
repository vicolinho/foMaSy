package de.uni_leipzig.dbs.formRepository.dataModel;

import java.io.Serializable;

public interface IEntity extends Serializable{

	public void setId(int id);
	public int getId();
	public void setAccession(String acc);
	public String getAccession();
	public String getType();
	public void setType(String type);
	public StringPropertyValueSet getValues(GenericProperty property);
	public void addPropertyValue(GenericProperty property, PropertyValue pv);
	public void addPropertyValues(GenericProperty property, StringPropertyValueSet pvs);
	public StringPropertyValueSet getValues(GenericProperty... properties);
	public void removeProperty(GenericProperty property);
	
}
