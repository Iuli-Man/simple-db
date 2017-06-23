
package ubb.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import ubb.handler.CatalogHandler;
import ubb.handler.DataHandler;
import ubb.model.Attribute;
import ubb.model.ForeignKey;
import ubb.model.IndexFile;
import ubb.model.KeyType;
import ubb.model.PrimaryKey;
import ubb.model.Table;
import ubb.model.UniqueKey;
import ubb.model.enums.AttributeType;
import ubb.util.Patterns;

public class RequestHandler {

	private static final String DATA_SEPARATOR = "#";
	private Socket connection;
	private BufferedReader reader;
	private DataOutputStream writer;
	private CatalogHandler catHandler;
	private DataHandler dataHandler;

	public RequestHandler(Socket connection, BufferedReader reader, DataOutputStream writer) {
		this.connection = connection;
		this.reader = reader;
		this.writer = writer;
		this.catHandler = new CatalogHandler();
		this.dataHandler = new DataHandler(catHandler.getStore());
	}

	public void handleRequests() {
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				handleLine(line);
				catHandler.flush();
			}
		} catch (IOException e) {
			System.out.println("Could not read/write from/to client: " + e.getMessage());
		}
	}

	private void handleLine(String line) throws IOException {
		String[] tokens = line.split(" ");
		String action = tokens[0];
		switch (action) {
		case "CREATE":
			handleCreate(line);
			break;
		case "SET":
			String message = catHandler.setSchema(tokens[2]);
			writer.writeBytes(message + '\n');
			break;
		case "DROP":
			handleDrop(line);
			break;
		case "INSERT":
			message = handleInsert(line);
			writer.writeBytes(message + '\n');
			break;
		case "DELETE":
			message = handleDelete(line);
			writer.writeBytes(message + '\n');
			break;
		case "SELECT":
			handleSelect(line);
			break;
		default:
			writer.writeBytes("Unknown action: " + action + '\n');
			break;
		}
	}

	private void handleCreate(String line) throws IOException {
		String[] tokens = line.split(" ");
		String object = tokens[1];
		String message;
		switch (object) {
		case "DATABASE":
			message = catHandler.createDatabase(tokens[2]);
			break;
		case "TABLE":
			message = handleCreateTable(line);
			break;
		case "INDEX":
			message = handleCreateIndex(line);
			break;
		case "UNIQUE":
			message = handleCreateIndex(line);
			break;
		default:
			message = "Unknown identifier: " + object;
			break;
		}
		writer.writeBytes(message + '\n');
	}

	private void handleDrop(String line) throws IOException {
		String[] tokens = line.split(" ");
		String object = tokens[1];
		String message;
		switch (object) {
		case "DATABASE":
			message = catHandler.dropDatabase(tokens[2]);
			break;
		case "TABLE":
			message = catHandler.dropTable(tokens[2]);
			break;
		default:
			message = "Unknown identifier: " + object;
			break;
		}
		writer.writeBytes(message + '\n');
	}

	private String handleCreateTable(String line) {
		String tableName = line.split("[ \\(\\)]")[2];
		List<String> attributes = extractAttributes(line, false, Patterns.TABLE_ATTRIBUTES);
		PrimaryKey primaryKey = new PrimaryKey();
		List<UniqueKey> uniqueKeys = new ArrayList<UniqueKey>();
		List<Attribute> attributesList = new ArrayList<Attribute>();
		List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();
		for (String a : attributes) {
			Attribute aModel = new Attribute();
			String name = a.split(" ")[0];
			aModel.setName(name);
			if (Patterns.CHAR.getMatcher(a).find()) {
				aModel.setType(AttributeType.CHAR);
				Matcher matcher = Patterns.ATTRIBUTES.getMatcher(a);
				matcher.find();
				String length = matcher.group();
				length = length.substring(1, length.length() - 1);
				aModel.setLength(Integer.parseInt(length));
			} else {
				aModel.setType(AttributeType.NUMBER);
				aModel.setLength(8);
			}
			aModel.setNull(!Patterns.NOT_NULL.getMatcher(a).find());
			if (Patterns.UNIQUE.getMatcher(a).find()) {
				UniqueKey uniqueKey = new UniqueKey();
				uniqueKey.setAttributeName(name);
				uniqueKeys.add(uniqueKey);
				aModel.setKeyType(KeyType.UNIQUE_KEY);
			}
			if (Patterns.PRIMARY_KEY.getMatcher(a).find()) {
				primaryKey.getAttributeName().add(name);
				aModel.setNull(false);
				aModel.setKeyType(KeyType.PRIMARY_KEY);
			}
			if (Patterns.FOREIGN_KEY.getMatcher(a).find()) {
				ForeignKey fk = new ForeignKey();
				String table = a.substring(a.indexOf("FOREIGN KEY ON") + 15);
				table = table.trim();
				String[] list = table.split("\\.");
				fk.setAttName(aModel.getName());
				fk.setRefTable(list[0]);
				fk.setRefAttr(list[1]);
				foreignKeys.add(fk);
				aModel.setKeyType(KeyType.FOREIGN_KEY);
			}
			attributesList.add(aModel);
		}
		return this.catHandler.createTable(tableName, attributesList, primaryKey, uniqueKeys, foreignKeys);
	}

	private String handleCreateIndex(String line) {
		String[] tokens = line.split(" +");
		String tableName, indexName;
		boolean unique;
		List<String> attributesString;
		if (Patterns.UNIQUE.getMatcher(line).find()) {
			indexName = tokens[3];
			tableName = tokens[5];
			unique = true;
		} else {
			indexName = tokens[2];
			tableName = tokens[4];
			unique = false;
		}
		attributesString = extractAttributes(line, false, Patterns.ATTRIBUTES);
		Table table = this.catHandler.getTable(tableName);
		List<Attribute> attributes = new ArrayList<Attribute>();
		for (Attribute a : table.getStructure().getAttributes()) {
			if (attributesString.contains(a.getName())) {
				attributes.add(a);
			}
		}
		return this.catHandler.createIndex(table, indexName, unique, attributes);
	}

	private List<String> extractAttributes(String line, boolean values, Patterns pattern) {
		List<String> attributes;
		Matcher matcher = pattern.getMatcher(line);
		matcher.find();
		if (values) {
			matcher.find();
		}
		String attributesString = matcher.group();
		attributesString = attributesString.substring(1, attributesString.length() - 1);
		attributes = Arrays.asList(attributesString.split("[ ]*,[ ]*"));
		return attributes;
	}

	private String handleInsert(String line) throws IOException {
		String tableName = line.split(" +")[2];
		String database = catHandler.getCurrentDatabase().getName();
		List<String> attributes = extractAttributes(line, false, Patterns.ATTRIBUTES);
		List<String> values = extractAttributes(line, true, Patterns.ATTRIBUTES);
		Table table = catHandler.getTable(tableName);
		if (table == null) {
			return "Table " + tableName + " does not exist";
		}
		StringBuilder key = new StringBuilder();
		StringBuilder data = new StringBuilder();
		int processedAttributes = 0;
		for (Attribute attribute : table.getStructure().getAttributes()) {
			if (attributes.contains(attribute.getName())) {
				String attributeValue = values.get(attributes.indexOf(attribute.getName()));
				if (attribute.getKeyType() == KeyType.PRIMARY_KEY) {
					key.append(attributeValue).append(DATA_SEPARATOR);
				} else {
					if (attribute.getKeyType() == KeyType.UNIQUE_KEY) {
						String message = dataHandler.checkUnique(database, table.getName(), attribute.getName(),
								attributeValue);
						if (message != null)
							return message;
						dataHandler.insertIndex(database, table.getName(), attribute.getName(), attributeValue,
								key.toString());
					}
					if (attribute.getKeyType() == KeyType.FOREIGN_KEY) {
						ForeignKey foreignKey = null;
						for (ForeignKey fk : table.getForeignKeys()) {
							if (fk.getAttName().equals(attribute.getName())) {
								foreignKey = fk;
								break;
							}
						}
						String message = dataHandler.checkForeignKey(database, foreignKey, attributeValue);
						if (message != null) {
							return message;
						}
						message = dataHandler.putRowInNonUniqueIndex(database, table.getName(), attribute.getName(),
								attributeValue, key.toString());
					}
					data.append(attributeValue).append(DATA_SEPARATOR);
				}
				processedAttributes++;
			} else {
				if (attribute.isNull())
					data.append("null").append(DATA_SEPARATOR);
				else
					return attribute.getName() + " cannot be null";
			}
		}
		if (attributes.size() != processedAttributes)
			return "Not all attributes are in table " + tableName;
		return dataHandler.insertRow(database, table, key.toString(), data.toString());
	}

	private String handleDelete(String line) {
		String[] words = line.split(" +");
		String tableName = words[2];
		String attribute = words[4];
		String value = words[6];
		Table table = catHandler.getTableByName(tableName);
		if (table == null) {
			return "Table " + tableName + " does not exist";
		}
		PrimaryKey key = table.getPrimaryKey();
		if (key.getAttributeName().isEmpty() || !key.getAttributeName().get(0).equals(attribute)) {
			return "The key attribute " + attribute + " does not exist";
		} else {
			for (Table t : table.getReferenceTables()) {
				for (ForeignKey fk : t.getForeignKeys())
					if (dataHandler.checkExists(catHandler.getNameOfCurrentDatabase(), table, t, value))
						return "Row is referenced by " + t.getName() + "." + fk.getAttName();
			}
			for (IndexFile index : table.getIndexFiles()) {
				dataHandler.deleteIndexRow(catHandler.getNameOfCurrentDatabase(), table, value,
						index.getIndexAttributes().get(0), index.getIndexName());
			}
			return dataHandler.deleteRow(catHandler.getNameOfCurrentDatabase(), tableName, value + "#");
		}
	}

	private void handleSelect(String line) throws IOException {
		String[] tokens = line.split(" ");
		String object = tokens[1];
		List<String> result = null;
		switch (object) {
		case "*":
			result = handleSelectAll(line,null);
			break;
		default:
			result = handleSelectProjection(line);
			break;
		}

		if (result != null) {
			for (String s : result) {
				writer.writeBytes(s + "/");
			}
			writer.writeBytes("end\n");
		}
		else{
			writer.writeBytes("One of the columns does not exist!/end\n");
		}
	}


	private List<String> handleSelectProjection(String line) {
		String[] tokens = line.split(" ");
		String[] selectedAttr = tokens[1].split(",");

		ArrayList<Boolean> attrlist = new ArrayList<Boolean>();
		List<Attribute> attributes = catHandler.getTableByName(tokens[3]).getStructure().getAttributes();
		for (String col:selectedAttr){
			Attribute a = new Attribute();
			a.setName(col);
			if(!attributes.contains(a)){
				return null;
			}
		}
			
		for (int i = 0; i < attributes.size(); i++) {
			Attribute attr = attributes.get(i);
			boolean found = false;
			for (String col : selectedAttr) {
				if (col.equals(attr.getName())) {
					attrlist.add(true);
					found = true;
				}
			}
			if (!found)
				attrlist.add(false);
		}
		
		return handleSelectAll(line,attrlist);
	}

	private List<String> handleSelectAll(String line, ArrayList<Boolean> attrlist) {
		ArrayList<String> conditions = new ArrayList<String>();
		if (line.contains("WHERE")) {
			Matcher matcher = Patterns.CONDITION.getMatcher(line);
			while (matcher.find()) {
				String cond = matcher.group();
				conditions.add(cond);
			}
		}
		String[] tokens = line.split(" ");
		if (line.contains("JOIN")) {
			return handleJoin(line);
		} else {
			return dataHandler.getAllWithSelection(catHandler.getNameOfCurrentDatabase(),
					catHandler.getTable(tokens[3]), conditions,attrlist);
		}
	}

	private List<String> handleJoin(String line) {
		String table1 = line.split(" ")[3];
		String t1 = line.split(" ")[4];
		Matcher matcher = Patterns.JOIN.getMatcher(line);
		matcher.find();
		String join = matcher.group();
		String[] tokens = join.split(" ");
		String table2 = tokens[1];
		String t2 = tokens[2];
		String[] p = tokens[4].split("=");
		String[] p1 = p[0].split("\\.");
		String[] p2 = p[1].split("\\.");
		String pr1, pr2;
		if (p1[0].equals(t1)) {
			pr1 = p1[1];
			pr2 = p2[1];
		} else {
			pr1 = p2[1];
			pr2 = p1[1];
		}
		return dataHandler.getAllValues(catHandler.getNameOfCurrentDatabase(), catHandler.getTable(table1),
				catHandler.getTable(table2), pr1, pr2);
	}

}
