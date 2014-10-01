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






import org.bson.types.ObjectId;

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
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.mongodb.gridfs.GridFSInputFile;
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
	
	/**
	 * Upload a file to MongoDB (GridFS)
	 * @param fileName - Local name of file to be uploaded, or complete path to file, e.g. C:/Users/John/myfile.xml
	 * @param fileNameDb - Name of file in database
	 */
	public void uploadFile(String fileNameLocal, String fileNameDb)
	{
		// Create GridFS instance
		GridFS gfs = new GridFS(this.db);
		
		try {
			GridFSInputFile nfile = gfs.createFile(new File(fileNameLocal));
			nfile.setFilename(fileNameDb);
			nfile.setMetaData(new BasicDBObject());
			nfile.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//fs = gfs.findOne("testFile.xml");
	}
	
	/**
	 * Add a key value pair to a file's metadata
	 * Note: if key already exist, existing value will be overwritten! (TODO: Implement a check for this.)
	 * @param file - File as GridFSDBFile instance
	 * @param key - String
	 * @param value - Object, e.g. String or DBObject
	 */
	public void addMetaDataField(GridFSDBFile file, String key, Object value)
	{		
		// Get current MetaData
		BasicDBObject metaData = (BasicDBObject) file.getMetaData();
		if(metaData != null) 
		{
			metaData.append(key, value);
			file.setMetaData(metaData);
			file.save();
		}
		else
		{
			echo.println("No metadata found. Please add metadata first.");
		}
	}

	
	
	/********* NOT IN USE ****************/
	
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
		
		// Create GridFS instance
		GridFS gfs = new GridFS(this.db);
		
		//uploadFile("testFile.xml", "otherName.xml");
		
		// Create DBFile instance
		GridFSDBFile fs = gfs.findOne("otherName.xml");
		
		echo.ln("ID: "+fs.getId().toString());
		echo.ln("Filename: "+fs.getFilename());
		echo.ln("Chunksize: "+fs.getChunkSize());
		echo.ln("Date: "+fs.getUploadDate());
		echo.ln("MD5: "+fs.getMD5());
		echo.ln("Length: "+fs.getLength());
		
		/*
		 * ADD METADATA
		BasicDBObject doc = new BasicDBObject("name", "MongoDB")
        .append("type", "database")
        .append("count", 1)
        .append("info", new BasicDBObject("x", 203).append("y", 102));
		fs.setMetaData(doc);
		fs.save();
		*/
		echo.ln("MetaData: "+fs.getMetaData());
		
		//fs.setMetaData(new BasicDBObject());
		//fs.save();
		
		this.addMetaDataField(fs, "author", "Rik");
		this.addMetaDataField(fs, "study", "Master");
		
		echo.ln("MetaData: "+fs.getMetaData());
		
	}
	
}
