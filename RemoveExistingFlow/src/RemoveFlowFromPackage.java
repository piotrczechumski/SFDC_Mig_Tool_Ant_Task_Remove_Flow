import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

public class RemoveFlowFromPackage extends Task{
    private String targetFolderPath; // path to flows folder in retrieved package (from target)
    private String deployFolder; // path to scr folder in deployment package
     /*// Just for testing
    public static void main(String [ ] args){
    	System.out.println("Just test");
    	RemoveFlowFromPackage rfp = new RemoveFlowFromPackage();
    	rfp.targetFolderPath = "C:\\_temp\\testj\\retrieveOutput\\flows";
    	rfp.deployFolder = "C:\\_temp\\testj\\";
    	rfp.execute();    	
    }
    */
    public void setTargetFolderPath (String path){
		targetFolderPath = path;
	}
	public void setDeployFolder (String path){
		deployFolder = path;
	}
	
    public void execute() {
        System.out.println("Start removing existing flows from package.xml");

        if (targetFolderPath == null){
        	throw new BuildException("No targetFolderPath set.");
        }
        
        if (deployFolder == null){
        	throw new BuildException("No deployFolder set.");
        }
        
        Set<String> sFlows = new HashSet<String>();
        
        File file = new File(targetFolderPath);  
        File[] files = file.listFiles();  
        for (File f:files)  
        {  
            System.out.println(f.getName().replace(".flow", ""));
            sFlows.add(f.getName().replace(".flow", ""));
        }
        try {
        	removeFlowFromPackage(sFlows);
        }
        catch (IOException ex){
        	throw new BuildException("Problems with changing package.xml: " + ex.getMessage());        	
        }
        deleteFiles(sFlows);
    }
	
    /*
     * Comment out flows in set from package.xml
     */
	private void removeFlowFromPackage(Set<String> sFlows) throws IOException{
		Path path = Paths.get(deployFolder + "package.xml");
		Charset charset = StandardCharsets.UTF_8;

		String content = new String(Files.readAllBytes(path), charset);
		
		for(String strFlowName : sFlows){
			String strLine = "<members>" + strFlowName + "</members>";
			content = content.replaceAll(strLine, "<!--" +  strLine + "-->");
		}
		Files.write(path, content.getBytes(charset));
	}
	
	/*
	 * Delete flow file from given set.
	 */
	private void deleteFiles(Set<String> sFlows){
		for(String strFlowName : sFlows){
			Path path = Paths.get(deployFolder + "flows/" + strFlowName + ".flow");
			deleteFile(path);
		}
	}
	/*
	 * Delete file in given path
	 */
	private void deleteFile(Path path){
		try {
		    Files.delete(path);
		} catch (NoSuchFileException x) {
		    System.err.format("%s: no such" + " file or directory%n", path);
		} catch (DirectoryNotEmptyException x) {
		    System.err.format("%s not empty%n", path);
		} catch (IOException x) {
		    // File permission problems are caught here.
		    System.err.println(x);
		}
	}

}
