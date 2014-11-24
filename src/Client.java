import java.net.*;
import java.util.Scanner;
import java.io.*;

public class Client implements Runnable {
	private Socket socket              = null;
	private Thread thread              = null;
	private DataInputStream  console   = null;
	private DataOutputStream streamOut = null;
	private ClientThread client    = null;
	private Scanner scanner = new Scanner(System.in);
	
   public Client(String serverName, int serverPort) {
	   System.out.println("Establishing connection. Please wait ...");
	   try {
		   socket = new Socket(serverName, serverPort);
		   System.out.println("Connected: " + socket);
		   System.out.println("Commands (without quotes) :\n"
		   		+ "- \".quit\" to quit\n"
		   		+ "- \".list\" to get userslist\n"
		   		+ "- \"idUser:message\" to sent a private message to a single user\n"
		   		+ "- \"[idUser1,idUser2,...,idUserN]:message\" to sent a private message to multpiple users\n"
		   		+ "- just enter a message to send it to every connected users\n");
		   start();
      } catch(UnknownHostException uhe) {
    	  System.out.println("Host unknown: " + uhe.getMessage());
      } catch(IOException ioe) {
    	  System.out.println("Unexpected exception: " + ioe.getMessage());
      }
   }
   
   public void run() {
	   while (thread != null) {
		   try {
			   streamOut.writeUTF(scanner.nextLine());
			   streamOut.flush();
		   } catch(IOException ioe) {
			   System.out.println(ioe.getMessage());
			   stop();
		   } catch (Exception e) {
			   stop();
		   }
      }
   }
   
   public void handle(String msg) {
	   if (msg.equals(".quit")) {
		   System.out.println("See you later. Press RETURN to exit...");
		   stop();
	   } else {
		   System.out.println(msg);
	   }
   }
   
   public void start() throws IOException {
	   console   = new DataInputStream(System.in);
	   streamOut = new DataOutputStream(socket.getOutputStream());
	   if (thread == null) {
		   client = new ClientThread(this, socket);
		   thread = new Thread(this);
		   thread.start();
	   }
   }
   
   public void stop() {
	   if (thread != null) {
		   thread.interrupt();
		   thread = null;
	   }
	   try {
		   if (console   != null) {
			   console.close();
		   }
		   if (streamOut != null) {
			   streamOut.close();
		   }
		   if (socket    != null) {
			   socket.close();
		   }
	   } catch(IOException ioe) {
		   System.out.println("Error closing ...");
	   }
	   client.close();  
	   client.interrupt();
   }
   
   public static void main(String args[]) {
	   if (args.length != 2) {
		   new Client("localhost", 2009);
	   } else {
		   new Client(args[0], Integer.parseInt(args[1]));
	   }
   }
}