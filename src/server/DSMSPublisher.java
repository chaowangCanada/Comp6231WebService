package server;

import java.io.IOException;

import javax.xml.ws.Endpoint;
/**
 * runner class to start server middle ware.
 * @author Chao
 *
 */
public class DSMSPublisher {

	public static void main(String[] args) throws IOException {
		Endpoint endpoint = Endpoint.publish("http://localhost:9999/CenterServerManagement", 
				new CenterServerManagement());
		
		System.out.println("Is WebService started ? "+endpoint.isPublished());
	}

}
