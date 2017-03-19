package ubb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IndexFile implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String indexName;
	private int keyLength;
	private boolean isUnique;
	private List<String> indexAttributes;
	
	public String getIndexName() {
		return indexName;
	}
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
	public int getKeyLength() {
		return keyLength;
	}
	public void setKeyLength(int keyLength) {
		this.keyLength = keyLength;
	}
	public boolean isUnique() {
		return isUnique;
	}
	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}
	public List<String> getIndexAttributes() {
		if(indexAttributes == null){
			this.indexAttributes = new ArrayList<String>();
		}
		return indexAttributes;
	}
	public void setIndexAttributes(List<String> indexAttributes) {
		this.indexAttributes = indexAttributes;
	}
	
}
