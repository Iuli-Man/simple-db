package ubb.model;

import java.io.Serializable;

public class UniqueKey implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String attributeName;

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

}
