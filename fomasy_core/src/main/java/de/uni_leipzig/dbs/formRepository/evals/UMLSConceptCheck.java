package de.uni_leipzig.dbs.formRepository.evals;

import de.uni_leipzig.dbs.formRepository.dataModel.importer.ImportAnnotation;
import de.uni_leipzig.dbs.formRepository.importer.annotation.odm.MDMAnnotationSAXParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by christen on 17.01.2017.
 */
public class UMLSConceptCheck {

  public static void main(String[] args){


    MDMAnnotationSAXParser parser = new MDMAnnotationSAXParser();
    File folder = new File (args[0]);
    Set<String> cuis = new HashSet<>();
    for (File f : folder.listFiles()){
      try {
        javax.xml.parsers.SAXParser p = SAXParserFactory.newInstance().newSAXParser();
        p.parse(f, parser);
        for (List<ImportAnnotation> values :parser.getMapping().getAnnotations().values()){
          for (ImportAnnotation ia : values){
            cuis.add(ia.getTargetAccession());
          }
        }

      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      } catch (SAXException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    System.out.println("covered concepts:"+ cuis.size());
    try {
      int count =0;
      java.sql.Connection con = DriverManager.getConnection("jdbc:mysql://dbserv2.informatik.uni-leipzig.de:3306/UMLS2014AB",
              "christen","start123");
      PreparedStatement pstmt = con.prepareStatement("SELECT CUI from MRCONSO where CUI =?");
      Set <String> notInVersion = new HashSet<>();
      for (String cui : cuis){
        pstmt.setString(1, cui);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()){
          count++;
        }else {
          if (cui.matches("C[0-9]{7}"))
            notInVersion.add("'"+cui+"'");
        }
        rs.close();
      }
      con.close();
      System.out.println("covered concepts:"+ count);
      System.out.println(notInVersion.toString());
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }
}
