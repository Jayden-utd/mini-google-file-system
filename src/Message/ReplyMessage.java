package Message;

import java.io.Serializable;
import java.util.List;

public class ReplyMessage extends Message implements Serializable {
    private List<Integer> chosenServers;
    private String chunkname;
    private long offset;
    private String content;
    public ReplyMessage(String type,int id,List<Integer> servers,String chunkname,long offset){
        super(type,id);
        this.chosenServers = servers;
        this.chunkname = chunkname;
        this.offset =offset;
    }
    //reply append, no offset
    public ReplyMessage(String type,int id,List<Integer> servers,String chunkname){
        super(type,id);
        this.chosenServers = servers;
        this.chunkname = chunkname;
    }
    //reply read return content
    public ReplyMessage(String type,int id,String content){
        super(type, id);
        this.content = content;
    }

    public List<Integer> getChosenServers() {
        return chosenServers;
    }

    public void setChosenServers(List<Integer> chosenServers) {
        this.chosenServers = chosenServers;
    }

    public String getChunkname() {
        return chunkname;
    }

    public void setChunkname(String chunkname) {
        this.chunkname = chunkname;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
