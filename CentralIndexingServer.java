package assignment1;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CentralIndexingServer implements ITFCentralIndexingServer {

	/*
	 * INSTANCE VARIABLES
	 */
	
	// A DICTIONARY MAPPING FILE TO PEERS
	private Map<String, ArrayList<String>> filesToPeersMapper ;
	
	/*
	 * CONSTRUCTOR
	 */
	
	// INITIALIZES AN EMPTY DICTIONARY
	public CentralIndexingServer() {
		filesToPeersMapper = new HashMap<String, ArrayList<String>>();
	}
	
	/*
	 * INTERFACE IMPLEMENTATIONS
	 */
	
	
	// RETURNS THE LIST OF PEERS IN THE NETWORK WHICH HAVE THE REQUESTED FILE
	@Override
	public boolean register(String clientName, ArrayList<String> fileNames) throws RemoteException {
		
		// If no files in the registering client's directory, only update the mapper
		if(fileNames.size() == 0){
			// If any file previously registered removed, update the mapper to reflect the deletion of the peer
            for (Map.Entry<String, ArrayList<String>> mapEntry : filesToPeersMapper.entrySet()) {
                ArrayList<String> fileHolders = mapEntry.getValue() ;
                fileHolders.remove(clientName) ;
            }
            return true ; 
        }
		
		// else if there are files to register, register them, and also, update the 
		// mapper to reflect any changes made in the registering client's dir.
		
		// for each file entry in the input directory of the requesting client : -
		for(int index=0; index<fileNames.size(); index++){
			// get the file
			String fileEntry = fileNames.get(index) ;
			
			// variable to hold the peers of the file 
			ArrayList<String> peers ;
			
			// if the server has never seen the file
			if(!filesToPeersMapper.containsKey(fileEntry)){
				// create an empty list which will hold the peers for this file
				peers = new ArrayList<String>();
				// create a dictionary entry in the server with an empty peer list
				// this empty list will soon be appended.
				filesToPeersMapper.put(fileEntry, peers);
			}// else if the file was already there
			else{
				// get the existing peer list
				peers = filesToPeersMapper.get(fileEntry);
			}
			
			// if peer not a registered holder of file
			if(!peers.contains(clientName)){
				// Append the current client as a holder of this file.
				peers.add(clientName);
			}
		}
		
		// If any file previously registered removed, update the mapper to reflect the deletion of the peer
		// For mapperEntry in file index dictionary
		for (Map.Entry<String, ArrayList<String>> mapEntry : filesToPeersMapper.entrySet()) {
			String file = mapEntry.getKey() ;
			// If the peer's directory does not contain the file ( deleted or otherwise)
			if(!fileNames.contains(file)){
				ArrayList<String> fileHolders = mapEntry.getValue() ; 
				// but the list of peers for that file conatins the peer's name
				if(fileHolders.contains(clientName))
					// delete peer from list
					fileHolders.remove(clientName);
			}
		}
		
		return true;
	}
	
	// RETRIEVES THE LIST OF PEERS FOR THE REQUESTED FILE.
	@Override
	public ArrayList<String> lookup(String fileName) throws RemoteException {
		
		// if file registered with the server return file
		if(filesToPeersMapper.containsKey(fileName)){
			return filesToPeersMapper.get(fileName);
		}// else return null to show its absence.
		else{
			return null ;
		}
	}
	
	/*
	 * THE CENTRAL INDEXING SERVER APPLICATION CODE
	 */
	
	public static void main(String[] args){
		
		// INITIALIZE SECURITY MANAGER NECESSARY FOR SAFE INTERPROCESS COMMUNICATION.
		if (System.getSecurityManager() == null) {
		    System.setSecurityManager(new SecurityManager());
		}
		
		try {
            
			// create a new CIS object, name it 'CentralIndexingServer'
			// this will help locate the remote object of this server from the rmireg.
			String name = "CentralIndexingServer";
            ITFCentralIndexingServer CIS = new CentralIndexingServer();
            
            // Create the remote object implementing the server interface, and
            // export it to receive all incoming connections at a rondom port
            ITFCentralIndexingServer ProxyCIS =
            		(ITFCentralIndexingServer) UnicastRemoteObject.exportObject(CIS, 0);
            
            // get the rmi registry which should already be running ; deafult port #1099
            Registry registry = LocateRegistry.getRegistry(1099);
            registry.rebind(name, ProxyCIS);
            
            // print reg-bound confirmation
            System.out.println("CentralIndexingServer Bound Successfull");
            
        } catch (Exception e) {
            
        	System.err.println("CentralIndexingServer exception:");
            e.printStackTrace();
        }
	}
}
