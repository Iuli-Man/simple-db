package ubb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Table implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String name;
	private Structure structure;
	private PrimaryKey primaryKey;
	private List<ForeignKey> foreignKeys;
	private List<UniqueKey> uniqueKeys;
	private List<IndexFile> indexFiles;

	public Structure getStructure() {
		return structure;
	}

	public void setStructure(Structure structure) {
		this.structure = structure;
	}

	public PrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(PrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
	}

	public List<UniqueKey> getUniqueKeys() {
		if(uniqueKeys == null){
			uniqueKeys = new ArrayList<UniqueKey>();
		}
		return uniqueKeys;
	}

	public void setUniqueKeys(List<UniqueKey> uniqueKeys) {
		this.uniqueKeys = uniqueKeys;
	}

	public List<IndexFile> getIndexFiles() {
		if(indexFiles == null){
			indexFiles = new ArrayList<IndexFile>();
		}
		return indexFiles;
	}

	public void setIndexFiles(List<IndexFile> indexFiles) {
		this.indexFiles = indexFiles;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Table other = (Table) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public List<ForeignKey> getForeignKeys() {
		return foreignKeys;
	}

	public void setForeignKeys(List<ForeignKey> foreignKeys) {
		this.foreignKeys = foreignKeys;
	}
	
	

}
