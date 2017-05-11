package de.uni_leipzig.dbs.formRepository.importer.umls;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportRelationship;
import de.uni_leipzig.dbs.formRepository.exception.ImportException;
import de.uni_leipzig.dbs.formRepository.importer.EntityStructureImporter;
import de.uni_leipzig.dbs.formRepository.importer.PreSourceImporter;

public class UMLSImporter extends PreSourceImporter{

	 public static final String GET_ISA = "SELECT  Distinct CUI1 as child, CUI2 as par, RELA from MRREL"
			 +" where REL ='PAR' "
	    		+ "  LIMIT ? OFFSET ?";
	
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
			String query = importer.getSqlQuery();
			try {
				con = DriverManager.getConnection(source, user, pw);
				this.ents = new ArrayList<ImportEntity>(this.getUMLSData(con, query).values());
				List<ImportRelationship> rels = new ArrayList<ImportRelationship>();
				if (isRelImport){
					this.rels = this.getRelations();
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

	private List<ImportRelationship> getRelations () throws SQLException{
		HashMap <String, List<String>> relations = new HashMap<String,List<String>>();
		PreparedStatement stmt = con.prepareStatement(GET_ISA);
		stmt.setQueryTimeout(3600*5);
		boolean found=true;
		int offset =0;
		int limit = 100000;
		while (found){
			found=false;
			stmt.setInt(1, limit);stmt.setInt(2, offset);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				found =true;
				String type = rs.getString(3);
				if (type ==null){
					type = "";
				}
				String parent = rs.getString(2);
				String child = rs.getString(1);
				/*if (type.equals("inverse_isa")){
					String tmp = parent;
					parent = child;
					child = tmp;
				}*/
				if (!parent.equals(child)){
					if (type.equals("inverse_isa")||type.equals("")||type.equals("isa")){
						List<String> relation = relations.get(parent);
						if (relation ==null && dataset.containsKey(parent)){
							relation = new ArrayList<String>();
							relations.put(parent, relation);
						}
						if (dataset.containsKey(child)&&relation!=null)
							relation.add(child);	
					}
				}
			}
			offset = offset+limit;
			System.out.println("fetch relations:"+offset);
		}
		stmt.close();
		List <ImportRelationship> rels = new ArrayList<ImportRelationship> ();
		for (Entry<String,List<String>> e: relations.entrySet()){
			for (String c: e.getValue()){
				rels.add(new ImportRelationship(e.getKey(),c,"is_a",true));
			}
		}
		return rels;
	}

	public static String preparePlaceHolders(int length) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length;) {
			builder.append("?");
			if (++i < length) {
				builder.append(",");
			}
		}
		return builder.toString();
	}
	
	private HashMap<String, ImportEntity> getUMLSData(Connection conn, String query) throws ImportException {
		dataset = new HashMap<String, ImportEntity>();
		float start = System.currentTimeMillis();
		Map<String,Set<String>> insertLUIS = new HashMap<String,Set<String>>();
		try {
			
			System.out.println("Get UMLS CUIs + concept information (name, synonyms..) ..");
			// Objekt zum Ausfuehren von Queries
			PreparedStatement psmt = conn.prepareStatement(query);
			ResultSet rs = psmt.executeQuery(query);
			System.out.println("Executed query");
			HashSet<String> prefferedConcepts = new HashSet<String>();
			while (rs.next()) {
				String lang = rs.getString(5);
				if (lang.equals("ENG"))
					lang = "EN";
				else if (lang.equals("GER"))
					lang = "DE";
					
				String cui = rs.getString(1);
				// System.out.println(cui);
				ImportEntity ie = dataset.get(cui);
				if (ie==null){
					ie = new ImportEntity(cui,"concept");
					dataset.put(cui, ie);
				}
				String value = rs.getString(2);
				String scopeUMLS = rs.getString(4);
				String scopeFMS =null;
				String attName;
				if (scopeUMLS.equals("PT")){
					attName = "synonym";
					scopeFMS = "main";
					prefferedConcepts.add(cui);
				}else if (scopeUMLS.contains("SY")){
					attName = "synonym";
					scopeFMS = "main";
					prefferedConcepts.add(cui);
				}else if (scopeUMLS.equals("PN")){
					attName = "name";
					scopeFMS = "main";
					prefferedConcepts.add(cui);
				}else {
					attName = "synonym";
					prefferedConcepts.add(cui);
				}
				scopeFMS = scopeUMLS;
				String newLui = rs.getString(7);
				Set<String> luis = insertLUIS.get(cui);
				if (luis == null){
					luis = new HashSet<String>();
					insertLUIS.put(cui, luis);
				}
				if (!luis.contains(newLui))	
					ie.addProperty(attName, value, "string", lang, scopeFMS);
				luis.add(newLui);
			}
			
			rs.close();
			psmt.close();
			System.out.println(dataset.size() + " CUIs selected");
			
			HashSet <String> notPreferred = new HashSet<String>(dataset.keySet());
			notPreferred.removeAll(prefferedConcepts);
			for (String np : notPreferred){
				dataset.remove(np);
			}
			System.out.println("reduced size: "+ dataset.size());
			String[] cuiArray =dataset.keySet().toArray(new String[]{});
			
			// Definitions
			System.out.println("Get defintions ..");
			String query2 = "SELECT con.CUI, def.DEF, con.LAT FROM MRDEF def, MRCONSO con WHERE def.CUI IN (" + preparePlaceHolders(cuiArray.length) + ") AND"
					+ " def.cui = con.cui AND def.AUI = con.AUI";
			PreparedStatement psmt2 = conn.prepareStatement(query2);
			setValues(psmt2, cuiArray);
			ResultSet rs2 = psmt2.executeQuery();
			
			while (rs2.next()) {
				String cui = rs2.getString(1);
				String def = rs2.getString(2);
				String lat = rs2.getString(3);
				if (lat.equals("ENG"))
					lat = "EN";
				else if (lat.equals("GER"))
					lat = "DE";
				if (def != null && def.length()<4000&&(lat.equals("DE")||lat.equals("EN"))){	
					dataset.get(cui).addProperty("definition", def, "string", lat, null);
				}
			}
			rs2.close();
			psmt2.close();

			// Semantic Types
			System.out.println("Get semantic types ..");
			String query3 = "SELECT CUI, STN, STY FROM MRSTY WHERE CUI IN (" + preparePlaceHolders(cuiArray.length) + ");";
			PreparedStatement psmt3 = conn.prepareStatement(query3);
			setValues(psmt3, cuiArray);
			ResultSet rs3 = psmt3.executeQuery();

			while (rs3.next()) {
				String cui = rs3.getString(1);
				dataset.get(cui).addProperty("sem_type_tree_nr", rs3.getString(2), "string",null, null);
				dataset.get(cui).addProperty("sem_type",rs3.getString(3),"string",null,null);
			}
			rs3.close();
			psmt3.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ImportException("error by loading umls from DB");
		}
		float end = (System.currentTimeMillis()-start)/(float)60000;
		System.out.println("Load data from UMLS repository done in "+end+" min.");
		return dataset;
	}

	public static void setValues(PreparedStatement preparedStatement,
			String[] values) throws SQLException {
		for (int i = 0; i < values.length; i++) {
			preparedStatement.setObject(i + 1, values[i]);
		}
	}
	
}
