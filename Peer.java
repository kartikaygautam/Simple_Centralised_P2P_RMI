package assignment1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings("unused")						
public class Peer implements ITFPeer {
	
	/*
	 * INSTANCE VARIABLES
	 */
	
	// THE DIRECTORY OF THE FILES WITH THE PEER
	private ArrayList<String> directory ;
	
	/*
	 * CONSTRUCTOR
	 */
	
	// INITIALIZES THE DIRECTORY OF THE PEER WITH THE LIST OF TEXT FILES IN ITS FOLDER.
	public Peer(){ initializeDirectory(); }
	
	/*
	 * INTERFACE IMPLEMENTATIONS
	 */
	
	
	// RETURNS THE LIST OF PEERS IN THE NETWORK WHICH HAVE THE REQUESTED FILE
	@Override
	public ArrayList<String> retrieve(String fileName) { 
		
		// Get object refercing the file.
		BufferedReader reader = openFile(fileName);
		// Read the file line by line into an array of strings.
		ArrayList<String> fileTextLines = readFileLines(reader) ;
		// Return the textlines' array.
		return fileTextLines ; 
	}
	
	// FETCHES THE NAMES OF THE FILES IN ITS DIRECTORY.
	@Override
	public synchronized ArrayList<String> getDirectory(){
		return this.directory ;
	}
	
	// RETURNS THE NUMBER OF FILES IN THE DIRECTORY
	@Override
	public synchronized int numFiles(){
		return this.directory.size() ;
	}
		
	/*
	 * THE PEER APPLICATION CODE
	 */
	
	public static void main(String[] args){
		
		// INITIALIZE SECURITY MANAGER NECESSARY FOR SAFE INTERPROCESS COMMUNICATION.
		if (System.getSecurityManager() == null) {
		    System.setSecurityManager(new SecurityManager());
		}
		
		try
		{
			// Assign the input name to the client
			String clientName = args[0] ;
			
			// Locate the central indexing server //
			Registry registry = LocateRegistry.getRegistry(1099);
			ITFCentralIndexingServer proxyCIS = (ITFCentralIndexingServer) 
					registry.lookup("CentralIndexingServer");
			
			// Create the peer object and its stub for remote operation 
            ITFPeer peer = new Peer();
			ITFPeer proxyPeer =
            		(ITFPeer) UnicastRemoteObject.exportObject(peer, 0);
            // Bind the client's object to the RMI Registry
			registry.rebind(clientName, proxyPeer);
			
			
			// If any, register files with the central indexing server, else
			// atleast update the mapper to reflect any changes made in the
			// diretory of the registering client - Auto Update.
			if(proxyCIS.register(clientName, peer.getDirectory()))
				System.out.println("Registration Successfull");
			
			
			// Timing information variables
			double start, end;
			start = System.currentTimeMillis();
			
			// Start sequential experiment
			for(int i=0; i<1000; i++){
				
				// - - FILE LOOKUP - - //
				
				String fileName = args[1] ;
				
				// Get the names of the fileSeeders
				ArrayList<String> fileSeeders = proxyCIS.lookup(fileName) ;
				
				// If file found on server
				if (fileSeeders != null){
					
					// Pick a random seeder
					String randomSeeder = fileSeeders.get(0) ;
					// Get the corresponding random object of the seeder from the reg
					ITFPeer proxySeeder = (ITFPeer) 
							registry.lookup(randomSeeder);
					// Initiate the transfer.
					ArrayList<String> textLines = proxySeeder.retrieve(fileName) ;
					writeTextLinesToDisk(textLines, fileName);
				}
				else{
					System.out.println("File Not Found On Network");
				}
			}
			
			end = System.currentTimeMillis() - start ;
			System.out.println("Average Time For 1000 seq accesses," + args[1] + " file : " + end/1000 + " ms");
			// End Experiment
		}
		catch (Exception e) 
		{
	        System.err.println("Client Exception: " + e.toString());
	        e.printStackTrace();
	    }
	}
	
	/*
	 * PUBLIC METHODS
	 */
	
	// WRITES THE FILE TO THE LOCAL DIRECTORY
	public static void writeTextLinesToDisk(ArrayList<String> textLines, String fileName) {
		
		try {
			PrintWriter wr = new PrintWriter(new FileWriter(fileName));
			for(int indexPointer = 0; indexPointer<textLines.size() ; indexPointer++){
				wr.println(textLines.get(indexPointer));
			}
			wr.close();
		} catch (Exception e) {
			System.err.println("File Write Exception: " + e.toString());
	        e.printStackTrace();
		}
	}
	
	
	/*
	 * PRIVATE METHODS
	 */
	
	// INITIALIZES THE DIRECTORY AT APPLICATION RUNTIME
	private void initializeDirectory(){
		
		// Get path to the current application directory.
		String pathToDirectory = getCurrentAbsolutePath() ;
		
		// Get the list of files in the directory. 
		File folder = new File(pathToDirectory);
		File[] listOfFiles = folder.listFiles();
		
		// Add titles of each text file in the directory.
		directory = new ArrayList<String>() ;
		for (File file : listOfFiles) {
		    if (file.isFile() && file.getName().endsWith(".txt")) {
		        directory.add(file.getName());
		    }
		}
	}
	
	// GETS THE ABSOLUTE FILEPATH TO CURRENT DIRECTORY.
	private String getCurrentAbsolutePath(){
		Path currentRelativePath = Paths.get("");
		String path = currentRelativePath.toAbsolutePath().toString();
		return path ;
	}
	
	// GETS A BUFFEREDREADER OBJECT READY TO READ THE FILE
	private BufferedReader openFile(String fileName){
		
		BufferedReader reader = null ; 		
		try
		{
			reader = new BufferedReader(new FileReader(fileName));
		}
		catch(IOException ex) 
		{
			System.out.println("Nice Try Punk. No Such File Exists");
		}
		
		return reader;
	}
	
	// READS THE FILE LINE BY LINE INTO AN ARRAY OF STRINGS AND RETURNS THE ARRAY
	private ArrayList<String> readFileLines(BufferedReader reader){
		
		ArrayList<String> fileTextLines = new ArrayList<String>() ;
		try
		{
			while(true)
			{	
				String line = reader.readLine();	
				if(line == null) 
					break;
				else
					fileTextLines.add(line);
			}
			reader.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return fileTextLines;
	}
}
