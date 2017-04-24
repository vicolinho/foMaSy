package de.uni_leipzig.dbs.formRepository.umls_extraction;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.DBConHandler;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by christen on 12.04.2017.
 */
public class UMLS_Extract {

  public static void main(String[] args){
    FormRepository rep = new FormRepositoryImpl();
    try {
      rep.initialize(args[0]);
      Connection con = DBConHandler.getInstance().getConnection();
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("Select SAB,Count(DISTINCT(umls.CUI)) from covered_concepts cc, UMLS2014AB.MRCONSO umls "+
      "where cc.accession = umls.CUI "+
      "AND LAT = 'ENG' AND SAB IN ('MDR','CHV', 'NCI','MTH','SNOMEDCT_US') group by SAB ");
      Set<String> ontologies= new HashSet<>();
      while (rs.next()){
        String o = rs.getString(1);
        System.out.println(o+" "+rs.getInt(2));
        ontologies.add(o);
      }
      PreparedStatement pstmt = con.prepareStatement("Select DISTINCT CUI from UMLS2014AB.MRCONSO where sab = ?");
      Set<String> concepts = new HashSet<>();
      for (String c: ontologies){

        pstmt.setString(1, c);
        ResultSet rs2 = pstmt.executeQuery();
        while (rs2.next()){
          concepts.add(rs2.getString(1));
        }
        System.out.println(c+"\t"+ concepts.size());
      }
      Statement stmt2 = con.createStatement();
      ResultSet rs3 = stmt2.executeQuery("Select cc.accession from covered_concepts cc");
      Set<String> refCoveredd = new HashSet<>();
      while (rs3.next()){
        refCoveredd.add(rs3.getString(1));
      }
      refCoveredd.removeAll(concepts);
      System.out.println("not covered:"+refCoveredd.size());
      PreparedStatement insert = con.prepareStatement("Insert Into not_covered values(?)");
      for (String ncc: refCoveredd){
        insert.setString(1, ncc);
        insert.executeUpdate();
      }
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InitializationException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
