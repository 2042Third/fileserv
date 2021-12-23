package storage;
import java.util.prefs.*;
import java.net.URLDecoder;
import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Scanner;
import java.io.File;

public class FileTracking {
    private Connection con;
    
    public Boolean login (String url, String user, String a){
        if (con!=null)
            logout();
        try {
            con = DriverManager.getConnection(
                        url, 
                        user, 
                        a
                    );
        }
        catch (Exception e) {
            System.out.println("[file tracker storage] log in failure");
        }
    }

    public Boolean exc_emplace_5 (String query, String a1,String a2,String a3,String a4,String a5){
        try { 
            PreparedStatement stat = con.prepareStatement(query);
            stat.setString(1, a1);
            stat.setString(2, a2);
            stat.setString(3, a3);
            stat.setString(4, a4);
            stat.setString(5, a5);
            stat.executeQuery();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("[file tracker storage] query failure \""+query+"\"");
            return false;
        }
        
        return true;
    }

    private Boolean exc (String a) {
        try{
            PreparedStatement stat = con.prepareStatement(a);
            ResultSet rs = stat.executeQuery();
            System.out.println("[file tracker storage] query excuted \""+a+"\"");
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("[file tracker storage] query failure \""+a+"\"");
            return false;
        }
        
        return true;
    }

    public Boolean logout (){
        if (con != null) {
          try {
            con.close();
          }
          catch (SQLException ex) {
            for (Throwable t : ex)
              System.out.println(t.getMessage());
            System.out.println("[file tracker storage] Closing connection unsuccessful!");
            return false;
          
          }
          return true;
        }
        return false;
    }
}