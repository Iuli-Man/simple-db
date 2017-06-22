package ubb.handler;

import java.util.List;

import ubb.berkeleydb.KeyValueStore;
import ubb.model.Attribute;
import ubb.model.ForeignKey;
import ubb.model.Table;

public class DataHandler {

	KeyValueStore store;

	public DataHandler(KeyValueStore store) {
		this.store = store;
	}

	public String insertRow(String database, Table table, String key, String data) {
		return store.putRow(database, table, key, data);
	}
	
	public String insertIndex(String database, String table, String attribute, String value, String primaryKey){
		return store.putRowInIndex(database, table, attribute, value, primaryKey);
	}
	
	public String deleteRow(String database, String tableName, String key) {
		return store.deleteRow(database + "." + tableName, key);
	}
	
	public String deleteIndexRow(String database, Table table, String key, Attribute attribute, String indexName){
		return store.deleteRow(indexName, store.getValue(database, table, key, attribute.getName()));
	}
	
	public String checkUnique(String database, String table, String attribute, String value){
		return store.checkUnique(database, table, attribute, value);
	}
	
	public String checkForeignKey(String database, ForeignKey fk, String value){
		return store.checkForeignKey(database, fk, value);
	}
	
	public String putRowInNonUniqueIndex(String database, String table, String attribute, String value, String primaryKey){
		return store.putRowInNonUniqueIndex(database, table, attribute, value, primaryKey);
	}
	
	public boolean checkExists(String database,Table deleteTable, Table referenceTable, String value){
		return store.checkExists(database, deleteTable, referenceTable, value);
	}
	
	public List<String> getAllValues(String database, String table){
		return store.getAllValues(database, table);
	}

}
