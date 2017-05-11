package de.uni_leipzig.dbs.entity_resolution.execution;

import de.uni_leipzig.dbs.entity_resolution.constants.MUSICBRAINZ_HEADER;
import de.uni_leipzig.dbs.entity_resolution.io.CSVReader;
import de.uni_leipzig.dbs.entity_resolution.io.Reader;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by christen on 03.05.2017.
 */
public class SoftIDFTest {


  public static final String PATH="";

  static Map<Integer, EntityStructureVersion> sources;
  public static void main(String[] args) throws IOException {
    sources = read();
  }



  private static Map<Integer,EntityStructureVersion>read() throws IOException {
    InputStream is = new FileInputStream(PATH);
    Reader reader = new CSVReader(MUSICBRAINZ_HEADER.ENT_ID_FIELD,"song",MUSICBRAINZ_HEADER.SOURCE_ID);
    return reader.read(is);
  }
}
