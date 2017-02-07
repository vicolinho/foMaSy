package de.uni_leipzig.dbs.formRepository.api.datastore;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import de.uni_leipzig.dbs.formRepository.api.util.DBConHandler;
import de.uni_leipzig.dbs.formRepository.exception.InstallationException;

public class RDBMS_RepositoryAPI implements RepositoryAPI {

	public static final String CREATE_SCHEMA = "create.sql";
	
	public static final String DROP_SCHEMA = "drop.sql";//, primary key(ent_struct_id)
	public static final String TEST = " Create table if not exists entity_structure(ent_struct_id int not null auto_increment, Primary Key(ent_struct_id), name varchar(500) unique, ent_type varchar(200), index(name))";
	public void installRepository() throws InstallationException {
		
		Connection con = null ;
		
		try {
			con= DBConHandler.getInstance().getConnection();
			String[] statements = this.readDMLFile(CREATE_SCHEMA);
			Statement stmt = con.createStatement();
			for (String s: statements){
				stmt.addBatch(s);
				//System.out.println(s);
				
			}
			stmt.executeBatch();
			stmt.clearBatch();
			stmt.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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

	}

	public void deleteRepository() {
		Connection con = null ;
		
		try {
			con= DBConHandler.getInstance().getConnection();
			String[] statements = this.readDMLFile(DROP_SCHEMA);
			Statement stmt = con.createStatement();
			for (String s: statements){
				stmt.addBatch(s);
			}
			stmt.executeBatch();
			stmt.clearBatch();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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

	}

	private String[] readDMLFile (String file) throws IOException{
		URL url = ClassLoader.getSystemResource(file);
		BufferedReader br = new BufferedReader(new FileReader(url.getPath()));
		StringBuffer sb = new StringBuffer();
		while (br.ready()){
			String line = br.readLine();
			if (!line.isEmpty())
				sb.append(line.trim());
		}
		br.close();
		String[] statements = sb.toString().split(";");
		return statements;
	}
}
