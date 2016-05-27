
import javax.net.ServerSocketFactory;
import javax.net.ssl.*;

import java.io.*;
import java.net.*;
import java.security.cert.*;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

//http://stilius.net/java/java_ssl.php and http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/samples/sockets/server/ClassFileServer.java
public class Server {

  private static OurFileSystem filesys;
  private static String type = "TLS";
  private static int port = 2323;

  public Server() {
    try {
      filesys = new OurFileSystem();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    try {

      ServerSocketFactory ssf = getServerSocketFactory(type);
      ServerSocket ss = ssf.createServerSocket(port);
      Socket sslsocket = ss.accept();
      
      /*
       * InputStream inputstream = sslsocket.getInputStream();
       * InputStreamReader inputstreamreader = new
       * InputStreamReader(inputstream); BufferedReader bufferedreader =
       * new BufferedReader(inputstreamreader);
       * 
       * OutputStream outputstream = sslsocket.getOutputStream();
       * OutputStreamWriter outputstreamwriter = new
       * OutputStreamWriter(outputstream); BufferedWriter bufferedwriter =
       * new BufferedWriter(outputstreamwriter);
       */

      InputStream inputstream = sslsocket.getInputStream();
      DataInputStream in = new DataInputStream(inputstream);
      
      //Print out the datainputstream
      System.out.println(in.readUTF());

      // bufferedwriter.write("hahahahaha");

      /*
       * String string = null; while ((string = bufferedreader.readLine())
       * != null) { System.out.println(string); System.out.flush(); }
       */

      // Doing switch operations from incoming data stream

      // This block of code parses through the incoming command line
      // stream from the client

      /*String clientCom;
      while ((clientCom = in.readUTF()) != null) {

        // I split the inputstream
        String[] splitClientCom = clientCom.split(" ");

        switch (splitClientCom[0]) {

        // List all the files in directory
        case "-l":

          filesys.listFiles();

          break;

        // Add a new file
        case "-a":

          filesys.add(splitClientCom[1], in);

          break;

        // Upload a certificate
        case "-u":

          filesys.uploadCert(splitClientCom[1], in);

          break;

        case "-v":

          filesys.vouchFile(splitClientCom[1], splitClientCom[2]);

          break;

        case "-f":

          // if less than or equal to 2 command options, pass the
          // filename straight through
          if (splitClientCom.length == 2) {

            filesys.fetch(splitClientCom[1], null, null);

          } else if (splitClientCom.length == 4) {

            // filesys.fetch(clientCom.substring(clientCom.indexOf("-n"),clientCom.indexOf("-c")),
            // certname, cir);

          }

          break;

        default:
          break;
        }

      }*/

      // Closes data input stream
      in.close();

      sslsocket.close();

    } catch (Exception exception) {
      System.out.println("Unable to start ClassServer: " + exception.getMessage());
      exception.printStackTrace();
    }

  }

  // This method was obtained from:
  // http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/samples/sockets/server/ClassFileServer.java
  // (27 May 2016)
  private static ServerSocketFactory getServerSocketFactory(String type) {
    if (type.equals("TLS")) {
      SSLServerSocketFactory ssf = null;
      try {
        // set up key manager to do server authentication
        SSLContext ctx;
        KeyManagerFactory kmf;
        KeyStore ks;
        char[] passphrase = "cits3002".toCharArray();

        ctx = SSLContext.getInstance("TLS");
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

  public class OurFileSystem {

    private ArrayList<ServerFile> serverFileSystem = new ArrayList<ServerFile>();

    public OurFileSystem() throws IOException {

      serverFileSystem = new ArrayList<ServerFile>();

    }

    public void add(String filename, DataInputStream in) throws IOException, NoSuchAlgorithmException {

      // next upcoming stream should be the data file
      FileOutputStream output = new FileOutputStream("Files/" + filename);

      // Make a byte array to be the length of incoming data stream
      byte[] outputFile = new byte[in.available()];
      in.readFully(outputFile);

      output.write(outputFile);

      output.close();

      // adding file into the filesystem array
      ServerFile newFile = new ServerFile(filename, outputFile);
      serverFileSystem.add(newFile);

    }

    // http://stackoverflow.com/questions/4852531/find-files-in-a-folder-using-javas
    public File fetch(final String filename, String certname, Integer cir) {

      // initialize the arguments if null values are given
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
        list += f.fileName + "\n";

      }

      return list;
    }

    public void uploadCert(String name, DataInputStream cert) throws IOException {

      FileOutputStream output = new FileOutputStream("Cert/" + name);

      byte[] outputBytes = new byte[cert.available()];

      cert.readFully(outputBytes);

      output.write(outputBytes);

      output.close();

    }

    public void vouchFile(String filename, final String cert) throws FileNotFoundException, CertificateException {

      for (ServerFile f : serverFileSystem) {

        // if the ServerFile mathces the name, add the certificate to
        // the arraylist
        if (f.fileName.equals(filename)) {

          // Find the certain certificate in the certificate
          // directories
          File certDir = new File("Cert/");
          File[] matchingCert = certDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {

              return name.equals(cert);
            }
          });

          // Once the certificate has been found, append to the
          // ServerFile certificate arraylist
          // https://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.htmls

          FileInputStream inputStream = new FileInputStream(matchingCert[0]);

          CertificateFactory certFac = CertificateFactory.getInstance("X.509");

          // Once the stream has been converted to a certificate,
          // append it to the ServerFile
          X509Certificate genCert = (X509Certificate) certFac.generateCertificate(inputStream);
          f.certificates.add(genCert);

        }

      }

    }

  }

  public class ServerFile implements Comparable<ServerFile> {

    String fileName;
    byte[] checksum;
    ArrayList<X509Certificate> certificates;

    public ServerFile(String name, byte[] rawFile) throws NoSuchAlgorithmException {

      fileName = name;
      checksum = MessageDigest.getInstance("MD5").digest(rawFile);
      certificates = new ArrayList<X509Certificate>();

    }

    @Override
    public int compareTo(ServerFile o) {

      return o.fileName.compareTo(this.fileName);
    }

  }

}
