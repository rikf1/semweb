package semweb.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.bson.types.ObjectId;

import semweb.core.Core;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

/**
 * This class handles the downloading of files to the Solr server.
 * @author Rik
 *
 */

public class Server {
	
	private Core core;
	private ObjectId fileId;

	public Server(String[] args) {
		// Translate the ports argument to a list
		ArrayList<Integer> ports = new ArrayList<Integer>();
		String[] s0 = args[1].split(",");
		for(String s1 : s0)	{ ports.add(Integer.parseInt(s1)); }
		
		// [host] [ports] [database] [user] [password] [fileId]
		this.core = new Core(args[0], ports, args[2], args[3], args[4]);
		this.fileId = new ObjectId(args[5]);
		this.run();
	}
	
	public void run() {
		GridFSDBFile file = this.core.getQueryHandler().getFileByObjectId(this.fileId);
		File f = new File("exampledocs/"+file.getFilename());
		if(!f.exists()) { 
			try {
				file.writeTo("exampledocs/"+file.getFilename());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("File already exists on disk. No file was downloaded from the database.");
		}
	}
}
