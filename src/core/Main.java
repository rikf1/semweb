/**
 * Main class
 * @author Rik
 */
package core;

import java.net.UnknownHostException;

@Deprecated
public class Main {
	
	public static void main(String[] args) {
		
		Echo echo = new Echo();
		echo.ln("WELCOME!");
		//echo.ln("Do you want to load the default configuration settings? (Y/N)");
		//echo.ln("y");
		try {
			OldClient client = new OldClient("config.ini", true);
			client.sandbox();
			
			//echo.ln("Talking to master: "+client.talkingToMaster());
			//client.setDatabase("riktest");
			
			//client.setCollection("testData");
			//client.sandbox2();
			//client.importJsonString("{ \"orgType\" : \"xml\", \"location\" : \"Enschede\", \"name\" : \"myBuildingNoRoot\", \"release\" : \"May 2015\",    \"wall\" : [   {            \"name\" : \"north wall\",            \"material\" : \"bricks\"        },        {            \"name\" : \"east wall\",            \"material\" : \"bricks\"        },        {            \"name\" : \"south wall\",            \"material\" : \"bricks\"        },        {            \"name\" : \"west wall\",            \"material\" : \"bricks\"    }    ]}");
			//client.importFromXmlFile("C:/Users/Rik/Desktop/Kattouw/model.dae.xml");
			

			//client.sandbox();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		
		echo.ln("Stopping application... Bye!");
		
	}

}
