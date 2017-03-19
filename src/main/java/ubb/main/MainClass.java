package ubb.main;

import ubb.handler.CatalogHandler;

public class MainClass {
	
	public static void main(String[] args){
		CatalogHandler handler = new CatalogHandler();
		handler.addDatabase("test");
	}

}
