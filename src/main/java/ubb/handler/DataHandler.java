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
	
	public String deleteRow(String database, String tableName, String key) {
		return store.deleteRow(database + "." + tableName, key);
	}

}
