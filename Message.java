import java.io.*;
public class Message implements Serializable {
    String time;
    String UID;
    String content;
    public Message(String args1, String args2){
        this.time = java.time.LocalDateTime.now().toString();
        UID = args1;
        content = args2;
        
    }
    @Override
    public String toString() {
    return "[" + time + "] " + UID + ": " + content;
}
}

