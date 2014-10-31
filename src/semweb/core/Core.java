package semweb.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.gridfs.GridFS;

/**
 * The core functions of the Semweb application.
 * @author Rik
 * 
 */

public class Core {

	private MongoClient 	mongoclient;	// MongoDB client
	private DB 				db;				// Current database
	
	// Variables for connection to MongoDB
	private String 	host;
	private ArrayList<Integer>	ports = new ArrayList<Integer>();
	private String 	userName;
	private String 	password;
	private String 	dbName;		// Name of current database. Necessary for connection.
	
	private GridFS 	gridfs;
	
	private DbHandler		dbHandler;
	private QueryHandler 	queryHandler;
	private SolrHandler		solrHandler;
	
	/**
	 * Create instance with default configuration file.
	 */
	public Core()  {
		this.loadConfig("config.ini");
		this.connect();
	}
	
	/**
	 * Create instance with custom configuration file.
	 * @param configFile - Filename if in same folder, complete path otherwise.
	 */
	public Core(String configFile) {
		this.loadConfig(configFile);
		this.connect();
	}
	
	/**
	 * Create instance with configuration given by arguments.
	 * @param host - URL to MongoDB replica set.
	 * @param ports - Ports of MongoDB replica set.
	 * @param userName - Username of user.
	 * @param password - Corresponding password of user.
	 * @param dbName - Database the user want to authenticate for.
	 */
	public Core(String host, ArrayList<Integer> ports, String userName, String password, String dbName) {
		// Set variables
		this.host = host;
		this.ports = ports;
		this.userName = userName;
		this.password = password;
		this.dbName = dbName;
		
		this.connect();
	} 
	
	/**
	 * Load configuration from a given file.
	 * @param configFile - Filename if in same folder, absolute path otherwise.
	 */
	private void loadConfig(String configFile) {
		try {
			// Load the file
			FileReader file = new FileReader(configFile);
			Scanner scanner = new Scanner(new BufferedReader(file));
			
			// Loop through every line (i.e. every setting)
			while(scanner.hasNext())
			{
				String[] settings = scanner.next().split("=");
				switch(settings[0])
				{
					case "host": this.host = settings[1];
						break;
					case "database": this.dbName = settings[1];
						break;
					case "user": this.userName = settings[1];
						break;
					case "password": this.password = settings[1];
						break;
					case "ports": 
						String[] s0 = settings[1].split(",");
						for(String s1 : s0)	{ this.ports.add(Integer.parseInt(s1)); }
						break;
					default:
						break;
				};
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Connect to MongoDb (replica set) and handle all other necessary statements for this class.
	 */
	public void connect()
	{
		try
		{
			// Put all servers in an arraylist
			ArrayList<ServerAddress> servers = new ArrayList<ServerAddress>();
			for(int p : this.ports)
			{
				servers.add(new ServerAddress(this.host, p));
			}
			// Create MongoClient
			this.mongoclient = new MongoClient(	servers, 
												Arrays.asList(
													MongoCredential.createMongoCRCredential(
														this.userName, 
														this.dbName, 
														this.password.toCharArray()
													)
												)
											);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		// Logged in, now select default database
		this.setDatabase(this.dbName);
		
		// Create GridFS instance
		this.gridfs = new GridFS(this.db);
		
		// Create handlers
		this.dbHandler = new DbHandler(this);
		this.queryHandler = new QueryHandler(this);
		this.solrHandler = new SolrHandler(this);
	}
	
	/**
	 * Select the database that is being used
	 * @param s - Database name
	 */
	public void setDatabase(String s)
	{
		this.dbName = s;
		this.db = mongoclient.getDB(this.dbName);
		// TODO What if one has no rights for this database?
	}
	
	/**
	 * Get the database that is being used
	 */
	public DB getDatabase()
	{
		return this.db;
	}
	
	public String getUsername() {
		return this.userName;
	}
	
	/**
	 * Close the connection to MongoDb
	 */
	public void disconnect()
	{
		this.mongoclient.close();
	}

	/**
	 * Get instance of DbHandler
	 * @return
	 */
	public DbHandler getDbHandler() {
		return this.dbHandler;
	}

	/**
	 * Get instance of QueryHandler
	 * @return
	 */
	public QueryHandler getQueryHandler() {
		return this.queryHandler;
	}

	/**
	 * Get instance of SolrHandler
	 * @return
	 */
	public SolrHandler getSolrHandler() {
		return this.solrHandler;
	}

	/**
	 * Get GridFS instance
	 * @return
	 */
	public GridFS getGridFS() {
		return this.gridfs;
	}

}
