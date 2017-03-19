package ubb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PrimaryKey implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private List<String> attributeNames;

	public List<String> getAttributeName() {
		if(attributeNames == null){
			this.attributeNames = new ArrayList<String>();
		}
		return attributeNames;
	}

	public void setAttributeName(List<String> attributeNames) {
		this.attributeNames = attributeNames;
	}

}
