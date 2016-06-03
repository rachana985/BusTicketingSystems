package BusTicketServerApp;

import java.io.Serializable;

public class BusSchedule implements Serializable {
	public int lScheduleId = (int)0;
	public int lBusNum = (int)0;
	public String strOrigin = null;
	public String strDest = null;
	public String strStartTime = null;
	public String strEndTime = null;
	public String strDuration = null;
	public double dFare = (double)0;
	public int lRemainSeat = (int)0;
	
	 public BusSchedule ()
	  {
	  }

	  public BusSchedule (int _lScheduleId, int _lBusNum, String _strOrigin, String _strDest, String _strStartTime, String _strEndTime, String _strDuration, double _dFare, int _lRemainSeat)
	  {
	    lScheduleId = _lScheduleId;
	    lBusNum = _lBusNum;
	    strOrigin = _strOrigin;
	    strDest = _strDest;
	    strStartTime = _strStartTime;
	    strEndTime = _strEndTime;
	    strDuration = _strDuration;
	    dFare = _dFare;
	    lRemainSeat = _lRemainSeat;
	  } 
}
