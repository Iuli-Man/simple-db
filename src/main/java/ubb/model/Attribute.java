package ubb.model;

import java.io.Serializable;

import ubb.model.enums.AttributeType;

public class Attribute implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String attributeName;
	private AttributeType type;
	private int length;
	private boolean isNull;
	private boolean isUnique;
	
	public String getName() {
		return attributeName;
	}
	public void setName(String name) {
		this.attributeName = name;
	}
	public AttributeType getType() {
		return type;
	}
	public void setType(AttributeType type) {
		this.type = type;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public boolean isNull() {
		return isNull;
	}
	public void setNull(boolean isNull) {
		this.isNull = isNull;
	}
	public boolean isUnique() {
		return isUnique;
	}
	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

}
