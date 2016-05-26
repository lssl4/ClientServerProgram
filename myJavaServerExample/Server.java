
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;



import java.io.*;
import java.util.*;


//http://stilius.net/java/java_ssl.php
public class Server {


    
  private  OurFileSystem exa;
  
  
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
            SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslserversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(8765);
            SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();

            InputStream inputstream = sslsocket.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            OutputStream outputstream = sslsocket.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
            BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);
 
           //bufferedwriter.write("hahahahaha");
           
            String string = null;
            while ((string = bufferedreader.readLine()) != null) {
                System.out.println(string);
                System.out.flush();
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

        public void add(String file, int sum) {
            ServerFile newFile = new ServerFile(file, sum);
            serverFileSystem.add(newFile);
            
            
            //Also outputting the data
           // InputStream inputstream = sslsocket.getInputStream();
            
            

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
      public class ServerFile implements Comparable<ServerFile>{

          String fileName;
          int checksum;
          ArrayList<String> certificates;

          public ServerFile(String name, int sum) {

              fileName = name;
              checksum = sum;

              certificates = new ArrayList<String>();

              

          }

         

    


    @Override
    public int compareTo(ServerFile o) {
      // TODO Auto-generated method stub
      return o.certificates.size() - this.certificates.size();
    }


      
      
      }

}
