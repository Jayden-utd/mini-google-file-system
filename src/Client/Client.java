package Client;

import Message.Message;
import Message.RequestMessage;
import Message.ErrorMessage;
import Message.CommitMessage;
import Message.ReplyMessage;
import com.sun.deploy.security.ValidationState;
import com.sun.javafx.iio.ios.IosDescriptor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Client {
    private int clientId;
    private Map<Integer, ObjectOutputStream> senders;
    private Map<Integer, ObjectInputStream> receivers;
    private Map<Integer, Socket> sendSocket;
    private Map<Integer, Socket> receiveSocket;
    private ServerSocket serverSocket;
    private static final String[] serverIp={"127.0.0.1","127.0.0.1","127.0.0.1","127.0.0.1","127.0.0.1","127.0.0.1"};
    private static final int[] serverId={0,1,2,3,4,5};
    private static final int []serverPorts= {9980,9981,9982,9983,9984,9985};
    private static final int []clientPorts ={9990,9991};
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private Map<Integer,String> replyMsg=new HashMap<>();
    private Map<Integer, Map<String,Long>> server_chunk_offset;
    private List<Integer> chosenServers;

    public Client(int id) throws Exception {
        chosenServers=new LinkedList<>();
        this.clientId=id;
        try{
            serverSocket = new ServerSocket(clientPorts[clientId]);
            listen();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized void listen() throws Exception {
        Client client = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = null;
                boolean flag =true;
                while (flag) {
                    try {
                        socket = serverSocket.accept();
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        System.out.println("new Message");
                        new Thread(new messageHandler(ois, client)).start();
                    } catch (IOException e) {
                        //e.printStackTrace();
                        //flag = false;

                    }

                }
            }
        }).start();
    }

    public void test() throws Exception {
        try{

            String filename=null;
            long offset;
            while (true) {
                System.out.println("Please select an option:");
                System.out.println("1, create");
                System.out.println("2, append");
                System.out.println("3,read");
                int op = Integer.parseInt(reader.readLine());
                switch (op) {
                    case 1:
                        System.out.print("file name:");
                        filename= reader.readLine();
                        create(filename);
                        break;
                    case 2:
                        System.out.println("Please input file name:");
                        filename=reader.readLine();
                        append(filename);
                        break;
                    case 3:
                        System.out.println("Please input file name:");
                        filename=reader.readLine();
                        System.out.print("offset");
                        offset= Integer.parseInt(reader.readLine());
                        read(filename,offset);
                        break;
                    default:
                        System.out.println("Wrong input");
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        //close();
    }

    //send message to Mserver

    public ReplyMessage sendMsgToMS(RequestMessage message) throws IOException, ClassNotFoundException, InterruptedException {
        System.out.println("send message to MServer");
        Socket socket = new Socket(serverIp[0],serverPorts[0]);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(message);
        oos.flush();
        InputStream inputStream = socket.getInputStream();
        while (inputStream==null){
            System.out.println("Waiting for message");
            Thread.sleep(1000);
        }
        ObjectInputStream ois = new ObjectInputStream(inputStream);
        ReplyMessage message1 =(ReplyMessage) ois.readObject();
        this.chosenServers= message1.getChosenServers();
        System.out.println("receive message:"+message1.getType());
        return message1;
    }

    //send message to server/read or append

    public synchronized Message sendReadToServer(RequestMessage message)throws Exception{
        int id = chosenServers.get(0);
        Socket socket = new Socket(serverIp[id],serverPorts[id]);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(message);
        oos.flush();
        System.out.println("send READ message to chunk server_"+id);
        InputStream inputStream = socket.getInputStream();
        while (inputStream==null){
            System.out.println("waiting for message");
            Thread.sleep(1000);
        }
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        return (Message) ois.readObject();
    }

    public synchronized void sendAppendToServer(RequestMessage message) throws IOException {
        try {
            for (int i = 0; i < chosenServers.size(); i++) {
                int id = chosenServers.get(i);
                Socket socket = new Socket(serverIp[id], serverPorts[id]);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(message);
                oos.flush();
                System.out.println("send APPEND message to chunk server_" + id);
                //return (Message) ois.readObject();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //send read request to M-Server
    public synchronized void read(String filename, long offset) throws Exception{
        try{
            RequestMessage message = new RequestMessage("READ",this.clientId,filename,offset);
            //send read request to MServer
            ReplyMessage replyMessage=sendMsgToMS(message);
            while (chosenServers.size()<3){
                Thread.sleep(2000);
            }
            ReplyMessage replyMessage2 = (ReplyMessage) sendReadToServer(new RequestMessage("READ",this.clientId,replyMessage.getChunkname(),replyMessage.getOffset()));

            String content1 = replyMessage2.getContent();
            int id2= replyMessage2.getId();
            String chunkname = replyMessage2.getChunkname();
            System.out.println("read from server_"+id2+",chunk:"+chunkname+"reading content is "+content1);
            sendReadToServer(new RequestMessage("READ",this.clientId,chunkname));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //send create to MServer
    public synchronized void create(String filename){
        try{
            Socket socket = new Socket(serverIp[0],serverPorts[0]);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            RequestMessage message = new RequestMessage("CREATE",this.clientId,filename);
            oos.writeObject(message);
            oos.flush();
            System.out.println("send CREATE message to MServer");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized  void append_server(String chunkname) throws InterruptedException, IOException {
        sendAppendToServer(new RequestMessage("APPEND",this.clientId,chunkname));
        //wait until receive all reply
       /* while (chosenServers.size()<3){
            Thread.sleep(1000);
        }*/
        /*while (replyMsg.size()!=chosenServers.size()){
            Thread.sleep(1000);
        }*/
        //if receive one abort than send abort message , else send agree
        if(replyMsg.containsValue("ABORT")){
           // sendMsgToServer(new CommitMessage("ABORT",this.clientId,chunkname));
        }else {
            //sendMsgToServer(new CommitMessage("COMMIT",this.clientId,chunkname));
        }
    }
    //send append to MServer, then send append to file server
    public synchronized void append(String filename) {
        try {
            //send append to M-Server
            RequestMessage message = new RequestMessage("APPEND", this.clientId, filename);
            ReplyMessage replyMessage=sendMsgToMS(message);
            this.chosenServers= replyMessage.getChosenServers();
            System.out.println("chosen server size ="+this.chosenServers.size());
            append_server(replyMessage.getChunkname());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void classifyMessage(Message message) throws IOException, InterruptedException {
        int id= message.getId();
        String type=message.getType();
        switch (type){
            case "ERROR":
                System.out.println("ERROR message");
                ErrorMessage errorMessage =(ErrorMessage) message;
                String content = errorMessage.getContent();
                int senderid = errorMessage.getId();
                System.out.println("received a ERROR message from server_"+senderid+": "+content);
            case"AGREE":
            case "ABORT":
                System.out.println("COMMIT message:"+type);
                replyMsg.put(id,type);
                break;
            //receive messages from MServer
            /*case "replyREAD":
                System.out.println("REPLY message");
                ReplyMessage replyMessage = (ReplyMessage) message;


                break; */
            /*case "replyAPPEND":
                System.out.println("REPLY message");
                ReplyMessage replyMessage1 = (ReplyMessage) message;
                this.chosenServers= replyMessage1.getChosenServers();
                append_server(replyMessage1.getChunkname());
                break;*/
            case "READContent":
                System.out.println("READ content message");
                ReplyMessage replyMessage2 = (ReplyMessage) message;
                String content1 = replyMessage2.getContent();
                int id2= replyMessage2.getId();
                String chunkname = replyMessage2.getChunkname();
                System.out.println("read from server_"+id2+",chunk:"+chunkname+"reading content is "+content1);
                break;
            default:
                break;
        }
    }

}
