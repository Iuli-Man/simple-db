package ubb.handler;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ubb.model.Database;
import ubb.model.Databases;

public class CatalogHandler {

	private static final String CATALOG_FILENAME = "src/main/resources/databaseCatalog.json";
	Gson gson;
	Databases db;
	Writer writer;
	
	public CatalogHandler(){
		try{
			writer =  new FileWriter(CATALOG_FILENAME);
			gson = new GsonBuilder().create();
			Reader reader = new FileReader(CATALOG_FILENAME);
			db = gson.fromJson(reader, Databases.class);
			reader.close();
		}catch (IOException e) {
			System.out.println();
		}
	}
	
	public void addDatabase(String name){
		Database newDB = new Database();
		newDB.setName(name);
		db.getDatabases().add(newDB);
	}
	
	public void destroy(){
		try {
			gson.toJson(db, writer);
			writer.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
