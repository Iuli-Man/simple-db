package ubb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Database  implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String dbName;
	private List<Table> tables;

	public List<Table> getTables() {
		if(tables == null){
			this.tables = new ArrayList<Table>();
		}
		return tables;
	}

	public void setTables(List<Table> tables) {
		if(tables == null){
			this.tables = new ArrayList<Table>();
		}
		this.tables = tables;
	}

	public String getName() {
		return dbName;
	}

	public void setName(String name) {
		this.dbName = name;
	}

}
