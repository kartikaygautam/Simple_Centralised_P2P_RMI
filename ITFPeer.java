package assignment1;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ITFPeer extends Remote {

	/**
	 * Retrieves a list of text lines of the file requested.
	 * 
	 * @param fileName -The file requested by a client/peer.  
	 * 
	 * @return A list of text lines of the file requested.
	 */
	public ArrayList<String> retrieve(String fileName) throws RemoteException;
	
	/**
	 * Retrieves the peer's file directory.  
	 * 
	 * @return A list of file names in the peer's directory
	 */
	public ArrayList<String> getDirectory() throws RemoteException ;
	
	/**
	 * Retrieves the number of files in the peer's directory  
	 * 
	 * @return The number of files in the peer's directory
	 */
	public int numFiles() throws RemoteException;
}
