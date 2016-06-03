package BusTicketServerApp;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;

import databaseDAO.BusScheduleDAO;
import databaseDAO.daoUtil;
//BusticketServer main class
public class BusTicketServer extends UnicastRemoteObject implements BusTicketInterface {

	private String strNum;

	public BusTicketServer() throws RemoteException{

	}

	public static Connection con;

	static {
		try{
			con = daoUtil.getConnection();
		}
		catch(Exception ex){}
	}

	public ArrayList<BusSchedule> RetrieveSchedule (String Origin, String Dest, String Date, int Qty, String C_ID, long OP_ID)//method to retirieve available schedule
  {
		BusScheduleDAO bsDAO = new BusScheduleDAO();
		System.out.print("server Method");
		ArrayList<BusSchedule> arrBusSchedule = new ArrayList<BusSchedule>();
		Vector vSchedule = bsDAO.RetrieveSchedule(Origin, Dest, Date, Qty, C_ID, OP_ID);
		arrBusSchedule.addAll(vSchedule);
		return(arrBusSchedule);

	}
	public int buyTicket (int ScheduleId, int Qty, long CardNo, double TotalFare, String C_ID, long OP_ID)//Method to buy a ticket
  {
		// TODO Auto-generated method stub
		int intReturnValue = 0;
		BusScheduleDAO bsDAO = new BusScheduleDAO();

			int intCardValid=bsDAO.isCardValid(CardNo, TotalFare);
			if (intCardValid == 0){
				if(bsDAO.isRemainSeat(ScheduleId, Qty) == true){
					intReturnValue=bsDAO.buyTicket (ScheduleId,Qty,CardNo,TotalFare,C_ID, OP_ID);
				}
				else{
					System.out.println("nO REMAIN SEAT");
					intReturnValue = -1;
				}
			}
			else if(intCardValid == -2){
				System.out.println("intCardValid is -2 ==> the balance is not sufficient.");
				intReturnValue = -2; //return insufficient balance.
			}
			else if(intCardValid == -3){
				intReturnValue = -3; //return incorrect card number.
			}
			else {
				System.out.println("DBDBDBB");
			}
		return intReturnValue;
	}

	public int cancelTicket (int BuyId, String C_ID, long OP_ID)//Method to cancel ticket
	{
		int intReturnValue = 0;

		BusScheduleDAO bsDAO = new BusScheduleDAO();//Bus schedule DAO creation

		int scheduleId;
		long lCardNo;
		int Qty;
		double dTotalCost;
		long lCarTime;
		long lPresentTime;


		if ( bsDAO.isValidBuyId(BuyId)== true) {
			Vector vCancelTicket = bsDAO.getCancel(BuyId);
			scheduleId = Integer.parseInt(vCancelTicket.elementAt(0).toString());
			lCardNo = Long.parseLong(vCancelTicket.elementAt(1).toString());
			Qty = Integer.parseInt(vCancelTicket.elementAt(2).toString());
			dTotalCost = Double.parseDouble(vCancelTicket.elementAt(3).toString());
			lCarTime = Long.parseLong(vCancelTicket.elementAt(4).toString());
			lPresentTime = Long.parseLong(vCancelTicket.elementAt(5).toString());
			if(lCarTime > lPresentTime){
				intReturnValue =bsDAO.cancelTicket (BuyId,scheduleId,lCardNo, Qty, dTotalCost, C_ID, OP_ID);
			}
			else{
				intReturnValue = -1;
			}

		} else {
			intReturnValue=-2;
		}
		return intReturnValue;
	}

	public boolean IsAlive () // To check the server is alive or not
	{
		return true;
	}
}
