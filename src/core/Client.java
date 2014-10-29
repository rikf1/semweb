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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;





//import net.sf.json.JSONObject; 
//import net.sf.json.JSONSerializer; 

//import org.apache.commons.io.IOUtils;
//import org.json.simple.JSONObject;
//import org.json.XML;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;




















import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
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
import com.mongodb.ReplicaSetStatus;
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
	
	private boolean			talkingToMaster = false;
	private boolean			isReplicaSet = false;
	
	// Variables for connection to MongoDB
	private String 	host;
	private int 	port;
	private ArrayList<Integer>	ports = new ArrayList<Integer>();
	private String 	userName;
	private String 	password;
	private String 	dbName;		// Name of current database. Necessary for connection.
	
	/**
	 * Constructor
	 * @param settingsFile - Create client based on this file containing default settings.
	 * @param isReplicaSet - True if we want to connect to a replica set, false for single server
	 * @throws UnknownHostException
	 * 
	 * The settings file (config.ini by default) must consists of the following layout:
	 * host=host.address.com
	 * port=27017
	 * database=yourDatabase
	 * user=yourUserName
	 * password=yourPassword
	 */
	public Client(String settingsFile, boolean isReplicaSet) throws UnknownHostException 
	{
		echo = new Echo();
		this.isReplicaSet = isReplicaSet;
		
		echo.print("Loading default configuration settings...");
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
		
		// Configuration file is read, so let's connect!
		if(isReplicaSet && ports.size() > 0)
		{
			// Only connect to replica set if we want to AND if we have a port range
			connectReplicaSet();
		}
		else if(port > 0)
		{
			// Do we actually have a port to connect to?
			connectSingle();
		}
		else
		{
			// Port (range) configuration was not set properly
			echo.fail();
		}
	}
	
	/**
	 * Connect client to a single server
	 */
	public void connectSingle() {
		echo.print("Connecting to single server...");
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
				echo.println("Not talking to master: writes not possible");
				echo.print("Set read preference...");
				ReadPreference preference = ReadPreference.primaryPreferred();
				db.setReadPreference(preference);
				echo.ok();
			} else {
				this.talkingToMaster = true;
			}
		
		}
	}
	
	/**
	 * Connect to a replica set
	 */
	public void connectReplicaSet()
	{
		echo.print("Connecting to replica set...");
		try
		{
			// Put all servers in an arraylist
			ArrayList<ServerAddress> servers = new ArrayList<ServerAddress>();
			for(int p : this.ports)
			{
				servers.add(new ServerAddress(this.host, p));
			}
			// Create client
			client = new MongoClient(	servers, 
										Arrays.asList(
											MongoCredential.createMongoCRCredential(
												this.userName, 
												this.dbName, 
												this.password.toCharArray()
											)
										)
									);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Logged in, now select default database
		setDatabase(this.dbName);
		echo.ok();
	}
	
	/**
	 * Checks whether we are communicating with the primary (master) server.
	 * @return true if talking to master or replica set, false otherwise
	 */
	public boolean talkingToMaster()
	{
		return this.isReplicaSet ? this.isReplicaSet : this.talkingToMaster;
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
			case "ports": 
				String[] s0 = setting[1].split(",");
				for(String s1 : s0)	{ this.ports.add(Integer.parseInt(s1)); }
				break;
			default: echo.println("Unknown setting: "+setting[0]);
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
	
	@Deprecated
	public void importJsonFile(String fileName)
	{
		File file = new File(fileName);
		//dbo = new DBObject();
	}
	
	@Deprecated
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
	@Deprecated
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
	 * @throws UnknownHostException 
	 */
	public void sandbox() throws UnknownHostException
	{
		// Dummy function
		
		// Create GridFS instance
		GridFS gfs = new GridFS(this.db);
		
		ReplicaSetStatus rss = client.getReplicaSetStatus();
		echo.println("RSS: "+rss);
		echo.println("Primary: "+rss.getMaster());
		
		//uploadFile("testFile.xml", "otherName4.xml");
		
		// Create DBFile instance
		GridFSDBFile fs = gfs.findOne("otherName4.xml");
		echo.println("Query:"+fs.toString());
		
		echo.println("ID: "+fs.getId().toString());
		echo.println("Filename: "+fs.getFilename());
		echo.println("Chunksize: "+fs.getChunkSize());
		echo.println("Date: "+fs.getUploadDate());
		echo.println("MD5: "+fs.getMD5());
		echo.println("Length: "+fs.getLength());
		
		/*
		 * ADD METADATA
		BasicDBObject doc = new BasicDBObject("name", "MongoDB")
        .append("type", "database")
        .append("count", 1)
        .append("info", new BasicDBObject("x", 203).append("y", 102));
		fs.setMetaData(doc);
		fs.save();
		*/
		echo.println("MetaData: "+fs.getMetaData());
		
		//fs.setMetaData(new BasicDBObject());
		//fs.save();
		
		this.addMetaDataField(fs, "author", "Rik");
		this.addMetaDataField(fs, "study", "Master");
		
		echo.println("MetaData: "+fs.getMetaData());
		
		echo.println("-------- QUERY TEST -------");
		// The awesome query
		BasicDBObject query = new BasicDBObject("filename", "otherName4.xml").append("chunkSize", 261120);
		
		// Did we already query this before?
		this.setCollection("qr.queries");
		DBObject alreadyAsked =this.coll.findOne(query);
		if(alreadyAsked != null) {
			echo.println("Query asked before!");
		}
		
		// Execute query
		this.setCollection("fs.files");
		DBCursor cursor = this.coll.find(query);
		try {
			while (cursor.hasNext())
			{
				echo.println(cursor.next());
			}
		} finally {
			cursor.close();
			
		}
		
		// Put query into collection
		this.setCollection("qr.queries");
		this.coll.insert(query);
		
	}
	
	public void sandbox3() {
		echo.println("-------- SOLR TEST ---------");
		
		HttpSolrServer server = new HttpSolrServer("http://solrctw.cloudapp.net:8080/solr");
		
		
		/*
		try {
		for(int i=1;i<4;i++){
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("cat", "film");
			doc.addField("id", "film-"+i);
			doc.addField("name", "The Film of the Hobbit part "+i);
		
			server.add(doc);
		}
		server.commit();
		} catch (SolrServerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		GridFS gfs = new GridFS(this.db);
		
		try {
			File file = new File("testFile.xml");
			DocumentObjectBinder binder = new DocumentObjectBinder();
			//SolrInputDocument doc = binder.toSolrInputDocument(file); werkt niet
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id", "testFile.xml");
			doc.addField("content", file.getAbsoluteFile());
			server.add(doc);
			server.commit();
			/*
			GridFSInputFile nfile = gfs.createFile(new File("testFile.xml"));
			nfile.setFilename("otherName5.xml");
			nfile.setMetaData(new BasicDBObject());
			nfile.save();
			*/
		} catch (IOException | SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SolrQuery query = new SolrQuery();
		query.setQuery("HERO");
		
		try {
			QueryResponse response = server.query(query);
			SolrDocumentList results = response.getResults();
			for (int i=0;i<results.size();i++){
				echo.println(results.get(i));
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void sandbox4() {
		

				SSHClient ssh = new SSHClient();
		        
		        try {
		        	
			        //ssh.loadKnownHosts();
		        	ssh.addHostKeyVerifier("3a:08:36:1d:b0:09:c1:80:dc:7f:5d:82:8a:35:ca:04");

			        ssh.connect("solrctw.cloudapp.net");
			        ssh.authPassword("azureuser", "UTR!k91Jelle");
		           	//ssh.authPublickey(System.getProperty("azureuser","UTR!k91Jelle"));
		            Session session = ssh.startSession();
		            try {
		            	/*
		                Command cmd = session.exec("ping -c 1 google.com");
		                System.out.println(IOUtils.readFully(cmd.getInputStream()).toString());
		                cmd.join(5, TimeUnit.SECONDS);
		                System.out.println("\n** exit status: " + cmd.getExitStatus());
		                */
		                
		                String commands = "";
		            	commands += "mongo mongodbctw.cloudapp.net:27019;";
		            	commands += "ls;";
		            	commands += "sudo wget http://www.rikvanoutersterp.nl/semweb/monitor3.xml;";
		            	commands += "ls;";
		            	Command cmd = session.exec(commands);
		                echo.println(IOUtils.readFully(cmd.getInputStream()).toString());
		                cmd.join(5, TimeUnit.SECONDS);
		                
		                System.out.println("\n** exit status: " + cmd.getExitStatus());
		            } finally {
		                session.close();
		            }
		            session = ssh.startSession();
		            try {
		            	String commands = "";
		            	commands += "cd /var/lib/tomcat6/solr/solr-4.10.1/example/exampledocs;";
		            	commands += "ls;";
		            	commands += "sudo wget http://www.rikvanoutersterp.nl/semweb/monitor3.xml;";
		            	commands += "ls;";
		            	Command cmd = session.exec(commands);
		                echo.println(IOUtils.readFully(cmd.getInputStream()).toString());
		                cmd.join(5, TimeUnit.SECONDS);
		            } finally {
		            	session.close();
		            }
		        } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
		            try {
						ssh.disconnect();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
		    

		

	}
	
}
