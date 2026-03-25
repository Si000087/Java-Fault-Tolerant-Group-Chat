import java.io.*;
public class Message implements Serializable {
    String time;
    String Username;
    String content;
    //generic message layout for defualt broadcast messaging is args1 and args2, will be adding a new argument for direct messaging.
    String TargetUID;
    
    public Message(String args1, String args2){
        this.time = java.time.LocalDateTime.now().toString();
        Username = args1;
        content = args2;
        TargetUID = args2;
        
    }
    @Override
    public String toString() {
    return "[" + time + "] " + Username + ": " + content;
}
}

