package semweb.core;

import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;

/**
 * This class handles the query functions regarding MongoDb.
 * @author Rik
 *
 */

public class QueryHandler {
	
	private Core core;
	
	public QueryHandler(Core core) {
		this.core = core;
	}
	
	/**
	 * Get a GridFSDBFile object of a file from the database.
	 * @param fileName
	 * @return
	 */
	public GridFSDBFile getFileByName(String fileName) {
		return core.getGridFS().findOne(fileName);
	}
	
	public GridFSDBFile getFileByObjectId(ObjectId objectId) {
		return core.getGridFS().findOne(objectId);
	}
	
	public List<GridFSDBFile> getFileListByQuery(DBObject query) {
		return core.getGridFS().find(query);
	}
	
	/**
	 * Check whether a file exists in the database 
	 * @param fileName
	 * @return
	 */
	public boolean fileExistsByFileName(String fileName) {
		boolean result = false;
		GridFSDBFile file = this.getFileByName(fileName);
		if(file != null) {
			result = true;
		}
		return result;
	}
	
	/**
	 * Get the metadata of a file
	 * @param fileName
	 * @return
	 */
	public DBObject getMetaData(GridFSDBFile file) {
		return file.getMetaData();
	}
	


}
