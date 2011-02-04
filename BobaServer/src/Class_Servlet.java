import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Vector;

import javax.servlet.ServletConfig;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
 
public class Class_Servlet extends HttpServlet{ private static final long serialVersionUID = 1L;
	final int UNDEFINED_TYPE=1;
	final int COURSE_NOT_FOUND=2;
	final int NO_COURSES_TO_VALIDATE=3;
	final int XML_FORMAT_ERROR=4;
	
	Hashtable<String, Course> table;
	
	Connection con;
	 public void init(ServletConfig config){
		if((con=getConnect())==null)
			this.destroy();
		if(!fill_table())
			this.destroy();
		System.out.println("Test");
	}
	
	/**This method fills the hashtable with courses
	 * Then each course, is stored with out any classes
	 * If a course is attempted to be validated then the classes will be populated at the time it is needed.
	 * @return
	 */
	private boolean fill_table(){
		System.out.println("Initilizing Data Structures, Please be patient. this may take up to 3 mins....");
		int count=0;
		long start=System.currentTimeMillis();
		try {
			Statement stmt=con.createStatement();
			String sql="SELECT DESCR, SUBJECT, CATALOG_NBR From School.Courses ORDER BY SUBJECT, CATALOG_NBR, DESCR";
			
			ResultSet res=stmt.executeQuery(sql);
			table=new Hashtable<String, Course>();
			
			while(res.next()){
				count++;
				if(count%500==0)System.out.println(count+"...");
				String title=res.getString("DESCR").trim();
				String catalog_number=res.getString("CATALOG_NBR").trim();
				String subject=res.getString("SUBJECT").trim();
				Course c = new Course( title, subject, catalog_number);
				c.fillClass(con);
				table.put(subject+":"+catalog_number, c);
			}
			res.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Initilizing Data Structures-Failed");
			return false;
		}
		long end=System.currentTimeMillis();
		start=(end-start)/1000;
		System.out.println("Initilizing Data Structures-DONE!, took "+start);
		return true;
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse res)throws ServletException{
		ServletOutputStream out=null;
		try {
			out = res.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    res.setContentType("text/html");
	    
		try {
			Scanner s = new Scanner(new File("BobaServer/index.html"));
			while(s.hasNext()){
		    	out.println(s.nextLine());
		    }
		} catch (FileNotFoundException e) {
		//	e.printStackTrace();
			return_html(e.getMessage(),out);
		} catch (IOException e) {
		//	e.printStackTrace();
			return_html(e.getMessage(),out);
		}
	    
	    
	    return;
	  
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp){
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
			InputSource is = new InputSource();
		    is.setCharacterStream(new StringReader(res));
	        doc = docBuilder.parse(is);
	       
	        if(doc==null) throw new NullPointerException();
	        doc.normalize();
	        doc.normalizeDocument();
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
		
		NodeList nodes = doc.getElementsByTagName("Request_Type");
		String type=nodes.item(0).getFirstChild().getTextContent();
		
		if(type==null)type="";
		
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
					"<Response_Type>Validation</Response_Type>\n";
					
		NodeList list = doc.getElementsByTagName("Course");
		
		if(list.getLength()==0){
			return error_document(NO_COURSES_TO_VALIDATE,"There are no courses to validate.\n");		
		}
		
		for(int i=0; i<list.getLength();i++){
			String v=list.item(i).getFirstChild().getTextContent();
			String sub=v.substring(0,3);
			String num=v.substring(3);
			
			xml=xml+"\t<Course>\n"+
					"\t\t<Subject_CourseID>"+v+"</Subject_CourseID>\n";
			
			
			Course c=table.get(v);
			if(c==null){
				return error_document(COURSE_NOT_FOUND,"No Information could be found on "+sub+":"+num+".");		
			}
			
			Vector<Clas> class_list=c.getClases();
			Vector<Clas> lab_list=c.getLabs();
			if(class_list.size()==0){
				return error_document(COURSE_NOT_FOUND,sub+":"+num+", is not being offered.");		
			}
			
			xml=xml+"\t\t<Main_Course>\n";
			for(int j=0; j<class_list.size(); j++){
				Clas clas=class_list.get(j);
				xml=xml+"\t\t\t<Class>\n";
					xml=xml+"\t\t\t\t<Class_ID>"+clas.course_id+"</Class_ID>\n";
					xml=xml+"\t\t\t\t<Component>"+clas.component+"</Component>\n";
					xml=xml+"\t\t\t\t<Title>"+c.title+"</Title>\n";
					xml=xml+"\t\t\t\t<Time>\n";
						xml=xml+"\t\t\t\t\t<Semester>"+clas.semester+"</Semester>\n";
						xml=xml+"\t\t\t\t\t<Year>"+clas.start_year+"</Year>\n";
						xml=xml+"\t\t\t\t\t<Days>"+clas.getDays()+"</Days>\n";
						xml=xml+"\t\t\t\t\t<Time_Start>"+clas.from_time+"</Time_Start>\n";
						xml=xml+"\t\t\t\t\t<Time_End>"+clas.to_time+"</Time_End>\n";
					xml=xml+"\t\t\t\t</Time>\n";		
				xml=xml+"\t\t\t</Class>\n";	
			}
			xml=xml+"\t\t</Main_Course>\n";
			if(lab_list.size()!=0){
				xml=xml+"\t\t<Lab_Course>\n";
					for(int j=0; j<lab_list.size(); j++){
						Clas clas=lab_list.get(j);
						xml=xml+"\t\t\t<Class>\n";
							xml=xml+"\t\t\t\t<Class_ID>"+clas.course_id+"</Class_ID>\n";
							xml=xml+"\t\t\t\t<Component>"+clas.component+"</Component>\n";
							xml=xml+"\t\t\t\t<Title>"+c.title+"</Title>\n";
							xml=xml+"\t\t\t\t<Time>\n";
								xml=xml+"\t\t\t\t\t<Semester>"+clas.semester+"</Semester>\n";
								xml=xml+"\t\t\t\t\t<Year>"+clas.start_year+"</Year>\n";
								xml=xml+"\t\t\t\t\t<Days>"+clas.getDays()+"</Days>\n";
								xml=xml+"\t\t\t\t\t<Time_Start>"+clas.from_time+"</Time_Start>\n";
								xml=xml+"\t\t\t\t\t<Time_End>"+clas.to_time+"</Time_End>\n";
							xml=xml+"\t\t\t\t</Time>\n";		
						xml=xml+"\t\t\t</Class>\n";		
					}
				xml=xml+"\t\t</Lab_Course>\n";
			}
			xml=xml+"\t</Course>\n";
		}
		
		
		xml=xml+"</Response>\n";
	
		return xml;
	}
	
	
	private void return_html(String xml,ServletOutputStream outs){
		String html="<HTML>\n" +
						"<HEAD>\n" +
						"\t<TITLE>Servlet_Response</TITLE>\n"+
						"</HEAD>\n"+
						"<BODY BGCOLOR=\"#FDF5E6\">>\n"+ "Servlet Response to XML<br>" +
								"<textarea rows=\"50\" cols=\"100\">"+xml+
								"</textarea>"+
						"</BODY>\n";
		try {
			outs.println(html);
			outs.flush();
			outs.close();
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
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
