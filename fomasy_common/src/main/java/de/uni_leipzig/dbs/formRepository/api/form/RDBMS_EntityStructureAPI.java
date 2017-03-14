package de.uni_leipzig.dbs.formRepository.api.form;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.api.util.DBConHandler;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.PropertyValue;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.util.DateFormatter;



public class RDBMS_EntityStructureAPI implements EntityStructureAPI {

	
	Logger log = Logger.getLogger(getClass());
	
	public static final String GET_VERSION_WITHIN_DATE = "select es.ent_struct_id, esv.date_from, "
			+ "esv.date_to from entity_structure es inner join "
			+ " entity_structure_version esv on es.ent_struct_id = esv.ent_struct_id_fk where es.name = ? AND"
			+ " es.ent_type = ? AND (? Between esv.date_from AND esv.date_to)";
	
	public static final String GET_VERSIONS_BY_TYPE ="select es.ent_struct_id, esv.date_from,  "
			+ "esv.date_to, es.ent_type, es.name from entity_structure es inner join "
			+ " entity_structure_version esv on es.ent_struct_id = esv.ent_struct_id_fk where"
			+ " es.ent_type = ?";
	
	public static final String GET_LATEST_VERSION = "select es.ent_struct_id, MAX(esv.date_from), "
			+ "MAX(esv.date_to) from entity_structure es inner join "
			+ " entity_structure_version esv on es.ent_struct_id = esv.ent_struct_id_fk where es.name = ? AND"
			+ "es.ent_type = ? "
			+ "group by (es.ent_struct_id)";
	
	public static final String GET_ENTITIES = "select e.ent_id, e.accession,e.ent_type from entity e where e.ent_struct_id_fk = ? AND"
			+ "(? Between e.date_from AND e.date_to)"
			+ " AND (? Between e.date_from AND e.date_to)";

	public static final String GET_RELS = "select r.src_id, r.target_id, rt.rel_name, rt.is_directed from entity_relationship r"
			+ " inner join rel_type  rt on r.rel_type_id_fk = rt.rel_type_id where "
			+ " r.ent_struct_id_fk = ? AND "
			+ "(? Between r.from_date AND r.to_date) AND "
			+ "(? Between r.from_date AND r.to_date)"; 
	
	public static final String GET_PROPERTIES = "Select p.prop_id, p.prop_name, p.lang, p.scope,e.ent_id, pv.prop_value_id, pv.prop_value from entity e, property p, property_value pv where"
			+ " e.ent_id = pv.ent_id_fk AND "
			+ " pv.prop_id_fk = p.prop_id AND "
			+ " e.ent_struct_id_fk =? AND "
			+ " (? Between pv.from_date AND pv.to_date)"
			+ " AND (? Between pv.from_date AND pv.to_date) ";


	public static final String GET_AVAILABLE_PROPERTIES = "Select DISTINCT p.prop_id, p.prop_name, p.lang, p.scope from entity e, property p, property_value pv where"
			+ " e.ent_id = pv.ent_id_fk AND "
			+ " pv.prop_id_fk = p.prop_id AND "
			+ " e.ent_struct_id_fk =? AND "
			+ " (? Between pv.from_date AND pv.to_date)"
			+ " AND (? Between pv.from_date AND pv.to_date)";
	
	public static final String GET_AVAILABLE_PROPERTIES_FOR_SOURCE = "Select DISTINCT p.prop_id, p.prop_name, p.lang, p.scope from entity e, "
			+ "property p, property_value pv, entity_structure esv where "
			+ " esv.ent_struct_id = e.ent_struct_id_fk AND"
			+ " e.ent_id = pv.ent_id_fk AND "
			+ " pv.prop_id_fk = p.prop_id AND "
			+ " esv.name = ? AND "
			+ "esv.ent_type = ? AND "
			+ " (? Between pv.from_date AND pv.to_date)";
	
	@Override
	public EntityStructureVersion getEntityStructureVersion(String name,
			String type, String date) throws VersionNotExistsException, StructureBuildException {
		Connection con = null;PreparedStatement pstmt =null;
		try {
			con = DBConHandler.getInstance().getConnection();
			pstmt = con.prepareStatement(GET_VERSION_WITHIN_DATE);
			pstmt.setString(1, name);
			pstmt.setString(2, type);
			pstmt.setString(3, date);
			ResultSet rs = pstmt.executeQuery();
			int structId; 
			String from ; 
			String to;
			if (rs.next()){
				structId = rs.getInt(1);
				from = DateFormatter.getFormattedDate(rs.getDate(2));
				to = DateFormatter.getFormattedDate(rs.getDate(3));
				
			}else {
				throw new VersionNotExistsException ("version with name:"+ name+
						", type:"+type +" and for the date:"+date+" doesn't exists");
			}
			VersionMetadata metadata = new VersionMetadata (structId,DateFormatter.getDate(from),
					DateFormatter.getDate(to),name,type);
			EntityStructureVersion structure = new EntityStructureVersion(metadata);
			List <GenericEntity> entities = this.getEntities(con, structId, from, to);
			for (GenericEntity e : entities){
				structure.addEntity(e);
			}
			log.debug("retrieve entities:"+ entities.size());
			structure = this.addRelationships(con,structure, structId, from, to);
			structure = this.addProperties(con, structure, structId, from, to);
			log.debug("load property values");
			//structure = this.addAvailableProperties(con, structure, structId, from, to);
			pstmt.close();
			con.close();
			return structure;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				pstmt.close();
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}

	@Override
	public EntityStructureVersion getEntityStructureVersion(String name, String type,
			String date, Set<GenericProperty> usedProps, Set<GenericProperty> optionalProperties) throws VersionNotExistsException, StructureBuildException {
		Connection con = null;PreparedStatement pstmt =null;
		try {
			con = DBConHandler.getInstance().getConnection();
			pstmt = con.prepareStatement(GET_VERSION_WITHIN_DATE);
			pstmt.setString(1, name);
			pstmt.setString(2, type);
			pstmt.setString(3, date);
			ResultSet rs = pstmt.executeQuery();
			int structId;
			String from ;
			String to;
			if (rs.next()){
				structId = rs.getInt(1);
				from = DateFormatter.getFormattedDate(rs.getDate(2));
				to = DateFormatter.getFormattedDate(rs.getDate(3));

			}else {
				throw new VersionNotExistsException ("version with name:"+ name+
								", type:"+type +" and for the date:"+date+" doesn't exists");
			}
			VersionMetadata metadata = new VersionMetadata (structId,DateFormatter.getDate(from),
							DateFormatter.getDate(to),name,type);
			EntityStructureVersion structure = new EntityStructureVersion(metadata);
			List <GenericEntity> entities = this.getEntities(con, structId, from, to);
			for (GenericEntity e : entities){
				structure.addEntity(e);
			}
			log.debug("retrieve entities:"+ entities.size());
			structure = this.addRelationships(con,structure, structId, from, to);
			structure = this.addProperties(con, structure, structId, usedProps,optionalProperties, from, to);
			log.debug("load property values");
			//structure = this.addAvailableProperties(con, structure, structId, from, to);
			pstmt.close();
			con.close();
			return structure;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				pstmt.close();
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	public EntityStructureVersion getLatestStructureVersion(String name,
			String type) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private List<GenericEntity> getEntities (Connection con, int struct_id,
 			String from ,String to) throws StructureBuildException{
		List<GenericEntity> ents = new ArrayList<GenericEntity>();
		try {
			PreparedStatement stmt = con.prepareStatement(GET_ENTITIES);
			stmt.setInt(1, struct_id);
			stmt.setString(2, from);
			stmt.setString(3, to);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				GenericEntity e = new GenericEntity(rs.getInt(1), rs.getString(2), rs.getString(3),struct_id);
				ents.add(e);
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new StructureBuildException("error by loading entities for structure version");
			
		}
		return ents;
	}
	
	private EntityStructureVersion addRelationships (Connection con, EntityStructureVersion esv,
			int struct_id, String from, String to) throws StructureBuildException{
		try {
			PreparedStatement pstmt = con.prepareStatement(GET_RELS);
			pstmt.setInt(1, struct_id);
			pstmt.setString(2, from);
			pstmt.setString(3, to);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()){
				esv.addRelationship(esv.getEntity(rs.getInt(1)), esv.getEntity(rs.getInt(2)), rs.getString(3));
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new StructureBuildException ("error by loading relationships");
		}
		return esv;
	}
	
	private EntityStructureVersion addAvailableProperties (Connection con, EntityStructureVersion esv,
			int struct_id, String from, String to) throws StructureBuildException{
		try {
			PreparedStatement pstmt = con.prepareStatement(GET_AVAILABLE_PROPERTIES);
			pstmt.setInt(1, struct_id);
			pstmt.setString(2, from);
			pstmt.setString(3, to);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()){
				int propId = rs.getInt(1);
				String propName = rs.getString(2);
				String lang = rs.getString(3);
				String scope = rs.getString(4);
				GenericProperty property = new GenericProperty(propId,propName, scope, lang);
				esv.addAvailableProperty(property);
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new StructureBuildException ("error by loading properties");
		}
		return esv;
		
	}
	
	private EntityStructureVersion addProperties (Connection con, EntityStructureVersion esv,
			int struct_id, String from, String to) throws StructureBuildException{
		try {
			PreparedStatement pstmt = con.prepareStatement(GET_PROPERTIES);
			pstmt.setInt(1, struct_id);
			pstmt.setString(2, from);
			pstmt.setString(3, to);	
			ResultSet rs = pstmt.executeQuery();
			IntSet avProperties = new IntOpenHashSet();
			while (rs.next()){
				int propId = rs.getInt(1);
				String propName = rs.getString(2);
				String lang = rs.getString(3);
				String scope = rs.getString(4);
				int pvid= rs.getInt(6);
				String propValue = rs.getString(7);
				if (!propValue.isEmpty()){
					PropertyValue pv = new PropertyValue(pvid,propValue);
					GenericEntity ent = esv.getEntity(rs.getInt(5));
					GenericProperty property = new GenericProperty(propId,propName, scope, lang);
					ent.addPropertyValue(property, pv);
					if (!avProperties.contains(propId)){
						avProperties.add(propId);
						esv.addAvailableProperty(property);
					}
				}
			}
			rs.close();
			pstmt.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new StructureBuildException ("error by loading properties");
		}
		return esv;
	}

	private EntityStructureVersion addProperties (Connection con, EntityStructureVersion esv,
			int struct_id, Set<GenericProperty> props, Set<GenericProperty> optProps,String from, String to) throws StructureBuildException{
		try {
			PreparedStatement pstmt = con.prepareStatement(GET_PROPERTIES);
			pstmt.setInt(1, struct_id);
			pstmt.setString(2, from);
			pstmt.setString(3, to);
			ResultSet rs = pstmt.executeQuery();
			IntSet avProperties = new IntOpenHashSet();
			IntSet entities = new IntOpenHashSet();
			while (rs.next()){
				int propId = rs.getInt(1);
				String propName = rs.getString(2);
				String lang = rs.getString(3);
				String scope = rs.getString(4);
				int pvid= rs.getInt(6);
				String propValue = rs.getString(7);

				if (!propValue.isEmpty()){
					GenericProperty property = new GenericProperty(propId,propName, scope, lang);
					if (this.isPropertyUsed(props, property)) {
						PropertyValue pv = new PropertyValue(pvid, propValue);
						GenericEntity ent = esv.getEntity(rs.getInt(5));
						ent.addPropertyValue(property, pv);
						entities.add(ent.getId());
						if (!avProperties.contains(propId)) {
							avProperties.add(propId);
							esv.addAvailableProperty(property);
						}
					}
					if (this.isPropertyUsed(optProps, property)){
						PropertyValue pv = new PropertyValue(pvid, propValue);
						GenericEntity ent = esv.getEntity(rs.getInt(5));
						ent.addPropertyValue(property, pv);
						if (!avProperties.contains(propId)) {
							avProperties.add(propId);
							esv.addAvailableProperty(property);
						}
					}
				}
			}
			rs.close();
			pstmt.close();
			for (GenericEntity ge: esv.getEntities()){
				if (!entities.contains(ge.getId())){
					esv.removeEntity(ge.getId());
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new StructureBuildException ("error by loading properties");
		}
		return esv;
	}

	private boolean isPropertyUsed (Set<GenericProperty> usedProps, GenericProperty fetchedProperty){
		for (GenericProperty gp: usedProps){
			if (gp.getName()!=null && fetchedProperty.getName() !=null){
				if (gp.getName().equals(fetchedProperty.getName())&&
								gp.getScope().equals(fetchedProperty.getScope())&&
								gp.getLanguage().equals(fetchedProperty.getLanguage())){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Set<EntityStructureVersion> getEntityStructureVersionsByType(
			Set<String> types) throws StructureBuildException {
		Connection con = null;PreparedStatement pstmt =null;
		Set<EntityStructureVersion> forms = new HashSet<EntityStructureVersion>();
		try {
			con = DBConHandler.getInstance().getConnection();
			for (String type :types){
				int structId; 
				String from ; 
				String to;
				String name;
				String t;
				pstmt = con.prepareStatement(GET_VERSIONS_BY_TYPE);
				pstmt.setString(1, type);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next()){
					structId= rs.getInt(1);
					from = rs.getString(2);
					to = rs.getString(3);
					t = rs.getString(4);
					name = rs.getString(5);
					
					VersionMetadata metadata = new VersionMetadata (structId,DateFormatter.getDate(from),
							DateFormatter.getDate(to),name,type);
					EntityStructureVersion structure = new EntityStructureVersion(metadata);
					List <GenericEntity> entities = this.getEntities(con, structId, from, to);
					for (GenericEntity e : entities){
						structure.addEntity(e);
					}
					structure = this.addRelationships(con,structure, structId, from, to);
					structure = this.addProperties(con, structure, structId, from, to);
					forms.add(structure);
				}
				rs.close();
				pstmt.close();
				con.close();
			}
			
		}catch (SQLException e ){
			throw new StructureBuildException ("error by loading properties", e.getCause());
		}
		return forms;
	}

	@Override
	public VersionMetadata getMetadata(String name, String type, String version) throws VersionNotExistsException {
		Connection con = null;PreparedStatement pstmt =null;
		try {
			con = DBConHandler.getInstance().getConnection();
			pstmt = con.prepareStatement(GET_VERSION_WITHIN_DATE);
			pstmt.setString(1, name);
			pstmt.setString(2, type);
			pstmt.setString(3, version);
			ResultSet rs = pstmt.executeQuery();
			int structId; 
			String from ; 
			String to;
			if (rs.next()){
				structId = rs.getInt(1);
				from = DateFormatter.getFormattedDate(rs.getDate(2));
				to = DateFormatter.getFormattedDate(rs.getDate(3));
				
			}else {
				throw new VersionNotExistsException ("version with name:"+ name+
						", type:"+type +" and for the date:"+version+" doesn't exists");
			}
			VersionMetadata metadata = new VersionMetadata (structId,DateFormatter.getDate(from),
					DateFormatter.getDate(to),name,type);
			return metadata;
		}catch (SQLException e){
			
		}finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public Set<GenericProperty> getAvailableProperties (String name, String from, String type){
		Set<GenericProperty> set = new HashSet<GenericProperty>();
		try {
			Connection con = DBConHandler.getInstance().getConnection();
			PreparedStatement pstmt = con.prepareStatement(GET_AVAILABLE_PROPERTIES_FOR_SOURCE);
			pstmt.setString(1, name);
			pstmt.setString(2, type);
			pstmt.setString(3, from);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()){
				int propId = rs.getInt(1);
				String propName = rs.getString(2);
				String lang = rs.getString(3);
				String scope = rs.getString(4);
				GenericProperty property = new GenericProperty(propId,propName, scope, lang);
				set.add(property);
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		return set;
		
	}

	@Override
	public Map<String, Integer> getIdMapping(String name, String date, String type) {
		Map<String,Integer> ents = new HashMap<>();
		PreparedStatement pstmt =null;
		Connection con =null;
		try {
			con = DBConHandler.getInstance().getConnection();
			pstmt = con.prepareStatement(GET_VERSION_WITHIN_DATE);
			pstmt.setString(1, name);
			pstmt.setString(2, type);
			pstmt.setString(3, date);
			ResultSet rs = pstmt.executeQuery();
			int structId;
			String from ;
			String to;
			if (rs.next()){
				structId = rs.getInt(1);
				from = DateFormatter.getFormattedDate(rs.getDate(2));
				to = DateFormatter.getFormattedDate(rs.getDate(3));
				PreparedStatement stmt = con.prepareStatement(GET_ENTITIES);
				stmt.setInt(1, structId);
				stmt.setString(2, from);
				stmt.setString(3, to);
				ResultSet rs2 = stmt.executeQuery();
				while(rs2.next()){
					ents.put(rs2.getString(2),rs2.getInt(1));
				}
				rs2.close();
				stmt.close();

			}
		} catch (SQLException e) {
			e.printStackTrace();

		}finally {
			try {
				pstmt.close();
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ents;
	}


}
