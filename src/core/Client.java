/**
 * Client
 * @author Rik
 */

package core;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;





//import net.sf.json.JSONObject; 
//import net.sf.json.JSONSerializer; 

//import org.apache.commons.io.IOUtils;
//import org.json.simple.JSONObject;
//import org.json.XML;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

public class Client {
	
	private Echo 			echo;	// Print system
	private MongoClient 	client;	// MongoDB client
	private DB 				db;		// Current database
	private DBCollection	coll;	// Current collection in database
	
	private boolean			talkingToMaster = true;
	
	// Variables for connection to MongoDB
	private String 	host;
	private int 	port;
	private String 	userName;
	private String 	password;
	private String 	dbName;		// Name of current database. Necessary for connection.
	
	/**
	 * Constructor
	 * @param settingsFile - Create client based on this file containing default settings.
	 * @throws UnknownHostException
	 * 
	 * The settings file (config.ini by default) must consists of the following layout:
	 * host=host.address.com
	 * port=27017
	 * database=yourDatabase
	 * user=yourUserName
	 * password=yourPassword
	 */
	public Client(String settingsFile) throws UnknownHostException 
	{
		echo = new Echo();
		
		echo.dot3("Loading default configuration settings");
		try {
			FileReader file = new FileReader(settingsFile);
			Scanner scanner = new Scanner(new BufferedReader(file));
			while(scanner.hasNext())
			{
				loadConfig(scanner.next());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		echo.ok();
		
		// Configuration file read, so let's connect!
		connect();
	}
	
	/**
	 * Connect client to the server
	 */
	public void connect() {
		echo.dot3("Connecting to server");
		try {
			client = new MongoClient(new ServerAddress(this.host, this.port), Arrays.asList(MongoCredential.createMongoCRCredential(this.userName, this.dbName, this.password.toCharArray())));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Logged in, now select default database
		setDatabase(this.dbName);
		echo.ok();
		
		// Check if we are connected to master
		try {
			db.getCollectionNames();
		} catch (MongoException e) { 
			if(e.getCode() == -3) {
				this.talkingToMaster = false;
				echo.ln("Not talking to master: writes not possible");
				echo.dot3("Set read preference");
				ReadPreference preference = ReadPreference.primaryPreferred();
				db.setReadPreference(preference);
				echo.ok();
			}
		
		}
	}
	
	/**
	 * Checks whether we are communicating with the primary (master) server.
	 * @return true if talking to master, false otherwise
	 */
	public boolean talkingToMaster()
	{
		return this.talkingToMaster;
	}
	
	/**
	 * Load given line from configuration file
	 * @param s - Line from configuration file
	 */
	public void loadConfig(String s) {
		String[] setting = s.split("=");
		switch(setting[0])
		{
			case "host": this.host = setting[1];
				break;
			case "port": this.port = Integer.parseInt(setting[1]);
				break;
			case "database": this.dbName = setting[1];
				break;
			case "user": this.userName = setting[1];
				break;
			case "password": this.password = setting[1];
				break;
			default: echo.ln("Unknown setting: "+setting[0]);
				break;
		}	
	}
	
	/**
	 * Select the database that is being used
	 * @param s - Database name
	 */
	public void setDatabase(String s)
	{
		this.dbName = s;
		this.db = client.getDB(this.dbName);
	}
	
	/**
	 * Get the database that is being used
	 */
	public String getDatabase()
	{
		String result = "";
		// Still everything alright? Are we still talking about the same database?
		if(this.dbName.equals(db.getName()))
		{
			result = dbName;
		}
		else
		{
			result = "An error occured. Please re-connect or contact your system administrator. (#101)";
		}
		return result;
	}
	
	/**
	 * Select the collection that is being used
	 * @param s - Collection name
	 */
	public void setCollection(String s)
	{
		coll = db.getCollection(s);
	}
	
	/********* NOT YET SURE OF METHODS BELOW ****************/
	
	public void importJsonFile(String fileName)
	{
		File file = new File(fileName);
		//dbo = new DBObject();
	}
	
	public void importJsonString(String s)
	{
		echo.dot3("Importing JSON");
		DBObject bson = (DBObject) JSON.parse( s );
	    BasicDBObject doc = new BasicDBObject(bson.toMap());
	    coll.insert(doc);
		echo.ok();
	}
	
	/**
	 * Import the content from an xml file into the database
	 * @param fileName - Complete path to file, e.g. C:/Users/John/myfile.xml
	 */
	@SuppressWarnings({ "static-access", "resource" })
	public void importFromXmlFile(String fileName)
	{
		echo.dot3("Importing file '"+fileName+"'");
		File file = new File(fileName);
		String xml = "";
		try {
			// Put file into String
			Scanner scanner = new Scanner(file);
			int count = 0;
			while(scanner.hasNextLine())
			{
				xml += scanner.nextLine();
				count++;
				if(count%1000==0) { echo.ln(""+count); }
			}
			
			// Translate the xml consisting String to JSON (can this be done in fewer steps?)
			XML translator = new XML();
			JSONObject json = translator.toJSONObject(xml);
			DBObject bson = (DBObject) JSON.parse( json.toString() );
	        BasicDBObject doc = new BasicDBObject(bson.toMap());
			coll.insert(doc);
			echo.ok();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Dummy function for testing.
	 */
	public void sandbox()
	{
		
		// Dummy function
		//File file = new File("C:/Users/Rik/Desktop/Kattouw/model.dae.json");
		try {
			
			
	        InputStream is = new FileInputStream("C:/Users/Rik/Desktop/Kattouw/model.dae.json");

	        JSONParser parser = new JSONParser();
	        
	        
	         
	        Object obj = parser.parse(new FileReader("C:/Users/Rik/Desktop/Kattouw/model.dae.json"));
	         
	        JSONObject jsonObject = (JSONObject) obj;
	        DBObject bson = (DBObject) JSON.parse( jsonObject.toString() );
	        BasicDBObject doc = new BasicDBObject(bson.toMap());
			
			/* JUST NO >>25 MIN
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			char[] s = new char[230000000];
			int count = 0;
			while(bis.available()>0)
			{
				int newChar = bis.read();
				s[count] = (char) newChar;
				count++;
				//echo.ln(":: "+(char)bis.read());
				
				
			}
			echo.ln("All chars in array");
			
			String st = "";
			for(int i=0;i<count;i++)
			{
				st += s[i];
			}
			System.out.print(st.substring(count-1000)); 
			
			//echo.ln(s);
			 * 
			 */
			
			/* ARRAY VAN CHARS +-10 MIN
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			char[] s = new char[230000000];
			int count = 0;
			int stop = 229000000;
			while(bis.available()>0)
			{
				int newChar = bis.read();
				s[count] = (char) newChar;
				count++;
				//echo.ln(":: "+(char)bis.read());
				
				if(count==stop) { 
					//for(int i = 220000000; i<stop; i++) System.out.print(s[i]); 
					break;
				}
			}
			for(int i = count-1000; i<=count; i++) System.out.print(s[i]); 
			//echo.ln(s);
			*/
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//DBCursor cur = new DBCursor(collection, query, null, preference);
		//coll = cur.getCollection();
		/*
		Set<String> colls = db.getCollectionNames();
		for(String s : colls)
		{
			echo.ln(s);
		}
		*/
	}

	public void sandbox2()
	{
		File file = new File("C:/Users/Rik/Desktop/Kattouw/model.dae.json");
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
			   // process the line.
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("C:/Users/Rik/Desktop/Kattouw/model.dae_new.json", true)));
				line = line.replace(System.getProperty("line.separator"), "");
				out.print(line);
			    out.close();
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
