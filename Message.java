import java.io.*;
public class Message implements Serializable {
    String time;
    String Username;
    String content;
    String TargetUID;
    
    public Message(String args1, String args2){
        this.time = java.time.LocalDateTime.now().toString();
        Username = args1;
        content = args2;
        TargetUID = null;
        
    }
    @Override
    public String toString() {
    return "[" + time + "] " + Username + ": " + content;
}
}

