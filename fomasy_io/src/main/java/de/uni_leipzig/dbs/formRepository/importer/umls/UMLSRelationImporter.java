package de.uni_leipzig.dbs.formRepository.importer.umls;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportRelationship;
import de.uni_leipzig.dbs.formRepository.exception.ImportException;
import de.uni_leipzig.dbs.formRepository.importer.EntityStructureImporter;
import de.uni_leipzig.dbs.formRepository.importer.PreSourceImporter;



public class UMLSRelationImporter extends PreSourceImporter{
	Connection con;

	public static final String RELEVANT_RELATIONS = "SELECT CUI1, CUI2, RELA FROM MRREL where RELA is not null AND"
			+ " RELA  not in ('inverse_isa','same_as','mapped_to') AND CUI1!=CUI2";
	@Override
	protected void loadSourceData() throws ImportException {
		this.rels = new ArrayList<ImportRelationship>();
		EntityStructureImporter importer = this.getMainImporter();
		String type = importer.getSourceType();
		
		if (type.equals("rdbms")){
			String user = importer.getUser();
			String pw = importer.getPw();
			String source =importer.getSource();
		
			con = null;
			try {
				con = DriverManager.getConnection(source, user, pw);
				this.rels = this.getRelations();
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

	private List<ImportRelationship> getRelations() throws SQLException {
		// TODO Auto-generated method stub
		List<ImportRelationship> list = new ArrayList<ImportRelationship>();
		PreparedStatement pstmt = con.prepareStatement(RELEVANT_RELATIONS);
		ResultSet rs = pstmt.executeQuery();
		Set<Integer> relSet = new HashSet<Integer>();
		while (rs.next()){
			String cui1 = rs.getString(1);
			String cui2 = rs.getString(2);
			String type = rs.getString(3);
			int hash = (cui1+cui2).hashCode();
			if (!relSet.contains(hash)){
				ImportRelationship ir = new ImportRelationship(cui1, cui2, type, true);
				list.add(ir);
				relSet.add(hash);
			}
		}
		return list;
	}

}
