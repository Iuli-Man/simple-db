package ubb.berkeleydb;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

import ubb.model.Attribute;
import ubb.model.Database;
import ubb.model.Databases;
import ubb.model.ForeignKey;
import ubb.model.IndexFile;
import ubb.model.KeyType;
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
				for (IndexFile index : table.getIndexFiles()) {
					createStoreFile(Constants.INDEX, index.getIndexName());
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
		File table = new File("./tables/" + tableName);
		envMap.remove(tableName);
		storeMap.remove(tableName);
		indexes.remove(tableName);
		for (String s : table.list()) {
			File currentFile = new File(table.getPath(), s);
			currentFile.delete();
		}
		table.delete();
	}

	public void deleteIndex(String indexName) {
		File index = new File("./indexes/" + indexName);
		envMap.remove(indexName);
		storeMap.remove(indexName);
		indexes.remove(indexName);
		for (String s : index.list()) {
			File currentFile = new File(index.getPath(), s);
			currentFile.delete();
		}
		index.delete();
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

	public String putRowInNonUniqueIndex(String database, String table, String attribute, String value,
			String primaryKey) {
		try {
			PrimaryIndex<String, StoreEntity> index = indexes.get(database + "." + table + "." + attribute);
			StoreEntity entity = index.get(value);
			if (entity == null) {
				StoreEntity row = new StoreEntity();
				row.setKey(value);
				row.setData(primaryKey);
				index.put(row);
				return null;
			} else {
				entity.setData(entity.getData() + primaryKey);
				index.put(entity);
				return null;
			}
		} catch (DatabaseException e) {
			return "Cannot insert into non unique index " + e.getMessage();
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

	public String getValue(String database, Table table, String key, String attribute) {
		try {
			key += "#";
			StoreEntity entity = indexes.get(database + "." + table.getName()).get(key);
			if (entity != null) {
				String data = entity.getData();
				String[] values = data.split("#");
				int index = 0;
				for (Attribute att : table.getStructure().getAttributes()) {
					if (att.equals(attribute)) {
						break;
					}
					if (att.getKeyType() != KeyType.PRIMARY_KEY)
						index++;
				}
				return values[index];
			}
			return "Key not found";
		} catch (DatabaseException e) {
			return "Cannot retrieve value " + e.getMessage();
		}
	}

	public List<String> getAllValues(String database, String table) {
		ArrayList<String> values = new ArrayList<String>();
		try {
			PrimaryIndex<String, StoreEntity> index = indexes.get(database + "." + table);
			if (index != null) {
				EntityCursor<StoreEntity> cursor = index.entities();
				for (StoreEntity row : cursor) {
					values.add(row.getKey() + "#" + row.getData());
					System.out.println(row.getKey() + "#" + row.getData());
				}
			} else
				values.add("ERR#Table not found");
		} catch (DatabaseException e) {
			values.add("ERR#Cannot retrieve values " + e.getMessage());
		}
		return values;
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
		String valuePk = value + "#";
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
			if (indexPk != null && indexPk.get(valuePk) != null) {
				return null;
			}
			return "Referenced key is not in " + fk.getRefTable() + "." + fk.getRefAttr();
		} catch (DatabaseException e) {
			return "Something went wrong when checking index: " + e.getMessage();
		}
	}

	public boolean checkExists(String indexName, String key) {
		PrimaryIndex<String, StoreEntity> index = indexes.get(indexName);
		try {
			if (index != null && index.get(key) != null) {
				return true;
			} else {
				return false;
			}
		} catch (DatabaseException e) {
			return false;
		}
	}

	public boolean checkExists(String database, Table deleteTable, Table referenceTable, String value) {
		PrimaryIndex<String, StoreEntity> index;
		for (ForeignKey fk : referenceTable.getForeignKeys()) {
			if (fk.getRefTable().equals(deleteTable.getName())) {
				index = indexes.get(database + "." + referenceTable.getName() + "." + fk.getAttName());
				if (index != null) {
					try {
						Iterator<StoreEntity> it;
						it = index.entities().iterator();
						while (it.hasNext()) {
							StoreEntity entity = it.next();
							if (entity.getData().contains(value)) {
								return true;
							}
						}
					} catch (DatabaseException e) {
						System.err.println(e.getMessage());
					}
				}
			}
		}
		return false;
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
