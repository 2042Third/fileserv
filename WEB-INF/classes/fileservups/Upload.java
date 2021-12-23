package fileservups;
import fileservups.ServiceType;
import storage.FileTracking;
import javaserver.chat.*;

import java.util.Calendar;
import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.io.InputStream;
import java.io.BufferedReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletOutputStream;
import java.io.InputStreamReader;
import jakarta.servlet.http.Part;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
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
    private FileTracking ftrack = new FileTracking();
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
            HttpServletResponse response) throws ServletException, IOException{


        System.out.println("[INCOMING FILE] \n");
        ftrack.login(
            "jdbc:mysql://127.0.0.1:3306/pdmfile",
            "pdm-files",
            "93eab1de880e03cc3f04f505298b3ab47034b784e3dd969d1f3d08f6978f0bb9"
            );
        ServiceType serv_type = ServiceType.NON;
        String appPath = request.getServletContext().getRealPath("");
        String savePath = appPath + File.separator + SAVE_DIR;
         
        File fileSaveDir = new File(savePath);
        if (!fileSaveDir.exists()) {
            fileSaveDir.mkdir();
        }
        String fileName = "";
        System.out.println("[INCOMING] \n");
        for (Part part : request.getParts()) {
            String tmppart = part.getName();
            System.out.println("Part: "+tmppart);
            //get the purpose of the connection, and resolve 
            switch(tmppart){
                case "listings":
                    System.out.println("[FileListing] received.");
                    serv_type=ServiceType.FILELISTING;
                    continue;
                case "file":
                    String fileNameTmp = extractFileName(part);
                    System.out.println("[FileName] Read "+fileNameTmp);
                    fileName = fileNameTmp;
                    serv_type=ServiceType.FILETRANSFER;
                    continue;
                case "chatreg":
                    serv_type=ServiceType.CHATREG;
                    System.out.println("[Chat Registration] set up: \n" + get_all_headers(part));
                    continue;
                case "user_name":
                    savePath = set_up_user_path(part,savePath);
                    System.out.println("[User Name] set up ");
                    continue;
                default:
                    System.out.println("[Request] Unkown request: "+tmppart);
            }
        }
        switch(serv_type){
            case CHATREG:
                System.out.println("[CHATREG] Chat Client IP: "+ request.getRemoteAddr());
                // Need to log the url/ip, and create socket connection and send to it
                chat_resolve(request, response);
                break;
            case FILETRANSFER:
                ftrack.track_write(savePath + File.separator + new File(fileName).getName(), "FILETRANSFER");
                save_the_file(fileName,savePath, request.getPart("file"));
                request.setAttribute("message", "File has been uploaded.");
                getServletContext().getRequestDispatcher("/index.jsp").forward(
                        request, response);
                break;
            case FILELISTING:
                ArrayList<String> files=getAllFiles(request,response);
                System.out.println("\t[FileListing] "+files);
                ServletOutputStream responseout = response.getOutputStream();
                
                String responseoutput = "{\"files\":["+String.join(",",files)+" ]"+
                ",\"ftimes\":{"+String.join(",",getFtime(files))+"}"+
                ",\"fsizes\":{"+String.join(",",getFsize(files))+"}"+
                "}";
                responseout.print(responseoutput);
                break;

        }
        ftrack.logout();
        // Marshall test
        // ChatMarshaller tester = new ChatMarshaller();
        // try{
        //     tester.reader_test();
        // }
        // catch(Exception e){
        //     System.out.println("marshelling failed!!!");
        //     e.printStackTrace();
        // }
    }
    
    /**
     * Return all of the headers in thsi part as a string
     * 
     * */
    private String get_all_headers (Part part){
        String a="";
        for (String b: part.getHeaderNames()){
            a += "\t"+b;
            a += "\n";
        }
        return a;
    }

    /**
     * Handles chats
     * */
    private void chat_resolve (HttpServletRequest request, HttpServletResponse response) {
        try{
            ServletOutputStream responseout = response.getOutputStream();
            responseout.print("{\"tester1\" , \"this is a msg\"}");
        }
        catch(Exception e){
            System.out.println("chat_resolve failure.");
        }
    }

    /**
     * Return all of the headers in thsi part as a string
     * 
     * */
    private String get_all_headers (HttpServletRequest part){
        String a="";
        for (Enumeration<String> e = part.getHeaderNames(); e.hasMoreElements();){
            a += "\t"+e.nextElement();
            a += "\n";
        }
        return a;
    }

    private List<String> getFtime(List<String> a) throws IOException{
        BasicFileAttributes fatr ;
        String ft="";
        ArrayList<String> ftime = new ArrayList<String>();
        final ServletContext servletContext = getServletContext();
        for (String b : a){
            fatr = Files.readAttributes(
                // (new File(servletContext.getRealPath("."+b.split("\"")[1]))).toPath()
                (new File(servletContext.getRealPath("."+b.split("\"")[1]))).toPath()
                , BasicFileAttributes.class);

            System.out.println("[file listing] getting file time for "+ b+", gotten \""+fatr.lastModifiedTime().toString().split("\\.")+"\"");
            for (String c : fatr.lastModifiedTime().toString().split("\\.")){
                System.out.print(c+",");
            }
            ft="\""+(fatr.lastModifiedTime().toString()).split("\\.")[0].replace("T"," ")+"\"";
            ftime.add(b+":"+ft);
        }
        return ftime;

    }
    private List<String> getFsize(List<String> a) throws IOException{
        BasicFileAttributes fatr ;
        String ft="";
        ArrayList<String> ftime = new ArrayList<String>();
        final ServletContext servletContext = getServletContext();
        for (String b : a){
            fatr = Files.readAttributes(
                (new File(servletContext.getRealPath("."+b.split("\"")[1]))).toPath()
                , BasicFileAttributes.class);
            ft="\""+fatr.size()+"\"";
            ftime.add(b+":"+ft);
        }
        return ftime;

    }
    private Boolean track_write(String a, String b) {
        BasicFileAttributes fatr ;
        String ft="";
        String cur_time = new Date().getTime().toString();
        File f = new File(servletContext.getRealPath("."+a.replace("\"","")));
        final ServletContext servletContext = getServletContext();
        // File b= File(servletContext.getRealPath("."+a.replace("\"","")));
        fatr = Files.readAttributes(
                f.toPath()
                , BasicFileAttributes.class);
        String action = f.exists()? "overwrite" : "create";
        ftrack.exc_emplace_5(
            "insert into fileservtracks(filename, action, time, size, status) values (\"?\",\"?\",\"?\",\"?\",\"?\");",
            action,
            cur_time,
            fatr.size(),
            b
            );
        return true;

    }

    /**
     * Returns a set of files saved on the server.
     * */
    private ArrayList<String> getAllFiles(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final ServletContext servletContext = getServletContext();
        try {

            Set<String> list_files = servletContext.getResourcePaths("/file-saved");
            ArrayList<String> list_f=new ArrayList<String>();
            for (String i:list_files){
                list_f.add("\""+i+"\"");
            }
            return list_f;

        } catch (Exception e) {
            System.out.println("getAllFiles failed");
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