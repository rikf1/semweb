/**
 * This file belongs to the JAR on the Solr server.
 * It can be called to download a GridFS file.
 * @author Rik
 */

package solr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ReplicaSetStatus;
import com.mongodb.ServerAddress;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

public class Downloader {
	
	//private static MongoClient client;
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(0 == args.length || args.length < 6 ) {
			System.out.println("Not enough arguments");
		}
		else if(args[0] == "-help") {
			System.out.println("Help activated (to be extended later)");
		}
		else
		{
			System.out.println("Initiating...");
			
			MongoClient client;
			// host
			String host = args[0];
			// ports (one port should also work)
			ArrayList<Integer> ports = new ArrayList<Integer>();
			String[] s0 = args[1].split(",");
			for(String s1 : s0)	{ ports.add(Integer.parseInt(s1)); }
			// database
			String dbName = args[2];
			// user
			String userName = args[3];
			// password
			String password = args[4];
			// unique file id
			String fileId = args[5];
			
			System.out.println("Arguments stored.");
			
			try
			{
				// Put all servers in an arraylist
				ArrayList<ServerAddress> servers = new ArrayList<ServerAddress>();
				for(int p : ports)
				{
					servers.add(new ServerAddress(host, p));
				}
				
				System.out.println("Server addresses stored.");
				
				// Create client
				client = new MongoClient(	servers, 
											Arrays.asList(
												MongoCredential.createMongoCRCredential(
													userName, 
													dbName, 
													password.toCharArray()
												)
											)
										);
				
				System.out.println("Made connection to MongoDb.");
				
				// Get the database for GridFS
				DB db = client.getDB(dbName);
				
				System.out.println("Selected database: "+dbName);
				
				// Create GridFS instance
				GridFS gfs = new GridFS(db);
				
				System.out.println("Created GridFS instance.");
				
				System.out.println("Search for file: "+fileId);
				
				GridFSDBFile fs = gfs.findOne(new ObjectId(fileId));
				
				
				
				if(fs == null) {
					System.out.println("No match found");
				} else {
					System.out.println("Found: "+fs.getFilename());
					System.out.println("Creating output...");
					File f = new File(fs.getFilename());
					if(!f.exists()) { 
						fs.writeTo(fs.getFilename());
					} else {
						System.out.println("File already exists on disk. No file was downloaded from the database.");
					}
				}
				
				System.out.println("Bye!");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
