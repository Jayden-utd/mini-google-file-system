package Message;


import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class Message implements Serializable {
    private String type;
    private int id;

    public Message(String type,int id){
        this.type = type;
        this.id = id;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


}
