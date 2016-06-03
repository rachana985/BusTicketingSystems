package databaseDAO;

import java.sql.*;

public class daoUtil {

	static final int SQL_TIMEOUT=10;

	public void setSQLTimeOut(PreparedStatement pstmt,int intTimeOutTime) throws SQLException {
		pstmt.setQueryTimeout(intTimeOutTime);
	}

	public static PreparedStatement prepareStatement( Connection conn, String strQuery) throws SQLException {
		PreparedStatement pstmt=conn.prepareStatement( strQuery );
		pstmt.setQueryTimeout(SQL_TIMEOUT);
		return pstmt;
	}
	public static Connection getConnection() throws SQLException //method to connect the database with the server
	{
		try{	//try the connection
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/BusTicketDB?user=root&password=mysql");
			con.setAutoCommit(false);
			return con;
		}catch (Exception ex){ //Could not connect exception
			System.out.println(ex.getMessage() );
		} //END of try

		return null;
	}

	static void closeConnection(Connection con, PreparedStatement stmt, ResultSet rset) {
		if (null != rset) {
			try {
				rset.close();
			} catch (SQLException e) {
				//logger.debug("Error in close connection:"+e);
				System.out.println("Error in close connection "+e);
				//logger.info("exception while closing ResultSet", e);
			}
		}
		if (null != stmt) {
			try {
				stmt.close();
			} catch (SQLException e) {
				//logger.debug("Error in close connection:"+e);
				System.out.println("Error in close connection "+e);
				//logger.info("exception while closing Statement", e);
			}
		}
		if (null != con) {
			try {
				con.close();
			} catch (SQLException e) {
				//logger.debug("Error in close connection:"+e);
				//System.out.println("Error in close connection "+e);
				//logger.info("exception while closing Connection", e);
			}
		}
		//logger.info("connection closed");
	}

}
