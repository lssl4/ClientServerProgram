import java.net.*;
import javax.net.ssl.*;
import java.io.*;

public class Client {
	public static void main(String[] args) {
		String serverName = "127.0.0.1";
		int port = 2323;

		BufferedReader in;

		try {
			
			
			
			 SSLSocketFactory factory =
						(SSLSocketFactory)SSLSocketFactory.getDefault();
					    SSLSocket client =
						(SSLSocket)factory.createSocket("130.95.252.73", 2323);

			System.out.println("Connecting to " + serverName + " on port " + port);

			System.out.println("Just connected to " + client.getRemoteSocketAddress());

			// Get output stream
		    client.startHandshake();

		    PrintWriter out = new PrintWriter(
					  new BufferedWriter(
					  new OutputStreamWriter(
	     				  client.getOutputStream())));

		    out.println("GET / HTTP/1.0");
		    out.println();
		    out.flush();
		    

		    /*
		     * Make sure there were no surprises
		     */
		    if (out.checkError())
			System.out.println(
			    "SSLSocketClient:  java.io.PrintWriter error");

			/* read response */
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));

			System.out.print("Server says: " + in.readLine());

			System.out.print("\n");

			in.close();
			out.close();
			client.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
