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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dbName == null) ? 0 : dbName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Database other = (Database) obj;
		if (dbName == null) {
			if (other.dbName != null)
				return false;
		} else if (!dbName.equals(other.dbName))
			return false;
		return true;
	}

}
