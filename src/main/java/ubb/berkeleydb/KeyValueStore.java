package ubb.berkeleydb;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

import ubb.model.Database;
import ubb.model.Databases;
import ubb.model.Table;

public class KeyValueStore {

	private Map<String, Environment> envMap = new HashMap<String, Environment>();
	private Map<String, EntityStore> storeMap = new HashMap<String, EntityStore>();

	private Map<String, PrimaryIndex<String, StoreEntity>> index = new HashMap<String, PrimaryIndex<String, StoreEntity>>();
	
	public KeyValueStore(Databases databases){
		for(Database db : databases.getDatabases()){
			for(Table table : db.getTables()){
				createTable(db.getName() + "." + table.getName());
			}
		}
	}

	public void createTable(String tableName) {
		EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
		envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);
		try {
			File file = new File("./tables");
			file.mkdir();
			file = new File("./tables/"+tableName);
			file.mkdir();
			envMap.put(tableName, new Environment(file, envConfig));
			storeMap.put(tableName, new EntityStore(envMap.get(tableName), "EntityStore", storeConfig));
			index.put(tableName, storeMap.get(tableName).getPrimaryIndex(String.class, StoreEntity.class));
		} catch (DatabaseException dbe) {
			System.out.println("Cannot open db environment: " + dbe.getMessage());
		}
	}
	
	public void deleteTable(String tableName){
		File table = new File("/tables/"+tableName);
		envMap.remove(tableName);
		storeMap.remove(tableName);
		index.remove(tableName);
		table.delete();
	}

	public String putRow(String database, Table table, String key, String data) {
		StoreEntity row = new StoreEntity();
		row.setKey(key);
		row.setData(data);
		try {
			StoreEntity entity = index.get(database + table.getName()).get(key);
			if(entity != null){
				return "Row with primary key " + Arrays.asList(key.split("#")).toString() + " exists";
			}
			index.get(database + table.getName()).put(row);
		} catch (DatabaseException e) {
			return "Cannot insert row: " + e.getMessage();
		}
		return "Row inserted";
	}

	public String deleteRow(String tableName, String key) {
		try {
			index.get(tableName).delete(key);
		} catch (DatabaseException e) {
			return "Cannot delete row: " + e.getMessage();
		}
		return "Row deleted";
	}

	public void close() {
		try {
			for (Environment env : this.envMap.values()) {
				if (env != null) {
					env.cleanLog();
					env.close();
				}
			}
			for (EntityStore store : this.storeMap.values()) {
				if (store != null) {
					store.close();
				}
			}
		} catch (DatabaseException e) {
			System.err.println("Cannot close db environment: " + e.getMessage());
		}
	}
}
