package server;

import java.io.IOException;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

/**
 * webservice intrface.
 * @author Chao
 *
 */
@WebService
@SOAPBinding(style = Style.DOCUMENT, use=Use.LITERAL) //optional
public interface DSMSWebIntrfc {
	
	@WebMethod
	public String createTRecord(String managerId, String firstName, String lastName, String address, 
			  					String phone, String specialization, String location) throws IOException;
	
	@WebMethod
	public String createSRecord(String managerId, String firstName, String lastName, String courseRegistered, 
								String status, String statusdate) throws IOException;
	
	@WebMethod
	public String getRecordCounts(String managerId) throws IOException ;
	
	@WebMethod
	public String editRecord(String managerId, String recordID, String filedname, String newValue) throws IOException ;
	
	@WebMethod
	public String transferRecord(String managerId, String recordID, String remoteCenterServerName) throws IOException ;


}
