package ubb.handler;

import ubb.berkeleydb.KeyValueStore;
import ubb.model.Databases;

public class DataHandler {

	KeyValueStore store;

	public DataHandler(Databases db) {
		store = new KeyValueStore(db);
	}

	public String insertRow(String database, String tableName, String key, String data) {
		return store.putRow(database + "." + tableName, key, data);
	}

}
