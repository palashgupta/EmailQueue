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

import java.io.*;
import java.util.ArrayList;

//Driver class
public class EmailQueue {
    
    public static int num_of_threads = 3;                   //no. of threads for execution
    public static ArrayList<String[]> address_pairs;        //stores the distinct from_address,to_address mail pairs from the database
    
    //main method
    public static void main(String[] args) throws IOException{
        
        show_configuration();                                //displays current configuration
        change_configuration();                              //function call if u want to change configuration
        InputStreamReader istream = new InputStreamReader(System.in) ;
        BufferedReader input = new BufferedReader(istream) ;
        OUTER:
        while (true) {
            System.out.print("Do you want to insert some sample records (Y/N): ");
            String option = input.readLine();
            switch (option) {
                case "Y":
                    insert_sample_data();
                    break OUTER;
                case "N":
                    break OUTER;
                default:
                    System.out.println("Invalid option.Enter again");
                    break;
            }
        }
        
        address_pairs = DistinctMailPairs();     //gets the distinct (from_email_address, to_email_address) pairs from DB
        
        if(address_pairs != null ) {            
            Manager t;                              //manager object extends threads class
            for(int i=1; i<=num_of_threads; i++) {
                t = new Manager(i);             //initializing manager object    
                t.start();
            }
        }
    }
    
    //function to display current configuration
    public static void show_configuration() {
        System.out.println("SMTP server used :- "+ SendMails.getHost());
        System.out.println("Email Address used for authentification :- "+ SendMails.getUser());
        System.out.println("Mysql Server host: " + DatabaseInterface.mysql_host);
        System.out.println("Name of the Database :- " + DatabaseInterface.database_name);
    }
    
    //function to change smtp cofiguration
    public static void change_smtp_settings() throws IOException{
        
        InputStreamReader istream = new InputStreamReader(System.in) ;
        BufferedReader input = new BufferedReader(istream) ;
        System.out.print("Enter the SMTP server address: ");
        String temp = input.readLine();
        SendMails.setHost(temp);
        System.out.print("Enter the SMTP server port: ");
        temp = input.readLine();
        SendMails.setPort(temp);
        System.out.print("Enter the email_address used for authentifiction: ");
        temp = input.readLine();
        SendMails.setUser(temp);
        System.out.print("Enter password: ");
        temp = input.readLine();
        SendMails.setPassword(temp);
    }
    //function to change mysql cofiguration
    public static void change_mysql_configuration() throws IOException{
        
        InputStreamReader istream = new InputStreamReader(System.in) ;
        BufferedReader input = new BufferedReader(istream) ;
        System.out.print("Enter MySQL user name: ");
        DatabaseInterface.username = input.readLine();
        System.out.print("Enter MySQL user password: ");
        DatabaseInterface.password = input.readLine();
        System.out.print("Enter MySQL server address: ");
        DatabaseInterface.mysql_server_address = input.readLine();
        System.out.print("Enter MySQL database name: ");
        DatabaseInterface.database_name = input.readLine();
        DatabaseInterface.mysql_host = "jdbc:mysql://"+DatabaseInterface.mysql_server_address+":"+DatabaseInterface.mysql_port+"/";
        DatabaseInterface.database_url = DatabaseInterface.mysql_host + DatabaseInterface.database_name;
    }
    
    //function to change all configurations
    private static void change_configuration() throws IOException {    
        InputStreamReader istream = new InputStreamReader(System.in) ;
        BufferedReader input = new BufferedReader(istream) ;
        String option;
        OUTER:
        while (true) {
            System.out.print("Enter:\n'Yes' if you want to change SMTP / MYSQL / No_of_thread settings.\n"
                    + "'No' if you dont want to change settings.\n"
                    + "'exit' to extt:\n");
            option = input.readLine();
            if (null != option) {
                switch (option) {
                    case "exit":
                        System.exit(0);
                    case "Yes":
                        OUTER_1:            
                        while (true) {                              //block for changing smtp settings
                            System.out.print("Do you want to change sntp server settings (Y/N): ");
                            String option_smtp = input.readLine();
                            switch (option_smtp) {
                                case "Y":
                                    change_smtp_settings();
                                    break OUTER_1;
                                case "N":
                                    break OUTER_1;
                                default:
                                    System.out.println("Invalid option.Enter again");
                                    break;
                            }
                        }
                        OUTER_2:
                        while (true) {                       //block for changing mysql settings
                            System.out.print("Do you want to change mysql server configuration (Y/N): ");
                            String option_mysql = input.readLine();
                            switch (option_mysql) {
                                case "Y":
                                    change_mysql_configuration();
                                    break OUTER_2;
                                case "N":
                                    break OUTER_2;
                                default:
                                    System.out.println("Invalid option.Enter again");
                                    break;
                            }
                        }
                        System.out.print("Enter how many threads you want to create: ");
                        num_of_threads = Integer.parseInt(input.readLine());
                        break OUTER;
                    case "No":
                        break OUTER;
                    default:
                        System.out.println("Invalid option.Enter again");
                        break;
                }
            }
        }
    }
    //gets the distinct <from, to> mail pairs from the database
    public static ArrayList<String[]> DistinctMailPairs() {
        DatabaseInterface obj = new DatabaseInterface();
        ArrayList<String[]> from_to_pairs;
        try {
            from_to_pairs = obj.DistinctToFromPair();     
            return from_to_pairs;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }
    
    //function to insert some sample data
    public static void insert_sample_data() {
        EmailRecord newRow;
        int flag = 0;
        for(int i=0; i<20; i++) {
           
            if (flag==0) {
                newRow = new EmailRecord("abc@gmail.com" + i%3, "xyz@gmail.com" + i%3, "test email"+ (i*3), "Empty body."+(i*3));
                newRow.insert_record();
                flag = 1;
            } else {
                newRow = new EmailRecord("def@gmail.com" + i%3, "abc@gmail.com" + i%3, "test email"+ (i*3), "Empty body"+(i*3));
                newRow.insert_record();
                flag = 0;
            }

        }
        
    }
}
