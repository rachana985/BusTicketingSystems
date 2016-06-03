package BusTicketServerApp;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

//Inter faces of bus ticketing server
public interface BusTicketInterface extends Remote {

    public ArrayList<BusSchedule> RetrieveSchedule (String strOrigin, String strDest, String strDate, int lQty, String C_ID, long OP_ID) throws RemoteException;
    public int buyTicket (int lScheduleId, int lQty, long lCardNo, double dTotalFare, String C_ID, long OP_ID) throws RemoteException;
    public int cancelTicket (int lBuyId, String C_ID, long OP_ID) throws RemoteException;
	public boolean IsAlive () throws RemoteException;

}
