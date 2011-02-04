import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;


public class Course implements Comparable<Course>{
	public String title, subject, catalog_number;
	boolean lab_required=false;
	Vector<Clas> classes;
	Vector<Clas> labs;
	public Course(String title, String subject, String catalog_number){
		this.title=title;
		this.subject=subject;
		this.catalog_number=catalog_number;	
	}
	public Vector<Clas> getClases(){
		return classes;
	}
	public Vector<Clas> getLabs(){
		return labs;
	}
	public void fillClass(Connection con){
		classes=new Vector<Clas>();
		labs=new Vector<Clas>();
		String sql="Select CLASS_NBR, From_Date, To_Date, Cancel_Date, MTWRFSD_Days, " +
		"From_Time, To_Time, Component FROM School.Classes WHERE " +
		"(SUBJECT='"+subject+"' AND CATALOG_NBR='"+catalog_number+"') ORDER BY From_Date, To_Date, Component, MTWRFSD_Days, From_Time,To_Time";
		
	//	System.out.println("Filling Course: "+subject+":"+catalog_number);
		try {
			Statement stmt= con.createStatement();
			ResultSet res=stmt.executeQuery(sql);
			
			while(res.next()){
				Date to_date=res.getDate("To_Date");
				Date from_date=res.getDate("From_Date");
				String days=res.getString("MTWRFSD_Days");
				String component=res.getString("Component");
				int from_time=res.getInt("From_Time");
				int to_time=res.getInt("To_Time");
				int course_id=res.getInt("CLASS_NBR");
				
				Clas c=new Clas(to_date, from_date, days, from_time, to_time, component, course_id);
				
				if(component.equalsIgnoreCase("LEC"))
					classes.add(c);
				else
					labs.add(c);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return;
	}
	
	public int compareTo(Course other) {
		int a=subject.compareTo(other.subject);
		if(a==0)
			return this.catalog_number.compareTo(other.catalog_number);
		else
			return a;
	}
	
	
	
}
