package de.uni_leipzig.dbs.formRepository.api.annontationCluster;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.api.annotation.entity.RDBMS_EntityAPI;
import de.uni_leipzig.dbs.formRepository.api.util.DBConHandler;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationCluster;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.exception.ClusterAPIException;
import de.uni_leipzig.dbs.formRepository.exception.ClusterNotExistsException;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;




public class RDBMS_ClusterAPI implements ClusterAPI{
	
	
	Logger log = Logger.getLogger(getClass());
	public  static  final  String ENTITIES_PER_TARGET_ANNOTATION ="Select targetEnt.ent_id, e.ent_id  from "
			+ "entity_structure esv, entity as e,annotations a , entity as targetEnt , entity_structure targetStruct "
			+ "where esv.ent_struct_id = e.ent_struct_id_fk AND "
			+ "a.src_entity_id = e.ent_id AND "
			+ "targetEnt.ent_id = a.target_entity_id AND "
			+ "esv.ent_struct_id = ? AND "
			+ "targetStruct.ent_struct_id = ? AND "
			+ "targetEnt.ent_struct_id_fk = targetStruct.ent_struct_id order by targetEnt.ent_id ";
	
	public static final String GET_ELEMENTS = "select ce.cluster_id_fk, e.accession, ce.entity_id_fk from annotation_cluster c,"
			+ "annotation_cluster_elem ce, entity e , entity elem"
			+ " where e.ent_id = ce.cluster_id_fk AND "
			+ " c.cluster_id = ce.cluster_id_fk AND "
			+ " elem.ent_id = ce.entity_id_fk AND "
			+ " ce.cluster_structure_id_fk = c.cluster_structure_id_fk AND"
			+ " c.cluster_structure_id_fk = ? order by ce.cluster_id_fk";
	
	
	public static final String CREATE_NEW_CLUSTER_STRUCTURE = "Insert into annotation_cluster_structure (name) values(?)";
	
	public static final String CREATE_NEW_CLUSTER = "Insert into annotation_cluster (cluster_id, cluster_structure_id_fk)"
			+ " values(?,?)";
	
	public static final String INSERT_ELEM_TO_CLUSTER = "Insert into annotation_cluster_elem (cluster_id_fk, cluster_structure_id_fk,"
			+ "entity_id_fk) values (?,?,?)";
	
	public static final String INSERT_REPRESENTANT_FOR_CLUSTER = "Insert into annotation_cluster_representant(cluster_id_fk,"
			+ "cluster_structure_id_fk, representant_value) values (?,?,?)";

	private static final String GET_REPRESENTANTS = "Select representant_value from annotation_cluster_representant WHERE"
			+ " cluster_structure_id_fk = ? AND "
			+ " cluster_id_fk = ? ";
			
	private static final String GET_INTERSECT = "Select count.c1, count.c2, Count(*) as elem from "+
"(Select Distinct v1.cluster_id as c1,v2.cluster_id as c2, v1.element_id from cluster_annotation_view v1,"+
			"cluster_annotation_view v2,annotation_cluster_structure acs "+  
			"where v1.cluster_id!= v2.cluster_id AND v1.element_id = v2.element_id AND "+
			"acs.name =? AND acs.cluster_structure_id = v1.cluster_structure_id AND "+
			"acs.cluster_structure_id = v2.cluster_structure_id)  as count "+
			"group by  count.c1, count.c2 having count(*)>=?";
	
	private static final String GET_ELEMENTS_WITH_STRUCTURE_ID = "select Distinct ce.cluster_id_fk, e.accession, elem.ent_struct_id_fk from "
			+ "annotation_cluster c, annotation_cluster_structure acs,"
			+ "annotation_cluster_elem ce, entity e , entity elem"
			+ " where e.ent_id = c.cluster_id AND "
			+ " c.cluster_id = ce.cluster_id_fk AND "
			+ " ce.cluster_structure_id_fk = acs.cluster_structure_id AND"
			+ " acs.cluster_structure_id =  c.cluster_structure_id_fk AND"
			+ " elem.ent_id = ce.entity_id_fk AND "
			+ " acs.name = ? order by ce.cluster_id_fk";
	public RDBMS_ClusterAPI() {
		// TODO Auto-generated constructor stub
	}


	public Map<GenericEntity, EntitySet<GenericEntity>> getCluster(
			Set<VersionMetadata> src, VersionMetadata target) throws EntityAPIException, ClusterAPIException {
		Connection con = null;
		try {
			con = DBConHandler.getInstance().getConnection();
			PreparedStatement pstmt = con.prepareStatement(ENTITIES_PER_TARGET_ANNOTATION);
			Int2ObjectMap<List<Integer>> map = new Int2ObjectOpenHashMap<List<Integer>>();
			Set<Integer> targetIds  =new HashSet<Integer>();
			Set<Integer> srcIds = new HashSet<Integer>();
			for (VersionMetadata vm : src){
				pstmt.setInt(1, vm.getId());
				pstmt.setInt(2, target.getId());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next()){
					int targetId  =rs.getInt(1);
					targetIds.add(targetId);
					int srcId = rs.getInt(2);
					srcIds.add(srcId);
					List<Integer> list = map.get(targetId);
					if (list==null){
						list = new ArrayList<Integer>();
						map.put(targetId, list);
					}
					list.add(srcId);
				}
			}
			
			RDBMS_EntityAPI api = new RDBMS_EntityAPI();
			EntitySet<GenericEntity> targetSet = api.getEntityWithProperties(targetIds);
			EntitySet<GenericEntity> srcSet = api.getEntityWithProperties(srcIds);
			Map<GenericEntity,EntitySet<GenericEntity>> annotationClusterMap = new HashMap<GenericEntity,
					EntitySet<GenericEntity>>();
			for (Entry<Integer,List<Integer>>e : map.entrySet()){
				GenericEntity targetEntity = targetSet.getEntity(e.getKey());
				EntitySet<GenericEntity> set = annotationClusterMap.get(targetEntity);
				if (set ==null){
					set = new GenericEntitySet();
					annotationClusterMap.put(targetEntity, set);
				}
				for (Integer srcId : e.getValue()){
					set.addEntity(srcSet.getEntity(srcId));
				}
			}
			return annotationClusterMap;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClusterAPIException(e.getCause());
		}finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void importClusters(Map<GenericEntity, AnnotationCluster> clusters,
			String name) throws ClusterAPIException {
		Connection con = null;
		try {
			con = DBConHandler.getInstance().getConnection();
			con.setAutoCommit(false);
			PreparedStatement pstmt = con.prepareStatement(CREATE_NEW_CLUSTER_STRUCTURE);
			pstmt.setString(1, name);
			pstmt.execute();
			con.commit();
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("Select cluster_structure_id from annotation_cluster_structure where name='"+name+"'");
			int struct_id;
			if (rs.next()){
				struct_id = rs.getInt(1);
			}else {
				throw new ClusterAPIException("can not insert new cluster structure");
			}
			PreparedStatement pstmtCluster = con.prepareStatement(CREATE_NEW_CLUSTER);
			PreparedStatement pstmtElem = con.prepareStatement(INSERT_ELEM_TO_CLUSTER);
			PreparedStatement pstmtRepresentant= con.prepareStatement(INSERT_REPRESENTANT_FOR_CLUSTER);
			for (Entry <GenericEntity, AnnotationCluster> e: clusters.entrySet()){
				pstmtCluster.setInt(1, e.getKey().getId());
				pstmtCluster.setInt(2, struct_id);
				pstmtCluster.execute();
				for (GenericEntity ge : e.getValue().getElements()){
					pstmtElem.setInt(1, e.getKey().getId());
					pstmtElem.setInt(2, struct_id);
					pstmtElem.setInt(3, ge.getId());
					pstmtElem.execute();
				}
				for (String value: e.getValue().getRepresentants()){
					pstmtRepresentant.setInt(1,e.getKey().getId());
					pstmtRepresentant.setInt(2, struct_id);
					pstmtRepresentant.setString(3, value);
					pstmtRepresentant.execute();
				}
			}
			con.commit();
			pstmtRepresentant.close();
			pstmtElem.close();
			pstmtCluster.close();
			con.setAutoCommit(true);
		}catch (SQLException e) {
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				throw new ClusterAPIException(e.getCause());
			}
			throw new ClusterAPIException(e.getCause());
		}finally {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}


	public Map<GenericEntity, AnnotationCluster> getDeterminedClusters(
			String clusterConfigName) throws ClusterAPIException,
			ClusterNotExistsException{
		Map<GenericEntity, AnnotationCluster> clusterMap = new HashMap<GenericEntity,AnnotationCluster>();
		Connection con = null;
		int structureId =-1;
		String clusterMapId=  "Select cluster_structure_id FROM annotation_cluster_structure where name ='"+
		clusterConfigName+"'";
		try {
			
			con = DBConHandler.getInstance().getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(clusterMapId);
			if (rs.next()){
				structureId = rs.getInt(1);
			}
			rs.close();
			if (structureId ==-1){
				throw new ClusterNotExistsException("cluster structure "+clusterConfigName+" not exists");
			}
			PreparedStatement pstmt = con.prepareStatement(GET_ELEMENTS);
			pstmt.setInt(1, structureId);
			ResultSet rs2 = pstmt.executeQuery();
			int oldCluster  = -1;
			RDBMS_EntityAPI api = new RDBMS_EntityAPI();
			AnnotationCluster currentCluster = null;
			Set<Integer> elements = new HashSet<Integer>();
			while (rs2.next()){
				int id = rs2.getInt(1);
				if (id != oldCluster){
					if (elements.size()!=0){
						EntitySet<GenericEntity> entElements = api.getEntityWithProperties(elements);
						currentCluster.setElements(entElements);
						elements.clear();
					}
					
					Set <Integer> ids = new HashSet<Integer>();
					ids.add(id);
					EntitySet<GenericEntity> entSet = api.getEntityWithProperties(ids);
					GenericEntity ge = entSet.iterator().next();
					 String accession = rs2.getString(2);
					currentCluster = new AnnotationCluster (id, accession, structureId);
					clusterMap.put(ge, currentCluster);
				}
				elements.add(rs2.getInt(3));
				oldCluster =id;
			}
			rs2.close();
			
			if (elements.size()!=0){
				EntitySet<GenericEntity> entElements = api.getEntityWithProperties(elements);
				currentCluster.setElements(entElements);
				elements.clear();
			}
			PreparedStatement repId = con.prepareStatement(GET_REPRESENTANTS);
			repId.setInt(1, structureId);
			for (Entry<GenericEntity,AnnotationCluster> acEntry : clusterMap.entrySet()){
				repId.setInt(2, acEntry.getKey().getId());
				ResultSet repResultSet = repId.executeQuery();
				while(repResultSet.next()){
					String repValue = repResultSet.getString(1);
					acEntry.getValue().addRepresentant(repValue,"EN");
				}
				repResultSet.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ClusterAPIException(e.getMessage(),e.getCause());
		} catch (EntityAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ClusterAPIException (e.getCause());
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return clusterMap;
	}
	
	public Map<Integer,Map<Integer,Float>> getCooccurences (String name,int common){
		Map<Integer,Map<Integer,Float>> map = new HashMap<Integer,Map<Integer,Float>>();
		Connection con =null;
		try {
			 con = DBConHandler.getInstance().getConnection();
			
			PreparedStatement pstmt = con.prepareStatement(GET_INTERSECT);
			pstmt.setString(1, name);
			pstmt.setInt(2, common);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()){
				int srcId =rs.getInt(1);
				Map<Integer,Float> mapPerCon = map.get(srcId);
				if (mapPerCon ==null){
					mapPerCon = new HashMap<Integer,Float>();
					map.put(srcId, mapPerCon);
				}
				mapPerCon.put(rs.getInt(2), (float)rs.getInt(3));
			}
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
		return map;
	}

	public Map<Integer,Map<Integer,Float>> getFormCooccurrences(String name, int common){
		Map<Integer,Map<Integer,Float>> cooccurrenceMeasureMap = new HashMap<Integer,Map<Integer,Float>>();
		try {
			Connection con = DBConHandler.getInstance().getConnection();
			PreparedStatement pstmt = con.prepareStatement(GET_ELEMENTS_WITH_STRUCTURE_ID);
			pstmt.setString(1, name);
			ResultSet rs = pstmt.executeQuery();
			Map<Integer,Set<Integer>> clusterToFormsMap= new HashMap<Integer,Set<Integer>>();
			while (rs.next()){
				int cluster_id = rs.getInt(1);
				int form_id = rs.getInt(3);
				Set<Integer> forms = clusterToFormsMap.get(cluster_id);
				if (forms==null){
					forms = new HashSet<Integer>();
					clusterToFormsMap.put(cluster_id, forms);
				}
				forms.add(form_id);
			}
			for (Entry<Integer,Set<Integer>> entry :clusterToFormsMap.entrySet()){
				for (Entry<Integer,Set<Integer>> entry2 : clusterToFormsMap.entrySet()){
					if (entry.getKey()!= entry2.getKey()){
						Set <Integer> intersect = new HashSet<Integer> (entry.getValue());
						intersect.retainAll(entry2.getValue());
						if (intersect!=null){
							//log.info(intersect.size());
							if (intersect.size()>=common){
								Map<Integer,Float> cooccurringCluster= cooccurrenceMeasureMap.get(entry.getKey());
								if (cooccurringCluster==null){
									cooccurringCluster = new HashMap<Integer,Float>(); 
									cooccurrenceMeasureMap.put(entry.getKey(), cooccurringCluster);
								}
								float measure = intersect.size()/(float)entry.getValue().size();
								cooccurringCluster.put(entry2.getKey(), measure);	
							}
						}
					}
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cooccurrenceMeasureMap;
		
	}
	
	
	

}
