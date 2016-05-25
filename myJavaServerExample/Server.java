
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.util.ArrayList;

public class Server {

    private static ArrayList<ServerFile> serverFileSystem;

    public Server() {

    }

    public static void main(String[] arstring) {
        try {
            SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslserversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(2323);
            SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();

            InputStream inputstream = sslsocket.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            OutputStream outputstream = sslsocket.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
            BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

            bufferedwriter.write("hahahahaha");

            sslsocket.close();

        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public class FileSystem {

        // public final int systemSize = 100;
        public FileSystem() {

            serverFileSystem = new ArrayList<ServerFile>();

        }

        public void add(String file, int size, String cert) {
            ServerFile newFile = new ServerFile(file, size, cert);
            serverFileSystem.add(newFile);

        }

        //http://stackoverflow.com/questions/4852531/find-files-in-a-folder-using-javas
        public File fetch(String name, int cir) {
            final String[] spilttedString = name.split(".", 2);

            File f = new File("Files/");

            File[] matchingFiles = f.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith(spilttedString[0]) && name.endsWith(spilttedString[1]);
                }
            });

            return matchingFiles[0];

        }

        public File[] listFiles(int cir) {

            File f = new File("Files/");

            return f.listFiles();
        }

    }

    public class ServerFile {

        String fileName;
        int checksum;
        ArrayList<String> certificates;

        public ServerFile(String name, int sum, String cert) {

            fileName = name;
            checksum = sum;

            certificates = new ArrayList();

            certificates.add(cert);

        }
        
        
        
        

    }

}
