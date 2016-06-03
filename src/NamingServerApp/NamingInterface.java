package NamingServerApp;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.net.MalformedURLException;

public interface NamingInterface extends Remote {
	
	public String[] getServers(String prefix) throws RemoteException, MalformedURLException;

}