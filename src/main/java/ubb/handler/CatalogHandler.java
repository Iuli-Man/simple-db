package ubb.handler;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
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
	
	public void setSchema(String database){
		for(Database db : databases.getDatabases()){
			if(db.getName().equals(database))
				currentDatabase = db;
		}
	}

	public void createDatabase(String dbName) {
		Database newDB = new Database();
		newDB.setName(dbName);
		databases.getDatabases().add(newDB);
	}

	public void dropDatabase(String dbName) {
		Iterator<Database> it = databases.getDatabases().iterator();
		boolean remove = false;
		while (it.hasNext()) {
			if (it.next().getName().equals(dbName)) {
				remove = true;
				break;
			}
		}
		if (remove) {
			it.remove();
		}
	}
	
	public void createIndex(String tableName, String indexName, boolean unique, List<String> attributes){
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
		}
	}

	public void createTable(String tableName, List<Attribute> attributes, PrimaryKey primaryKey,
			List<UniqueKey> uniqueKeys) {
		Table newTable = new Table();
		newTable.setName(tableName);
		Structure structure = new Structure();
		structure.setAttributes(attributes);
		newTable.setStructure(structure);
		newTable.setPrimaryKey(primaryKey);
		newTable.setUniqueKeys(uniqueKeys);
		currentDatabase.getTables().add(newTable);
	}
	
	public void dropTable(String tableName){
		Iterator<Table> it = this.currentDatabase.getTables().iterator();
		boolean remove = false;
		while(it.hasNext()){
			if(it.next().getName().equals(tableName)){
				remove = true;
				break;
			}
		}
		if(remove){
			it.remove();
		}
	}

	public void destroy() {
		try {
			Writer writer = new FileWriter(CATALOG_FILENAME);
			gson.toJson(databases, writer);
			writer.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
