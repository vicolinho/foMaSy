package de.uni_leipzig.dbs.formRepository.importer.umls;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportRelationship;
import de.uni_leipzig.dbs.formRepository.exception.ImportException;
import de.uni_leipzig.dbs.formRepository.importer.EntityStructureImporter;
import de.uni_leipzig.dbs.formRepository.importer.PreSourceImporter;

public class SemanticNetworkImporter extends PreSourceImporter{

	
	public static final String GET_SEMANTIC_TYPES = "Select UI, STY, DEF, ABR FROM SRDEF where RT = 'STY'";
	
	public static final String GET_RELS = "SELECT STY1,STY2,RL FROM SRSTR";
	Connection con ;

	private HashMap<String, ImportEntity> dataset;
	
	@Override
	protected void loadSourceData() throws ImportException {
		EntityStructureImporter importer = this.getMainImporter();
		String type = importer.getSourceType();
		
		if (type.equals("rdbms")){
			String user = importer.getUser();
			String pw = importer.getPw();
			String source =importer.getSource();
			boolean isRelImport = importer.isRelationImport();
			try {
				con = DriverManager.getConnection(source, user, pw);
				Map<String,ImportEntity> map = this.getSemanticNetworkData(con);
				this.ents = new ArrayList<ImportEntity>(map.values());
				if (isRelImport){
					this.rels = this.getRelations(con,map);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new ImportException("import error");
			}finally{
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}

	private Map<String, ImportEntity> getSemanticNetworkData(Connection con2) throws SQLException {
		Statement stmt = con2.createStatement();
		Map<String,ImportEntity> map = new HashMap<String,ImportEntity>();
		ResultSet rs = stmt.executeQuery(GET_SEMANTIC_TYPES);
		while (rs.next()){
			String accession = rs.getString(1);
			String name = rs.getString(2);
			String DEF = rs.getString(3);
			String abbrev = rs.getString(4);
			ImportEntity ie = new ImportEntity(accession, "semantic_type");
			ie.addProperty("name", name, "string", "EN", null);
			ie.addProperty("definition",DEF,"string","EN",null);
			ie.addProperty("abbreviation", abbrev, "string", "EN", null);
			map.put(name, ie);
		}
		return map;
	}

	private List<ImportRelationship> getRelations (Connection con2,Map<String,ImportEntity> entities) throws SQLException{
		Statement stmt = con2.createStatement();
		ResultSet rs = stmt.executeQuery(GET_RELS);
		List<ImportRelationship> rels = new ArrayList<ImportRelationship>();
		while (rs.next()){
			String src = rs.getString(1);
			String target = rs.getString(2);
			String relType = rs.getString(3);
			if (entities.containsKey(src)&&entities.containsKey(target)){
				ImportRelationship ir = new ImportRelationship(entities.get(src).getAccession(),
						entities.get(target).getAccession(), relType, true);
				rels.add(ir);
			}
		}
		
		
		return rels;
		
	}
}
