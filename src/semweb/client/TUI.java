package semweb.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoTimeoutException;
import com.mongodb.gridfs.GridFSDBFile;

/**
 * This class provides a small TUI as a demo and placeholder for a GUI.
 * @author Rik
 *
 */

public class TUI {
	
	// Commands
	private static final String HELP = "help";
	private static final String EXIT = "exit";
	private static final String SETDB = "setdb";
	private static final String FFILENAME = "findfile";
	private static final String GETMETADATA = "getmd";
	private static final String SETMETADATA = "addmd";
	private static final String UPLOADFILE = "upload";
	private static final String SEARCHQUERY = "query";
	private static final String MDQUERY = "mdsearch";
	private static final String TRANSFERSOLR = "transfer";
	private static final String SEARCHSOLR = "search";
	
	private Client client;
	private Scanner input;
	private GridFSDBFile file;
	
	public TUI(Client client) {
		this.client = client;
		this.input = new Scanner(System.in);
		this.file = null;
		this.start();
	}
	
	/**
	 * Run the client and print the menu
	 */
	private void start() {
		this.println("Welcome to Semweb!");
		String choice = HELP;
		this.printMenu();
		while(choice != EXIT) {
			choice = this.readUserInput();
			this.executeChoice(choice);
		}
	}
	
	/**
	 * Print a menu with possible options to do
	 */
	private void printMenu() {
		this.println("");
		this.println("This menu can always be obtained using the help command.");
		this.println("");
		this.printMenuItem(HELP, "Show this menu");
		this.printMenuItem(SETDB, "Select database");
		this.printMenuItem(GETMETADATA, "Get metadata from current file");
		this.printMenuItem(SETMETADATA, "Add metadata to current file");
		this.printMenuItem(UPLOADFILE, "Upload a file into the database");
		this.printMenuItem(SEARCHQUERY, "Search for a file using a query");
		this.printMenuItem(MDQUERY, "Search through metadata");
		this.printMenuItem(TRANSFERSOLR, "Download a file from MongoDB to Solr");
		this.printMenuItem(SEARCHSOLR, "Search through file using Solr");
		this.printMenuItem(EXIT, "Exit this program");
		this.println("");
	}
	
	/**
	 * Print an item of the main menu
	 * @param command - Command
	 * @param text - Description of command
	 */
	private void printMenuItem(String command, String text) {
		this.println(  "    "+command+"	  "+text);
	}

	/**
	 * Switch of what the user wants to do.
	 * @param choice - A predefined command
	 */
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
			case GETMETADATA:
				this.getMetaData();
				break;
			case SETMETADATA:
				this.addMetaData();
				break;
			case UPLOADFILE:
				this.uploadFile();
				break;
			case SEARCHQUERY:
				this.searchQuery();
				break;
			case MDQUERY:
				this.metaDataQuery();
				break;
			case TRANSFERSOLR:
				this.transferToSolr();
				break;
			case SEARCHSOLR:
				this.searchSolr();
				break;
			default:
				this.println("Unknown command. Try again or enter the help command to obtain the menu.");
				break;
		}
	}
	
	private void searchSolr() {
		String input = this.readUserInput("Enter query:");
		SolrDocumentList results = this.client.getCore().getSolrHandler().searchFile(input);
		
		for (int i=0;i<results.size();i++){
			this.println(results.get(i));
		}
	}

	private void transferToSolr() {
		if(this.file == null) {
			this.println("No file selected.");
		} else {
			/*
			SSHClient ssh = new SSHClient();
			try {
				ssh.addHostKeyVerifier(this.client.getCore().getSolrHandler().getServerKey());
		        ssh.connect(this.client.getCore().getSolrHandler().getAddress());
			    ssh.authPassword(this.client.getCore().getSolrHandler().getServerUser(), this.client.getCore().getSolrHandler().getServerPass());
		        Session session = ssh.startSession();
		        
				try {
			        String commands = "";
			        commands += "cd "+this.client.getCore().getSolrHandler().getServerPath()+";";
			        commands += "java -jar semweb.jar";
			        // System.out.println("[host] [ports] [database] [user] [password] [fileId]");
			        commands += " "+this.client.getCore().getHost();
			        commands += " "+this.client.getCore().getPorts();
			        commands += " "+this.client.getCore().getDatabase().getName();
			        commands += " "+this.client.getCore().getUsername();
			        commands += " "+this.client.getCore().getPassword();
			        commands += " "+this.file.getId();
			        commands += ";";
			        
			        Command cmd = session.exec(commands);
		            this.println(IOUtils.readFully(cmd.getInputStream()).toString());
		            cmd.join(10, TimeUnit.SECONDS);
	                //System.out.println("\n** exit status: " + cmd.getExitStatus());
	            } finally {
	                session.close();
	            }
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
	            try {
					ssh.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        */
			this.println(this.client.getCore().getSolrHandler().transferToSolr(this.file.getId()));
		}
	}


	/**
	 * Build a query with key-value pairs.
	 * For now everything has to match exactly. TODO
	 * @return Query that is used by searchQuery() and metaDataSearch()
	 */
	private BasicDBObject buildQuery() {
		String key = this.readUserInput("Enter field key:");
		String value = this.readUserInput("Enter value for key "+key+":");
		BasicDBObject query = new BasicDBObject(key, value);
		key = this.readUserInput("Enter field key (enter ; to send query):");
		while(!key.equals(";")) {
			value = this.readUserInput("Enter value of key "+key+":");
			query.append(key, value);
			key = this.readUserInput("Enter field key (enter ; to send query):");
		}
		return query;
	}
	
	/**
	 * Select a file from the list of found files
	 * @param files
	 */
	private void fileListSelect(List<GridFSDBFile> files) {
		if(!files.isEmpty())
		{
			this.println("#   Name (Upload date");
			for (int i = 0; i < files.size(); i++) {
				GridFSDBFile gfs = files.get(i);
				this.println(i+"   "+gfs.getFilename()+" ("+gfs.getUploadDate()+")");
			}
			int choice = -1;
			try {
				choice = Integer.parseInt(this.readUserInput("Enter the number of the file you want to select (enter ; for none):"));
			} catch (NumberFormatException e) {
				
			}
			if(choice >= 0) {
				this.file = this.client.getCore().getQueryHandler().getFileByObjectId((ObjectId) files.get(choice).getId());
			}
		} else {
			this.println("No files found that match the query.");
		}
	}
	
	/**
	 * Regulates a search query for metadata
	 */
	private void metaDataQuery() {
		BasicDBObject query = new BasicDBObject("metadata", this.buildQuery());
		fileListSelect(this.client.getCore().getQueryHandler().getFileListByQuery(query));
	}

	/**
	 * Regulates a search query
	 */
	private void searchQuery() {
		this.println("Possible fields:");
		this.printMenuItem("filename", "Name of a file");
		fileListSelect(this.client.getCore().getQueryHandler().getFileListByQuery(this.buildQuery()));
	}

	/**
	 * Upload a local file to the database
	 */
	private void uploadFile() {
		String localFile = this.readUserInput("Enter filename or full path of file to upload (only paths without spaces supported):");
		String newFile = this.readUserInput("Enter filename you want the file to have in the database:");
		boolean alreadyExists = this.client.getCore().getQueryHandler().fileExistsByFileName(newFile);
		while(alreadyExists) {
			String choice = this.readUserInput("WARNING: There already exists a file with this name in the database.\n"
									+ "If you use this name the existing file will become unavailable using the current version of the program.\n"
									+ "Are you sure you want to use this name? (y/n)");
			if(choice.equals("y") || choice.equals("Y")) {
				break;
			} else if(choice.equals("n") || choice.equals("N")) {
				newFile = this.readUserInput("Enter filename you want the file to have in the database:");
				alreadyExists = this.client.getCore().getQueryHandler().fileExistsByFileName(newFile);
			}
		}
		this.client.getCore().getDbHandler().uploadFile(localFile, newFile);
	}

	/**
	 * Add metadata to the current file.
	 */
	private void addMetaData() {
		if(file == null) {
			// No file selected, so nothing to add metadata to
			this.println("No file selected. Please use "+FFILENAME+" to find a file.");
		} else {
			// We use a map to store all new key-value pairs in, that we later will add to the file
			Map<String,String> kv = new HashMap<String,String>();
			String keyText = "Enter key of new metadata (enter ; to store entered metadata)";
			String key = this.readUserInput(keyText);
			while(!key.equals(";")) {
				String value = this.readUserInput("Enter value of key "+key);
				kv.put(key, value);
				key = this.readUserInput(keyText);
			}
			if(kv.isEmpty()) {
				// Apparently no key-value pairs entered
				this.println("WARNING: No metadata added.");
			} else {
				// Now store every key-value pair
				for (Map.Entry<String, String> pair : kv.entrySet()) {
					this.client.getCore().getDbHandler().addMetaDataField(file, pair.getKey(), pair.getValue());
				}
				this.println("Metadata added.");
			}
		}
	}

	/**
	 * Get the metadata of the selected file
	 */
	private void getMetaData() {
		if(this.file != null) {
			this.println(this.file.getMetaData());
		} else {
			this.println("No file selected.");
		}
	}

	/**
	 * Find (and select TODO split?) a file from the database.
	 * TODO database doesn't matter?
	 */
	@Deprecated
	private void findFileByName() {
		String fileName = this.readUserInput("Enter file name");
		try {
			this.file = this.client.getCore().getQueryHandler().getFileByName(fileName);
		} catch (MongoTimeoutException e) {
			this.println("Lost connection to server...");
		}
		if(this.file != null) {
			this.println("File found: "+file.getFilename());
		} else {
			this.println("No file could be found.");
		}
	}

	/**
	 * Set the name of the current database
	 */
	private void setDatabase() {
		String output = "Enter the name of database";
		this.client.getCore().setDatabase(this.readUserInput(output));
		this.println("Database set to: "+this.client.getCore().getDatabase());
	}
	
	/**
	 * Shows the command line whereafter a user can input commands and other input
	 * @return The user's input (only first command)
	 */
	private String readUserInput() {
		this.print(client.getCore().getUsername()+"@"+client.getCore().getDatabase()+" > "+(this.file==null ? "" : "file: "+this.file.getFilename()+" > "));
		String result = this.input.next();
		this.input.nextLine();
		return result;
	}

	/**
	 * Shows a message above the commandline from readUserInput().
	 * Will call readUserInput() itself.
	 * @param s - The message to show
	 * @return The user's input (only first command)
	 */
	private String readUserInput(String s) {
		this.println(s);
		return this.readUserInput();
	}
	
	/**
	 * Workaround for System.out.println
	 * @param o - Object to print
	 */
	private void println(Object o) {
		System.out.println(o);
	}
	
	/**
	 * Workaround for System.out.print
	 * @param o - Object to print
	 */
	private void print(Object o) {
		System.out.print(o);
	}


}
