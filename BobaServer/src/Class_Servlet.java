import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
 
public class Class_Servlet extends HttpServlet{ private static final long serialVersionUID = 1L;
	final int UNDEFINED_TYPE=1;
	final int COURSE_NOT_FOUND=2;
	final int NO_COURSES_TO_VALIDATE=3;
	final int XML_FORMAT_ERROR=4;
	
	Connection con;
	public void intit(){	
		con=getConnect();
	}
	public void doGet(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException {
		ServletOutputStream out = res.getOutputStream();
	    res.setContentType("text/html"); 

	    String file = "/index.html";
	    if (file == null) {
	      out.println("Extra path info was null; should be a resource to view");
	      return;
	    }

	    URL url = getServletContext().getResource(file);
	    if (url == null) {
	      out.println("Resource " + file + " not found");
	      return;
	    }
	    Scanner s=new Scanner(url.openStream());
	    while(s.hasNext()){
	    	out.println(s.nextLine());
	    }
	    
	    
	  
	}
	public void doPost(HttpServletRequest req, HttpServletResponse resp){
		System.out.println("DoPost:::>\n");
		String res="";
		Document doc=null;
		ServletOutputStream outs=null;
		
		resp.setContentType("text/html");
		try{
			//Gets the OutputStream
	        outs = resp.getOutputStream();
	        res=req.getParameter("xml");
	        if(res==null) throw new NullPointerException();
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();      
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			
	        doc = docBuilder.
	   
	        
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
			error_document(XML_FORMAT_ERROR,e.getMessage(),outs);
			return;
		} catch (SAXException e) {
			e.printStackTrace();
			error_document(XML_FORMAT_ERROR,e.getMessage(),outs);
			return;
		} catch (IOException e) {
			e.printStackTrace();
			error_document(XML_FORMAT_ERROR,e.getMessage(),outs);
			return;
		}
		catch (Exception e) {
			e.printStackTrace();
			error_document(XML_FORMAT_ERROR,e.getMessage(),outs);
			return;
		}
		String type=doc.getElementById("Request_Type").getNodeValue();
		
		if(type.equals("DegreeCourseList")){
		
		}
		else if(type.equals("Validation")){
			res=(validate_course_list(doc));
		}
		else if(type.equals("Schedule")){
			
		}
		else{
			error_document(UNDEFINED_TYPE,"The type: "+type+" is unrecongiable by this server.",outs);
			return;
		}
		
		return_html(res,outs);
		return;
		
		
	}
	
	/*
	 <Response>
		<Response_Type> “Validation”</Response_Type>
		<Course>
			<Subject_CourseID>”Subject:ClassNumber”</Subject_CourseID>
			<Class>
				<Course_ID >”999999”</Course_ID>
				<Title>”Course Title”</Title>	
				<Time>
					<Semester>”Fall”</Semester>
					<Year>”Year”</Year>
					<Days>”MTWHFS”</Days>
					<Time_Start>”10:15”</Time_Start>
					<Time_End>”11:30”</Time_End>
				</Time>
			</Class>
			<Class>
				<Course_ID >”999999”</Course_ID>
				<Title>”Course Title”</Title>	
				<Time>
					<Semester>”Fall”</Semester>
					<Year>”Year”</Year>
					<Days>”MTWHFS”</Days>
					<Time_Start>”10:15”</Time_Start>
					<Time_End>”11:30”</Time_End>
				</Time>
			</Class>
		</Course>
	
	</Response>
	 
	 */
	private String validate_course_list(Document doc) {
		
		String xml="<Response>\n"+
					"<Response_Type>“Validation”</Response_Type>\n" +
					"<Course>\n";
		NodeList list = doc.getElementsByTagName("Course");
		
		if(list.getLength()==0){
			return error_document(NO_COURSES_TO_VALIDATE,"There are no courses to validate.\n");		
		}
		
		for(int i=0; i<list.getLength();i++){
			String v=list.item(i).getNodeValue();
			String sub=v.substring(0,3);
			String num=v.substring(3);
			
			xml=xml+"<Course>\n" +
					"<Subject_CourseID>"+sub+":"+num+"</Subject_CourseID>";
			
			String sql="Select CLASS_NBR, SUBJECT, CATALOG_NBR, CLASS_SECTION, " +
					"DESCR, From_Date, Cancel_Date, MTWRFSD_Days, " +
					"From_Time, To_Time, Component FROM School.Courses where ";
			
			
			sql=sql+" (SUBJECT='"+sub+"' AND CATALOG_NBR='"+num+"') ORDER BY SUBJECT, CATALOG_NBR, From_Date, To_Date, DESCR";	
			
			try {
				Statement stmt=con.createStatement();
				ResultSet res=stmt.executeQuery(sql);
				if(!res.next()){
					return error_document(COURSE_NOT_FOUND,"No Information could be found on "+sub+":"+num+".");		
				}
				do{
					xml=xml+"<Class>\n";
					xml=xml+"<Course_ID>"+res.getString("CLASS_NBR")+"</Course_ID>\n";
					xml=xml+"<Component>"+res.getString("Component")+"</Component>\n";
					xml=xml+"<Title>"+res.getShort("DESCR")+"</Title>\n";
					xml=xml+"<Time>\n";
						xml=xml+"<Semester>"+getSemester(res.getString("From_Date"),res.getString("To_Date"))+"</Semester>\n";
						xml=xml+"<Year>"+getYear(res.getString("From_Date"))+"</Year>\n";
						xml=xml+"<Days>"+res.getString("MTWRFSD_Days")+"</Days>\n";
						xml=xml+"<Time_Start>"+res.getString("From_Time")+"</Time_Start>\n";
						xml=xml+"<Time_End>"+res.getString("To_Time")+"</Time_End>\n";
					xml=xml+"</Time>\n";		
					xml=xml+"</Class>\n";
					
				}while(res.next());
				
				xml=xml+"</Course>\n" +
						"</Response>\n";
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return xml;
	}
	private String getYear(String str) {
		return str.substring(str.lastIndexOf('/'));
	}
	
	private String getSemester(String start, String end) {
		String month=start.substring(0,start.indexOf('/'));
		int mon=Integer.parseInt(month);
		if(mon==8){
			return "Fall";
		}
		else if(mon==1){
			return "Spring";
		}
		
		//Summer
		//A Begins May    Ends June
		//B Begins June   Ends Aug
		//C Begins May    Ends Aug
		//D Begins May    Ends July
		
		else if(mon==5){//A or C or D
			month=end.substring(0,end.indexOf('/'));
			mon=Integer.parseInt(month);
			if(mon==6)
				return "Summer A";
			else if(mon==8)
				return "Summer C";
			else if(mon==7)
				return "Summer D";
			else
				return null;
		}
		else if(mon==6){//B
			return "Summer B";
		}
		else
			return null;
	}
	private void return_html(String xml,ServletOutputStream outs){
		String html="<HTML>\n" +
						"<HEAD>\n" +
						"\t<TITLE>Submitting Text Areas</TITLE>\n"+
						"</HEAD>\n"+
						"<BODY BGCOLOR=\"#FDF5E6\">>\n" +xml+
						"</BODY>\n";
		
		try {
			outs.println(html);
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		return;
		
	}
	private void error_document(int code, String message, ServletOutputStream outs){
		String xml=error_document(code,message);
		return_html(xml,outs);
		return;
	}
	private String error_document(int code, String message){
		String xml="<Response>\n" +
				"<Response_Type>Error</Response_Type>\n" +
				"<Error_Code>"+code+"</Error_Code>\n" +
				"<Error_Message>"+message+"</Error_Message>\n" +
				"</Response>\n";
		return xml;
	}
	private static Connection getConnect() {
		String url = "jdbc:mysql://192.168.1.104:3306/";
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
