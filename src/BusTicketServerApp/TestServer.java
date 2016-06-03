package BusTicketServerApp;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import FaultTolerantApp.FaultTolerantServer;
import NamingServerApp.NamingServer;

public class TestServer  {

	public static Registry registry ;
	public static void main(String[] args) //Main method to register a server
	{

		try {
			System.setSecurityManager (new RMISecurityManager() {
				public void checkConnect (String host, int port) {}
				public void checkConnect (String host, int port, Object context) {}
			});

			if(args.length == 3)
			{
				registry= LocateRegistry.createRegistry(Integer.parseInt(args[1]));

				Naming.rebind("rmi://"+args[0]+":" + args[1] +"/BusTicketInterface1", new BusTicketServer());
				System.out.println("Server is connected and ready for operation");

				Naming.rebind("rmi://"+args[0]+":" + args[1] +"/BusTicketInterface2", new BusTicketServer());
				System.out.println("Server is connected and ready for operation");

				Naming.rebind("rmi://"+args[0]+":" + args[1] +"/BusTicketInterface3", new BusTicketServer());
				System.out.println("Server is connected and ready for operation");

				registry= LocateRegistry.createRegistry(Integer.parseInt(args[2]));
				Naming.rebind("rmi://"+args[0]+":" + args[2] +"/NamingInterface", new NamingServer());
				System.out.println("Server is connected and ready for operation");

				FaultTolerantServer ftserver = new FaultTolerantServer("rmi://"+args[0]+":" + args[1] +"/");
				Thread th = new Thread(ftserver);
				th.start();
				System.out.println("Fault Tolerant Server is started");
			}
			else
			{
				System.out.println("Arguemts error");
			}
		}catch(Exception e){
			System.out.println("Error: "+ e);
		}
	}
}
