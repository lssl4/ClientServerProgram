
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import java.io.*;
import java.security.cert.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

//http://stilius.net/java/java_ssl.php
public class Server {

  private static OurFileSystem filesys;

  public Server() {
    try {
      filesys = new OurFileSystem();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void main(String[] arstring) {
    try {
      SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory
          .getDefault();
      SSLServerSocket sslserversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(2323);
      SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();

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

      // bufferedwriter.write("hahahahaha");

      /*
       * String string = null; while ((string = bufferedreader.readLine())
       * != null) { System.out.println(string); System.out.flush(); }
       */

      // Doing switch operations from incoming data stream

      // This block of code parses through the incoming command line
      // stream from the client
      
      
      String clientCom;
      while ((clientCom = in.readUTF()) != null) {

        // I split the inputstream
        String[] splitClientCom = clientCom.split(" ");

        switch (splitClientCom[0]) {
        
        //List all the files in directory
        case "-l":

          filesys.listFiles();

          break;

        //Add a new file  
        case "-a":
          
          
          filesys.add(splitClientCom[1], in);
          
          
          break;
        
          
        //Upload a certificate  
        case "-u":
          
          
          filesys.uploadCert(splitClientCom[1], in);
          
          
          break;
          
        case "-v":
          
          
          
          filesys.vouchFile(splitClientCom[1], splitClientCom[2]);
          
          break;
        
        default:
          break;
        }

      }
      
      //Closes data input stream
      in.close();

      sslsocket.close();

    } catch (Exception exception) {
      exception.printStackTrace();
    }

  }

  public class OurFileSystem {

    private ArrayList<ServerFile> serverFileSystem = new ArrayList<ServerFile>();

    public OurFileSystem() throws IOException {

      serverFileSystem = new ArrayList<ServerFile>();

    }

    public void add(String filename, DataInputStream in) throws IOException, NoSuchAlgorithmException {
      
      //next upcoming stream should be the data file
      FileOutputStream output = new FileOutputStream("Files/" + filename);
      
      //Make a byte array to be the length of incoming data stream
      byte[] outputFile = new byte[in.available()];
      in.readFully(outputFile);
      
      output.write(outputFile);
      
      output.close();
      
      
      //adding file into the filesystem array
      ServerFile newFile = new ServerFile(filename, outputFile);
      serverFileSystem.add(newFile);
      
      

    }

    // http://stackoverflow.com/questions/4852531/find-files-in-a-folder-using-javas
    public File fetch(String name, String n, Integer cir) {
      n = n != null ? n : "";
      cir = cir != null ? cir : 0;

      final String[] spilttedString = name.split(".", 2);

      File f = new File("Files/");

      File[] matchingFiles = f.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.startsWith(spilttedString[0]) && name.endsWith(spilttedString[1]);
        }
      });

      // find file in the serverFileSystem
      for (ServerFile obj : serverFileSystem) {
        ArrayList<X509Certificate> listOfCerts = obj.certificates;

        if (obj.fileName.equals(name) && obj.certificates.size() >= cir) {

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
        
        //if the ServerFile mathces the name, add the certificate to the arraylist
        if (f.fileName.equals(filename)) {
          
          //Find the certain certificate in the certificate directories
          File certDir = new File("Cert/");
          File[] matchingCert = certDir.listFiles(new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
              
              return name.equals(cert);
            }
          });
          
          //Once the certificate has been found, append to the ServerFile certificate arraylist
          //https://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.htmls
          
          FileInputStream inputStream = new FileInputStream(matchingCert[0]);
          
          
          CertificateFactory certFac = CertificateFactory.getInstance("X.509");
          
          //Once the stream has been converted to a certificate, append it to the ServerFile
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
