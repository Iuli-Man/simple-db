package ubb.handler;

import ubb.berkeleydb.KeyValueStore;
import ubb.model.Databases;
import ubb.model.Table;

public class DataHandler {

	KeyValueStore store;

	public DataHandler(Databases db) {
		store = new KeyValueStore(db);
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
	
	public String checkUnique(String database, String table, String attribute, String value){
		return store.checkUnique(database, table, attribute, value);
	}

}
