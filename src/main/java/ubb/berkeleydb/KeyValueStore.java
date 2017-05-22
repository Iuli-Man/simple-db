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
import ubb.model.ForeignKey;
import ubb.model.Table;
import ubb.model.UniqueKey;
import ubb.util.Constants;

public class KeyValueStore {

	private Map<String, Environment> envMap = new HashMap<String, Environment>();
	private Map<String, EntityStore> storeMap = new HashMap<String, EntityStore>();

	private Map<String, PrimaryIndex<String, StoreEntity>> indexes = new HashMap<String, PrimaryIndex<String, StoreEntity>>();

	public KeyValueStore(Databases databases) {
		for (Database db : databases.getDatabases()) {
			for (Table table : db.getTables()) {
				createStoreFile(Constants.TABLE, db.getName() + "." + table.getName());
				for (UniqueKey uniqueKey : table.getUniqueKeys()) {
					createStoreFile(Constants.INDEX,
							db.getName() + "." + table.getName() + "." + uniqueKey.getAttributeName());
				}
			}
		}
	}

	public void createStoreFile(String type, String indexName) {
		EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
		envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);
		try {
			File file = new File("./" + type);
			file.mkdir();
			file = new File("./" + type + '/' + indexName);
			file.mkdir();
			envMap.put(indexName, new Environment(file, envConfig));
			storeMap.put(indexName, new EntityStore(envMap.get(indexName), "EntityStore", storeConfig));
			indexes.put(indexName, storeMap.get(indexName).getPrimaryIndex(String.class, StoreEntity.class));
		} catch (DatabaseException dbe) {
			System.out.println("Cannot open db environment: " + dbe.getMessage());
		}
	}

	public void deleteTable(String tableName) {
		File table = new File("/tables/" + tableName);
		envMap.remove(tableName);
		storeMap.remove(tableName);
		indexes.remove(tableName);
		table.delete();
	}

	public String putRow(String database, Table table, String key, String data) {
		StoreEntity row = new StoreEntity();
		row.setKey(key);
		row.setData(data);
		try {
			PrimaryIndex<String, StoreEntity> index = indexes.get(database + "." + table.getName());
			StoreEntity entity = index.get(key);
			if (entity != null) {
				return "Row with primary key " + Arrays.asList(key.split("#")).toString() + " exists";
			}

			index.put(row);
		} catch (DatabaseException e) {
			return "Cannot insert row: " + e.getMessage();
		}
		return "Row inserted";
	}

	public String putRowInIndex(String database, String table, String attribute, String value, String primaryKey) {
		StoreEntity row = new StoreEntity();
		row.setKey(value);
		row.setData(primaryKey);
		try {
			PrimaryIndex<String, StoreEntity> index = indexes.get(database + "." + table + "." + attribute);
			index.put(row);
			return null;
		} catch (DatabaseException e) {
			return "Error occured during update of index for " + attribute + ": " + e.getMessage();
		}
	}

	public String deleteRow(String tableName, String key) {
		try {
			indexes.get(tableName).delete(key);
		} catch (DatabaseException e) {
			return "Cannot delete row: " + e.getMessage();
		}
		return "Row deleted";
	}

	public String checkUnique(String database, String table, String attribute, String value) {
		PrimaryIndex<String, StoreEntity> index = indexes.get(database + "." + table + "." + attribute);
		try {
			if (index.contains(value)) {
				return attribute + " already contains value " + value;
			}
			return null;
		} catch (DatabaseException e) {
			return "Something went wrong when checking uniqueness: " + e.getMessage();
		}
	}

	public String checkForeignKey(String database, ForeignKey fk, String value) {
		String valuePk = value+"#";
		StringBuffer indexName = new StringBuffer(database + "." + fk.getRefTable());
		PrimaryIndex<String, StoreEntity> indexPk = indexes.get(indexName.toString());
		indexName.append("." + fk.getRefAttr());
		PrimaryIndex<String, StoreEntity> index = indexes.get(indexName.toString());
		try {
			// has unique index
			if (index != null && index.get(value) != null) {
				return null;
			}
			// has primary key index
			if(indexPk != null && indexPk.get(valuePk) != null){
				return null;
			}
			return "Referenced key is not in " + fk.getRefTable() + "." + fk.getRefAttr();
		} catch (DatabaseException e) {
			return "Something went wrong when checking index: " + e.getMessage();
		}
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
