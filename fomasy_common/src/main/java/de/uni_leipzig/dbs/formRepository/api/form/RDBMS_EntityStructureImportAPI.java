package de.uni_leipzig.dbs.formRepository.api.form;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;









import de.uni_leipzig.dbs.formRepository.api.util.DBConHandler;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportRelationship;
import de.uni_leipzig.dbs.formRepository.exception.ImportException;
import de.uni_leipzig.dbs.formRepository.util.DateFormatter;

public class RDBMS_EntityStructureImportAPI implements EntityStructureImportAPI {

	
	public static final String INSERT_TMP_OBJ = "Insert into tmp_entity (accession,ent_type) values (?,?)";
	
	public static final String INSERT_TMP_PROP = "Insert into tmp_properties (accession_fk,prop_name"
			+ ",prop_value,lang,scope,datatype) values(?,?,?,?,?,?)";
	
	public static final String INSERT_TMP_RELS = "Insert into tmp_relationship (par_relationship,"
			+ "child_relationship, rel_type, is_directed) values(?,?,?,?)";
	
	public static final String ANCESTOR_VERSION_DATE = "Select Min(date_from),Max(sv.ent_struct_version_id) from entity_structure s inner join "
			+ "entity_structure_version sv on s.ent_struct_id = sv.ent_struct_id_fk"
			+ " where s.name=[name] and s.ent_type = [type] and [date] > sv.date_from ";
	
	public static final String VERSION_EXISTS = "Select Min(date_from) from entity_structure s inner join "
			+ "entity_structure_version sv on s.ent_struct_id = sv.ent_struct_id_fk"
			+ " where s.name=[name] and s.ent_type = [type] and [date] = sv.date_from ";
	
	
	public static final String INSERT_ADDED_ENTITIES = "Insert into tmp_added_entity (accession) "
			+ " Select e.accession from tmp_entity e where e.accession NOT IN "
			+ " (Select e2.accession from  entity_structure_version sv inner join "
			+ " entity_structure s on sv.ent_struct_id_fk = s.ent_struct_id inner join "
			+ " entity e2 on e2.ent_struct_id_fk = s.ent_struct_id AND "
			+ " s.name = ? AND (? BETWEEN sv.date_from AND sv.date_to) AND "
			+ " (? BETWEEN e2.date_from AND e2.date_to))";
	
	public static final String INSERT_DEL_ENTITIES = "Insert into tmp_del_entity (accession)"
			+ " Select e2.accession from  entity_structure_version sv inner join "
			+ " entity_structure s on sv.ent_struct_id_fk = s.ent_struct_id inner join "
			+ " entity e2 on e2.ent_struct_id_fk = s.ent_struct_id AND "
			+ " s.name = ? AND (? BETWEEN sv.date_from AND sv.date_to) AND "
			+ " (? BETWEEN e2.date_from AND e2.date_to) AND"
			+ " e2.accession NOT IN ("
			+ "Select e.accession from tmp_entity e where e.accession )";
	
	
	
	
	public static final String GET_DELETED_ATT_ID ="Select pv.prop_value_id from property_value pv,property p,"
			+ " tmp_entity te, entity e where "
			+ " te.accession = e.accession and "
			+ " pv.ent_id_fk = e.ent_id and "
			+ " pv.prop_id_fk = p.prop_id and "
			+ " (? BETWEEN pv.from_date AND pv.to_date) and "
			+ " pv.prop_value not in "
			+ " (Select tp.prop_value from tmp_properties tp where p.prop_name = tp.prop_name AND p.lang = tp.lang AND"
			+ " p.scope = tp.scope AND p.datatype = tp.datatype)";
	
	public static final String UPDATE_DEL_ATTS = "UPDATE property_value set to_date = ? where prop_value_id IN ("
			+ "Select pv.prop_value_id from property_value pv,property p,"
			+ " tmp_entity te, entity e where "
			+ " te.accession = e.accession and "
			+ " pv.ent_id_fk = e.ent_id and "
			+ " pv.prop_id_fk = p.prop_id and "
			+ " (? BETWEEN pv.from_date AND pv.to_date) and "
			+ " pv.prop_value not in "
			+ " (Select tp.prop_value from tmp_properties tp where p.prop_name = tp.prop_name AND p.lang = tp.lang AND"
			+ " p.scope = tp.scope AND p.datatype = tp.datatype)";
	
	public static final String INSERT_ADDED_ATTS = "Insert into tmp_added_properties(accession_fk,prop_name,"
			+ "lang, scope, datatype,prop_value) Select tp.accession_fk, tp.prop_name, tp.lang,tp.scope,"
			+ " tp.datatype, tp.prop_value "
			+ " from tmp_properties tp where (tp.accession_fk, tp.prop_name, tp.datatype, tp.scope, tp.lang, tp.prop_value)"
			+ " NOT IN (Select e.accession, p.prop_name,  p.datatype, p.scope,p.lang, pv.prop_value from "
			+ " entity e, property p, property_value pv where e.ent_id = pv.ent_id_fk AND"
			+ " p.prop_id = pv.prop_id_fk)";
	
	public static final String UPDATED_DELETED_RELS = "Update entity_relationship set to_date = ? "
			+ "where (src_id,target_id,rel_type_id_fk) NOT IN ("
			+ "Select par.ent_id, child.ent_id,rt.rel_type_id from entity par, entity child,"
			+ " rel_type rt, tmp_relationship tmpRel"
			+ " where par.accession = tmpRel.par_relationship AND "
			+ " child.accession = tmpRel.child_relationship AND "
			+ " rt.rel_name = tmpRel.rel_type "
			+ ")";
	public static final String INSERT_ADDED_RELS = "Insert into tmp_added_relationship(par_relationship,child_relationship, rel_type , is_directed)"
			+ "Select tr.par_relationship, tr.child_relationship, tr.rel_type, tr.is_directed from tmp_relationship tr where (tr.child_relationship,tr.par_relationship, tr.rel_type)"
			+ " NOT IN"
			+ " (Select child.accession, par.accession,rt.rel_name from entity par, entity child, entity_relationship er, rel_type rt where par.ent_id = er.src_id AND"
			+ " child.ent_id = er.target_id AND "
			+ " er.rel_type_id_fk = rt.rel_type_id)"; 
	
	public static final String INSERT_NEW_STRUCTURE = "Insert into entity_structure (name, ent_type,description) Values(?,?,?)";
	
	public static final String UPDATE_OLD_VERSION = "UPDATE entity_structure_version set date_to = ? where "
			+ "ent_struct_id_fk in (Select ent_struct_id from entity_structure where name = ? AND ent_type =?)";
	
	public static final String INSERT_NEW_VERSION = "Insert into entity_structure_version (ent_struct_id_fk,date_from) Select ent_struct_id,? "
			+ " from entity_structure where name = ? AND ent_type = ?";
	
	public static final String INSERT_ENTITIES = "Insert into entity(ent_struct_id_fk,accession,ent_type,date_from)"
			+ "(Select struct.ent_struct_id, te.accession, te.ent_type, ? "
			+ "from tmp_entity te, tmp_added_entity tae, entity_structure struct"
			+ " where te.accession = tae.accession AND "
			+ " struct.name =? AND struct.ent_type = ?)";
	
	public static final String INSERT_NEW_PROPS = "Insert into property(prop_name, lang, scope, datatype) Select Distinct tmp.prop_name, tmp.lang, tmp.scope, tmp.datatype from "
			+ "tmp_properties tmp where (tmp.prop_name, tmp.lang, tmp.scope, tmp.datatype) NOT IN"
			+ "(Select p.prop_name, p.lang, p.scope, p.datatype from property p)";
			
	
	public static final String INSERT_NEW_PROP_VALUES =" Insert into property_value (prop_id_fk,ent_id_fk,prop_value,from_date) "
			+ "Select p.prop_id, e.ent_id, tap.prop_value, ? from tmp_added_properties tap, entity e, property p where tap.prop_name = p.prop_name AND "
			+ " tap.lang = p.lang AND "
			+ " tap.scope = p.scope AND "
			+ " tap.datatype = p.datatype AND "
			+ " tap.accession_fk = e.accession";
	
	public static final String INSERT_NEW_REL_TYPES = "Insert into rel_type (rel_name, is_directed) Select Distinct "
			+ "tmpr.rel_type, tmpr.is_directed from tmp_relationship tmpr where (tmpr.rel_type, tmpr.is_directed) not in "
			+ " (Select rt.rel_name, rt.is_directed from rel_type rt)";
	
	public static final String INSERT_NEW_RELS = "Insert into entity_relationship (src_id,target_id,ent_struct_id_fk,rel_type_id_fk, from_date) "
			+ "Select par.ent_id, child.ent_id, par.ent_struct_id_fk, rt.rel_type_id, ? from tmp_added_relationship tar, entity par, entity child, rel_type rt where tar.par_relationship=par.accession AND"
			+ " tar.child_relationship = child.accession AND "
			+ "tar.rel_type = rt.rel_name AND tar.is_directed = rt.is_directed"; 
	
	
	public void importTmpEntities(List<ImportEntity> importEntities) throws ImportException {
		Connection con = null;
		try {
			con = DBConHandler.getInstance().getConnection();
			con.setAutoCommit(false);
			PreparedStatement pstmtEnt = con.
					prepareStatement(INSERT_TMP_OBJ);
			PreparedStatement pstmtAtts = con.
					prepareStatement(INSERT_TMP_PROP);
			int batchSize =10000;
			int currentSize =0; 
			for (ImportEntity e: importEntities){
				if (currentSize==batchSize){
					pstmtEnt.executeBatch();
					pstmtAtts.executeBatch();
					pstmtEnt.clearBatch();
					pstmtAtts.clearBatch();
					con.commit();
					currentSize =0;
				}
				
				pstmtEnt.setString(1, e.getAccession());
				pstmtEnt.setString(2, e.getType());
				pstmtEnt.addBatch();
				for (ImportProperty prop :e.getProperties()){
					pstmtAtts.setString(1, e.getAccession());
					pstmtAtts.setString(2, prop.getName());
					pstmtAtts.setString(3, prop.getValue());
					if ( prop.getLan()!=null)
						pstmtAtts.setString(4, prop.getLan());
					else 
						pstmtAtts.setString(4, "N/A");
					if ( prop.getScope()!=null)
						pstmtAtts.setString(5, prop.getScope());
					else 
						pstmtAtts.setString(5, "N/A");
					if ( prop.getDataType()!=null)
						pstmtAtts.setString(6, prop.getDataType());
					else
						pstmtAtts.setString(6, "N/A");
					pstmtAtts.addBatch();
					
				}
				currentSize++;
			}
			if (currentSize!=0){
				pstmtEnt.executeBatch();
				pstmtAtts.executeBatch();
				pstmtEnt.clearBatch();
				pstmtAtts.clearBatch();
				con.commit();
				pstmtEnt.close();
				pstmtAtts.close();
			}
			con.setAutoCommit(true);
			con.close();
		} catch (SQLException e) {
			
			e.printStackTrace();
			throw new ImportException(" entities");
		}finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void importTmpRelationships(
			List<ImportRelationship> importRelationships) throws ImportException {
		Connection con = null;
		try {
			
			con = DBConHandler.getInstance().getConnection();
			PreparedStatement pstmtEnt = con.
					prepareStatement(INSERT_TMP_RELS);
			int batchSize = 5000;
			int currentSize =0;
			con.setAutoCommit(false);
				for (ImportRelationship r: importRelationships){
					if (currentSize ==batchSize){
						pstmtEnt.executeBatch();
						pstmtEnt.clearBatch();
						con.commit();
						currentSize =0;
					}
					
					pstmtEnt.setString(1, r.getSrc());
					pstmtEnt.setString(2,r.getTarget());
					pstmtEnt.setString(3, r.getType());
					pstmtEnt.setBoolean(4, r.isDirected());
					pstmtEnt.addBatch();

					currentSize++;
				}
				
				if (currentSize!=0){
					pstmtEnt.executeBatch();
					pstmtEnt.clearBatch();
					con.commit();
					pstmtEnt.close();
				}
				con.setAutoCommit(true);
				con.close();
		}catch (SQLException e) {
			
			e.printStackTrace();
			throw new ImportException(" relationships");
		}finally{
			try {
				con.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean checkVersion(VersionMetadata m) throws ImportException{
		String ancestorQuery = VERSION_EXISTS ;
		ancestorQuery = ancestorQuery.replaceAll("\\[name\\]", "'"+m.getName()+"'");
		ancestorQuery = ancestorQuery.replaceAll("\\[date\\]", "'"+DateFormatter.getFormattedDate(m.getFrom())+"'");
		ancestorQuery = ancestorQuery.replaceAll("\\[type\\]", "'"+m.getTopic()+"'");
		Statement stmt;
		Connection con = null;
		try {
			con = DBConHandler.getInstance().getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(ancestorQuery);
			if (rs.next()){
				Date d = rs.getDate(1);
				return d != null;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ImportException ("error by retrieving previous version of:"+m.getName());
		}finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new ImportException ("error by retrieving previous version of:"+m.getName());
			}
		}
		return true;
	}
	
	public VersionMetadata getPreviousVersion(VersionMetadata m) throws ImportException {
		String ancestorQuery = ANCESTOR_VERSION_DATE ;
		ancestorQuery = ancestorQuery.replaceAll("\\[name\\]", "'"+m.getName()+"'");
		ancestorQuery = ancestorQuery.replaceAll("\\[date\\]", "'"+DateFormatter.getFormattedDate(m.getFrom())+"'");
		ancestorQuery = ancestorQuery.replaceAll("\\[type\\]", "'"+m.getTopic()+"'");
		Statement stmt;
		VersionMetadata ancestorMetadata = null;
		Connection con = null;
		try {
			con = DBConHandler.getInstance().getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(ancestorQuery);
			if (rs.next()){
				Date d = rs.getDate(1);
				int structVersionId;
				if (d!=null){
					structVersionId = rs.getInt(2);
					ancestorMetadata = new VersionMetadata(structVersionId,d, null, m.getName(), m.getTopic());
				}
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ImportException ("error by retrieving previous version of:"+m.getName());
		}finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new ImportException ("error by retrieving previous version of:"+m.getName());
			}
		}
		return ancestorMetadata;
	}

	public void determineDiff(VersionMetadata previousMeta,VersionMetadata current) {
		Connection con =null;
		try {
			con = DBConHandler.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement(INSERT_ADDED_ENTITIES);
			
			java.sql.Date d ;
			String name ;
			if (previousMeta != null){
				d =new java.sql.Date(previousMeta.getFrom().getTime());
				name =previousMeta.getName();
			}else{
				d = new java.sql.Date(DateFormatter.getDate("2099-01-01").getTime());
				name ="not";
			}
			stmt.setString(1, name);
			stmt.setDate(2, d);
			stmt.setDate(3, d);
			stmt.execute();
			stmt.close();
			PreparedStatement stmt2 = con.prepareStatement(INSERT_DEL_ENTITIES);
			stmt2.setString(1,name);
			stmt2.setDate(2, d);
			stmt2.setDate(3, d);
			stmt2.execute();
			stmt2.close();
			
			
			PreparedStatement stmt4 = con.prepareStatement(UPDATE_DEL_ATTS);
			Calendar c = Calendar.getInstance();
			c.setTime(current.getFrom());
			c.add(Calendar.DATE, -1);
			java.sql.Date to_date = new java.sql.Date(c.getTimeInMillis());
			stmt4.setDate(1, to_date);
			stmt4.close();
			
			
			PreparedStatement stmt7 = con.prepareStatement(INSERT_ADDED_ATTS);
			stmt7.execute();
			stmt7.close();
			
			
			PreparedStatement stmt5 = con.prepareStatement(UPDATED_DELETED_RELS);
			stmt5.setDate(1, to_date);
			stmt5.execute();
			stmt5.close();
			
			PreparedStatement stmt8 = con.prepareStatement(INSERT_ADDED_RELS);
			stmt8.execute();
			stmt8.close();
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
	}
	

	public void determineRelDiff(VersionMetadata current){
		
		
		Connection con =null;
		try {
			con = DBConHandler.getInstance().getConnection();		
			PreparedStatement stmt8 = con.prepareStatement(INSERT_ADDED_RELS);
			stmt8.executeUpdate();
			stmt8.close();
		}catch(SQLException e ){
			
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	}

	public void importVersion (VersionMetadata previous, VersionMetadata current) {
		Connection con =null;
		try {
			java.sql.Date from_date = new java.sql.Date(current.getFrom().getTime());
			Calendar c = Calendar.getInstance();
			c.setTime(current.getFrom());
			c.add(Calendar.DATE, -1);
			java.sql.Date to_date = new java.sql.Date(c.getTimeInMillis());
			
			con = DBConHandler.getInstance().getConnection();
			//con.setAutoCommit(false);
			if (previous!=null){
				PreparedStatement updateOld = con.prepareStatement(UPDATE_OLD_VERSION);
				updateOld.setDate(1, to_date);
				updateOld.setString(2, previous.getName());
				updateOld.setString(3, previous.getTopic());
				updateOld.execute();
				updateOld.close();
			}else{
				PreparedStatement insertNew = con.prepareStatement(INSERT_NEW_STRUCTURE);
				
				insertNew.setString(1, current.getName());
				insertNew.setString(2, current.getTopic());
				insertNew.setString(3, current.getDescr());
				insertNew.execute();
				insertNew.close();
			}
			
			PreparedStatement newVersion = con.prepareStatement(INSERT_NEW_VERSION);
			newVersion.setDate(1, from_date);
			newVersion.setString(2, current.getName());
			newVersion.setString(3, current.getTopic());
			newVersion.execute();
			newVersion.close();
			
			PreparedStatement stmt = con.prepareStatement(INSERT_NEW_PROPS);
			stmt.execute();
			stmt.close();
			PreparedStatement relType = con.prepareStatement(INSERT_NEW_REL_TYPES);
			relType.execute();
			relType.close();
			
			PreparedStatement newEntities = con.prepareStatement(INSERT_ENTITIES);
			newEntities.setDate(1, from_date);
			newEntities.setString(2, current.getName());
			newEntities.setString(3, current.getTopic());
			newEntities.execute();
			newEntities.close();
			
			PreparedStatement newRels = con.prepareStatement(INSERT_NEW_RELS);
			newRels.setDate(1, from_date);
			newRels.execute();
			newRels.close();
		
			PreparedStatement stmt6 = con.prepareStatement(INSERT_NEW_PROP_VALUES);
			stmt6.setDate(1, from_date);
			stmt6.execute();
			stmt6.close();
			//con.commit();
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
		
	}

	public void cleanTmpTables() {
		// TODO Auto-generated method stub
		Connection con =null;
		try {
			con = DBConHandler.getInstance().getConnection();
			Statement stmt = con.createStatement();
			stmt.execute("truncate tmp_entity");
			stmt.execute("truncate tmp_properties");
			stmt.execute("truncate tmp_relationship");
			stmt.execute("truncate tmp_added_entity");
			stmt.execute("truncate tmp_added_relationship");
			stmt.execute("truncate tmp_added_properties");
			stmt.execute("truncate tmp_del_entity");
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void importRelsForVersion(
			VersionMetadata currentVersion) {
		Connection con =null;
		PreparedStatement relType;
		try {
			con = DBConHandler.getInstance().getConnection();
			relType = con.prepareStatement(INSERT_NEW_REL_TYPES);
			relType.execute();
			relType.close();
			
			PreparedStatement newRels = con.prepareStatement(INSERT_NEW_RELS);
			java.sql.Date from_date = new java.sql.Date(currentVersion.getFrom().getTime());
			newRels.setDate(1, from_date);
			newRels.execute();
			newRels.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

}
