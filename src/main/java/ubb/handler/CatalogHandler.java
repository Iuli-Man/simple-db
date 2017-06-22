package ubb.handler;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ubb.berkeleydb.KeyValueStore;
import ubb.model.Attribute;
import ubb.model.Database;
import ubb.model.Databases;
import ubb.model.ForeignKey;
import ubb.model.IndexFile;
import ubb.model.KeyType;
import ubb.model.PrimaryKey;
import ubb.model.Structure;
import ubb.model.Table;
import ubb.model.UniqueKey;
import ubb.util.Constants;

public class CatalogHandler {

	private static final String CATALOG_FILENAME = "tables/databaseCatalog.json";
	Gson gson;
	Databases databases;
	Database currentDatabase;
	KeyValueStore store;

	public KeyValueStore getStore() {
		return store;
	}

	public void setStore(KeyValueStore store) {
		this.store = store;
	}

	public CatalogHandler() {
		try {
			gson = new GsonBuilder().create();
			Reader reader = new FileReader(CATALOG_FILENAME);
			databases = gson.fromJson(reader, Databases.class);
			if (databases == null) {
				databases = new Databases();
			}
			store = new KeyValueStore(databases);
			currentDatabase = databases.getDatabases().isEmpty() ? new Database() : databases.getDatabases().get(0);
			if (databases.getDatabases().isEmpty()) {
				currentDatabase.setName("MASTER");
				databases.getDatabases().add(currentDatabase);
			}
			reader.close();
		} catch (IOException e) {
			System.out.println();
		}
	}
	
	public Databases getDatabases(){
		return databases;
	}
	
	public Database getCurrentDatabase() {
		return currentDatabase;
	}

	public String setSchema(String database) {
		for (Database db : databases.getDatabases()) {
			if (db.getName().equals(database)) {
				currentDatabase = db;
				return "Current database set to: " + db.getName();
			}
		}
		return "Could not find database: " + database;
	}

	public String createDatabase(String dbName) {
		Database newDB = new Database();
		newDB.setName(dbName);
		if (databases.getDatabases().contains(newDB)) {
			return "Database already exists!";
		}
		databases.getDatabases().add(newDB);
		return "Database " + dbName + " succesfully created!";
	}

	public String dropDatabase(String dbName) {
		Database db = new Database();
		db.setName(dbName);
		if (!databases.getDatabases().contains(db)) {
			return "Database " + dbName + " does not exist!";
		}
		databases.getDatabases().remove(db);
		return "Database " + dbName + " removed!";
	}
	
	public String createIndex(Table table, String indexName, boolean unique, String attributeName){
		List<Attribute> attributes = new ArrayList<Attribute>();
		for(Attribute att : table.getStructure().getAttributes()){
			if(att.getName().equals(attributeName)){
				attributes.add(att);
			}
		}
		return this.createIndex(table, indexName, unique, attributes);
	}

	public String createIndex(Table table, String indexName, boolean unique, List<Attribute> attributes) {
		if (table != null) {
			IndexFile indexFile = new IndexFile();
			indexFile.setIndexAttributes(attributes);
			indexFile.setIndexName(indexName);
			indexFile.setUnique(unique);
			int keyLength = 0;
			for (Attribute att : attributes) {
					keyLength += att.getLength();
			}
			indexFile.setKeyLength(keyLength);
			table.getIndexFiles().add(indexFile);
			this.store.createStoreFile(Constants.INDEX, indexName);
			return "Index created!";
		} else {
			return "Table does not exist!";
		}
	}

	public String createTable(String tableName, List<Attribute> attributes, PrimaryKey primaryKey,
			List<UniqueKey> uniqueKeys, List<ForeignKey> foreignKeys) {
		Table newTable = new Table();
		newTable.setName(tableName);
		if (currentDatabase.getTables().contains(newTable)) {
			return "Table " + tableName + "already exists!";
		}
		for(ForeignKey fk : foreignKeys){
			Table tb = getTableByName(fk.getRefTable());
			if(tb == null){
				return "Referenced table " + fk.getRefTable() + " does not exist!";
			}
			boolean contains = false;
			for(Attribute att : tb.getStructure().getAttributes()){
				if(att.getName().equals(fk.getRefAttr())){
					if(att.getKeyType() == KeyType.PRIMARY_KEY || att.getKeyType() == KeyType.UNIQUE_KEY){
						contains = true;
						break;
					}
					boolean hasIndex = false;
					for(IndexFile idx : tb.getIndexFiles()){
						if(idx.getIndexAttributes().contains(att.getName())){
							hasIndex = true;
							contains = true;
							break;
						}
					}
					if(!hasIndex){
						return "Referenced key "+fk.getRefTable()+"."+fk.getRefAttr()+" doesn't have an index!";
					}
				}
			}
			if(!contains){
				return "Referenced key " + fk.getRefAttr() + " does not belong to table " + fk.getRefTable();
			}
			tb.getReferenceTables().add(newTable);
		}
		Structure structure = new Structure();
		structure.setAttributes(attributes);
		newTable.setStructure(structure);
		newTable.setPrimaryKey(primaryKey);
		newTable.setUniqueKeys(uniqueKeys);
		newTable.setForeignKeys(foreignKeys);
		currentDatabase.getTables().add(newTable);
		store.createStoreFile(Constants.TABLE, currentDatabase.getName() + "." + tableName);
		for(UniqueKey uniqueKey : uniqueKeys){
			this.createIndex(newTable, currentDatabase.getName()+"."+tableName+"."+uniqueKey.getAttributeName(), true, uniqueKey.getAttributeName());
		}
		for(ForeignKey foreignKey : foreignKeys){
			this.createIndex(newTable, currentDatabase.getName()+"."+tableName+"."+foreignKey.getAttName(), false, foreignKey.getAttName());
		}
		return "Table " + tableName + " created!";
	}

	public String dropTable(String tableName) {
		Table table = getTable(tableName);
		if (table == null) {
			return "Table " + tableName + " does not exist!";
		}
		for(IndexFile index : table.getIndexFiles()){
			store.deleteIndex(index.getIndexName());
		}
		currentDatabase.getTables().remove(table);
		store.deleteTable(currentDatabase.getName() + "." + tableName);
		return "Table removed!";
	}
	
	public Table getTable(String tableName){
		Table table = new Table();
		table.setName(tableName);
		int index = currentDatabase.getTables().indexOf(table);
		if(index >= 0)
			return currentDatabase.getTables().get(index);
		return null;
	}
	
	public Table getTableByName(String tableName){
		Table table = new Table();
		table.setName(tableName);
		int index = currentDatabase.getTables().indexOf(table);
		if(index >= 0)
			return currentDatabase.getTables().get(index);
		return null;
	}
	
	public String getNameOfCurrentDatabase(){
		return currentDatabase.getName();
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
