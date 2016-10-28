package de.uni_leipzig.dbs.formRepository.api.annotation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import de.uni_leipzig.dbs.formRepository.api.util.DBConHandler;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportAnnotationMapping;
import de.uni_leipzig.dbs.formRepository.util.CantorDecoder;

public class RDBMS_AnnotationAPI implements AnnotationAPI {

	
	Logger log = Logger.getLogger(getClass());
	public static final String CREATE_MAPPING = "Select src.ent_id,src.accession, target.ent_id, target.accession from entity src,"
			+ " entity target where src.ent_id = ? AND target.ent_id = ? ";
	
	
	public static final String GET_ANNOTATION_MAPPING_BY_STRUCT = "Select mapping_id, method from annotation_mapping "
			+ "  where src_ent_struct_version_id_fk =? "
			+ " and target_ent_struct_version_id_fk =? AND name = ?";
	
	public static final String GET_ANNOTATIONS_FOR_MAPPING = "Select src_entity_id,e.accession, target_entity_id,e2.accession, similarity, is_verified"
			+ " from annotations a, entity e, entity e2 "
			+ " where mapping_id_fk = ? AND e.ent_id = src_entity_id AND e2.ent_id = target_entity_id";
	
	public static final String CHECK_VERSIONS = "Select ent_struct_id from entity_structure_version esv,entity_structure es"
			+ " where  es.ent_struct_id = esv.ent_struct_id_fk AND "
			+ "es.name = ? AND es.ent_type =? AND (? Between esv.date_from AND esv.date_to)";
	
	public static final String INSERT_NEW_MAPPING = "Insert into annotation_mapping(name,src_ent_struct_version_id_fk,"
			+ "target_ent_struct_version_id_fk,method) Values (?,?,?,?)";


	private static final String INSERT_ANNOS = "Insert into annotations( mapping_id_fk,src_entity_id, target_entity_id, similarity, is_verified)"
			+ " Select ?, s.ent_id, t.ent_id,?,? from entity s, entity t where s.accession = ? AND t.accession = ?";
		

	public AnnotationMapping getAnnotationMapping(Long2FloatMap mapping,
			int sourceStructureId, int targetStructureId) {
		AnnotationMapping am = new AnnotationMapping();
		Connection con =null;
		try {
			 con = DBConHandler.getInstance().getConnection();
			 PreparedStatement pstmt = con.prepareStatement(CREATE_MAPPING);
			 for (Entry<Long, Float> e: mapping.entrySet()){
				 int srcId = (int) CantorDecoder.decode_a(e.getKey());
				 int targetId = (int) CantorDecoder.decode_b(e.getKey());
				 pstmt.setInt(1, srcId);
				 pstmt.setInt(2,targetId);
				 ResultSet rs = pstmt.executeQuery();
				 if (rs.next()){
					 String srcAcc = rs.getString(2);
					 String targetAcc = rs.getString(4);
					 EntityAnnotation ea = new EntityAnnotation(srcId, targetId, srcAcc, targetAcc,e.getValue(),false);
					 am.addAnnotation(ea);
				 }	
			 }
			 pstmt.close();
			 return am;
			 
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				con.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return am;
	}

	
	public AnnotationMapping getAnnotationMapping(VersionMetadata src, VersionMetadata target, String name){
		Connection con =null;
		AnnotationMapping am = new AnnotationMapping(); 
		try {
			con = DBConHandler.getInstance().getConnection();
			 PreparedStatement pstmt = con.prepareStatement(GET_ANNOTATION_MAPPING_BY_STRUCT);
			 pstmt.setInt(1, src.getId());
			 pstmt.setInt(2, target.getId());
			 pstmt.setString(3, name);
			 ResultSet rs = pstmt.executeQuery();
			 int structId=-1;
			 String method= null;
		
			 if (rs.next()){
				 structId = rs.getInt(1);
				 method = rs.getString(2);
			 }
			 am.setMethod(method);
			 am.setName(name);
			 am.setSrcVersion(src);;
			 am.setTargetVersion(target);
			 pstmt.close();
			 PreparedStatement annStmt = con.prepareStatement(GET_ANNOTATIONS_FOR_MAPPING);
			 annStmt.setInt(1, structId);
			 ResultSet rs2 = annStmt.executeQuery();
			 while (rs2.next()){
				 int srcEntId = rs2.getInt(1);
				 int targetEntId = rs2.getInt(3);
				 String srcAccession = rs2.getString(2);
				 String targetAccession = rs2.getString(4);
				 float sim = rs2.getFloat(5);
				 boolean isver = rs2.getBoolean(6);
				 EntityAnnotation ea = new EntityAnnotation(srcEntId, targetEntId, srcAccession, targetAccession, sim, isver);
				 am.addAnnotation(ea);
			 }
			 annStmt.close();
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
		return am;
		
	}

	public List <VersionMetadata> checkInvolvedVersion(	VersionMetadata srcStruct, VersionMetadata targetStruct) {
		List<VersionMetadata> involvedStructures = new ArrayList<VersionMetadata>();
		involvedStructures.add(srcStruct);involvedStructures.add(targetStruct);
		Connection con =null;
		try {
			 con = DBConHandler.getInstance().getConnection();
			 PreparedStatement pstmt = con.prepareStatement(CHECK_VERSIONS);
		for (int i = 0; i<involvedStructures.size();i++){
			VersionMetadata vm = involvedStructures.get(i);
			pstmt.setString(1, vm.getName());
			pstmt.setString(2, vm.getTopic());
			pstmt.setDate(3,new java.sql.Date(vm.getFrom().getTime()));
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next()){
				vm.setId(rs.getInt(1));
			}else {
				log.warn(vm.getFrom().toGMTString()+"-"+vm.getName()+"-"+vm.getTopic());
				log.warn("not exists:"+srcStruct.getName());
				involvedStructures.remove(i--);
			}
			
		}
		}catch (SQLException e ){
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return involvedStructures;
		// TODO Auto-generated method stub
		
	}


	public void importMapping(
			List<VersionMetadata> guranteedInvolvedStructures,
			ImportAnnotationMapping iam) {
		Connection con =null; 
		int map_id =-1;
		try {
			con = DBConHandler.getInstance().getConnection();
			PreparedStatement pstmt = con.prepareStatement(INSERT_NEW_MAPPING);
			pstmt.setString(1, iam.getName());
			log.debug(guranteedInvolvedStructures.get(0).getId()+"-"+guranteedInvolvedStructures.get(1).getId());
			pstmt.setInt(2, guranteedInvolvedStructures.get(0).getId());
			pstmt.setInt(3, guranteedInvolvedStructures.get(1).getId());
			pstmt.setString(4, iam.getMethod());
			pstmt.execute();
			pstmt.close();
			Statement s = con.createStatement();
			String query = "Select mapping_id from annotation_mapping where src_ent_struct_version_id_fk="+  guranteedInvolvedStructures.get(0).getId()+
					" AND target_ent_struct_version_id_fk="+guranteedInvolvedStructures.get(1).getId()+ " AND name ='"+iam.getName()+"' AND"
					 +" method ='"+iam.getMethod()+"'";
			ResultSet rs = s.executeQuery(query);
			
			if (rs.next()){
				map_id = rs.getInt(1);
			}
			
			PreparedStatement pstmtAnno = null;
			pstmtAnno = con.prepareStatement(INSERT_ANNOS);
			for (List<ImportAnnotation> a : iam.getAnnotations().values()){
				for (ImportAnnotation an:a){
					try {
						pstmtAnno.setInt(1, map_id);
						pstmtAnno.setFloat(2,an.getSim());
						pstmtAnno.setBoolean(3, an.isVerified());
						pstmtAnno.setString(4, an.getSrcAccession());
						pstmtAnno.setString(5, an.getTargetAccession());
						
						int change = pstmtAnno.executeUpdate();
						if (change ==0){
							//log.warn(an.getSrcAccession()+" "+an.getTargetAccession() +" not inserted");
						}
					} catch (SQLException e1) {
						log.error(an.getSrcAccession()+"-"+an.getTargetAccession()+"-"+guranteedInvolvedStructures.get(0).getName());
						e1.printStackTrace();
					} 
				}
				
			}
			pstmtAnno.close();	
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



	public void importMapping(
			List<VersionMetadata> guranteedInvolvedStructures,
			AnnotationMapping iam) {
		// TODO Auto-generated method stub

	}
}
