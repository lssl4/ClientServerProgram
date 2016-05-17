import java.net.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.io.*;





public class Client {
	public static void main(String [] args) throws UnknownHostException
	   {
		byte[] ipAddress = new byte[4];
		
		ipAddress[0]= Byte.parseByte("10000010",2);
		ipAddress[1]= Byte.parseByte("01011111",2);
		ipAddress[2]= Byte.parseByte("11111100",2);
		ipAddress[3]= Byte.parseByte("01110001",2);

		
		InetAddress serverName = InetAddress.getByAddress(ipAddress);
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
