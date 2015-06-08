/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emailqueue_package;

/**
 *
 * @author palash
 */
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

//this class interacts with mysql database and retrieves and insert data
public class DatabaseInterface {
    
    public static String jdbc_connector = "com.mysql.jdbc.Driver";       //jdbc driver used for connection to mysql
    public static String database_name = "EmailQueue";
    public static String table_name = "EmailQueue";
    public static String mysql_server_address = "localhost"; // mysql server name
    public static int mysql_port = 3306; //mysql port
    public static String username = "root"; 
    public static String password = "---";
    public static String mysql_host = "jdbc:mysql://localhost:3306/";      //mysql host address
    public static String database_url  = mysql_host + database_name;        //database connection url
    private static Connection conn = null;
    public DatabaseInterface() {
        if(DatabaseInterface.conn == null) {
            int database_exists = 0;
            int table_exists = 0;
            try {
                Class.forName(jdbc_connector);
                DatabaseInterface.conn = DriverManager.getConnection(mysql_host, username, password); //connecting to mysql server

                Statement s = conn.createStatement();
                //create database if it does not exist already
                s.executeUpdate("CREATE DATABASE IF NOT EXISTS " + database_name);
                database_exists = 1;

            } catch(ClassNotFoundException | SQLException e) {
                System.out.println("Error: " + e.getMessage());
            } finally {
                if(database_exists == 1) {     //database exists so we connect to database_url
                    try {
                        conn.close();
                        conn = DriverManager.getConnection(database_url, username, password);
                        
                        if(table_exists == 0) {     //create table if it does not exist already
                            Statement s = conn.createStatement();
                            s.executeUpdate("CREATE TABLE IF NOT EXISTS " + table_name + " ("
                                + "  `id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                                + "  `from_email_address` varchar(100) NOT NULL,"
                                + "  `to_email_address` varchar(100) NOT NULL,"
                                + "  `subject` varchar(200) NOT NULL,"
                                + "  `body` varchar(1000) NOT NULL,"
                                + "  `flag` int(11) NOT NULL DEFAULT '0');");  //to record whether this email has been sent or not.
                            
                            table_exists = 1;
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(DatabaseInterface.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    
    //flags the emails that are sent
    public int setFlag(ArrayList<Integer> ids) throws SQLException {
        if(conn == null) {
            return -1;
        } else {
            int rows_affected = 0; 
            try (Statement st = conn.createStatement()) {
                String changed_ids = "(";
                for(int i=0; i<ids.size()-1; i++) {
                    changed_ids += ids.get(i);
                    changed_ids += ",";
                }
                changed_ids += ids.get(ids.size()-1);
                changed_ids += ")";
                String sql_string = "UPDATE EmailQueue SET flag = 1 WHERE id IN " + changed_ids;
                rows_affected = st.executeUpdate(sql_string);
            } catch (SQLException e) {
                throw e;
            }
            return rows_affected;
        }
    }
    
    //retrieves distinct (from_address,to_address) pairs from the table
    public ArrayList<String[]> DistinctToFromPair() throws Exception{
        if(conn == null) {
            return null;
        } else {
            ArrayList<String[]> results = new ArrayList<>();        //variable for storing distinct to and from address pair.
            ResultSet record_set = null;                            //variable for storing query result     
            Statement st = null;
            try {
                 st = DatabaseInterface.conn.createStatement();
            
                String sql_string = "SELECT DISTINCT from_email_address, to_email_address FROM " + DatabaseInterface.table_name + ";";
                record_set = st.executeQuery(sql_string);
                
                while(record_set.next()) {                          //store only address pair from record_set
                    String[] row = {
                        record_set.getString("from_email_address"),
                        record_set.getString("to_email_address")
                    };
                    results.add(row);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                try{
                    record_set.close();
                    st.close();
                } catch (Exception e) {
                    return null;
                }
            }
            return results;
        }
    }
    
    //Inserts a record in database
    public int insertMail(String from, String to, String subject, String body) {
        if(conn == null) {
            return -1;
        } else {
            Statement st = null;
            try {
                st = conn.createStatement();
            
                String sql_string = "INSERT INTO " + table_name
                        + "(from_email_address, to_email_address, subject, body)"
                        + "VALUES ('"+ from +"', '"+ to +"', '"+ subject + "', '"+ body +"')";
                
                int inserted_rows = st.executeUpdate(sql_string);
                return inserted_rows;
                
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseInterface.class.getName()).log(Level.SEVERE, null, ex);
            }  finally {
                try {
                    st.close();
                } catch(Exception e) {
                    return -1;
                }
            }
        }
        return -1;
    }
    
    //retrieves all the records for the given (from_address,to_address) set
    public ArrayList<String[]> retrieveRecords(String from, String to) throws Exception {
        if(conn == null) {
            return null;
        } else {
            ArrayList<String[]> extracted_rows = new ArrayList<>();     //variable for storing records of a given to,from address pair
            try {
                Statement st;
                ResultSet result;
                st = conn.createStatement();
            
                String sql_string = "SELECT id, from_email_address, to_email_address, subject, body FROM " 
                        + table_name + " WHERE from_email_address = '" 
                        + from + "' AND to_email_address = '" 
                        + to + "' AND flag = 0 ORDER BY id";
                
                result = st.executeQuery(sql_string);
                while(result.next()) {
                    String[] row = new String[5];
                    row[0] = ""+result.getInt("id");
                    row[1] = result.getString("from_email_address");
                    row[2] = result.getString("to_email_address");
                    row[3] = result.getString("subject");
                    row[4] = result.getString("body");
                    extracted_rows.add(row);
                }
                result.close();
                st.close();
            } catch (Exception e) {
                throw e;
            }
            return extracted_rows;
        }
    }
}
