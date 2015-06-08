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
public class EmailRecord {
    
    public String from_address, to_address, subject, body;
    public int id;
    
    public EmailRecord() {
        
    }
    public EmailRecord(String from, String to, String subject, String body) {
        this.from_address = from;
        this.to_address = to;
        this.subject = subject;
        this.body = body;
    }
    
    //saves a record in the database
    public int insert_record() {
        DatabaseInterface email = new DatabaseInterface();
        int rows_inserted = email.insertMail(this.from_address, this.to_address, this.subject, this.body);      
        if(rows_inserted != -1) {
            return 1;
        } 
        return 0;
    }
}
