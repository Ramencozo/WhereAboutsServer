package jp.ramensroom.whereaboutsserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

public class MainWindow {
	private static Server server;
	private static WebSocketServer webSocketServer;
	
	private static WebSocketClientFactory factory;
	private static WebSocketClient client;
	private static Future<Connection> futureConnection;
		
	public static void main(String[] args) {
		launchServer();

		factory = new WebSocketClientFactory();
		try {
			factory.start();
			client = factory.newWebSocketClient();
			futureConnection = client.open(new URI("ws://192.168.1.4:38400/echo"), new WebSocket.OnTextMessage() {
				@Override
				public void onOpen(Connection arg0) { System.out.println("MainWindow() -> onOpen()"); }
				@Override
				public void onClose(int arg0, String arg1) { System.out.println("MainWindow() -> onClose()"); }
				@Override
				public void onMessage(String arg0) { System.out.println("MainWindow() -> onMessage()"); }
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void launchServer() {
		new Thread(){
			@Override
			public void run() {
				server = new Server(19200);
				ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
				server.setHandler(context);
				context.addServlet(new ServletHolder(new MyHttpServlet()), "/whereabouts");

				try {
					server.start();
					server.join();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		
		new Thread(){
			public void run() {
				webSocketServer = new WebSocketServer(38400, "./html");
				try {
					webSocketServer.start();
					webSocketServer.join();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}

	public static class MyHttpServlet extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setContentType("text/html; charset=UTF-8");
		    PrintWriter out = resp.getWriter();

		    out.println(createHTML());

		    out.close();
		}

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {		
			resp.setContentType("text/html; charset=UTF-8");

			String id = req.getParameter("id");
			if(id != null){
				System.out.print(id);
			}else{
				System.out.print("no id");
			}
			System.out.print(", ");

			String name = req.getParameter("student_name");
			if(name != null){
				System.out.print(name);
			}else{
				System.out.print("nobody");
			}
			System.out.print(", ");

			String place = req.getParameter("place");
			if(place != null){
				System.out.print(place);
				
				String place_other = req.getParameter("place_other");
				if(place_other != null && place.equals("other")){
					System.out.print("-");
					System.out.println(place_other);					
				}else if(place_other.equals("") && place.equals("other")){
					System.out.print("-");
					System.out.println("anywhere.");
				}
				
				if(place.equals("inside_campus")){
					System.out.println("inside_campus -> 学内");
					place = "学内";
				}else if(place.equals("outside_campus")){
					System.out.println("outside_campus -> 学外");
					place = "学外";
				}else if(place.equals("go_home")){
					System.out.println("go_home -> 帰宅");
					place = "帰宅";
				}else{
					System.out.println("other -> その他");
					place = "その他";
				}
				if(!place_other.equals("")){
					place = place_other;
				}
			}else{
				System.out.print("nowhere.");
			}

			PrintWriter out = resp.getWriter();

			out.println(doneHTML(id, name, place));

			out.close();
			
			try {
				Connection connection = futureConnection.get();
				connection.sendMessage(id + "," + name + "," + place);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		protected String createHTML(){
		    StringBuffer sb = new StringBuffer();

		    sb.append("<html>");
		    sb.append("<head>");
		    sb.append("<title>WhereAbouts</title>");
		    sb.append("</head>");
		    sb.append("<body>");
		    sb.append("<font size=\"4\">");

		    sb.append("<p>居場所マネージャ \"WhereAbouts\"</p>");

		    sb.append("<form action=\"/whereabouts\" method=\"post\">");
		    
		    sb.append("<p>");
		    sb.append("ID対応表:");
		    sb.append("<br />");
		    sb.append("0:岸本享太,　　　　 1:下地泰寛,");
		    sb.append("<br />");
		    sb.append("2:高良修平,　　　　 3:仲尾一也,");
		    sb.append("<br />");
		    sb.append("4:ヲレ　　　　,　　　　5:松原諒,");
		    sb.append("<br />");
		    sb.append("6:諸見里斉,　　　　 7:未定(AC1年),");
		    sb.append("<br />");
		    sb.append("8:未定(AC1年),　　 9:未定(AC1年),");
		    sb.append("<br />");
		    sb.append("10:未定(AC1年), 　11:未定(AC1年),");
		    sb.append("<br />");
		    sb.append("12:未定(AC1年), 　13:未定(AC1年),");
		    sb.append("<br />");
		    sb.append("14:未定(AC1年), 　15:以降不在");
		    sb.append("</p>");

		    sb.append("<p>");
		    sb.append("ID:");
		    sb.append("<select name=\"id\">");
		    sb.append("<option>0</option>");
		    sb.append("<option>1</option>");
		    sb.append("<option>2</option>");
		    sb.append("<option>3</option>");
		    sb.append("<option>4</option>");
		    sb.append("<option>5</option>");
		    sb.append("<option>6</option>");
		    sb.append("<option>7</option>");
		    sb.append("<option>8</option>");
		    sb.append("<option>9</option>");
		    sb.append("<option>10</option>");
		    sb.append("<option>11</option>");
		    sb.append("<option>12</option>");
		    sb.append("<option>13</option>");
		    sb.append("<option>14</option>");
		    sb.append("<option>15</option>");
		    sb.append("<option>16</option>");
		    sb.append("<option>17</option>");
		    sb.append("<option>18</option>");
		    sb.append("<option>19</option>");
		    sb.append("<option>20</option>");
		    sb.append("<option>21</option>");
		    sb.append("<option>22</option>");
		    sb.append("<option>23</option>");
		    sb.append("<option>24</option>");
		    sb.append("<option>25</option>");
		    sb.append("<option>26</option>");
		    sb.append("<option>27</option>");
		    sb.append("<option>28</option>");
		    sb.append("<option>29</option>");
		    sb.append("<option>30</option>");
		    sb.append("</select>");
		    sb.append("</p>");

		    sb.append("<p>");
		    sb.append("氏名:");
		    sb.append("<br />");
		    sb.append("<input type=\"text\" name=\"student_name\" />");
		    sb.append("</p>");

		    sb.append("<p>");
		    sb.append("居場所:");
		    sb.append("<br />");
		    sb.append("<input type=\"radio\" name=\"place\"value=\"inside_campus\" />学内");
		    sb.append("<br />");
		    sb.append("<input type=\"radio\" name=\"place\"value=\"outside_campus\" />学外");
		    sb.append("<br />");
		    sb.append("<input type=\"radio\" name=\"place\"value=\"go_home\" />帰宅");
		    sb.append("<br />");
		    sb.append("<input type=\"radio\" name=\"place\"value=\"other\" />その他");
		    sb.append("<input type=\"text\" name=\"place_other\" />");
		    sb.append("<br />");
		    sb.append("<input type=\"submit\" value=\"送信\">");
		    sb.append("</form>");
		    sb.append("</p>");
		    
		    sb.append("</font>");
		    sb.append("</body>");
		    sb.append("</html>");

		    return (new String(sb));
		  }
		protected String doneHTML(String id, String name, String place){
		    StringBuffer sb = new StringBuffer();

		    sb.append("<html>");
		    sb.append("<head>");
		    sb.append("<title>WhereAbouts</title>");
		    sb.append("</head>");
		    sb.append("<body>");
		    sb.append("<font size=\"4\">");

		    sb.append("<p>居場所マネージャ \"WhereAbouts\"</p>");

		    
		    sb.append("<p>");
		    sb.append("送信しました。");
		    sb.append("</p>");

		    sb.append("<p>");
		    sb.append("ID:" + id);
		    sb.append("<br />");
		    sb.append("氏名:" + name);
		    sb.append("<br />");
		    sb.append("居場所:" + place);
		    sb.append("</p>");
		    
		    sb.append("</font>");
		    sb.append("</body>");
		    sb.append("</html>");

		    return (new String(sb));
		  }
	}
}
