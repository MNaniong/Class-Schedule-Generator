import java.sql.Date;
import java.util.Arrays;


public class Clas implements Comparable<Clas>{
	String  component, semester;
	boolean [] days;
	int from_time, to_time,course_id, start_year;
	Date to_date,from_date;
	@SuppressWarnings("deprecation")
	public Clas( Date to_date, Date from_date, String days, int from_time, int to_time, String component, int course_id){
		
		this.component=component.trim();
		
		this.to_time= to_time;
		this.course_id=course_id;
		this.to_date=to_date;
		this.from_date=from_date;
		this.to_time=to_time;
		this.from_time=from_time;
		
		start_year=from_date.getYear();
		
		getDays(days);
		this.semester=getSemester();
	}
	private void getDays(String d) {
		days=new boolean[7];
		Arrays.fill(days, false);
		if(d==null){ 
			days[0]=true; 
			return;
		}
		d=d.trim();
		if(d.length()==0){
			days[0]=true; 
			return;
		}
	
		if(d.contains("M")) days[1]=true;
		if(d.contains("T")) days[2]=true;
		if(d.contains("W")) days[3]=true;
		if(d.contains("H")) days[4]=true;
		if(d.contains("F")) days[5]=true;
		if(d.contains("S")) days[6]=true;
		
		return;
	}
	public boolean conflicts(Clas c){
		if(c.days.equals(this.days))
			return time_conflicts(c);
		else 
			return false;
		
	}
	
	public boolean time_conflicts(Clas c){
		if(this.from_time>=c.from_time && this.from_time<=c.to_time){
			return false;
		}
		else if(this.to_time>=c.from_time && this.to_time<=c.to_time){
			return false;
		}
		else if(this.from_time<=c.from_time && this.from_time>=c.to_time){
			return false;
		}
		return true;
	}
	public int compareTo(Clas o) {
		int com=from_date.compareTo(o.from_date);
		if(com!=0)
			return com;
		else if(this.from_time>o.from_time)
			return 1;
		else if(this.from_time<o.from_time)
			return -1;
		else
			return 0;
	}
	@SuppressWarnings("deprecation")
	private String getSemester() {
		int start_month=from_date.getMonth();
		int end_month=to_date.getMonth();
		if(start_month==8){
			return "Fall";
		}
		else if(start_month==1){
			return "Spring";
		}
		
		//Summer
		//A Begins May    Ends June
		//B Begins June   Ends Aug
		//C Begins May    Ends Aug
		//D Begins May    Ends July
		
		else if(start_month==5){//A or C or D
			if(end_month==6)
				return "Summer A";
			else if(end_month==8)
				return "Summer C";
			else if(end_month==7)
				return "Summer D";
			else
				return null;
		}
		else if(start_month==6){//B
			return "Summer B";
		}
		else
			return null;
	}
	public String getDays() {
		String ret="";
		
		if(days[1]) ret=ret+"M";
		if(days[2]) ret=ret+"T";
		if(days[3]) ret=ret+"W";
		if(days[4]) ret=ret+"H";
		if(days[5]) ret=ret+"F";
		if(days[6]) ret=ret+"S";
		if(days[0]) ret=ret+"Web";
		return ret;
	}
}
