package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import Config.*;
import Config.PublicParamters.*;
import Record.*;

/**
 * Server class, using Webservice, and UDP, for server-server communication
 * @author Chao
 *
 */

public class CenterServer{	
	
	private File logFile = null;
	private HashMap<Character, LinkedList<Record>> recordData;  // store Student Record and Teacher Record. Servers doen't share record
	private Location location;
	private int recordCount = 0; 
	
	public CenterServer(Location loc)throws IOException{
		super();
		location = loc;
		logFile = new File(location+"_log.txt");
		if(! logFile.exists())
			logFile.createNewFile();
		else{
			if(logFile.delete())
				logFile.createNewFile();
		}
		recordData = new HashMap<Character, LinkedList<Record>>();
	}
	
	public void startService(){
		
	}
	
	
	// create new thread wrapper class
	public void openUDPListener(){

		new UDPListenerThread(this){

		}.start();

	}
	
	// thread for while(true) loop, waiting for reply
	private class UDPListenerThread extends Thread{

		private CenterServer server = null;
		
		private String recordCount ;
		
		public UDPListenerThread(CenterServer threadServer) {
			server = threadServer;
		}
		
		@Override
		public void run() {
			DatagramSocket aSocket = null;

			try {
				aSocket  = new DatagramSocket(server.location.getPort());
				byte[] buffer = new byte[1000];
				
				// 3 types of reply, getRecordCount, move Student Record among server, move teacher record among server
				while(true){
					DatagramPacket request = new DatagramPacket(buffer, buffer.length);
					aSocket.receive(request);
					if(request.getData() != null){
						String requestStr = new String(request.getData(), request.getOffset(),request.getLength());
						if(requestStr.equalsIgnoreCase("RecordCounts")){ 
							server.writeToLog("Receive UDP message for : "+ requestStr );
							recordCount = Integer.toString(server.recordCount);
							DatagramPacket reply = new DatagramPacket(recordCount.getBytes(),recordCount.getBytes().length, request.getAddress(), request.getPort()); 
							aSocket.send(reply);
						}
						else if (requestStr.substring(0, 13).equalsIgnoreCase("TeacherRecord")){
							server.writeToLog("Receive UDP message for creating : "+ requestStr.substring(0, 13));
							String[] info = requestStr.split("&");
							server.createTRecord(info[1], info[2], info[3], info[4], info[5], info[6], info[7]);
							String replyStr = "Successfully create Teatcher Record";
							DatagramPacket reply = new DatagramPacket(replyStr.getBytes(),replyStr.getBytes().length, request.getAddress(), request.getPort()); 
							aSocket.send(reply);
						}
						else if (requestStr.substring(0, 13).equalsIgnoreCase("StudentRecord")){
							server.writeToLog("Receive UDP message for creating : "+ requestStr.substring(0, 13));
							String[] info = requestStr.split("&");
							server.createSRecord(info[1], info[2], info[3], info[4].replaceAll("\\[", "").replaceAll("\\]",""), info[5], info[6]);
							String replyStr = "Successfully create Student Record";
							DatagramPacket reply = new DatagramPacket(replyStr.getBytes(),replyStr.getBytes().length, request.getAddress(), request.getPort()); 
							aSocket.send(reply);
						}
					}
				}
			}catch (SocketException e ){System.out.println("Socket"+ e.getMessage());
			}catch (IOException e) {System.out.println("IO"+e.getMessage());
			}finally { if (aSocket !=null ) aSocket.close();}
		}
	}
	
	public void start(){
	
		new ProcessThread(this){

		}.start();

	}
	
	
	private class ProcessThread extends Thread{

		private CenterServer server = null;
		private String returnMsg ;
		private Queue<String> localQueue;

		
		public ProcessThread(CenterServer threadServer) {
			server = threadServer;
			returnMsg = "NULL";
			if(server.location==Location.MTL)
				localQueue = CenterServerManagement.requestQ_MTL;
			else if(server.location == Location.LVL)
				localQueue = CenterServerManagement.requestQ_LVL;
			else if(server.location == Location.DDO)
				localQueue = CenterServerManagement.requestQ_DDO;
		}
		
		@Override
		public void run() {

			while(true){
				Iterator it =  localQueue.iterator();
				while(it.hasNext()){
					String[] requestArr = it.next().toString().split("|");
					if(requestArr[0].equalsIgnoreCase(server.location.toString())){
						try {
							if(requestArr[1].equalsIgnoreCase("CT"))
								returnMsg=server.createTRecord(requestArr[2], requestArr[3], requestArr[4], requestArr[5], requestArr[6], requestArr[7], requestArr[8]);
							else if(requestArr[1].equalsIgnoreCase("CS"))
								returnMsg=server.createSRecord(requestArr[2], requestArr[3], requestArr[4], requestArr[5], requestArr[6], requestArr[7]);
							else if(requestArr[1].equalsIgnoreCase("RC"))
								returnMsg=server.createSRecord(requestArr[2], requestArr[3], requestArr[4], requestArr[5], requestArr[6], requestArr[7]);
							else if(requestArr[1].equalsIgnoreCase("ER"))
								returnMsg=server.editRecord(requestArr[2], requestArr[3], requestArr[4], requestArr[5]);
							else if(requestArr[1].equalsIgnoreCase("TR"))
								returnMsg=server.transferRecord(requestArr[2], requestArr[3], requestArr[4]);
							
							if(returnMsg != "NULL"){
								server.writeToLog("failed to process request from queue");
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
				
			}
		}

	}
	
	/**
	 * Create Teacher Record implementation
	 * @param managerId
	 * @param firstName
	 * @param lastName
	 * @param address
	 * @param phone
	 * @param specialization
	 * @param location
	 * @return String
	 * @throws IOException
	 */
	public String createTRecord(String managerId, String firstName, String lastName, String address, 
							  String phone, String specialization, String location) throws IOException {
		if(location.equalsIgnoreCase(this.getLocation().toString()) ){
			this.writeToLog("Manager: "+ managerId + " "+location + " creates Teacher record.");
			Record tchrRecord = new TeacherRecord(firstName, lastName, address, phone, Specialization.valueOf(specialization), Location.valueOf(location));
			if(recordData.get(lastName.charAt(0)) == null){
				recordData.put(lastName.charAt(0), new LinkedList<Record>());
			}
			synchronized(recordData.get(lastName.charAt(0))){ // linked list is not thread safe, need to lock avoid race condition
				if(recordData.get(lastName.charAt(0)).add(tchrRecord)){
					String output = "Manager: "+ managerId + " sucessfully write Teacher record. Record ID: "+tchrRecord.getRecordID();
					this.writeToLog(output);
					recordCount++;
					return output;
				}
			}
		}
		this.writeToLog("failed to write Teacher Record");
		System.out.println("failed to write Teacher Record");
		return "failed to write Teacher Record";
	}
	
	/**
	 * Create Student Record implementation
	 * @param managerId
	 * @param firstName
	 * @param lastName
	 * @param courseRegistered
	 * @param status
	 * @param statusdate
	 * @return String
	 * @throws IOException
	 */
	public String createSRecord(String managerId, String firstName, String lastName, String courseRegistered, 
								String status, String statusdate) throws IOException{
		this.writeToLog("Manager: "+ managerId + " "+ location.toString() + " creates Student record.");
		Record studntRecord = new StudentRecord(firstName, lastName, Course.valueOf(courseRegistered), Status.valueOf(status), statusdate);
		if(recordData.get(lastName.charAt(0)) == null){
			recordData.put(lastName.charAt(0), new LinkedList<Record>());
		}
		synchronized(recordData.get(lastName.charAt(0))){ // linked list is not thread safe, need to lock avoid race condition
			if(recordData.get(lastName.charAt(0)).add(studntRecord)){
				String output = "Manager: "+ managerId + " Sucessfully write Student record. Record ID: "+studntRecord.getRecordID();
				this.writeToLog(output);
				recordCount++;
				return output;
			}
		}
		this.writeToLog("failed to write Student Record");
		System.out.println("failed to write Student Record");
		return "failed to write Student Record";
	}
	
	
	/**
	 * Get Record Count Implementation.
	 * Could use single thread, or multiple  thread.
	 * @param managerId
	 * @return
	 * @throws IOException
	 */
	public String getRecordCounts(String managerId) throws IOException{
		this.writeToLog("try to count all record at "+ location.toString());
		String output = this.location.toString() + " " + recordCount + ", ";
		if(CenterServerManagement.serverList.size() ==1 ){
			return output;
		}
		// send request using multi threading
//		else{
//			ExecutorService pool = Executors.newFixedThreadPool(ServerRunner.serverList.size()-1);
//			List<Future<String>> requestArr = new ArrayList<Future<String>>();
//			for(CenterServer server : ServerRunner.serverList){
//				if(server.getLocation() !=this.getLocation()){
//					Future<String> request = pool.submit(new RecordCountRequest(this));
//					requestArr.add(request);
//				}
//			
//			}
//			
//			for(int i = 0 ; i < requestArr.size(); i++){
//				try {
//					output += requestArr.get(i).get();
//				} catch (InterruptedException | ExecutionException e) {
//					e.printStackTrace();
//				}
//			}
//			pool.shutdown();
//		}
		// send request one by one, no threading
		for(CenterServer server : CenterServerManagement.serverList){
			if(server.getLocation() !=this.getLocation()){
				output += server.getLocation().toString() + " " + requestRecordCounts(server) + ",";
			}
		}
//		
		return output;
	}


	/**
	 * Request record count to other server class
	 * @author Chao
	 *
	 */
	private class RecordCountRequest implements Callable<String>{
		
		private CenterServer server;
		private String output;

		public RecordCountRequest(CenterServer server){
			this.server = server;
		}
		
		/**
		 * run method 
		 */
		@Override
		public String call() throws Exception {
			DatagramSocket aSocket = null;
			
			try{
				aSocket = new DatagramSocket();
				byte[] message = "RecordCounts".getBytes();
				InetAddress aHost = InetAddress.getByName("localhost");  // since all servers on same machine
				int serverPort = server.getLocation().getPort();
				DatagramPacket request = new DatagramPacket(message, message.length, aHost , serverPort);
				server.writeToLog("UDP message to "+ server.getLocation().toString());
				aSocket.send(request);
				
				byte[] buffer = new byte[1000];
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(reply);
				server.writeToLog("Receive UDP reply from "+ server.getLocation().toString());
				String str = new String(reply.getData(), reply.getOffset(),reply.getLength());

				return str;
			}
			catch (SocketException e){
				System.out.println("Socket"+ e.getMessage());
			}
			catch (IOException e){
				System.out.println("IO: "+e.getMessage());
			}
			finally {
				if(aSocket != null ) 
					aSocket.close();
			}
			return null;
			
		}
	}
	
	/**
	 * socket programming send message to other server
	 * @param server
	 * @return message to manager log
	 */
	public String requestRecordCounts(CenterServer server){
		DatagramSocket aSocket = null;
		
		try{
			aSocket = new DatagramSocket();
			byte[] message = "RecordCounts".getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");  // since all servers on same machine
			int serverPort = server.getLocation().getPort();
			DatagramPacket request = new DatagramPacket(message, message.length, aHost , serverPort);
			this.writeToLog("UDP message to "+ server.getLocation().toString());
			aSocket.send(request);
			
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);
			this.writeToLog("Receive UDP reply from "+ server.getLocation().toString());
			String str = new String(reply.getData(), reply.getOffset(),reply.getLength());

			return str;
		}
		catch (SocketException e){
			System.out.println("Socket"+ e.getMessage());
		}
		catch (IOException e){
			System.out.println("IO: "+e.getMessage());
		}
		finally {
			if(aSocket != null ) 
				aSocket.close();
		}
		return null;
		
	}
	
	/**
	 * Edit record implementation.
	 * @param managerId
	 * @param recordID
	 * @param fieldName
	 * @param newValue
	 * @return
	 * @throws IOException
	 */
	public String editRecord(String managerId, String recordID, String fieldName, String newValue) throws IOException{
		this.writeToLog("try to edit record for "+recordID);
		String output = new String();

		if(recordID.substring(0,2).equalsIgnoreCase("TR")){
			if(fieldName.equalsIgnoreCase("address")||
					fieldName.equalsIgnoreCase("phone")||
					fieldName.equalsIgnoreCase("location")){
				output= traverseToEdit(recordID, fieldName, newValue, 't', managerId); // t means teacher record
				this.writeToLog(output);
			} else{
				output ="wrong fieldName";
			}
		} 
		else if(recordID.substring(0,2).equalsIgnoreCase("SR")){
			if(fieldName.equalsIgnoreCase("course")||
					fieldName.equalsIgnoreCase("status")||
					fieldName.equalsIgnoreCase("status Date")){
				output = traverseToEdit(recordID, fieldName, newValue, 's', managerId); // s means student record
				this.writeToLog(output);
			}
			else{
				output ="wrong fieldName";
			}
		}
		else{
			output ="wrong recordID";
			this.writeToLog(output);
		}
		
		return output;

	}
	
	/**
	 * transfer record implementation
	 * @param managerId
	 * @param recordID
	 * @param remoteCenterServerName
	 * @return
	 */
	public String transferRecord(String managerId, String recordID, String remoteCenterServerName) {

		Iterator it = recordData.entrySet().iterator();
		while(it.hasNext()){
			   Entry entry = (Entry) it.next();
			   LinkedList<Record> recordList = (LinkedList<Record>) entry.getValue();
			   
			   synchronized(recordList){
				   Iterator listIt = recordList.iterator();
				   
				   while(listIt.hasNext()){
					   Record record = (Record) listIt.next();
					   if(record.getRecordID().equalsIgnoreCase(recordID) &&
							   (remoteCenterServerName.equalsIgnoreCase(Location.MTL.toString()) ||
							    remoteCenterServerName.equalsIgnoreCase(Location.LVL.toString()) ||
							    remoteCenterServerName.equalsIgnoreCase(Location.DDO.toString()))){
						   if(! remoteCenterServerName.equalsIgnoreCase(this.location.toString())){
								String output = "Manager: "+ managerId + " change " + recordID +" locaiton to "+ remoteCenterServerName;
								for(CenterServer server : CenterServerManagement.serverList){
									if(server.getLocation() == Location.valueOf(remoteCenterServerName)){
										if(record instanceof TeacherRecord) 
											((TeacherRecord)record).setLocation(remoteCenterServerName);
								  		requestCreateRecord(server, record, managerId);
								  		listIt.remove();
								  		recordCount --;
									}
								}
								return output;
						   }
						   else{
							   return "cannot transfer record to itself server";
						   }
					   }
				   }
					   
			   }
		}
		return "error while processing record transfer";

	}
	
	/**
	 * since don't have the key, need to traverse hashmap to find the right record
	 * @param recordID
	 * @param fieldName
	 * @param newValue
	 * @param RecordInit
	 * @return
	 */
	private String traverseToEdit(String recordID, String fieldName, String newValue, char RecordInit, String managerID) {
		Iterator it = recordData.entrySet().iterator();
		while(it.hasNext()){
			   Entry entry = (Entry) it.next();
			   LinkedList<Record> recordList = (LinkedList<Record>) entry.getValue();
			   
			   synchronized(recordList){
				   Iterator listIt = recordList.iterator();
				   
				   while(listIt.hasNext()){
					   Record record = (Record) listIt.next();
					   if(record.getRecordID().equalsIgnoreCase(recordID)){
						   if(RecordInit == 't'){
							   if(fieldName.equalsIgnoreCase("address")){
								   ((TeacherRecord)record).setAddress(newValue);
				        	  		return recordID+"'s address is changed to "+((TeacherRecord)record).getAddress();
							   } 
							   else if(fieldName.equalsIgnoreCase("phone")){
								   ((TeacherRecord)record).setPhone(newValue);
				        	  		return recordID+"'s phone is changed to "+((TeacherRecord)record).getPhone();
							   }
							   else if(fieldName.equalsIgnoreCase("location")){
								   newValue = newValue.toUpperCase(); // location are all upper case
								   ((TeacherRecord)record).setLocation(newValue);
				        	  		String output = recordID+"'s location is changed to "+((TeacherRecord)record).getLocation().toString();
				        			for(CenterServer server : CenterServerManagement.serverList){
				        				if(server.getLocation() == Location.valueOf(newValue)){
						        	  		requestCreateRecord(server, record, managerID);
						        	  		listIt.remove();
						        	  		recordCount --;
				        				}
				        			}
				        	  		return output;
							   }
						   } 
						   else if(RecordInit == 's'){
							   if(fieldName.equalsIgnoreCase("course")){
								   newValue = newValue.toUpperCase(); // course, status are all upper case
								   ((StudentRecord)record).editCourse(newValue);
				        	  		return recordID+"'s course is changed to "+((StudentRecord)record).getCourse();
							   } 
							   else if(fieldName.equalsIgnoreCase("status")){
								   newValue = newValue.toUpperCase(); // course, status are all upper case
								   ((StudentRecord)record).setStatus(newValue);
				        	  		return recordID+"'s status is changed to "+((StudentRecord)record).getStatus().toString();
							   }
							   else if(fieldName.equalsIgnoreCase("status date")){
								   ((StudentRecord)record).setStatusDate(newValue);
				        	  		return recordID+"'s status date is changed to "+((StudentRecord)record).getStatusDate();   
							   }
						   }
						   else{
							   return "RecordId has problem";
						   }
					   }
				   }
			   }
		}

		return "cannot find such record";
	}

	/**
	 * socket programming, request to other server to add a record
	 * record re allocation
	 * @param server
	 * @param record
	 */
	private void requestCreateRecord(CenterServer server, Record record, String managerID) {

		DatagramSocket aSocket = null;
		
		try{
			aSocket = new DatagramSocket();
			String recordString  = "";
		    if(record instanceof TeacherRecord) 
		    	recordString += "TeacherRecord&"+managerID+"&"+record.getFirstName()+"&"+record.getLastName()+"&"+((TeacherRecord)record).getAddress()+
		    					"&"+((TeacherRecord)record).getPhone()+"&"+((TeacherRecord)record).getSpecialization().toString()+
		    					"&"+((TeacherRecord)record).getLocation().toString();
		    if (record instanceof StudentRecord)
		    	recordString += "StudentRecord&"+managerID+"&"+record.getFirstName()+"&"+record.getLastName()+"&"+((StudentRecord)record).getCourse()+
								"&"+((StudentRecord)record).getStatus().toString()+
								"&"+((StudentRecord)record).getStatusDate();
		    System.out.println(recordString);
			byte[] message = recordString.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");
			int serverPort = server.getLocation().getPort();
			DatagramPacket request = new DatagramPacket(message, message.length, aHost , serverPort);
			aSocket.send(request);
			
			byte[] buffer = new byte[5000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);

			String str = new String(reply.getData(), reply.getOffset(),reply.getLength());
			System.out.println( str);
		}
		catch (SocketException e){
			System.out.println("Socket"+ e.getMessage());
		}
		catch (IOException e){
			System.out.println("IO: "+e.getMessage());
		}
		finally {
			if(aSocket != null ) 
				aSocket.close();
		}
		
	}
	
	/**
	 * log file write always needs to be multrual exclusion
	 * @param str
	 * @throws IOException
	 */
	public synchronized void writeToLog(String str) throws IOException{
		 FileWriter writer = new FileWriter(logFile,true);
		 Date date = new Date();
		 writer.write(PublicParamters.dateFormat.format(date) +" : " + str  +"\n");
		 writer.flush();
		 writer.close();
	}

	public File getLogFile() {
		return logFile;
	}

	public void setLog(File log) {
		this.logFile = log;
	}

	public HashMap<Character, LinkedList<Record>> getRecordData() {
		return recordData;
	}

	public void setRecordData(HashMap<Character, LinkedList<Record>> recordData) {
		this.recordData = recordData;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public int getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

}
