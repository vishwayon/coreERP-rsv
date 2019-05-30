/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ReportServer;

import java.io.File;

/**
 *
 * @author girish
 */
public class SessionFolderManager {
    
    private SessionFolderManager() {
        // Restrict instance creation
    }
    
    public static String getRenderFolder(String session_id, String viewer_id) {
        return "/reportcache/" + session_id + "/" + viewer_id;
    }

    
    public static File getFolder(String session_id, String viewer_id) {
        // Create the folder
        String path = ReportGenerator.servletPath + "/reportcache/" + session_id;
        File sessionPath = new File(path);
        if(!sessionPath.exists()) {
            sessionPath.mkdir();
            sessionPath.setReadable(true, false);
            sessionPath.setWritable(true, false);
            sessionPath.setExecutable(true, false);
        }
        File viewerPath = new File(path + "/" + viewer_id);
        if(viewerPath.exists()) {
            for(File file: viewerPath.listFiles()) {
                file.delete();
            }
            viewerPath.delete();
        }
        // always create this path for each request
        viewerPath.mkdir();
        viewerPath.setReadable(true, false);
        viewerPath.setWritable(true, false);
        viewerPath.setExecutable(true, false);
        return viewerPath;
    }
    
    public void cleanUp(String session_id) {
        // Find the folder and cleanup
        String path = ReportGenerator.servletPath + "/reportcache/" + session_id;
        File sessionPath = new File(path);
        if(sessionPath.exists()) {
            for(File viewerPath: sessionPath.listFiles()) {
                for(File file: viewerPath.listFiles()) {
                    file.delete();
                }
                viewerPath.delete();
            }
            sessionPath.delete();
        }
    }
    
    public static void makeReadable(File outFile) {
        outFile.setReadable(true, false);
        File imagePath = new File(outFile.getAbsoluteFile()+"_files");
        if(imagePath.exists()) {
            imagePath.setReadable(true, false);
            imagePath.setExecutable(true, false);
            File[] images = imagePath.listFiles();
            for(File image: images) {
                makeReadable(image);
            }
        }
    }
}
