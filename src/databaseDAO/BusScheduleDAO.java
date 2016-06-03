package databaseDAO;


import java.sql.*;
import java.util.*;

import BusTicketServerApp.BusSchedule;

import java.io.*;

public class BusScheduleDAO {

	Statement stmt = null;
	ResultSet rs = null;
	PreparedStatement pstmt = null;

	public Vector getSelectData(Connection conn,String strOrigin, String strDest,String strDate, int intQty)//method to search the schedule in the database
	 {
		Vector vBusScheduleData = new Vector();

		StringBuffer sf = new StringBuffer();
		sf.append("");
		sf.append("SELECT A.SCHEDULE_ID,         \n");
		sf.append("       A.BUS_ID,              \n");
		sf.append("       B.ORIGIN,              \n");
		sf.append("       B.DEST,                \n");
		sf.append("       DATE_FORMAT(A.START_TIME,'%M %e, %Y %l:%i %p') START_TIME,\n");
		sf.append("       DATE_FORMAT(A.END_TIME,'%M %e, %Y %l:%i %p') END_TIME, \n");
		sf.append("       TIMEDIFF(END_TIME,START_TIME) DURATION,  \n");
		sf.append("       B.FARE,                \n");
		sf.append("       A.REMAIN_SEAT          \n");
		sf.append("FROM TB_BUS_SCHEDULE A,       \n");
		sf.append("     TB_ROUTE_INFO B          \n");
		sf.append("WHERE A.ROUTE_ID = B.ROUTE_ID \n");
		sf.append("AND B.ORIGIN= ? \n");
		sf.append("AND B.DEST= ? \n");
		sf.append("AND ? = SUBSTR(A.START_TIME,1,10) \n");
		try {
			pstmt=daoUtil.prepareStatement( conn, sf.toString());
			pstmt.setString(1,strOrigin);
			pstmt.setString(2,strDest);
			pstmt.setString(3,strDate);
			rs = pstmt.executeQuery();
			while (rs.next() ) {
				System.out.print( rs.getString(5));
				System.out.print( rs.getString(6));
				System.out.print( rs.getString(7));
				vBusScheduleData.addElement(new BusSchedule( rs.getInt(1),
						rs.getInt(2),
						rs.getString(3),
						rs.getString(4),
						rs.getString(5),
						rs.getString(6),
						rs.getString(7),
						rs.getDouble(8),
						rs.getInt(9)
						)
						);
			}
		} catch (SQLException e) {
			System.out.println("sql Error in retrieve Bus Schedule "+e);
		} catch (Exception e) {
			System.out.println("other Error in retrieve Bus Schedule "+e);

		}
		return vBusScheduleData;

	}

	public Vector RetrieveSchedule(String strOrigin, String strDest,String strDate, int intQty, String strClientId, long lOperationId) //method to retrive schedule
	 {
		System.out.println("RetrieveSchedule Method");
		Vector vBusScheduleData= new Vector();
		Connection conn=null;
		boolean isTranactionExecuted=false;

		try {

			conn= daoUtil.getConnection();

			if (conn != null) {
				stmt = conn.createStatement();
			} else {
				System.out.println("connection is invalie");
			}

			// insert clientId and operationId into Transaction table
			insertTransaction(strClientId,lOperationId,conn);
			System.out.println("insert transaction ");
			isTranactionExecuted=true;

			// retrieve bus schedule data //
			vBusScheduleData=getSelectData(conn,strOrigin, strDest, strDate, intQty);


			// update retrieved data
			updateTransaction(strClientId,lOperationId,conn,vBusScheduleData);

			conn.commit();
		}  catch (Exception ex) {
			/* it must be duplication in Transaction */
			try {
				if (isTranactionExecuted == false) {
					vBusScheduleData=getJavaObject(conn,strClientId,lOperationId);
				} else {
					System.out.println("ERROR IN Retrieve Schedule= " + ex);
				}

			} catch (Exception e) {
				System.out.println("error in wait thread" + e);
			}

		} finally {
			daoUtil.closeConnection(conn, pstmt, rs);
		}
		return vBusScheduleData;

	}

	public void insertTransaction (String strClientId, long lOperationId, Connection conn) throws Exception //method to store transactions
	{
		StringBuffer f = new StringBuffer();
		f.append("");
		f.append(" INSERT INTO TB_TRANSACTION  \n");
		f.append("( CLIENT_ID,OPERATION_ID) VALUES \n");
		f.append("( ?,? ) \n");

		pstmt=daoUtil.prepareStatement( conn, f.toString());
		pstmt.setString(1,strClientId);
		pstmt.setLong(2,lOperationId);

		pstmt.executeUpdate();

	}

	public void updateTransaction (String strClientId, long lOperationId, Connection conn, Vector vBusScheduleData) throws SQLException, IOException //method to update transactions
	{
		StringBuffer f = new StringBuffer();
		f.append("");
		f.append(" UPDATE TB_TRANSACTION  \n");
		f.append(" SET JAVA_OBJECT=? \n");
		f.append(" WHERE CLIENT_ID=?  \n");
		f.append(" AND OPERATION_ID=?  \n");

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(outStream);
		out.writeObject(vBusScheduleData);
		ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());

		pstmt=daoUtil.prepareStatement( conn, f.toString());

		pstmt.setBinaryStream(1,inStream,inStream.available());

		pstmt.setString(2,strClientId);
		pstmt.setLong(3,lOperationId);

		pstmt.executeUpdate();
	}
	public Vector getJavaObject(Connection conn,String strClientId, long lOperationId) throws Exception  //to ge the transactions from the database table
	{
		StringBuffer aaf = new StringBuffer();
		Vector vJavaObejct= new Vector();

		aaf.append("");
		aaf.append("SELECT JAVA_OBJECT \n");
		aaf.append("FROM TB_TRANSACTION    \n");
		aaf.append(" WHERE CLIENT_ID=?  \n");
		aaf.append(" AND OPERATION_ID=?  \n");
		aaf.append(" AND JAVA_OBJECT IS NOT NULL \n");
		pstmt=daoUtil.prepareStatement( conn, aaf.toString());
		pstmt.setString(1,strClientId);
		pstmt.setLong(2,lOperationId);

		while(true) {
			rs = pstmt.executeQuery();
			if ( rs.next()== false) {
				continue;
			} else {
				ObjectInput in = new ObjectInputStream(rs.getBinaryStream(1));
				vJavaObejct = (Vector)in.readObject();
				break;
			}

		}
		return vJavaObejct;

	}


	public int buyTicket (int intScheduleId, int intQty, long lCardNo, double dTotalFare, String strClientId, long lOperationId) //Method to buy a ticket from available tickets
	{
		int intRetVal=0;

		Connection conn=null;
		boolean isTranactionExecuted= false;
		try {
			conn = daoUtil.getConnection();
			/*insert transaction */
			insertTransaction(strClientId,lOperationId,conn);
			isTranactionExecuted=true;

			/* update Card Info */
			updateCardInfo(conn,  lCardNo, dTotalFare );
			pstmt=null;

			/* update remain seat */
			updateRemainSeat( conn,intScheduleId, intQty );
			pstmt=null;

			/*insertBuyTicket*/
			insertBuyTicket(conn,intScheduleId, lCardNo, intQty, strClientId, lOperationId);
			pstmt=null;
			/* update transaction table for another replica */

			updateTransaction(strClientId,lOperationId,conn,new Vector(intRetVal));
			conn.commit();
			pstmt=null;
			intRetVal = getBuyId(conn, strClientId, lOperationId);

		} catch (Exception ex) {

			/* if it's error not for Transaction insert */
			if ( isTranactionExecuted == true ) {
				System.out.println("ERROR IN BUY TICKET "+ex);
				intRetVal=-999;
			} else {
				/* falut positive: this message may not be shown in client*/
				intRetVal =-100;
			}
			if (conn != null) try { conn.rollback(); } catch(SQLException e) {}

		} finally {
			daoUtil.closeConnection(conn, pstmt, rs);
		}
		return intRetVal;

	}

	public int cancelTicket (int intBuyId,int intScheduleId, long lCardNo,int intQty, double dTotalFare, String strClientId, long lOperationId ) //Method to cancel a ticket
	{
		Connection conn=null;
		int intCacelTicket=0;
		boolean isTranactionExecuted = false;
		try {

			StringBuffer sf = new StringBuffer();
			conn = daoUtil.getConnection();

			/*insert transaction */
			insertTransaction(strClientId,lOperationId,conn);
			isTranactionExecuted=true;


			sf.append("");
			sf.append(" UPDATE TB_BUY_TICKET      \n");
			sf.append(" SET IS_CANCELED='Y' \n");
			sf.append(" WHERE BUY_ID=?");
			pstmt=daoUtil.prepareStatement( conn, sf.toString());
			pstmt.setInt(1,intBuyId);

			pstmt.executeUpdate();
			pstmt=null;

			StringBuffer ssf = new StringBuffer();
			ssf.append("");
			ssf.append(" UPDATE TB_BUS_SCHEDULE      \n");
			ssf.append(" SET REMAIN_SEAT = REMAIN_SEAT-? \n" );
			ssf.append(" WHERE SCHEDULE_ID=? \n");
			pstmt=daoUtil.prepareStatement( conn, ssf.toString());

			pstmt.setInt(1,-intQty);
			pstmt.setInt(2,intScheduleId);
			pstmt.executeUpdate();
			pstmt=null;

			StringBuffer f = new StringBuffer();
			f.append("");
			f.append(" UPDATE TB_CARD_INFO      \n");
			f.append(" SET BALANCE= BALANCE- ? \n");
			f.append(" WHERE CARD_NO=? \n");

			pstmt=daoUtil.prepareStatement( conn, f.toString());
			pstmt.setDouble(1,-dTotalFare);
			pstmt.setLong(2,lCardNo);

			pstmt.executeUpdate();
			conn.commit();
		}catch (Exception ex) {
			if ( isTranactionExecuted== false ) {

				/* This error can be neglected */
				intCacelTicket=-100;
			} else {
				System.out.println("DATABASE ERROR***** "+ex);
				intCacelTicket=-999;
			}
			if (conn != null) try { conn.rollback(); } catch(SQLException e) {}

		} finally {
			daoUtil.closeConnection(conn, pstmt, rs);
		}

		return intCacelTicket;
	}

	public int isCardValid (long lCardNo, double dTotalFare ) //To check the card provided is valid or not
	{
		Connection conn=null;
		int intCardInfo=0;
		try {

			conn = daoUtil.getConnection();
			if (conn != null) {
				stmt = conn.createStatement();
			} else {
				System.out.println("isCard Valid connection is invalie");
			}
			StringBuffer sf = new StringBuffer();
			sf.append("");
			sf.append("SELECT BALANCE- ?        \n");
			sf.append("FROM TB_CARD_INFO        \n");
			sf.append("WHERE CARD_NO =? \n");

			pstmt=daoUtil.prepareStatement( conn, sf.toString());
			pstmt.setDouble(1,dTotalFare);
			pstmt.setLong(2,lCardNo);

			rs = pstmt.executeQuery();

			if( rs.next()==false ) {
				System.out.println("Invalid CardNo="+lCardNo);
				intCardInfo =-3;
			} else {
				if ( rs.getDouble(1) >= 0 ) {
					intCardInfo =0;
				} else {
					System.out.println("Not Enough Balance");
					intCardInfo =-2;
				}
			}

		} catch (Exception ex) {
			System.out.println("Exception:"+ex);
			intCardInfo=-999;

		} finally {
			daoUtil.closeConnection(conn, pstmt, rs);
		}
		return intCardInfo;
	}

	public boolean isRemainSeat (int intScheduleId, int intQty ) //To check whether there are any seats left
	{
		Connection conn=null;
		boolean isDataExists=true;
		try {

			conn = daoUtil.getConnection();
			if (conn != null) {
				stmt = conn.createStatement();
			} else {
				System.out.println("isRemainSead is invalie");
			}
			StringBuffer sf = new StringBuffer();
			sf.append("");
			sf.append("SELECT 1         \n");
			sf.append("FROM TB_BUS_SCHEDULE        \n");
			sf.append("WHERE SCHEDULE_ID =?  \n");
			sf.append("AND REMAIN_SEAT >=?   \n");

			pstmt=daoUtil.prepareStatement( conn, sf.toString());
			pstmt.setInt(1,intScheduleId);
			pstmt.setInt(2,intQty);
			rs = pstmt.executeQuery();

			if( rs.next()) {
				isDataExists=true;
			} else {
				isDataExists=false;
			}

		} catch (Exception ex) {
			System.out.println("Exception:"+ex);
		}  finally {
			daoUtil.closeConnection(conn, pstmt, rs);
		}
		return isDataExists;
	}


	public void updateCardInfo(Connection conn, long lCardNo, double dTotalFare ) throws Exception  //user can update card info with this method
	{

		StringBuffer sf = new StringBuffer();

		/* update card info */
		sf.append(" UPDATE TB_CARD_INFO      \n");
		sf.append(" SET BALANCE= BALANCE- ? \n");
		sf.append(" WHERE CARD_NO=? \n");
		pstmt=daoUtil.prepareStatement( conn, sf.toString());
		pstmt.setDouble(1,dTotalFare);
		pstmt.setLong(2,lCardNo);
		pstmt.executeUpdate();
	}

	public void updateRemainSeat( Connection conn,int intScheduleId, int intQty )throws Exception {
		/*updateRemainSeat*/
		StringBuffer sff = new StringBuffer();
		sff.append("");
		sff.append(" UPDATE TB_BUS_SCHEDULE      \n");
		sff.append(" SET REMAIN_SEAT = REMAIN_SEAT-? \n" );
		sff.append(" WHERE SCHEDULE_ID=? \n");

		pstmt=daoUtil.prepareStatement( conn, sff.toString());
		pstmt.setInt(1,intQty);
		pstmt.setInt(2,intScheduleId);
		pstmt.executeUpdate();
		pstmt=null;

	}
	public void insertBuyTicket(Connection conn,int intScheduleId, long lCardNo, int intQty, String strClientId, long lOperationId) throws Exception //insert a record when users buys a ticket
	{
		StringBuffer f = new StringBuffer();
		f.append("");
		f.append(" INSERT INTO TB_BUY_TICKET  \n");
		f.append("( SCHEDULE_ID,CARD_NO,QTY,UNIT_COST,TOTAL_COST, IS_CANCELED, CLIENT_ID,OPERATION_ID) \n");
		f.append("( SELECT A.SCHEDULE_ID, \n");
		f.append("         ?, \n");
		f.append("         ?, \n");
		f.append("         B.FARE, \n");
		f.append("         B.FARE*?, \n" );
		f.append("         'N',  \n");
		f.append("         ?,  \n");
		f.append("         ?  \n");
		f.append(" FROM TB_BUS_SCHEDULE A, \n");
		f.append("      TB_ROUTE_INFO B \n");
		f.append(" WHERE A.ROUTE_ID = B.ROUTE_ID \n");
		f.append(" AND SCHEDULE_ID=? ) \n");
		pstmt=daoUtil.prepareStatement( conn, f.toString());
		pstmt.setLong(1,lCardNo);
		pstmt.setInt(2,intQty);
		pstmt.setInt(3,intQty);
		pstmt.setString(4,strClientId);
		pstmt.setLong(5,lOperationId);
		pstmt.setInt(6,intScheduleId);
		pstmt.executeUpdate();

	}

	public boolean isValidBuyId (int intBuyId ) //To check wheteher the buy id is valid or not
	{
		Connection conn=null;
		boolean isDataExists=true;
		try {

			conn = daoUtil.getConnection();
			if (conn != null) {
				stmt = conn.createStatement();
			} else {
				System.out.println("isValidBuyId connection is invalie");
			}
			StringBuffer sf = new StringBuffer();
			sf.append("");
			sf.append("SELECT 1         \n");
			sf.append("FROM TB_BUY_TICKET        \n");
			sf.append("WHERE BUY_ID =? \n");
			pstmt=daoUtil.prepareStatement( conn, sf.toString());
			pstmt.setInt(1,intBuyId);
			rs=pstmt.executeQuery();

			if( rs.next()) {
				isDataExists = true;
			} else {
				System.out.println("not exists buy_id["+intBuyId+"]");
				isDataExists=false;
			}
		} catch (Exception ex) {
			System.out.println("Exception:"+ex);
		}  finally {
			daoUtil.closeConnection(conn, pstmt, rs);
		}
		return isDataExists;
	}

	public Vector getCancel(int intBuyId) //To cancel the ticket
	{
		Connection conn=null;
		Vector vCancelData= new Vector();
		try {

			conn = daoUtil.getConnection();
			if (conn != null) {
				stmt = conn.createStatement();
			} else {
				System.out.println("getCancel connection is invalie");
			}
			StringBuffer sf = new StringBuffer();
			sf.append("");
			sf.append("SELECT A.SCHEDULE_ID,       \n");
			sf.append("       A.CARD_NO,           \n");
			sf.append("       A.QTY,               \n");
			sf.append("       A.TOTAL_COST,        \n");
			sf.append("       DATE_FORMAT(B.START_TIME,'%c%d%H%i'), \n");
			sf.append("       DATE_FORMAT(CURDATE(),'%c%d%H%i')     \n");
			sf.append("FROM TB_BUY_TICKET   A,     \n");
			sf.append("     TB_BUS_SCHEDULE B      \n");
			sf.append("WHERE A.BUY_ID =                ?  \n");
			sf.append("AND   A.SCHEDULE_ID = B.SCHEDULE_ID ");
			pstmt=daoUtil.prepareStatement( conn, sf.toString());
			pstmt.setInt(1,intBuyId);

			rs=pstmt.executeQuery();

			while (rs.next() ) {
				vCancelData.add(new Integer(rs.getInt(1)));
				vCancelData.add(new Long( rs.getLong(2)));
				vCancelData.add(new Integer( rs.getInt(3)));
				vCancelData.add(new Double( rs.getDouble(4)));
				vCancelData.add(new Long( rs.getString(5)));
				vCancelData.add(new Long( rs.getString(6)));
			}
		} catch (Exception ex) {
			System.out.println("Exception:"+ex);
		}  finally {
			daoUtil.closeConnection(conn, pstmt, rs);
		}
		return vCancelData;

	}

	public int updateBuyTicket(int intBuyId ) //to update a ticket
	{
		Connection conn=null;
		int intResult=0;
		try {

			conn = daoUtil.getConnection();
			if (conn != null) {
				stmt = conn.createStatement();
			} else {
				System.out.println("updateBuyTicket connection is invalie");
			}
			StringBuffer sf = new StringBuffer();
			sf.append("");
			sf.append(" UPDATE TB_BUY_TICKET      \n");
			sf.append(" SET IS_CANCELED='Y' \n");
			sf.append(" WHERE BUY_ID=?");
			pstmt=daoUtil.prepareStatement( conn, sf.toString());
			pstmt.setInt(1,intBuyId);

			intResult=pstmt.executeUpdate();
			conn.commit();

		} catch (Exception ex) {
			System.out.println("Exception:"+ex);
		}  finally {
			daoUtil.closeConnection(conn, pstmt, rs);
		}
		return intResult;
	}

	public int getBuyId ( Connection conn, String strClientId, long lOperationId) throws Exception  //Method to get buy id
	{

		StringBuffer aaf = new StringBuffer();
		int intRetVal=0;
		aaf.append("");
		aaf.append("SELECT BUY_ID \n");
		aaf.append("FROM TB_BUY_TICKET    \n");
		aaf.append("WHERE CLIENT_ID=?  \n");
		aaf.append("AND   OPERATION_ID=?  \n");
		pstmt=daoUtil.prepareStatement( conn, aaf.toString());
		pstmt.setString(1,strClientId);
		pstmt.setLong(2,lOperationId);

		rs = pstmt.executeQuery();

		while (rs.next() ) {
			intRetVal =rs.getInt(1);
		}
		return 	intRetVal;
	}
}
