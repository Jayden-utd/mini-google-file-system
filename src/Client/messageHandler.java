package Client;

import Message.Message;

import java.io.IOException;
import java.io.ObjectInputStream;

public class messageHandler extends Thread {
    ObjectInputStream ois;
    Client client;
    public messageHandler(ObjectInputStream ois, Client c){
        this.ois = ois;
        this.client = c;
    }

    public void run(){
        try {
            Message msgIn;
            while ((msgIn = (Message) ois.readObject()) != null){
                System.out.println("I have received a "+msgIn.getType()+" message from Server_"+msgIn.getId());
                client.classifyMessage(msgIn);


            }
        } catch (IOException e1 ) {
            //e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
