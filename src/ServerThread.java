import java.net.*;
import java.io.*;

public class ServerThread extends Thread {
	private Socket          socket   = null;
	private Server      server   = null;
	private int             ID       = -1;
	private DataInputStream streamIn =  null;
	private DataOutputStream streamOut = null;

   public ServerThread(Server _server, Socket _socket) {
	   server = _server;
	   socket = _socket;
	   ID = socket.getPort();
   }
   
   public void run() {
	   System.out.println("Server Thread " + ID + " running.");
	   while (true) {
		   try {
			   server.handle(ID, streamIn.readUTF());
		   } catch(IOException ioe) {
			   System.out.println(ID + " " + ioe.getMessage());
	            server.remove(ID);
	            Thread.currentThread().interrupt();
                break;
		   }
      }
   }
   
   public void send(String msg) {
	   try {
		   streamOut.writeUTF(msg);
		   streamOut.flush();
       } catch(IOException ioe) {
    	   System.out.println(ID + " ERROR sending: " + ioe.getMessage());
    	   server.remove(ID);
    	   Thread.currentThread().interrupt();
       }
   }
   
   public int getID() {
	   return ID;
   }
   
   public void open() throws IOException {
	   streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
	   streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
   }
   
   public void close() throws IOException {
	   if (socket != null) {
		   socket.close();
	   }
	   if (streamIn != null) {
		   streamIn.close();
	   }
	   if (streamOut != null) {
		   streamOut.close();
	   }
   }
}