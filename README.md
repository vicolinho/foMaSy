# FoMaSy- medical form Management System
This project provides a management system for medical forms, which are used in several domains of medicine. To simplify the integration of 
study result data or patient data, a harmonization of medical forms by annotations of standardized vocabularies such as ontologies is useful.
The goal of the management system is that physicians, biologists or medicine are able to efficiently and effectively identify similar questions or similar forms by using the annotation with a standardized vocabulary like UMLS.

The datastore is a DBMS, which stores the ontologies, the medical documents and the annotations. The data model of the datastore conforms the EAV (Entity Attribute Value) principles, so that arbitrary medical document may be stored.

## Initialization
The system is initialized by an ini-file that specify the location of the database, the user, the password, the jdbc driver and and data store type. The data store type is currently "jdbc". An example of an ini-file is the fmsDummy.ini.
 ```java
// instantiation
FormRepository rep = new FormRepositoryImpl();
// initialize the connection properties to the data store
rep.initialize(<iniFilePath>);
```

## Import

A medical document or an ontology can be imported from a file or a database. The import of a source works by the following commands 
 ```java
// instantiation
FormRepository rep = new FormRepositoryImpl();
// initialize the connection properties to the data store
rep.initialize(<iniFilePath>);
// import the specified source with the configuration that is defined in the properties
rep.getFormManager().import(propertiesMap)
```


###Import settings 
The following parameters has to be specified in the propertiesMap 

| parameter name| Output description                              |
|:--------------|:------------------------------------------------:|
| formName    	| name of the source        | 
| timestamp     | date of the version in the format 'yyyy-MM-dd'             |
| topic         |  kind of the source (ontology, eligibility form, EHR...)   |
| source        |  The source can be a filepath,URL to a database, WEB service etc.    |
| sourceType    |  ("file" or "webservice" or "rdbms") If the source type is "rdbms" a user and a password has to be specified| 
| user  | account data for the source database      |
| password    | password for the account       |
| importerClass   |  class that extract the entities and relationships of the specified source|
| description       |  description of the content of the source    | Yes  |
| isRelationshipImport     |  boolean false only concepts are imported - true relationships inclusive|


### Importer class

An importer class is responsiple to parse a specific source to a list of ImportEntity objects and a list of ImportRelationship. This class has to extend the `PreSourceImporter` class and has to implement `loadSourceData()`. 

### Add relations to an imported source
Ontologies have a huge amount of properties, that are sometimes irrelvant for the current annotation process. However, it is also possible to add the missing relations. The specification of the propertyMap is identical to the `importForm(propertiesMap)`

 ```java
// import the specified source with the configuration that is defined in the properties
rep.getFormManager().importRelationshipsForVersion(propertiesMap)
```

### Retrieve stored sources 

A source is retrieved with its entities, e.g. concepts, questions,facts etc. that are described by their properties and the relations.

 ```java
// instantiation
FormRepository rep = new FormRepositoryImpl();
// initialize the connection properties to the data store
rep.initialize(<iniFilePath>);
// get a source by its name, type and the version that is specfied by a date string ('yyyy-MM-dd')
rep.getFormManager().getStructureVersion (String name,String type, String version)
```
Moreover, a set of sources for a specified type is retrievable `rep.getFormManager().getStructureVersionsByType (Set<String> types)`.
 

