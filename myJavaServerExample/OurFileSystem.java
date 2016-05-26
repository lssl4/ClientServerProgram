import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;







/**
 *
 * @author Uni
 */
public class OurFileSystem {
	
	
	  private static ArrayList<ServerFile> serverFileSystem;
	 

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
    
    
    
    
    public class ServerFile implements Comparable{

        String fileName;
        int checksum;
        ArrayList<String> certificates;

        public ServerFile(String name, int sum) {

            fileName = name;
            checksum = sum;

            certificates = new ArrayList<String>();

            

        }

       

		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			return ((ServerFile) o).certificates.size() - this.certificates.size();
		}


		
    
    }
    
}
