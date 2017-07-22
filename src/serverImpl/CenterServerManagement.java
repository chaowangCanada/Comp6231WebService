package serverImpl;

import java.io.IOException;
import java.util.ArrayList;

import Config.PublicParamters.*;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

@WebService(endpointInterface="Server.DSMSWebIntrfc")
public class CenterServerManagement implements DSMSWebIntrfc{

	public static ArrayList<CenterServer> serverList = new ArrayList<CenterServer>();
	private CenterServer mtl, lvl, ddo;
	
	public CenterServerManagement() throws IOException{

			mtl = new CenterServer(Location.MTL);
			lvl = new CenterServer(Location.LVL);
			ddo = new CenterServer(Location.DDO); 
			
			serverList.add(mtl);
			serverList.add(lvl);
			serverList.add(ddo);
			
			// UDP waiting request thread
			mtl.openUDPListener();
			lvl.openUDPListener();
			ddo.openUDPListener();
	}


	@Override
	public String createTRecord(String managerId, String firstName, String lastName, String address, String phone,
			String specialization, String location) throws IOException {
		if(managerId.substring(0, 3).equalsIgnoreCase("mtl")){
			return mtl.createTRecord(managerId, firstName, lastName, address, phone, specialization, location);
		}
		else if(managerId.substring(0, 3).equalsIgnoreCase("lvl")){
			return lvl.createTRecord(managerId, firstName, lastName, address, phone, specialization, location);
		}
		else if(managerId.substring(0, 3).equalsIgnoreCase("ddo")){
			return ddo.createTRecord(managerId, firstName, lastName, address, phone, specialization, location);
		}
		return "error of finding server";
	}


	@Override
	public String createSRecord(String managerId, String firstName, String lastName, String courseRegistered,
			String status, String statusdate) throws IOException {
		if(managerId.substring(0, 3).equalsIgnoreCase("mtl")){
			return mtl.createSRecord(managerId, firstName, lastName, courseRegistered, status, statusdate);
		}
		else if(managerId.substring(0, 3).equalsIgnoreCase("lvl")){
			return lvl.createSRecord(managerId, firstName, lastName, courseRegistered, status, statusdate);
		}
		else if(managerId.substring(0, 3).equalsIgnoreCase("ddo")){
			return ddo.createSRecord(managerId, firstName, lastName, courseRegistered, status, statusdate);
		}
		return "error of finding server";
	}


	@Override
	public String getRecordCounts(String managerId) throws IOException {
		if(managerId.substring(0, 3).equalsIgnoreCase("mtl")){
			return mtl.getRecordCounts(managerId);
		}
		else if(managerId.substring(0, 3).equalsIgnoreCase("lvl")){
			return lvl.getRecordCounts(managerId);
		}
		else if(managerId.substring(0, 3).equalsIgnoreCase("ddo")){
			return ddo.getRecordCounts(managerId);
		}
		return "error of finding server";
	}


	@Override
	public String editRecord(String managerId, String recordID, String filedname, String newValue) throws IOException {
		if(managerId.substring(0, 3).equalsIgnoreCase("mtl")){
			return mtl.editRecord(managerId, recordID, filedname, newValue);
		}
		else if(managerId.substring(0, 3).equalsIgnoreCase("lvl")){
			return lvl.editRecord(managerId, recordID, filedname, newValue);
		}
		else if(managerId.substring(0, 3).equalsIgnoreCase("ddo")){
			return ddo.editRecord(managerId, recordID, filedname, newValue);
		}
		return "error of finding server";
	}


	@Override
	public String transferRecord(String managerId, String recordID, String remoteCenterServerName) throws IOException {
		if(managerId.substring(0, 3).equalsIgnoreCase("mtl")){
			return mtl.transferRecord(managerId, recordID, remoteCenterServerName);
		}
		else if(managerId.substring(0, 3).equalsIgnoreCase("lvl")){
			return lvl.transferRecord(managerId, recordID, remoteCenterServerName);
		}
		else if(managerId.substring(0, 3).equalsIgnoreCase("ddo")){
			return ddo.transferRecord(managerId, recordID, remoteCenterServerName);
		}
		return "error of finding server";
	}

}
