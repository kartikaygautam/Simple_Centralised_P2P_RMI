package assignment1;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ITFCentralIndexingServer extends Remote {
	
	/**
	 * Registers a client and its file directory
	 * 
	 * @param clientName  String ID of a peer registering its file directory with the server.
	 * @param fileNames  A list of files the peer wants to register with the CIS.
	 * 
	 * @return true, if registration successfull, false otherwise.
	 */
	public boolean register(String clientName, ArrayList<String> fileNames) throws RemoteException;
	
	/**
	 * Looks up the list of peer name ids with the requested file.
	 * 
	 * @param fileName -The file requested by a client/peer.  
	 * 
	 * @return A list of string ids of the peers holding the requested file.
	 */
	public ArrayList<String> lookup(String fileName) throws RemoteException;
}
