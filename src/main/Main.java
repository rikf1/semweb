package main;

import semweb.client.Client;
import semweb.server.Server;


public class Main {
	
	public static void main() {
		// Initiate Client
		Client client = new Client();
	}
	
	public static void main(String configFile) {
		// Initiate Client
		Client client = new Client(configFile);
	}

	public static void main(String[] args) {
		if(args.length == 1 && args[0] != "help") {
			// Settings loaded through custom configuration file
			// Client initiated
			Client client = new Client(args[0]);
		} else if(args.length == 6 && args[0] != "-help") {
			// All settings given as argument
			// Server initiated
			Server server = new Server(args);
		} else {
			// Called for help or something went wrong
			System.out.println("For the client use the following argument:");
			System.out.println("[configFile]");
			System.out.println("For the server use the following arguments:");
			System.out.println("[host] [ports] [database] [user] [password] [fileId]");
			System.out.println("This text can be retrieved using -help as first argument.");
		}

	}

}
