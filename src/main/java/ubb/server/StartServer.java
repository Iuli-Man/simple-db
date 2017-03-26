package ubb.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class StartServer {
	
	public static void main(String args[]){
		if(args.length != 1){
			System.out.println("usage: StartServer port");
			return;
		}
		int port = Integer.parseInt(args[0]);
		try{
			ServerSocket serverSocket = new ServerSocket(port);
			System.out.println("Server started at port: " + port);
			while(true){
				Socket connectionSocket = serverSocket.accept();
				BufferedReader fromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				DataOutputStream toClient = new DataOutputStream(connectionSocket.getOutputStream());
				RequestHandler handler = new RequestHandler(connectionSocket, fromClient, toClient);
				handler.handleRequests();
			}
		}catch(IOException e){
			System.out.println("Could not start server: " + e.getMessage());
		}
	}

}
