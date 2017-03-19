package ubb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Table implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String name;
	private Structure structure;
	private PrimaryKey primaryKey;
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

}
