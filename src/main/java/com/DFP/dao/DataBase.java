package com.DFP.dao;

import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;

@Repository
public class DataBase {
    public int executeDMLQuery(String DBURL, String DBName, String DBUser,String DBPass,String query){
        int rowsEffected=0;
        Statement stmt =null;
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con= DriverManager.getConnection(
                    "jdbc:mysql://"+DBURL+"/"+DBName,DBUser,DBPass);
            stmt=con.createStatement();
            rowsEffected = stmt.executeUpdate(query);


        }catch (Exception e){System.out.println(e);}finally {
            try{
                if(stmt!=null)
                    stmt.close();
            }catch(SQLException se2){
            }// nothing we can do
            try{
                if(con!=null)
                    con.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }
        return rowsEffected;
    }
    public ArrayList <ArrayList<String> > executeSelectQuery(String DBURL, String DBName, String DBUser,String DBPass,String query){
        ArrayList <ArrayList<String> > result = new ArrayList<ArrayList<String>>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con= DriverManager.getConnection(
                    "jdbc:mysql://"+DBURL+"/"+DBName,DBUser,DBPass);

            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery(query);

            ResultSetMetaData metadata = rs.getMetaData();
            int columnCount = metadata.getColumnCount();

            while(rs.next()){
                ArrayList<String> row = new ArrayList<String>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getString(i));
                }
                result.add(row);
            }

            return result;
        }catch (Exception e){System.out.println(e);}

        return null;
    }
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
//    public void displayResult(ResultSet rs)  {
//        try{
//            ResultSetMetaData metadata = rs.getMetaData();
//            int columnCount = metadata.getColumnCount();
//
//            while(rs.next()){
//                String row = "";
//                for (int i = 1; i <= columnCount; i++) {
//                    row += rs.getString(i) + ", ";
//                }
//                System.out.println(row);
//            }
//
//        }  catch (SQLException e){
//            System.out.println(e);
//        }
//
//    }
}
