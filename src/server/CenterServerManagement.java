package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import Config.PublicParamters.*;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;
/**
 * Adding middle ware class only for webserver, 
 * since we only have 1 interface, we cannot generate 3 wsdl.xml for such interface. 
 * either we write 3 interface, or we build 1 middle ware class based on such inferface, 
 * such middle ware maanage 3 center server.
 * @author Chao
 *
 */
@WebService(endpointInterface="server.DSMSWebIntrfc")
public class CenterServerManagement implements DSMSWebIntrfc{

	public static ArrayList<CenterServer> serverList = new ArrayList<CenterServer>();
	private CenterServer mtl, lvl, ddo;
	public static Queue<String> requestQ_MTL = new LinkedList<String>();
	public static Queue<String> requestQ_LVL = new LinkedList<String>();
	public static Queue<String> requestQ_DDO = new LinkedList<String>();
	
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
		
		mtl.startService();
		lvl.startService();
		ddo.startService();
	}


	/**
	 * depend on managerID, dicdie to call which center server.
	 */
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


	/**
	 * depend on managerID, dicdie to call which center server.
	 */
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


	/**
	 * depend on managerID, dicdie to call which center server.
	 */
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


	/**
	 * depend on managerID, dicdie to call which center server.
	 */
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

/**
 * depend on managerID, dicdie to call which center server.
 */
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

//	/**
//	 * depend on managerID, dicdie to call which center server.
//	 */
//	@Override
//	public String createTRecord(String managerId, String firstName, String lastName, String address, String phone,
//			String specialization, String location) throws IOException {
//		if(managerId.substring(0, 3).equalsIgnoreCase("mtl")){
//			String request = "mtl|" + "CT" + "|" + managerId + "|"  + firstName + "|" + lastName + "|" + address + "|" + phone + "|" + specialization + "|" + location;
//			requestQ_MTL.add(request);
//		}
//		else if(managerId.substring(0, 3).equalsIgnoreCase("lvl")){
//			String request = "lvl|" + "CT" + "|" + managerId + "|"  + firstName + "|" + lastName + "|" + address + "|" + phone + "|" + specialization + "|" + location;
//			requestQ_LVL.add(request);
//		}
//		else if(managerId.substring(0, 3).equalsIgnoreCase("ddo")){
//			String request = "ddo|" + "CT" + "|" + managerId + "|"  + firstName + "|" + lastName + "|" + address + "|" + phone + "|" + specialization + "|" + location;
//			requestQ_DDO.add(request);
//		}
//		return "request queued";
//	}
//
//
//	/**
//	 * depend on managerID, dicdie to call which center server.
//	 */
//	@Override
//	public String createSRecord(String managerId, String firstName, String lastName, String courseRegistered,
//			String status, String statusdate) throws IOException {
//		if(managerId.substring(0, 3).equalsIgnoreCase("mtl")){
//			String request = "mtl|" + "CS" + "|" + managerId + "|"  + firstName + "|" + lastName + "|" + courseRegistered + "|" + status + "|" + statusdate;
//			requestQ_MTL.add(request);
//		}
//		else if(managerId.substring(0, 3).equalsIgnoreCase("lvl")){
//			String request = "lvl|" + "CS" + "|" + managerId + "|"  + firstName + "|" + lastName + "|" + courseRegistered + "|" + status + "|" + statusdate ;
//			requestQ_LVL.add(request);
//		}
//		else if(managerId.substring(0, 3).equalsIgnoreCase("ddo")){
//			String request = "ddo|" + "CS" + "|" + managerId + "|"  + firstName + "|" + lastName + "|" + courseRegistered + "|" + status + "|" + statusdate;
//			requestQ_DDO.add(request);
//		}
//		return "request queued";
//	}
//
//
//	/**
//	 * depend on managerID, dicdie to call which center server.
//	 */
//	@Override
//	public String getRecordCounts(String managerId) throws IOException {
//		if(managerId.substring(0, 3).equalsIgnoreCase("mtl")){
//			String request = "mtl|" + "RC" + "|" + managerId ;
//			requestQ_MTL.add(request);
//		}
//		else if(managerId.substring(0, 3).equalsIgnoreCase("lvl")){
//			String request = "lvl|" + "RC" + "|" + managerId ;
//			requestQ_LVL.add(request);
//		}
//		else if(managerId.substring(0, 3).equalsIgnoreCase("ddo")){
//			String request = "ddo|" + "RC" + "|" + managerId ;
//			requestQ_DDO.add(request);
//		}
//		return "request queued";
//	}
//
//
//	/**
//	 * depend on managerID, dicdie to call which center server.
//	 */
//	@Override
//	public String editRecord(String managerId, String recordID, String filedname, String newValue) throws IOException {
//		if(managerId.substring(0, 3).equalsIgnoreCase("mtl")){
//			String request = "mtl|" + "ER" + "|" + managerId + "|"  + recordID + "|" + filedname + "|" + newValue ;
//			requestQ_MTL.add(request);
//		}
//		else if(managerId.substring(0, 3).equalsIgnoreCase("lvl")){
//			String request = "lvl|" + "ER" + "|" + managerId + "|"  + recordID + "|" + filedname + "|" + newValue ;
//			requestQ_LVL.add(request);
//		}
//		else if(managerId.substring(0, 3).equalsIgnoreCase("ddo")){
//			String request = "ddo|" + "ER" + "|" + managerId + "|"  + recordID + "|" + filedname + "|" + newValue ;
//			requestQ_DDO.add(request);
//		}
//		return "request queued";
//	}
//
//	/**
//	 * depend on managerID, dicdie to call which center server.
//	 */
//	@Override
//	public String transferRecord(String managerId, String recordID, String remoteCenterServerName) throws IOException {
//		if(managerId.substring(0, 3).equalsIgnoreCase("mtl")){
//			String request = "mtl|" + "TR" + "|" + managerId + "|"  + recordID + "|" + remoteCenterServerName ;
//			requestQ_MTL.add(request);
//		}
//		else if(managerId.substring(0, 3).equalsIgnoreCase("lvl")){
//			String request = "lvl|" + "TR" + "|" + managerId + "|"  + recordID + "|" + remoteCenterServerName ;
//			requestQ_LVL.add(request);
//		}
//		else if(managerId.substring(0, 3).equalsIgnoreCase("ddo")){
//			String request = "ddo|" + "TR" + "|" + managerId + "|"  + recordID + "|" + remoteCenterServerName ;
//			requestQ_DDO.add(request);
//		}
//		return "request queued";
//	}

}
