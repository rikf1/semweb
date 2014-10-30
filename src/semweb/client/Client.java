package semweb.client;

import semweb.core.Core;

public class Client {
	
	private Core core;
	
	public Client() {
		this.core = new Core();
	}
	
	public Client(String configFile) {
		this.core = new Core(configFile);
	}

}
