package com.DFP.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DataBase {

     public String getUser(String DBURL,String DBName,String DBUser, String DBPass,String query){
         try{
             Class.forName("com.mysql.cj.jdbc.Driver");
             Connection con= DriverManager.getConnection(
                     "jdbc:mysql://"+DBURL+"/"+DBName,DBUser,DBPass);

             Statement stmt=con.createStatement();
             ResultSet rs=stmt.executeQuery(query);
             while(rs.next())
                 System.out.println(rs.getInt(1)+"  "+rs.getString(2)+"  "+rs.getString(3));
             con.close();
         }catch(Exception e){ System.out.println(e);}

         return null;
     }
}
