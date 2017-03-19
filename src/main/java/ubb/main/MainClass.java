package ubb.main;

import java.util.ArrayList;
import java.util.List;

import ubb.handler.CatalogHandler;
import ubb.model.Attribute;
import ubb.model.PrimaryKey;
import ubb.model.UniqueKey;
import ubb.model.enums.AttributeType;

public class MainClass {
	
	public static void main(String[] args){
		CatalogHandler handler = new CatalogHandler();
//		List<Attribute> attributes = new ArrayList<Attribute>();
//		Attribute a = new Attribute();
//		a.setLength(10);
//		a.setName("nume");
//		a.setNull(false);
//		a.setType(AttributeType.CHAR);
//		attributes.add(a);
//		PrimaryKey primaryKey = new PrimaryKey();
//		List<String> aNames = new ArrayList<String>();
//		aNames.add("nume");
//		primaryKey.setAttributeName(aNames);
//		List<UniqueKey> uniqueKeys = new ArrayList<UniqueKey>();
//		UniqueKey uKey = new UniqueKey();
//		uKey.setAttributeName("nume");
//		handler.createTable("test", attributes, primaryKey, uniqueKeys);
		handler.dropTable("test");
		handler.destroy();
	}

}
