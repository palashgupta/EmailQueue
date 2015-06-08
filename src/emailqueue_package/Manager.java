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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.mail.MessagingException;

//this class uses threads for reteieving records from database and sending mails
public class Manager extends Thread {
    private final int index;
    private Thread t = null;
    public ArrayList<Integer> id_list;
    public String name;      

    Manager(int num){
        this.index = num;
        this.name = "Thread_"+num;
        id_list = new ArrayList<>();
    }
    
    @Override
    public void start () { 
        System.out.println("Starting " +  name );
        if (t == null) {
            t = new Thread(this, name);
            t.start ();
        }
    }
    
    @Override
    public void run() {
        try {

            //synchronized(EmailQueue.address_pairs) {
            SendMails mail_obj = new SendMails();
            DatabaseInterface sql_obj = new DatabaseInterface();
            for(int i=this.index-1 ; i<EmailQueue.address_pairs.size(); i += EmailQueue.num_of_threads) {
                
                String[] str = EmailQueue.address_pairs.get(i);
                EmailRecord[] mail_data = this.getEmailRecordset(str[0], str[1], sql_obj);   //gets all the records for a given to and from pair
                if(mail_data.length > 0) {
                    String from_address = mail_data[0].from_address;
                    String to_address = mail_data[0].to_address;
                    boolean res = this.send_mail(from_address, to_address, mail_data, mail_obj);       //send all the mails for a pair of to from addresses 
                    if(!res) {          
                        System.out.println(this.name + ": Could not send emails from " + from_address + " to " + to_address);
                    }
                }
            }
            //}
            if (id_list.size()>0){
                try {
                    sql_obj.setFlag(this.id_list);     //flag the emails that were sent
                } catch (SQLException e) {
                    System.out.println(this.name + "Error:" + e.getMessage());
                }
            }
            System.out.println(name + " deleivered " + this.id_list.size() +" emails." );
        } catch (Exception e) {
            System.out.println("Error: " +  name + " - " + e.getMessage());
        }
    }
    
    //send all the mails for a given pair of to and from addresses. returns true if emails are sent successfully
    public boolean send_mail(String from, String to, EmailRecord[] mails, SendMails obj) {

        try {
            obj.sendMail(from, to, mails, this);       // call sendmail to deliver emails over smtp session
            return true;
        } catch (MessagingException ex) {
            System.out.println("Error while sending mails " + ex.getMessage());
            return false;
        }
    }
    
    //function to retrieve all the records for a given to and from address pairs
    public EmailRecord[] getEmailRecordset(String from, String to, DatabaseInterface obj) {
        ArrayList<String[]> record_set;
        try {
            record_set = obj.retrieveRecords(from, to);   //retrieves all the records for a given to and from address pairs
            
            EmailRecord[] mail_data = new EmailRecord[record_set.size()];   //array for storing all the details of records fetched from database
            
                Iterator<String[]> iterator = record_set.iterator();
                int i=0;
                while (iterator.hasNext()){                 //storing results in mail_data variable
                    String[] str = iterator.next();
                    mail_data[i] = new EmailRecord();
                    mail_data[i].id = Integer.parseInt(str[0]);
                    mail_data[i].from_address = str[1];
                    mail_data[i].to_address = str[2];
                    mail_data[i].subject = str[3];
                    mail_data[i].body = str[4];
                    i++;
                }
           
            return mail_data;
        } catch (Exception e) {
            System.out.println("Error: could not fetch records: " + e.getMessage());
            return null;
        }

    }
}
