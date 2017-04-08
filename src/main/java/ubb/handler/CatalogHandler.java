package ubb.handler;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ubb.model.Attribute;
import ubb.model.Database;
import ubb.model.Databases;
import ubb.model.IndexFile;
import ubb.model.PrimaryKey;
import ubb.model.Structure;
import ubb.model.Table;
import ubb.model.UniqueKey;

public class CatalogHandler {

	private static final String CATALOG_FILENAME = "src/main/resources/databaseCatalog.json";
	Gson gson;
	Databases databases;
	Database currentDatabase;

	public CatalogHandler() {
		try {
			gson = new GsonBuilder().create();
			Reader reader = new FileReader(CATALOG_FILENAME);
			databases = gson.fromJson(reader, Databases.class);
			if(databases == null){
				databases = new Databases();
			}
			currentDatabase = databases.getDatabases().isEmpty() ? new Database() : databases.getDatabases().get(0);
			if (databases.getDatabases().isEmpty()) {
				currentDatabase.setName("master");
				databases.getDatabases().add(currentDatabase);
			}
			reader.close();
		} catch (IOException e) {
			System.out.println();
		}
	}
	
	public String setSchema(String database){
		for(Database db : databases.getDatabases()){
			if(db.getName().equals(database)){
				currentDatabase = db;
				return "Current database set to: " + db.getName(); 
			}
		}
		return "Could not find database: " + database;
	}

	public String createDatabase(String dbName) {
		Database newDB = new Database();
		newDB.setName(dbName);
		if(databases.getDatabases().contains(newDB)){
			return "Database already exists!";
		}
		databases.getDatabases().add(newDB);
		return "Database " + dbName + " succesfully created!";
	}

	public String dropDatabase(String dbName) {
		Database db = new Database();
		db.setName(dbName);
		if(!databases.getDatabases().contains(db)){
			return "Database " + dbName + " does not exist!";
		}
		databases.getDatabases().remove(db);
		return "Database " + dbName + " removed!";
	}
	
	public String createIndex(String tableName, String indexName, boolean unique, List<String> attributes){
		Table table = null;
		for(Table t : currentDatabase.getTables()){
			if(t.getName().equals(tableName)){
				table = t;
				break;
			}
		}
		if(table != null){
			IndexFile indexFile = new IndexFile();
			indexFile.setIndexAttributes(attributes);
			indexFile.setIndexName(indexName);
			indexFile.setUnique(unique);
			int keyLength = 0;
			for(Attribute att : table.getStructure().getAttributes()){
				if(attributes.contains(att.getName())){
					keyLength+=att.getLength();
				}
			}
			indexFile.setKeyLength(keyLength);
			table.getIndexFiles().add(indexFile);
			return "Index created!";
		}else{
			return "Table " + tableName + " does not exist!";
		}
	}

	public String createTable(String tableName, List<Attribute> attributes, PrimaryKey primaryKey,
			List<UniqueKey> uniqueKeys) {
		Table newTable = new Table();
		newTable.setName(tableName);
		if(currentDatabase.getTables().contains(newTable)){
			return "Table " + tableName + "already exists!";
		}
		Structure structure = new Structure();
		structure.setAttributes(attributes);
		newTable.setStructure(structure);
		newTable.setPrimaryKey(primaryKey);
		newTable.setUniqueKeys(uniqueKeys);
		currentDatabase.getTables().add(newTable);
		return "Table " + tableName + " created!";
	}
	
	public String dropTable(String tableName){
		Table table = new Table();
		table.setName(tableName);
		if(!currentDatabase.getTables().contains(table)){
			return "Table " + tableName + " does not exist!";
		}
		currentDatabase.getTables().remove(table);
		return "Table removed!";
	}

	public void flush() {
		try {
			Writer writer = new FileWriter(CATALOG_FILENAME);
			gson.toJson(databases, writer);
			writer.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
}
