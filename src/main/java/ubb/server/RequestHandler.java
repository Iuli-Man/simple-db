package ubb.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

import ubb.handler.CatalogHandler;

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
		switch (action.toLowerCase()) {
		case "create":
			handleCreate(tokens);
			break;
		case "set":
			String message = handler.setSchema(tokens[2]);
			writer.writeBytes(message + '\n');
			break;
		case "drop":
			handleDrop(tokens);
			break;
		default:
			writer.writeBytes("Unknown action: " + action + '\n');
			break;
		}
	}
	
	private void handleCreate(String[] tokens) throws IOException{
		String object = tokens[1];
		String message;
		switch (object.toLowerCase()) {
		case "database":
			message = handler.createDatabase(tokens[2]);
			break;
		case "table":
			message = handler.createTable(tokens[2], null, null, null);
			break;
		case "index":
			message = handler.createIndex(tokens[4], tokens[2], true, null);
			break;
		default:
			message = "Unknown identifier: " + object;
			break;
		}
		writer.writeBytes(message + '\n');
	}
	
	private void handleDrop(String[] tokens) throws IOException{
		String object = tokens[1];
		String message;
		switch (object.toLowerCase()) {
		case "database":
			message = handler.dropDatabase(tokens[2]);
			break;
		case "table":
			message = handler.dropTable(tokens[2]);
			break;
		default:
			message = "Unknown identifier: " + object;
			break;
		}
		writer.writeBytes(message + '\n');
	}

}
