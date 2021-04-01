package Server;

import Message.Message;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import Message.Message;
import Message.RequestMessage;
import Message.ErrorMessage;
import Message.CommitMessage;
import Message.ReplyMessage;

public class ServerThread extends Thread{
    private String[] serverPath ={"MServer//","Server1//","Server2//","Server3//","Server4//","Server5//"};
    private  int serverID ;
    private String path = null;
    Socket socket = null;
    private List<String> chunks;

    public ServerThread(Socket s,int serverId){
        this.socket = s;
        this.serverID = serverId;
    }


    public void create(RequestMessage message){
        String chunk_name = message.getFilename();
        try{
            File file = new File(path+chunk_name);
            if(!file.exists()){
                file.createNewFile();
                System.out.println("Create a new chunk"+chunk_name);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String Read(RequestMessage message) throws FileNotFoundException {
        String chunkname=message.getFilename();
        StringBuffer sb = new StringBuffer();
        long offset = message.getOffset();
        try (RandomAccessFile raf = new RandomAccessFile(path + chunkname, "r")) {
            if(raf.length() == 0) {
                return "Empty File";
            }
            long end = ThreadLocalRandom.current().nextLong(offset, raf.length());
            int len = (int) (end - offset);
            byte[] bytes = new byte[len];
            raf.readFully(bytes);
            sb.append(new String(bytes));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();

    }
    public String Write(RequestMessage message) throws IOException {
        RandomAccessFile raf = null;
        System.out.println("Writing");
        try {
            raf = new RandomAccessFile(path+message.getFilename(), "rw");
            System.out.println(" Server is Writing "+path+message.getFilename());
            long len = raf.length();
            raf.seek(len);
            raf.writeBytes(message.getId()+"qwertyuiop\n");
            return "Writing Succeed";
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "Writing Failed";


    }

    public void run(){
        try {
            path = serverPath[serverID];
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            //read object from client
            Message msgIn = (Message) ois.readObject();
            String content;
            Message msgOut ;
            System.out.println("received an message:"+msgIn.getType());
            if (msgIn!=null){
                switch (msgIn.getType()){
                    case "CREATE":
                        RequestMessage requestMessage =(RequestMessage) msgIn;
                        create(requestMessage);
                        break;
                    case "READ":
                        System.out.println("READ operation");
                        RequestMessage requestMessage1 =(RequestMessage) msgIn;
                        content=Read(requestMessage1);
                        oos.writeObject(new ReplyMessage("READContent",this.serverID,content));
                        oos.flush();
                        System.out.println(content);
                        break;
                    case "APPEND" :
                        RequestMessage requestMessage2 =(RequestMessage)msgIn;
                        content=Write(requestMessage2);
                        oos.writeObject(new ReplyMessage("APPENDInfo",this.serverID,content));
                        oos.flush();
                        break;
                    default:
                        break;

                }

            }

            ois.close();
            oos.close();
            socket.close();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
