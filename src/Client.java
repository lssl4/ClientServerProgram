import java.net.*;

import java.io.*;

public class Client {
	public static void main(String[] args) {
		String serverName = "127.0.0.1";
		int port = 2323;

		BufferedReader in;
		BufferedWriter out;

		try {
			System.out.println("Connecting to " + serverName + " on port " + port);

			Socket client = new Socket(serverName, port);
			System.out.println("Just connected to " + client.getRemoteSocketAddress());

			// Get output stream
			out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			out.write("Hello from " + client.getLocalSocketAddress());

			/* read response */
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));

			System.out.print("Server says: " + in.readLine());

			System.out.print("\n");

			in.close();

			client.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
