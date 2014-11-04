package semweb.core;

import java.io.IOException;
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

/**
 * This class handles the connection to the Solr server and searching the files on the Solr server.
 * @author Rik
 *
 */

public class SolrHandler {
	
	private Core core;
	private HttpSolrServer solrServer;
	
	private String address;
	private String port;
	private String dir;
	private String serverUser;
	private String serverPass;
	private String serverPath;
	private String serverKey;
	
	
	public SolrHandler(Core core) {
		this.core = core;
	}
	
	/**
	 * Connect to the Solr server.
	 */
	public void connect() {
		 solrServer = new HttpSolrServer("http://"+this.getAddress()+":"+this.getPort()+"/"+this.getDir());
	}
	
	/**
	 * Search through a file using Solr.
	 * TODO actually it searches through all files now
	 * @param searchTerm
	 * @return
	 */
	public SolrDocumentList searchFile(String searchTerm) {
		SolrQuery query = new SolrQuery();
		query.setQuery(searchTerm);
		
		SolrDocumentList results = null;
		
		try {
			QueryResponse response = this.solrServer.query(query);
			results = response.getResults();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		
		return results;
	}
	
	/**
	 * Call to the server to download a file from MongoDB to the server.
	 * @param fileId - Id of the file
	 * @return Possible console output
	 */
	public String transferToSolr(Object fileId) {
		String result = "";
		SSHClient ssh = new SSHClient();
		try {
			ssh.addHostKeyVerifier(this.getServerKey().toString()); // toString() necessary for some reason
	        ssh.connect(this.getAddress());
		    ssh.authPassword(this.getServerUser(), this.getServerPass());
	        Session session = ssh.startSession();
	        
			try {
		        String commands = "";
		        commands += "cd "+this.getServerPath()+";";
		        commands += "java -jar semweb.jar";
		        commands += " "+this.core.getHost();
		        commands += " "+this.core.getPorts();
		        commands += " "+this.core.getDatabase().getName();
		        commands += " "+this.core.getUsername();
		        commands += " "+this.core.getPassword();
		        commands += " "+fileId;
		        commands += ";";
		        
		        Command cmd = session.exec(commands);
	            result += IOUtils.readFully(cmd.getInputStream()).toString();
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
		return result;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getServerUser() {
		return serverUser;
	}

	public void setServerUser(String serverUser) {
		this.serverUser = serverUser;
	}

	public String getServerPass() {
		return serverPass;
	}

	public void setServerPass(String serverPass) {
		this.serverPass = serverPass;
	}

	public String getServerPath() {
		return serverPath;
	}

	public void setServerPath(String serverPath) {
		this.serverPath = serverPath;
	}

	public String getServerKey() {
		return serverKey;
	}

	public void setServerKey(String serverKey) {
		this.serverKey = serverKey;
	}

}
