
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;

//http://stilius.net/java/java_ssl.php
public class Server {

  private static OurFileSystem exa;

  public Server() {
    try {
      exa = new OurFileSystem();
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
        case "-l":

          exa.listFiles();

          break;

        case "-a":

          break;

        case "-u":

          break;

        default:
          break;
        }

      }

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

    public void add(String file) {
      ServerFile newFile = new ServerFile(file);
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
        ArrayList<Certificate> listOfCerts = obj.certificates;

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

    public void vouchFile(String filename, String cert, String subject) {

      for (ServerFile f : serverFileSystem) {
        if (f.fileName.equals(filename)) {

          f.certificates.add(new Certificate(cert, subject));

        }

      }

    }

  }

  public class ServerFile implements Comparable<ServerFile> {

    String fileName;
    MessageDigest checksum;
    ArrayList<Certificate> certificates;

    public ServerFile(String name) {

      fileName = name;
      // checksum = sum;

      certificates = new ArrayList<Certificate>();

    }

    @Override
    public int compareTo(ServerFile o) {

      return o.fileName.compareTo(this.fileName);
    }

  }

  public class Certificate {
    String filename;
    String subjectName;

    public Certificate(String f, String s) {
      filename = f;
      subjectName = s;

    }

  }

}
