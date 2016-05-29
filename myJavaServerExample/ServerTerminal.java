import java.io.IOException;
public class ServerTerminal {
public static void main(String[] args) throws IOException{
	Server myServ = new Server("Server.ser");
	myServ.run();
}
}
