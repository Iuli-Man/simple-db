package ubb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Database  implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String name;
	private List<Table> tables;

	public List<Table> getTables() {
		return tables;
	}

	public void setTables(List<Table> tables) {
		if(tables == null){
			this.tables = new ArrayList<Table>();
		}
		this.tables = tables;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
