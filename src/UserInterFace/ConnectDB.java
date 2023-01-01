/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UserInterFace;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author vyyy0
 */
public class ConnectDB {
    private Connection conn;
        ConnectDB() {};
        public Connection createConn(){
        try{
         Class.forName("com.mysql.jdbc.Driver");
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_banhang?useSSL=false","root","123456");
            if (conn == null)
                System.out.println("Connect Failed");
            else
                System.out.println("Connect Successful");
        } catch(Exception e){
            e.printStackTrace();
        }
        return conn;
    }
}
