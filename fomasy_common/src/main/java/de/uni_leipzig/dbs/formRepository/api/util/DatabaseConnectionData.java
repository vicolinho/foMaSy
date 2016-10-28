package de.uni_leipzig.dbs.formRepository.api.util;


/**
 * bean that hold the connection properties
 * @author christen
 *
 */
public class DatabaseConnectionData {

	private String url;
	private String pw;
	private String user;
	private String driver;
	
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getPw() {
		return pw;
	}
	public void setPw(String pw) {
		this.pw = pw;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
}
