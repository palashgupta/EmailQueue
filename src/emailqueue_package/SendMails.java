/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emailqueue_package;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author palash
 */

/* NOTE - Most smtp servers do not allow setting the sender address. 
Just changing the "From:" field of our mails won't solve the problem because we
have to log in with a valid username and password in order to be allowed to send mails.
And IF we log in with a valid account it's very likely that Gmail 
(and other public mail providers) will override our setting with the data from 
the registered account. So email will always be appearing to come from the email
account used for authentification.
*/


//this class is resposible for delivering mails
public class SendMails {
    
    private static String host = "smtp.gmail.com";       //smtp server used. Here, i have used google smtp server
    private static String port = "587";                  //smtp port
    //username and password used for authentification
    private static String user = "---";
    private static String password = "---";       
    private static Properties prop = null;             //properties object for the session
    private static Session session = null;              //session object
    Transport transport = null;
    //constructor to set properties for session
    public SendMails() {
        if(SendMails.prop == null) {                 
            SendMails.prop = new Properties();
            SendMails.prop.put("mail.smtp.auth", "true");
            SendMails.prop.put("mail.smtp.starttls.enable", "true");
            SendMails.prop.put("mail.smtp.host", host);
            SendMails.prop.put("mail.smtp.port", port);
            SendMails.prop.put("mail.smtp.ssl.trust", host);
        }
        
        //creates a new session and transport object
        if(SendMails.session == null) {
            session = Session.getInstance(prop);
            try {
                transport = session.getTransport("smtp");
                transport.connect(host, user, password);
            } catch (NoSuchProviderException ex) {
                Logger.getLogger(SendMails.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MessagingException ex) {
                Logger.getLogger(SendMails.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
    }
    
    //methods to set smtp server properties
    public static void setHost(String str) {
        host = str;
    }
    
    public static String getHost() {
        return host;
    }
    
    public static void setPort(String str) {
        port = str;
    }
    
    public static void setUser(String str) {
        user = str;
    }
    
    public static String getUser() {
        return user;
    }

    public static void setPassword(String str) {
        password = str;
    }
    
    //send the mail
    public void sendMail(String from_address, String to_address, EmailRecord[] mailqueue, Manager cur_thread) throws MessagingException {
        try {
            Message message = new MimeMessage(session);             //message object
            message.setFrom(new InternetAddress(from_address));      //setting from_address
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to_address)); //setting recipients
            message.setReplyTo(new javax.mail.Address[] {
                                    new javax.mail.internet.InternetAddress(from_address)
                               });
            message.setHeader("On-Behalf-Of", from_address);
            for (EmailRecord mailqueue1 : mailqueue) {              //sending mails one by one for given (from_address,to_address) pairs
                message.setSubject(mailqueue1.subject);
                message.setText("From: "+ from_address + "\n\n" + mailqueue1.body);
                transport.sendMessage(message, message.getAllRecipients());
                cur_thread.id_list.add(mailqueue1.id);
                System.out.println(cur_thread.name + ": Sent email from " + from_address + " to " + to_address);
            }
        } catch (MessagingException e) {
            throw e;
        }
    }
}
