package Message;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

public class HeartBeatMessage extends Message implements Serializable {
    private List<String> chunks;
    private Timestamp timestamp;
    public HeartBeatMessage(String type,int id,List<String> chunks,Timestamp timestamp){
        super(type,id);
        this.chunks=chunks;
        this.timestamp=timestamp;

    }

    public List<String> getChunks() {
        return chunks;
    }

    public void setChunks(List<String> chunks) {
        this.chunks = chunks;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
