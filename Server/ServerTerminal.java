/*
* Name: Tzi Siong Leong (20753794), Alexander Miller (2151714), Trae Shaw (21521443)
* Date: May 28 2016
*/



import java.io.IOException;



public class ServerTerminal {
	public static void main(String[] args) throws IOException {


		Server myServ = new Server("Server.ser");
		myServ.run();
		}


}
