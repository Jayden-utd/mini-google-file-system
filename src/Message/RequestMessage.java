package Message;

import java.io.Serializable;

public class RequestMessage extends Message implements Serializable {
    private String filename ;
    private long offset;
    private String content;
    public RequestMessage(String type,int id,String filename,long offset){
        super(type,id);
        this.filename=filename;
        this.offset=offset;
    }
    public RequestMessage(String type,int id,String filename){
        super(type,id);
        this.filename=filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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
