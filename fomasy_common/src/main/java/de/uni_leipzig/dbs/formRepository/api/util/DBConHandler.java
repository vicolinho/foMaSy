package de.uni_leipzig.dbs.formRepository.api.util;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;



public class DBConHandler {

	
	private static DBConHandler instance ;
	
	private DataSource source;
	
	private DatabaseConnectionData data; 
	
	private DBConHandler (){
	}
	
	public void createConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		if (source==null){
			if (data.getDriver()!=null&&data.getUrl()!=null &&data.getPw()!=null && data.getUser()!=null){
				Class.forName(this.data.getDriver()).newInstance();
				GenericObjectPoolConfig config = new GenericObjectPoolConfig();
				config.setTestOnBorrow(true);
				config.setMaxTotal(-1);
				ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
						this.data.getUrl(),
						this.data.getUser(),
						this.data.getPw());
				PoolableConnectionFactory poolConFactory = new PoolableConnectionFactory(
						connectionFactory,null);
				
				ObjectPool connectionPool = new GenericObjectPool(poolConFactory,config);
				poolConFactory.setPool(connectionPool);
				this.source = new PoolingDataSource(connectionPool);
			}
		}
	}
	
	public void closeConnection() throws SQLException{
		if (source!=null){
			source =null;
		}
	}
	
	public Connection getConnection() throws SQLException{
		return source.getConnection();	
	}
	

	public static DBConHandler getInstance (){
		if (instance ==null){
			instance = new DBConHandler ();
		}
		return instance;
	}
	
	public DatabaseConnectionData getData() {
		return data;
	}

	public void setData(DatabaseConnectionData data) {
		this.data = data;
	}
}
