import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Main {
	public static void main(String[] args){
		String sql="Select PK, To_Date, From_Date, Cancel_Date,  To_Date_Old, From_Date_Old, Cancel_Date_Old from School. Classes WHERE PK>13092";
		Connection con=getConnect();
		if(con==null)
			return;
		try {
			Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			
			ResultSet res=stmt.executeQuery(sql);
			
			int count=0;
	
			while(res.next()){
				
				//System.out.println(".."+count);
				if(count%250==0)System.out.println("..."+count);
				count++;
				
				String can=res.getString("Cancel_Date_Old");
				String to_d=res.getString("To_Date_Old");
				String from_d=res.getString("From_Date_Old");
	
				Date d1=proper_format(can);
				Date d2=proper_format(to_d);
				Date d3=proper_format(from_d);
				
				if(d1!=null)
					res.updateDate("Cancel_Date", d1);
				if(d2!=null)
					res.updateDate("To_Date", d2);
				if(d3!=null)
					res.updateDate("From_Date", d3);
				
				res.updateRow();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		
	}
	public static Date proper_format(String s){
		
		if(s==null || s.trim().length()==0)
			return null;
		
		String year =s.substring(s.lastIndexOf("/")+1);
		String day =s.substring(s.indexOf("/")+1,s.lastIndexOf("/"));
		String mon  =s.substring(0,s.indexOf("/"));
		 if(mon.length()==1)
			 mon="0"+mon;
		 if(day.length()==1)
			 day="0"+day;
		 
		
		return Date.valueOf(year+"-"+mon+"-"+day);
		
	}
	private static Connection getConnect() {
		String url = "jdbc:mysql://boba.dyndns-server.com:3306/";
	    String dbName = "School";
	    String driver = "com.mysql.jdbc.Driver";
	    String userName = "feldman"; 
	    String password = "WOW!!!";
	
	    try {
	        Class.forName(driver).newInstance();
	        Connection con = DriverManager.getConnection(url+dbName,userName,password);
	        System.out.println("Connected to the database");
	        return con;
	      } catch (Exception e) {
	        e.printStackTrace();
	      }
	      
		return null;

	
	}
}