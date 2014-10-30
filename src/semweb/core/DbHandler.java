package semweb.core;

import java.io.File;
import java.io.IOException;

import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * This class handles the administration functions regarding MongoDb, such as upload and edit files.
 * @author Rik
 *
 */

public class DbHandler {
	
	private Core core;
	
	public DbHandler(Core core) {
		this.core = core;
	}
	
	/**
	 * Upload a file to MongoDB (GridFS)
	 * @param fileName - Local name of file to be uploaded, or complete path to file, e.g. C:/Users/John/myfile.xml
	 * @param fileNameDb - Name of file in database
	 */
	public void uploadFile(String fileNameLocal, String fileNameDb)
	{
		try {
			GridFSInputFile file = this.core.getGridFS().createFile(new File(fileNameLocal));
			file.setFilename(fileNameDb);
			file.setMetaData(new BasicDBObject());
			file.save();
		} catch (IOException e) {
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
			// MetaData must exist to add data
			metaData.append(key, value);
			file.setMetaData(metaData);
			file.save();
		}
	}

}
