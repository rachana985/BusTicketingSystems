package FaultTolerantApp;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import BusTicketServerApp.BusTicketInterface;
import BusTicketServerApp.BusTicketServer;
import NamingServerApp.NamingServer;

public class FaultTolerantServer implements Runnable {

	private String serverPreFix = "";
	private BusTicketInterface[] servers;
	private String[] serversNames;


	public FaultTolerantServer(String pre) {
		NamingServer nameingServer;
		try {
			serverPreFix = pre;
			nameingServer = new NamingServer();
			serversNames =  nameingServer.getServers(serverPreFix);
			servers = new BusTicketInterface[serversNames.length];

			for(int i=0;i<serversNames.length;i++)
			{
				servers[i] = (BusTicketInterface)Naming.lookup(serversNames[i]);
			}

		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		while(true)
		{
			for(int i=0;i<servers.length;i++)
			{
				try{
					// Check if the server is alive
					if(servers[i].IsAlive())
						System.out.println("Server "+ serversNames[i] + " is alive");
				}
				catch(Exception ex)
				{
					try {
						// trying to rebind the dead server
						Naming.rebind(serversNames[i], new BusTicketServer());
						System.out.println("rebinding server " + serversNames[i]);
					} catch (RemoteException | MalformedURLException e) {
						// error in rebing dead server
						System.out.println("Error While rebinding server");
					}
				}
			}
			try {
				// wait for 10 seconds
				Thread.sleep(10000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
