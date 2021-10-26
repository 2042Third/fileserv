package fileservups;
import fileservups.*;
import java.io.File;
import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.io.InputStream;
import java.io.BufferedReader;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStreamReader;
import javax.servlet.http.Part;
@WebServlet("/Upload")
@MultipartConfig(fileSizeThreshold= 1024 * 1024 * 1, // 1MB
                 maxFileSize=-1L,      // any
                 maxRequestSize=-1L)   // any
public class Upload extends HttpServlet {
    /**
     * Name of the directory where uploaded files will be saved, relative to
     * the web application directory.
     */
    private static final String SAVE_DIR = "file-saved";

    public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
      Map<String, String> query_pairs = new LinkedHashMap<String, String>();
      String[] pairs = query.split("&");
      for (String pair : pairs) {
        System.out.println(pair);
          int idx = pair.indexOf("=");
          query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
      }
      return query_pairs;
    } 
    /**
     * handles file upload
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        System.out.println("#############FILE###############");
        ServiceType serv_type = ServiceType.NON;
        String appPath = request.getServletContext().getRealPath("");
        String savePath = appPath + File.separator + SAVE_DIR;
         
        File fileSaveDir = new File(savePath);
        if (!fileSaveDir.exists()) {
            fileSaveDir.mkdir();
        }
        String fileName = "";
        for (Part part : request.getParts()) {
            System.out.println("Part: "+part.getName());
            //get the purpose of the connection, and resolve
            switch(part.getName()){
                case "file":
                    String fileNameTmp = extractFileName(part);
                    System.out.println("[FileName] Read "+fileNameTmp);
                    fileName = fileNameTmp;
                    serv_type=ServiceType.FILETRANSFER;
                    continue;
                case "user_name":
                    savePath = set_up_user_path(part,savePath);
                    continue;
            }
        }
        switch(serv_type){
            case FILETRANSFER:
                save_the_file(fileName,savePath, request.getPart("file"));
                request.setAttribute("message", "File has been uploaded.");
                getServletContext().getRequestDispatcher("/index.jsp").forward(
                        request, response);
                break;
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        listAllFiles(request, response);
    }

    private void listAllFiles(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final ServletContext servletContext = Parameters.getServletContext();
        try {

            List<String> list_files = servletContext.getResourcePaths("/file-saved");
            request.setAttribute("list_files", list_files);

            RequestDispatcher dispatcher = request.getRequestDispatcher("download");
            dispatcher.forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
    /**
     *  Writes the file
     * 
     * */
    private void save_the_file(String fileName,String savePath, Part part )throws IOException{
        if(fileName!=""){
            fileName = new File(fileName).getName();
            part.write(savePath + File.separator + fileName);
        }
    }

    /**
     * Get the file path the file to be saved
     * */
    private String set_up_user_path(Part part,String savePath){
        String userName = extractUserName(part); 
        try{
            userName = read_user_name(part);
            if(!userName.equals("")){
                savePath = savePath + File.separator  + userName; // Checks if the user exists. Make a folder for the user if it doesn't
                File fileSaveDir = new File(savePath);
                if (!fileSaveDir.exists()) {
                    fileSaveDir.mkdir();
                    System.out.println("\t[Account Access]New user, access account for \""+userName+"\", saving in process...");
                }
                else {
                    System.out.println("\t[Account Access]Access account for \""+userName+"\", saving in process...");
                }
            }
        }
        catch(Exception e){
            System.out.println("\t[Account Access]get user name failure!");
        }
        return savePath;
    }

    /**
     * Gets the user name of the packet
     * */
    private String read_user_name(Part part)throws IOException{
        String user_str = "";
        if (!part.getName().equals("user_name")) return user_str;
        InputStream istream = part.getInputStream();
        int i;
        while((i = istream.read())!=-1) {
            user_str = user_str+(char)i;
        }
        return user_str;
    }

    /**
     * reads the type
     * */
    private String read_serv_type(Part part)throws IOException{
        String service = "";
        if (!part.getName().equals("serv_type")) return service;
        InputStream istream = part.getInputStream();
        int i;
        while((i = istream.read())!=-1) {
            service = service+(char)i;
        }
        return service;
    }

    /**
     * Extracts file name from HTTP header content-disposition
     */
    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        // System.out.println(contentDisp);
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                // System.out.println(s);
                return s.substring(s.indexOf("=") + 2, s.length()-1);
            }
        }
        return "";
    }
    /**
     * Extracts file name from HTTP header content-disposition
     */
    private String extractUserName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            // System.out.println(s);
            if (s.trim().startsWith("user_name")) {
                return s.substring(s.indexOf("=") + 2, s.length()-1);
            }
        }
        return "";
    }
}