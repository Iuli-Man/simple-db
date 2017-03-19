package ubb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Structure implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private List<Attribute> attributes;

	public List<Attribute> getAttributes() {
		if(attributes == null){
			attributes = new ArrayList<Attribute>();
		}
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

}
