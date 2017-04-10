
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
		this.dataHandler = new DataHandler(catHandler.getDatabases());
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
	
	private void handleLine(String line) throws IOException{
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
		default:
			writer.writeBytes("Unknown action: " + action + '\n');
			break;
		}
	}
	
	private void handleCreate(String line) throws IOException{
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
	
	private void handleDrop(String line) throws IOException{
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
	
	private String handleCreateTable(String line){
		String tableName = line.split("[ \\(\\)]")[2];
		List<String> attributes = extractAttributes(line, false, Patterns.TABLE_ATTRIBUTES);
		PrimaryKey primaryKey = new PrimaryKey();
		List<UniqueKey> uniqueKeys = new ArrayList<UniqueKey>();
		List<Attribute> attributesList = new ArrayList<Attribute>();
		for(String a : attributes){
			Attribute aModel = new Attribute();
			String name = a.split(" ")[0];
			aModel.setName(name);
			if(Patterns.CHAR.getMatcher(a).find()){
				aModel.setType(AttributeType.CHAR);
				Matcher matcher = Patterns.ATTRIBUTES.getMatcher(a);
				matcher.find();
				String length = matcher.group();
				length = length.substring(1, length.length()-1);
				aModel.setLength(Integer.parseInt(length));
			}else{
				aModel.setType(AttributeType.NUMBER);
				aModel.setLength(8);
			}
			aModel.setNull(!Patterns.NOT_NULL.getMatcher(a).find());
			if(Patterns.UNIQUE.getMatcher(a).find()){
				UniqueKey uniqueKey = new UniqueKey();
				uniqueKey.setAttributeName(name);
				uniqueKeys.add(uniqueKey);
			}
			if(Patterns.PRIMARY_KEY.getMatcher(a).find()){
				primaryKey.getAttributeName().add(name);
				aModel.setNull(false);
			}
			attributesList.add(aModel);
		}
		return this.catHandler.createTable(tableName, attributesList, primaryKey, uniqueKeys);
	}
	
	private String handleCreateIndex(String line){
		String[] tokens = line.split(" +");
		String tableName, indexName;
		boolean unique;
		List<String> attributes;
		if(Patterns.UNIQUE.getMatcher(line).find()){
			indexName = tokens[3];
			tableName = tokens[5];
			unique = true;
		}else{
			indexName = tokens[2];
			tableName = tokens[4];
			unique = false;
		}
		attributes = extractAttributes(line,false, Patterns.ATTRIBUTES);
		return this.catHandler.createIndex(tableName, indexName, unique, attributes);
	}

	private List<String> extractAttributes(String line, boolean values, Patterns pattern) {
		List<String> attributes;
		Matcher matcher = pattern.getMatcher(line);
		matcher.find();
		if(values){
			matcher.find();
		}
		String attributesString = matcher.group();
		attributesString = attributesString.substring(1, attributesString.length()-1);
		attributes = Arrays.asList(attributesString.split("[ ]*,[ ]*"));
		return attributes;
	}
	
	private String handleInsert(String line) throws IOException{
		String tableName = line.split(" +")[2];
		List<String> attributes = extractAttributes(line,false, Patterns.ATTRIBUTES);
		List<String> values = extractAttributes(line,true, Patterns.ATTRIBUTES);
		Table table = catHandler.getTable(tableName);
		if(table == null){
			return "Table " + tableName + " does not exist";
		}
		StringBuilder key = new StringBuilder();
		StringBuilder data = new StringBuilder();
		int processedAttributes = 0;
		for(Attribute attribute : table.getStructure().getAttributes()){
			if(attributes.contains(attribute.getName())){
				String attributeValue = values.get(attributes.indexOf(attribute.getName()));
				if(table.getPrimaryKey().getAttributeName().contains(attribute.getName())){
					key.append(attributeValue).append(DATA_SEPARATOR);
				}else{
					data.append(attributeValue).append(DATA_SEPARATOR);
				}
				processedAttributes++;
			}else{
				data.append("null").append(DATA_SEPARATOR);
			}
		}
		if(attributes.size() != processedAttributes)
			return "Not all attributes are in table " + tableName;
		return dataHandler.insertRow(catHandler.getCurrentDatabase().getName(), table, key.toString(), data.toString());
	}
	
	private String handleDelete(String line){
		String[] words = line.split(" +");
		String tableName = words[2];
		String attribute = words[4];
		String value = words[6];
		Table table = catHandler.getTableByName(tableName);
		if(table == null){
			return "Table " + tableName + " does not exist";
		}
		PrimaryKey key = table.getPrimaryKey();
		if(key.getAttributeName().isEmpty()||!key.getAttributeName().get(0).equals(attribute)){
			return "The key attribute " + attribute + " does not exist";
		}
		else{
			dataHandler.deleteRow(catHandler.getNameOfCurrentDatabase(), tableName, value);
		}
		
		return null;
	}

}
