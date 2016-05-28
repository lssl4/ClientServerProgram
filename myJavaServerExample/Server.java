
import javax.net.ServerSocketFactory;
import javax.net.ssl.*;

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

  private static OurFileSystem filesys;
  private static String type = "SSL";
  private static int port = 2323;

  public Server() {
    try {
      filesys = new OurFileSystem();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    try {

      ServerSocketFactory ssf = getServerSocketFactory(type);
      ServerSocket ss = ssf.createServerSocket(port);
while(true){
      Socket sslsocket = ss.accept();

      InputStream socketInputStream = sslsocket.getInputStream();
      OutputStream socketOutputStream = sslsocket.getOutputStream();

      BufferedReader in = new BufferedReader(new InputStreamReader(socketInputStream));
      BufferedWriter resp = new BufferedWriter( new OutputStreamWriter(socketOutputStream));

      // Print out the bufferedinputstream
      //System.out.println(in.readLine());

      // bufferedwriter.write("hahahahaha");

      /*
       * String string = null; while ((string = bufferedreader.readLine())
       * != null) { System.out.println(string); System.out.flush(); }
       */

      // Doing switch operations from incoming data stream
      // This block of code parses through the incoming command line
      // stream from the client
      String ClientCom;

      while ((ClientCom = in.readLine()) != null) {
	System.out.println(ClientCom);
        // The circle circumference and certificate name. Commands are
        // used to assign them values.
        int cir = 0;
        String certName = "";

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
          byte[] rawFile = writeFile("Files/",filename , Integer.parseInt(in.readLine()),
              socketInputStream);

          // passes the afilename and raw file array to add method to
          // add to the OurFileSystem object
          filesys.add(filename, rawFile);


          break;

        // Upload a certificate case "-u":
        case 'u':

          // Writing the file as a certificate and put it in the
          // certificates folder
          writeFile("Certificates/", ClientCom.substring(3), Integer.parseInt(in.readLine()),
              socketInputStream);

          break;

        // Vouch file with a certificate
        case 'v':

          filesys.vouchFile(ClientCom.substring(3), in.readLine());

          break;

        case 'f':

        //File fetched = filesys.fetch(ClientCom.substring(3), certName, cir);
	File fetched = new File(ClientCom.substring(3));
        // Get the size of the file
        long length = fetched.length();
        byte[] bytes = new byte[(int)length];
	resp.write(String.valueOf(length)+ "\n");
	resp.flush();
	InputStream fin = new FileInputStream(fetched);


        fin.read(bytes);
	socketOutputStream.write(bytes, 0, (int)length);
	fin.close();

          break;

        case 'n':
          cir = Integer.parseInt(ClientCom.substring(3));

          break;

        case 'c':

          certName = ClientCom.substring(3);

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
  private static byte[] writeFile(String type, String filename, int fileSize, InputStream inStream)
      throws IOException {

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
	if(count!=fileSize){
		System.out.println("error reading file");
		return null;
}
    output.write(fileBytes, 0, count);

    // Close output stream
    output.close();

    return fileBytes;

  }

  public class OurFileSystem {

    private ArrayList<ServerFile> serverFileSystem = new ArrayList<ServerFile>();

    public OurFileSystem() throws IOException {

      serverFileSystem = new ArrayList<ServerFile>();

    }

    public void add(String filename, byte[] input) throws FileNotFoundException, NoSuchAlgorithmException {

      // adding file into the filesystem array
	System.out.println("hello");
	ServerFile newFile = new ServerFile(filename, input);
	int listLength = serverFileSystem.size();
	int found = -1;
	for (int i = 0; i < listLength; i++){
		if(serverFileSystem.get(i).fileName.equals(filename)){
			found = i;
			break;
		}
	}
	if(found != -1){
		serverFileSystem.remove(found);
	}      		
		serverFileSystem.add(newFile);
    }


    // http://stackoverflow.com/questions/4852531/find-files-in-a-folder-using-javas
    // -f filename -c size -n name
    public File fetch(final String filename, String certname, Integer cir) {

      // initialize the arguments if null values are given
	//https://github.com/jgrapht/jgrapht/wiki/DirectedGraphDemo
      certname = certname != null ? certname : "";
      cir = cir != null ? cir : 0;

      // Finding the appropriate file in the files directory
      File f = new File("Files/");
      File[] matchingFiles = f.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.equals(filename);
        }
      });

      // find file in the serverFileSystem to check if the conditions have
      // been met to be fetched


        for (ServerFile obj : serverFileSystem) {
        ArrayList<X509Certificate> listOfCerts = obj.certificates;

        // Getting all of the list of name from the ServerFile
        // certificates
        ArrayList<String> listOfNames = new ArrayList<String>();
        for (X509Certificate cert : listOfCerts) {

          listOfNames.add(cert.getSubjectX500Principal().getName());

        }

        if (obj.fileName.equals(filename) && obj.certificates.size() >= cir && listOfNames.contains(certname)) {

          return matchingFiles[0];

        }


        }



        return null;
    }

    public String listFiles() {
      String list = "";

      for (ServerFile f : serverFileSystem) {
        list += f.fileName + ": " /*+ f.maxCircle*/ + "\n";

      }

      return list;
    }

    public void vouchFile(String filename, final String cert) throws FileNotFoundException, CertificateException {

      // For each serverfile in the serverfile system, find the file with
      // the same name as filename
      for (ServerFile f : serverFileSystem) {

        // if the ServerFile matches the name, add the certificate to
        // the arraylist
        if (f.fileName.equals(filename)) {

          // Find the certain certificate in the certificate
          // directories
          File certDir = new File("Certificates/");
          File[] matchingCert = certDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {

              return name.equals(cert);
            }
          });

          // Once the certificate has been found, append to the
          // ServerFile certificate arraylist
          // From:
          // https://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.htmls
          // (27052016)

          FileInputStream inputStream = new FileInputStream(matchingCert[0]);

          CertificateFactory certFac = CertificateFactory.getInstance("X.509");

          // Once the stream has been converted to a certificate,
          // add it to the ServerFile certificate array
          X509Certificate genCert = (X509Certificate) certFac.generateCertificate(inputStream);
          f.certificates.add(genCert);

        }

      }

    }

  }

  public class ServerFile implements Comparable<ServerFile> {

    String fileName;
    byte[] hash;
    ArrayList<X509Certificate> certificates;
    ArrayList<ArrayList<Principal>> cycleList;
    DefaultDirectedGraph<Principal,DefaultEdge> graph;
    //int maxCircle;

    public ServerFile(String name, byte[] rawFile) throws NoSuchAlgorithmException {

      fileName = name;
      hash = MessageDigest.getInstance("MD5").digest(rawFile);
      certificates = new ArrayList<X509Certificate>();



    }

    @Override
    public int compareTo(ServerFile o) {

      return o.fileName.compareTo(this.fileName);
    }

  }

}
