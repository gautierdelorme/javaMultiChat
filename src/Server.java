import java.net.*;
import java.io.*;

public class Server implements Runnable {
	private ServerThread clients[] = new ServerThread[50];
	private ServerSocket server = null;
	private Thread       thread = null;
	private int clientCount = 0;

	public Server(int port){
		try {
			System.out.println("Binding to port " + port + ", please wait  ...");
			server = new ServerSocket(port);  
			System.out.println("Server started: " + server);
			start();
		} catch(IOException ioe) {
			System.out.println("Can not bind to port " + port + ": " + ioe.getMessage());
		}
	}
	
	public void run() {
		while (thread != null) {
			try {
				System.out.println("Waiting for a client ..."); 
	            addThread(server.accept());
	        } catch(IOException ioe) {
	        	 System.out.println("Error server accept client : " + ioe);
	        	 stop();
	        }
		}
	}
	
	private int findClient(int ID) {
		for (int i = 0; i < clientCount; i++) {
			if (clients[i].getID() == ID) {
	            return i;
			}
		}
		return -1;
	}
	
	public synchronized void handle(int ID, String input) {
		if (input.equals(".quit")) {
			clients[findClient(ID)].send(".quit");
			remove(ID);
		} else if (input.equals(".list")) {
			String sendString = "\n"+clientCount+" user(s) :\n";
	        for (int i = 0; i < clientCount; i++) {
	        	sendString += "- "+clients[i].getID()+"\n";
	        }
	        clients[findClient(ID)].send(sendString);
		} else if (input.matches("^[0-9]+:.*")) {
			int idChar = 0;
			int i = 0;
			while (Character.isDigit(input.charAt(i))) {
				idChar = 10*idChar+Character.getNumericValue(input.charAt(i));
				i++;
			}
			if (findClient(idChar) != -1 && clients[findClient(idChar)].getID() != ID) {
				clients[findClient(idChar)].send(ID + ": " + input.substring(i+1));
			} else {
				clients[findClient(ID)].send("server :  Can't send to this user.");
			}
		} else if (input.matches("^\\[([0-9]+,* *)+\\]:.*")) {
			String inputSend = input.split(":")[1];
			int i = 0;
			while (input.charAt(i) != ']') {
				int idChar = 0;
				int j = i;
				while (Character.isDigit(input.charAt(i))) {
					idChar = 10*idChar+Character.getNumericValue(input.charAt(i));
					i++;
				}
				if (idChar != 0) {
					if (findClient(idChar) != -1 && clients[findClient(idChar)].getID() != ID) {
						clients[findClient(idChar)].send(ID + ": " + inputSend);
					} else {
						clients[findClient(ID)].send("server :  Can't send to this user ("+idChar+").");
					}
				}
				if (j == i) {
					i++;
				}
			}
		} else {
			for (int i = 0; i < clientCount; i++) {
				if (clients[i].getID() != ID) {
					clients[i].send(ID + ": " + input); 
				}
			}
		}
	}
	
	public synchronized void remove(int ID) {
		int pos = findClient(ID);
		if (pos >= 0) {
			ServerThread toTerminate = clients[pos];
			System.out.println("Removing client thread " + ID + " at " + pos);
	        if (pos < clientCount-1) {
	        	for (int i = pos+1; i < clientCount; i++) {
	        		clients[i-1] = clients[i];
	        	}
	        }
	        clientCount--;
	        getUsersList();
	        try {
	        	toTerminate.close();
	        } catch(IOException ioe) {
	        	System.out.println("Error closing thread: " + ioe);
	        }
	        toTerminate.interrupt();
		}
	}
	
	private synchronized void getUsersList() {
		String sendString = clientCount+" user(s) :\n";
        for (int i = 0; i < clientCount; i++) {
        	sendString += "- "+clients[i].getID()+"\n";
        }
        System.out.println(sendString);
        for (int i = 0; i < clientCount; i++) {
			clients[i].send(sendString); 
		}
	}
	
	private void addThread(Socket socket) {
		if (clientCount < clients.length) {
			System.out.println("Client accepted: " + socket);
	        clients[clientCount] = new ServerThread(this, socket);
	        try {
	        	clients[clientCount].open(); 
	            clients[clientCount].start();  
	            clientCount++;
	            getUsersList();
	        } catch(IOException ioe) {
	        	System.out.println("Error opening thread: " + ioe);
	        }
		} else {
			System.out.println("Client refused: max ("+clients.length+") reached.");
		}
	}
	
	public void start() {
		if (thread == null) {
			thread = new Thread(this); 
	        thread.start();
	    }
	}
	
	public void stop() {
		if (thread != null){
			thread.interrupt();
	        thread = null;
	    }
	}
	
	public static void main(String args[]){
		if (args.length != 1) {
			new Server(2009);
		} else {
			new Server(Integer.parseInt(args[0]));
		}
	}
}