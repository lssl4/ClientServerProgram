
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.util.*;

public class Server {

    private static ArrayList<ServerFile> serverFileSystem;
    protected SSLSocket sslsocket;

    public Server() {

    }

    public static void main(String[] arstring) {
        try {
            SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslserversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(2323);
            SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();

            /*InputStream inputstream = sslsocket.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);*/

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
                
                
                //Also outputing the data
                InputStream inputstream = sslsocket.getInputStream();
                
                

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

        public ArrayList<String> listFiles(int cir) {

            ArrayList<String> list = new ArrayList<String>();
            
            Collections.sort(serverFileSystem);
            
            for( ServerFile f: serverFileSystem){
            	
            	if(f.certificates.size() >=cir){
            		
            		list.add(f.fileName);
            		
            	}
            }
            
            return list;
        }

    }

    public class ServerFile implements Comparable{

        String fileName;
        int checksum;
        ArrayList<String> certificates;

        public ServerFile(String name, int sum, String cert) {

            fileName = name;
            checksum = sum;

            certificates = new ArrayList();

            certificates.add(cert);

        }

        
        public int compareTo(ServerFile o) {
            try{
            return o.certificates.size() - this.certificates.size();
            }catch(UnsupportedOperationException os){
            
            	throw new UnsupportedOperationException(os);
            }
        
        }

        
        
        
        

    }

}
