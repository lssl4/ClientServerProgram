
import javax.net.ServerSocketFactory;
import javax.net.ssl.*;

import org.jgrapht.alg.*;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.io.*;
import java.net.*;
import java.security.cert.*;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.*;

//http://stilius.net/java/java_ssl.php and http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/samples/sockets/server/ClassFileServer.java
public class Server {

	private OurFileSystem filesys;
	private String type = "SSL";
	private int port = 2323;

	public Server() {
		try {
			filesys = new OurFileSystem();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {

			ServerSocketFactory ssf = getServerSocketFactory(type);
			ServerSocket ss = ssf.createServerSocket(port);
			while (true) {
				try {

					Socket sslsocket = ss.accept();

					// initialize the fetching arguments
					String certName = null;
					int cir = 0;

					InputStream socketInputStream = sslsocket.getInputStream();
					OutputStream socketOutputStream = sslsocket.getOutputStream();

					BufferedReader in = new BufferedReader(new InputStreamReader(socketInputStream));
					BufferedWriter resp = new BufferedWriter(new OutputStreamWriter(socketOutputStream));

					// Print out the bufferedinputstream
					// System.out.println(in.readLine());

					// bufferedwriter.write("hahahahaha");

					/*
					 * String string = null; while ((string =
					 * bufferedreader.readLine()) != null) {
					 * System.out.println(string); System.out.flush(); }
					 */

					// Doing switch operations from incoming data stream
					// This block of code parses through the incoming command
					// line
					// stream from the client
					String ClientCom;

					while ((ClientCom = in.readLine()) != null) {
						System.out.println(ClientCom + "ClientCom");
						// The circle circumference and certificate name.
						// Commands
						// are
						// used to assign them values.

						String flag = ClientCom.substring(0, 2);

						switch (flag.charAt(1)) {

						// List all the files in directory case "-l":
						case 'l':
							filesys.listFiles();

							break;

						// Add a new file case "-a":
						case 'a':

							// Write the file to the server directory
							String filename = ClientCom.substring(3);

							// Adding file to harddisk
							writeFile("Files/", filename, Integer.parseInt(in.readLine()), socketInputStream);

							// passes the filename to add method to
							// add to the OurFileSystem object
							filesys.add(filename);

							break;

						// Upload a certificate case "-u":
						case 'u':

							// Writing the file as a certificate and put it in
							// the
							// certificates folder
							writeFile("Certificates/", ClientCom.substring(3), Integer.parseInt(in.readLine()),
									socketInputStream);

							break;

						// Vouch file with a certificate
						case 'v':

							if (filesys.vouchFile(ClientCom.substring(3), in.readLine())) {

								resp.write("File was vouched successfully");
								resp.flush();
							} else {

								resp.write(
										"File can not be vouched because it doesn't exist in the server and/or the certificate doesn't exist in the server");
								resp.flush();

							}

							break;

						case 'f':

							// File fetched =
							// filesys.fetch(ClientCom.substring(3),
							// certName, cir);

							// File fetched = new File(ClientCom.substring(3));
							// // Get the size of the file
							// long length = fetched.length();
							// byte[] bytes = new byte[(int)length];
							// resp.write(String.valueOf(length)+"\n");
							//
							// resp.flush();
							// InputStream fin = new FileInputStream(fetched);
							//
							//
							// fin.read(bytes);
							// socketOutputStream.write(bytes, 0, (int)length);
							// fin.close();

							File fetched;

							// If the file exists, send to client, if not send
							// an
							// error message
							if ((fetched = filesys.fetch(ClientCom.substring(3), certName, cir)) != null) {

								long length = fetched.length();
								byte[] bytes = new byte[(int) length];

								System.out.println(String.valueOf(length));
								// Sending length to client
								resp.write(String.valueOf(length) + "\n");

								// Ensures everything is sent
								resp.flush();

								InputStream fin = new FileInputStream(fetched);
								fin.read(bytes);
								socketOutputStream.write(bytes, 0, (int) length);
								fin.close();

							} else {

								resp.write("-1\n");
								resp.flush();
							}

							break;

						case 'n':
							certName = ClientCom.substring(3);

							break;

						case 'c':

							cir = Integer.parseInt(ClientCom.substring(3));

							resp.write("Certificate circle length successful");
							resp.flush();
							break;

						default:
							break;

						}

					}

					// Closes data input stream
					in.close();

					sslsocket.close();

				} catch (Exception e) {
					System.out.println("Error while running: " + e.getMessage());
					e.printStackTrace();
				}

			}

		} catch (Exception exception) {
			System.out.println("Unable to start Server: " + exception.getMessage());
			exception.printStackTrace();
		}
	}

	// This method was obtained from:
	// http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/samples/sockets/server/ClassFileServer.java
	// (27 May 2016)
	private static ServerSocketFactory getServerSocketFactory(String type) {
		if (type.equals(type)) {
			SSLServerSocketFactory ssf = null;
			try {
				// set up key manager to do server authentication
				SSLContext ctx;
				KeyManagerFactory kmf;
				KeyStore ks;
				char[] passphrase = "cits3002".toCharArray();

				ctx = SSLContext.getInstance(type);
				kmf = KeyManagerFactory.getInstance("SunX509");
				ks = KeyStore.getInstance("JKS");

				ks.load(new FileInputStream("mykeystore.jks"), passphrase);
				kmf.init(ks, passphrase);
				ctx.init(kmf.getKeyManagers(), null, null);

				ssf = ctx.getServerSocketFactory();
				return ssf;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return ServerSocketFactory.getDefault();
		}
		return null;

	}

	// Helper function to write a file to the server and return the raw file in
	// byte arrays
	private static void writeFile(String type, String filename, int fileSize, InputStream inStream) throws IOException {

		// next upcoming stream should be the data file
		// http://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets
		// (27052016)

		OutputStream output = new FileOutputStream(type + filename);

		// Make a byte array to be the length of incoming data stream to
		// populate it with the raw data file
		byte[] fileBytes = new byte[fileSize];

		// Obtaining the number of bytes that is read and use that to write the
		// file using the fileBytes
		int count = inStream.read(fileBytes);
		if (count != fileSize) {
			System.out.println("error reading file");

		}
		output.write(fileBytes, 0, count);

		// Close output stream
		output.close();

	}

	public class OurFileSystem {

		private ArrayList<ServerFile> serverFileSystem = new ArrayList<ServerFile>();

		public OurFileSystem() throws IOException {

			serverFileSystem = new ArrayList<ServerFile>();

		}

		public void add(String filename) throws FileNotFoundException, NoSuchAlgorithmException {

			// adding file into the filesystem array
			ServerFile newFile = new ServerFile(filename);
			int listLength = serverFileSystem.size();
			int found = -1;
			for (int i = 0; i < listLength; i++) {
				if (serverFileSystem.get(i).fileName.equals(filename)) {
					found = i;
					break;
				}
			}
			if (found != -1) {
				serverFileSystem.remove(found);
			}
			serverFileSystem.add(newFile);
		}

		// http://stackoverflow.com/questions/4852531/find-files-in-a-folder-using-javas
		// -f filename -c size -n name
		public File fetch(final String fname, String certname, Integer cir) throws FileNotFoundException {

			File output = null;

			// Finding the appropriate file in the files directory if it exists,
			// otherwise return null
			for (ServerFile f : serverFileSystem) {
				if (f.fileName.equals(fname)) {
					// Once the file is found, apply the isValid function to
					// determine if the file can be returned to client, if not
					// return null
					if (f.isValid(cir, certname)) {
						System.out.println("valid");
						output = new File("Files/" + fname);
					}

				}
			}

			return output;

		}

		public String listFiles() {
			String list = "";

			for (ServerFile f : serverFileSystem) {
				list += f.fileName + ": " + f.maxCircle + "\n";

			}

			return list;
		}

		public boolean vouchFile(String filename, final String certname)
				throws FileNotFoundException, CertificateException {

			// For each serverfile in the serverfile system, find the file with
			// the same name as filename
			boolean status = false;
			for (ServerFile f : serverFileSystem) {

				// if the ServerFile matches the name, add the certificate to
				// the arraylist
				if (f.fileName.equals(filename)) {

					// Find the certain certificate in the certificate
					// directories
					// Once the certificate has been found, append to the
					// ServerFile certificate arraylist
					// From:
					// https://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.htmls
					// (27052016)

					FileInputStream inputStream = new FileInputStream("Certificates/" + certname);

					CertificateFactory certFac = CertificateFactory.getInstance("X.509");

					// Once the stream has been converted to a certificate,
					// add it to the ServerFile certificate array
					X509Certificate genCert = (X509Certificate) certFac.generateCertificate(inputStream);
					f.certAdd(genCert);

					status = true;

				}

			}
			return status;

		}

	}

	public class ServerFile implements Comparable<ServerFile> {

		String fileName;
		private ArrayList<X509Certificate> certificates;
		private List<List<Principal>> cycleList;
		private DefaultDirectedGraph<Principal, DefaultEdge> graph;
		private ArrayList<Principal> vertices;
		int maxCircle;

		public ServerFile(String name) throws NoSuchAlgorithmException {

			fileName = name;
			certificates = new ArrayList<X509Certificate>();
			graph = new DefaultDirectedGraph<Principal, DefaultEdge>(DefaultEdge.class);
			vertices = new ArrayList<Principal>();
			maxCircle = 0;

		}

		// Add certificate's issuer and subjects to the vertices arraylist and
		// then the graph
		public void certAdd(X509Certificate cert) {

			//Adding certificate to certificates arraylist
			certificates.add(cert);

			Principal issuer = cert.getIssuerDN();
			Principal subject = cert.getSubjectDN();
			if (!vertices.contains(issuer)) {
				vertices.add(issuer);
				graph.addVertex(issuer);
			}
			if (!vertices.contains(subject)) {
				vertices.add(subject);
				graph.addVertex(subject);
			}
			graph.addEdge(subject, issuer);
			constructCycles();
		}

		// Outputs a boolean if the file is valid to be fetched
		public boolean isValid(int cycleSize, String certname) {
			int arraySize;
			int innerArraySize;
			if (cycleSize != 0 || certname != null) {
				arraySize = cycleList.size();
				for (int i = 0; i < arraySize; i++) {
					innerArraySize = cycleList.get(i).size();
					if (innerArraySize >= cycleSize) {
						if (certname == null) {
							return true;
						} else {
							for (int j = 0; j < innerArraySize; j++) {
								if (cycleList.get(i).get(j).getName().equals(certname)) {
									return true;
								}
							}
						}
					}
				}
				return false;
			} else {
				return true;
			}
		}

		// Find all the cycles in the graph and add it to the cyclelist
		private void constructCycles() {
			JohnsonSimpleCycles<Principal, DefaultEdge> johnsons = new JohnsonSimpleCycles<Principal, DefaultEdge>(graph);

			cycleList = johnsons.findSimpleCycles();
			for (List<Principal> cycle : cycleList) {

				for (Principal ele : cycle) {

					System.out.println(ele.getName());
				}System.out.println("new cycle");

			}
			findMaxCircle();

		}

		// Find the cycle with the largest number of nodes.
		private int findMaxCircle() {

			int maxSize = 0;

			for (List<Principal> cycle : cycleList) {

				if (cycle.size() > maxSize) {

					maxSize = cycle.size();

				}

			}
			return maxSize;

		}

		@Override
		public int compareTo(ServerFile o) {

			return o.fileName.compareTo(this.fileName);
		}

	}

}
