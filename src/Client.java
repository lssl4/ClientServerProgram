import java.net.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.io.*;





public class Client {
	public static void main(String [] args)
	   {
	      String serverName = "106.69.248.238";
	      int port = 2323;
	      try
	      {
	         System.out.println("Connecting to " + serverName +
			 " on port " + port);
	         
	         Socket client = new Socket(serverName, port);
	         System.out.println("Just connected to " 
			 + client.getRemoteSocketAddress());
	         
	         OutputStream outToServer = client.getOutputStream();
	         DataOutputStream out = new DataOutputStream(outToServer);
	         out.writeUTF("Hello from "
	                      + client.getLocalSocketAddress());
	         
	         InputStream inFromServer = client.getInputStream();
	         DataInputStream in = new DataInputStream(inFromServer);
	         System.out.println("Server says " + in.readUTF());
	         client.close();
	      }catch(IOException e)
	      {
	         e.printStackTrace();
	      }
	   }
}
