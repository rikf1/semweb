package semweb.client;

import java.util.Scanner;

import com.mongodb.gridfs.GridFSDBFile;

/**
 * This class provides a small TUI as a demo and placeholder for a GUI.
 * @author Rik
 *
 */

public class TUI {
	
	private static final String HELP = "help";
	private static final String EXIT = "exit";
	private static final String SETDB = "setdb";
	private static final String FFILENAME = "findfile";
	
	private Client client;
	private Scanner input;
	
	public TUI(Client client) {
		this.client = client;
		this.input = new Scanner(System.in);
		this.start();
	}
	
	private void start() {
		this.println("Welcome to Semweb!");
		String choice = HELP;
		this.printMenu();
		while(choice != EXIT) {
			choice = this.readUserInput();
			this.executeChoice(choice);
		}
	}
	
	private void printMenu() {
		this.println("");
		this.println("This menu can always be obtained using the help command.");
		this.println("");
		this.printMenuItem(HELP, "Show this menu");
		this.printMenuItem(SETDB, "Select database");
		this.printMenuItem(FFILENAME, "Find file by name");
		this.printMenuItem(EXIT, "Exit this program");
		this.println("");
	}
	
	private void printMenuItem(String command, String text) {
		this.println(  "    "+command+"	  "+text);
	}

	private void executeChoice(String choice) {
		switch(choice) {
			case HELP:
				this.printMenu();
				break;
			case EXIT:
				this.println("Program now terminating... Bye!");
				System.exit(0);
				break;
			case SETDB:
				this.setDatabase();
				break;
			case FFILENAME:
				this.findFileByName();
				break;
			default:
				this.println("Unknown command. Try again or enter the help command to obtain the menu.");
				break;
		}
	}

	private void findFileByName() {
		String fileName = this.readUserInput("Enter file name");
		GridFSDBFile file = this.client.getCore().getQueryHandler().getFileByName(fileName);
		if(file != null) {
			this.println("File found: "+file.getFilename());
		} else {
			this.println("No file could be found.");
		}
	}

	private void setDatabase() {
		String output = "Enter the name of database";
		this.client.getCore().setDatabase(this.readUserInput(output));
		this.println("Database set to: "+this.client.getCore().getDatabase());
	}
	
	private String readUserInput() {
		this.print(client.getCore().getUsername()+"@"+client.getCore().getDatabase()+" > ");
		String result = this.input.next();
		this.input.nextLine();
		return result;
	}

	private String readUserInput(String s) {
		this.println(s);
		return this.readUserInput();
	}
	
	private void println(Object o) {
		System.out.println(o);
	}
	
	private void print(Object o) {
		System.out.print(o);
	}


}
