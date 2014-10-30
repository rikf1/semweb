package semweb.core;

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
	
	public SolrHandler(Core core) {
		this.core = core;
		
		this.connect();
	}
	
	/**
	 * Connect to the Solr server.
	 */
	public void connect() {
		 solrServer = new HttpSolrServer("http://solrctw.cloudapp.net:8080/solr");
		 // TODO make URL dynamic
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

}
