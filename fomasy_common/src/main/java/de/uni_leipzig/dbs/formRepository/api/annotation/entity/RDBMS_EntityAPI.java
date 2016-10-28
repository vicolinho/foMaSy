package de.uni_leipzig.dbs.formRepository.api.annotation.entity;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.api.util.DBConHandler;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.PropertyValue;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;

public class RDBMS_EntityAPI implements EntityAPI {

	
	Logger log = Logger.getLogger(getClass());
	public static final String GET_PROPERTIES = "Select p.prop_id, p.prop_name, p.lang, p.scope,e.ent_id, pv.prop_value_id, pv.prop_value from entity e, property p, property_value pv where"
			+ " e.ent_id = pv.ent_id_fk AND e.ent_id =? AND "
			+ " pv.prop_id_fk = p.prop_id ";
	
	public static final String GET_ENTITIES_BY_PROPERTY =" Select e.ent_id from entity e, "
			+ "property_value pv, property p where "
			+ "e.ent_id = pv.ent_id_fk AND pv.prop_id_fk = p.prop_id AND "
			+ "e.ent_struct_id_fk = ? AND "
			+ "p.prop_id =  ? AND "
			+ "pv.prop_value in (%1s)";
	@Override
	public EntitySet<GenericEntity> getEntitiesById(Set<Integer> ids) throws EntityAPIException {
		// TODO Auto-generated method stub
		EntitySet<GenericEntity> es = new GenericEntitySet();
		Connection con = null;
		if (!ids.isEmpty()){
			try {
				con = DBConHandler.getInstance().getConnection();			
				Formatter format = new Formatter(Locale.US);
				StringBuffer sb = new StringBuffer();
				for (int id : ids){
					sb.append(id+",");
				}
				if (sb.length()!=0)
					sb.deleteCharAt(sb.length()-1);
				format.format("Select ent_id, ent_struct_id_fk, accession, ent_type, date_from from entity where ent_id in (%1$s)", sb.toString());
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(format.toString());
				while (rs.next()){
					GenericEntity ge = new GenericEntity(rs.getInt(1), rs.getString(3),rs.getString(4), rs.getInt(2));
					es.addEntity(ge);
				}
				stmt.close();
				format.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				//log.error(ids.toString());
				throw new EntityAPIException("retrieve entities by ids",e);
			}finally{
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return es;
		
	}
	
	
	public EntitySet<GenericEntity> getEntityWithProperties(Set<Integer> ids) throws EntityAPIException {
		EntitySet<GenericEntity> entitySet = this.getEntitiesById(ids);
		Connection  con= null;
		try {
		con = DBConHandler.getInstance().getConnection();
		PreparedStatement pstmt = con.prepareStatement(GET_PROPERTIES);
		
		for (GenericEntity ge: entitySet){
			pstmt.setInt(1, ge.getId());
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
					GenericProperty property = new GenericProperty(propId,propName, scope, lang);
					ge.addPropertyValue(property, pv);
					if (!avProperties.contains(propId)){
						avProperties.add(propId);
					}
				}
			}
			rs.close();
		}
		pstmt.close();
		return entitySet; 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
		
	}

	public EntitySet<GenericEntity> getEntityWithPropertiesByAccession(Set<String> accs) throws EntityAPIException {
		EntitySet<GenericEntity> entitySet = this.getEntitiesByAccession(accs);
		Connection  con= null;
		try {
		con = DBConHandler.getInstance().getConnection();
		PreparedStatement pstmt = con.prepareStatement(GET_PROPERTIES);
		
		for (GenericEntity ge: entitySet){
			pstmt.setInt(1, ge.getId());
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
					GenericProperty property = new GenericProperty(propId,propName, scope, lang);
					ge.addPropertyValue(property, pv);
					if (!avProperties.contains(propId)){
						avProperties.add(propId);
					}
				}
			}
			rs.close();
		}
		pstmt.close();
		return entitySet; 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
		
	}
	
	private EntitySet<GenericEntity> getEntitiesByAccession(Set<String> ids) throws EntityAPIException {
		EntitySet<GenericEntity> es = new GenericEntitySet();
		Connection con = null;
		if (!ids.isEmpty()){
			try {
				con = DBConHandler.getInstance().getConnection();			
				Formatter format = new Formatter(Locale.US);
				StringBuffer sb = new StringBuffer();
				for (String id : ids){
					sb.append("'"+id+"'"+",");
				}
				if (sb.length()!=0)
					sb.deleteCharAt(sb.length()-1);
				format.format("Select ent_id, ent_struct_id_fk, accession, ent_type, date_from from entity where accession in (%1$s)", sb.toString());
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(format.toString());
				while (rs.next()){
					GenericEntity ge = new GenericEntity(rs.getInt(1), rs.getString(3),rs.getString(4), rs.getInt(2));
					es.addEntity(ge);
				}
				stmt.close();
				format.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				//log.error(ids.toString());
				throw new EntityAPIException("retrieve entities by ids",e);
			}finally{
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return es;
		
	}


	@Override
	public EntitySet<GenericEntity> getEntityWithPropertiesByProperty(
			Collection<String> values,VersionMetadata version, Set<GenericProperty> filterProperties)
			throws EntityAPIException {
		Connection  con= null;
		Formatter format = new Formatter(Locale.US);
		String query = GET_ENTITIES_BY_PROPERTY;
		try {
			con = DBConHandler.getInstance().getConnection();
			StringBuffer sb = new StringBuffer();
			for (String v : values){
				sb.append("'"+v+"',");
			}
			if (sb.length()!=0){
				sb.deleteCharAt(sb.length()-1);
			}
		format.format(query,sb.toString());
		query = format.toString();
		log.debug(query.toString());
		format.close();
		PreparedStatement pstmt = con.prepareStatement(query);
		pstmt.setInt(1, version.getId());
		
		EntitySet<GenericEntity> externalEntities  = new GenericEntitySet();
		Set<Integer> ids =new HashSet<Integer>();
		for (GenericProperty gp : filterProperties){
			log.debug("version: "+version.getId()+" gp:"+gp.getId());
			pstmt.setInt(2, gp.getId());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()){
				ids.add(rs.getInt(1));
			}
		}
		
		externalEntities = this.getEntityWithProperties(ids);
		log.debug("ids: "+ids.size());
		return externalEntities;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
		
	}

}
