package serverImpl;

import java.io.IOException;

import javax.xml.ws.Endpoint;

public class DSMSPublisher {

	public static void main(String[] args) throws IOException {
		Endpoint endpoint = Endpoint.publish("http://localhost:8080/Comp6231WebService/services/CenterServerManagement", 
				new CenterServerManagement());
		
		System.out.println("Is WebService started ? "+endpoint.isPublished());
	}

}
