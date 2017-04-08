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
import ubb.model.Attribute;
import ubb.model.PrimaryKey;
import ubb.model.UniqueKey;
import ubb.model.enums.AttributeType;
import ubb.util.Patterns;

public class RequestHandler {

	private Socket connection;
	private BufferedReader reader;
	private DataOutputStream writer;
	private CatalogHandler handler;

	public RequestHandler(Socket connection, BufferedReader reader, DataOutputStream writer) {
		this.connection = connection;
		this.reader = reader;
		this.writer = writer;
		this.handler = new CatalogHandler();
	}

	public void handleRequests() {
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				handleLine(line);
				handler.flush();
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
			String message = handler.setSchema(tokens[2]);
			writer.writeBytes(message + '\n');
			break;
		case "DROP":
			handleDrop(line);
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
			message = handler.createDatabase(tokens[2]);
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
			message = handler.dropDatabase(tokens[2]);
			break;
		case "TABLE":
			message = handler.dropTable(tokens[2]);
			break;
		default:
			message = "Unknown identifier: " + object;
			break;
		}
		writer.writeBytes(message + '\n');
	}
	
	private String handleCreateTable(String line){
		String tableName = line.split(" ")[2];
		Matcher matcher = Patterns.ATTRIBUTES.getMatcher(line);
		matcher.find();
		String attributesString = matcher.group();
		attributesString = attributesString.substring(1, attributesString.length()-1);
		String[] attributes = attributesString.split("[ ]*,[ ]*");
		PrimaryKey primaryKey = new PrimaryKey();
		List<UniqueKey> uniqueKeys = new ArrayList<UniqueKey>();
		List<Attribute> attributesList = new ArrayList<Attribute>();
		for(String a : attributes){
			Attribute aModel = new Attribute();
			String name = a.split(" ")[0];
			aModel.setName(name);
			if(Patterns.CHAR.getMatcher(a).find()){
				aModel.setType(AttributeType.CHAR);
				matcher = Patterns.ATTRIBUTES.getMatcher(a);
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
		return this.handler.createTable(tableName, attributesList, primaryKey, uniqueKeys);
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
		Matcher matcher = Patterns.ATTRIBUTES.getMatcher(line);
		matcher.find();
		String attributesString = matcher.group();
		attributesString = attributesString.substring(1, attributesString.length()-1);
		attributes = Arrays.asList(attributesString.split("[ ]*,[ ]*"));
		return this.handler.createIndex(tableName, indexName, unique, attributes);
	}

}
