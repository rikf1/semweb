/**
 * Main class
 * @author Rik
 */
package core;

import java.net.UnknownHostException;

public class Main {
	
	public static void main(String[] args) {
		
		Echo echo = new Echo();
		echo.ln("WELCOME!");
		//echo.ln("Do you want to load the default configuration settings? (Y/N)");
		//echo.ln("y");
		try {
			Client client = new Client("config.ini");
			echo.ln("Talking to master: "+client.talkingToMaster());
			//client.setDatabase("riktest");
			//client.sandbox();
			client.setCollection("testData");
			//client.sandbox2();
			//client.importJsonString("{ \"orgType\" : \"xml\", \"location\" : \"Enschede\", \"name\" : \"myBuildingNoRoot\", \"release\" : \"May 2015\",    \"wall\" : [   {            \"name\" : \"north wall\",            \"material\" : \"bricks\"        },        {            \"name\" : \"east wall\",            \"material\" : \"bricks\"        },        {            \"name\" : \"south wall\",            \"material\" : \"bricks\"        },        {            \"name\" : \"west wall\",            \"material\" : \"bricks\"    }    ]}");
			//client.importFromXmlFile("C:/Users/Rik/Desktop/Kattouw/model.dae.xml");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		
		echo.ln("Stopping application... Bye!");
		
	}

}