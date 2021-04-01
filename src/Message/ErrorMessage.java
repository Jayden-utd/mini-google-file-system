package Message;

import java.io.Serializable;

public class ErrorMessage extends Message implements Serializable {
    private String content;
    public ErrorMessage(String type,int id,String content){
        super(type,id);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
