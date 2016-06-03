package NamingServerApp;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.net.MalformedURLException;
import java.rmi.Naming;

public class NamingServer extends UnicastRemoteObject implements NamingInterface {

	public NamingServer() throws RemoteException
	{

	} // Constructor

	@Override
	public String[] getServers(String prefix) throws RemoteException, MalformedURLException {
		String[] r = Naming.list(prefix);
		return r;
	} // return running servers

}
