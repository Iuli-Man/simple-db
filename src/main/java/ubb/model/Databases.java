package ubb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Databases implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private List<Database> databases;

	public List<Database> getDatabases() {
		if(databases == null){
			databases = new ArrayList<Database>();
		}
		return databases;
	}

	public void setDatabases(List<Database> databases) {
		this.databases = databases;
	}
	
}
