import java.net.*;

import java.io.*;

public class Client {
	public static void main(String[] args) {
		String serverName = "127.0.0.1";
		int port = 2323;
		try {
			System.out.println("Connecting to " + serverName + " on port " + port);

			Socket client = new Socket(serverName, port);
			System.out.println("Just connected to " + client.getRemoteSocketAddress());

			/*read in response*/
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));

			out.println("Hello from haha " + client.getLocalSocketAddress() + "\n");
			out.flush();

			/* read response */
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

			System.out.println("Server says: " + in.readLine());

			in.close();
			out.close();

			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
