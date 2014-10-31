package semweb.client;

import semweb.core.Core;

public class Client {
	
	private Core core;
	
	/**
	 * Configure system with default location of configuration file
	 */
	public Client() {
		this.core = new Core();
		new TUI(this);
	}
	
	/**
	 * Configure system with custom location of configuration file
	 * @param configFile
	 */
	public Client(String configFile) {
		this.core = new Core(configFile);
		new TUI(this);
	}
	
	public Core getCore() {
		return this.core;
	}

}
