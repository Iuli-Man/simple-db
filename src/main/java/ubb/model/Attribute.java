package ubb.model;

import java.io.Serializable;

import ubb.model.enums.AttributeType;

public class Attribute implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String name;
	private AttributeType type;
	private int length;
	private boolean isNull;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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

}
