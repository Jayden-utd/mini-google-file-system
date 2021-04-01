package Message;

import java.io.Serializable;
import java.util.Collections;

public class CommitMessage extends Message implements Serializable {
    private String chunkname;
    public CommitMessage(String type, int id, String chunkname){
        super(type,id);
        this.chunkname=chunkname;
    }

    public String getChunkname() {
        return chunkname;
    }

    public void setChunkname(String chunkname) {
        this.chunkname = chunkname;
    }
}
