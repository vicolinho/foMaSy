package de.uni_leipzig.dbs.formRepository.evaluation.tool.dictionary.io;

import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportRelationship;
import de.uni_leipzig.dbs.formRepository.importer.EntityStructureImporter;

import java.sql.*;
import java.util.*;

/**
 * Created by christen on 09.02.2017.
 */
public class UMLSSourceReader implements SourceReader{

  public List<ImportEntity> readSource(Properties prop) {

    String user =(String) prop.get(EntityStructureImporter.USER);
    String pw = (String) prop.get(EntityStructureImporter.PW);
    String source =(String) prop.get(EntityStructureImporter.SOURCE);
    Connection con =null;
    List<ImportEntity> ents = new ArrayList<ImportEntity>();
    try {
      con = DriverManager.getConnection(source, user, pw);
      ents = new ArrayList<>(this.getUMLSData(con).values());

    } catch (SQLException e) {
      // TODO Auto-generated catch block
    }finally{
      try {
        con.close();
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return ents;
  }

  private HashMap<String, ImportEntity> getUMLSData(Connection conn) throws SQLException {
    HashMap<String,ImportEntity> dataset = new HashMap<String, ImportEntity>();
    float start = System.currentTimeMillis();
    Map<String,Set<String>> insertLUIS = new HashMap<String,Set<String>>();


    System.out.println("Get UMLS CUIs + concept information (name, synonyms..) ..");

    String query = "SELECT CUI, STR, SUPPRESS, TTY, LAT, STT, LUI"
            + " FROM MRCONSO "
            + "WHERE ( lcase(LAT) = 'eng')";

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
      if (ie == null) {
        ie = new ImportEntity(cui, "concept");
        dataset.put(cui, ie);
      }
      String value = rs.getString(2);
      String scopeUMLS = rs.getString(4);
      String scopeFMS = null;
      String attName;
      if (scopeUMLS.equals("PT")) {
        attName = "synonym";
        scopeFMS = "main";
        prefferedConcepts.add(cui);
      } else if (scopeUMLS.contains("SY")) {
        attName = "synonym";
        scopeFMS = "main";
        prefferedConcepts.add(cui);
      } else if (scopeUMLS.equals("PN")) {
        attName = "name";
        scopeFMS = "main";
        prefferedConcepts.add(cui);
      } else {
        attName = "synonym";
        prefferedConcepts.add(cui);
      }
      scopeFMS = scopeUMLS;
      String newLui = rs.getString(7);
      Set<String> luis = insertLUIS.get(cui);
      if (luis == null) {
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

    HashSet<String> notPreferred = new HashSet<String>(dataset.keySet());
    notPreferred.removeAll(prefferedConcepts);
    for (String np : notPreferred) {
      dataset.remove(np);
    }

    String[] cuiArray =dataset.keySet().toArray(new String[]{});
    System.out.println("Get semantic types ..");
    String query3 = "SELECT CUI, STN, STY FROM MRSTY WHERE CUI IN (" + preparePlaceHolders(cuiArray.length) + ");";
    PreparedStatement psmt3 = conn.prepareStatement(query3);
    setValues(psmt3, cuiArray);
    ResultSet rs3 = psmt3.executeQuery();

    while (rs3.next()) {
      String cui = rs3.getString(1);
      dataset.get(cui).addProperty("sem_type",rs3.getString(3),"string",null,null);
    }
    rs3.close();
    psmt3.close();
     return dataset;
    }


  public static void setValues(PreparedStatement preparedStatement,
                               String[] values) throws SQLException {
    for (int i = 0; i < values.length; i++) {
      preparedStatement.setObject(i + 1, values[i]);
    }
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
}
