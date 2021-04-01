package MetaServer;

import Message.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class messageHandler extends Thread {
    Socket socket;
    ObjectInputStream ois;
    MServer mServer;
    public messageHandler(Socket socket, MServer s) throws IOException {
        this.socket=socket;
        this.ois = new ObjectInputStream(socket.getInputStream());
        this.mServer = s;



    }

    public void run(){
        try {
            Message msgIn;
            while ((msgIn = (Message) ois.readObject()) != null){
                if(!msgIn.getType().equals("HEARTBEAT")) {
                    System.out.println("I have received a " + msgIn.getType() + " message from Server_" + msgIn.getId());
                }
                mServer.classifyMessage(msgIn,new ObjectOutputStream(this.socket.getOutputStream()));

            }
        } catch (IOException e1 ) {
            //e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
