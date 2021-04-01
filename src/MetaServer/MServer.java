package MetaServer;

import Message.Message;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.*;

import Message.Message;
import Message.RequestMessage;
import Message.ErrorMessage;
import Message.CommitMessage;
import Message.ReplyMessage;
import Message.HeartBeatMessage;

public class MServer {
    //private MetaData metaData;
    private int id=0;
    private String filepath = "MServer//";
    private List<String> filenames=null;
    private Map<String,List<String>> fileChunks;
    private Map<String,List<Integer>> chunk_serverMapping;
    private Map<Integer,List<String>> server_chunkMapping;
    //lastUpdateTSes;
    private Map<Integer,Timestamp> lastUpdateTS;
    private String[] serverIPs={"127.0.0.1","127.0.0.1","127.0.0.1","127.0.0.1","127.0.0.1","127.0.0.1"};
    private static final int []serverPorts= {9980,9981,9982,9983,9984,9985};
    private int[] serverIDs={1,2,3,4,5};
    private String[] clientIps={"127.0.0.1","127.0.0.1"};
    private ServerSocket serverSocket;
    private List<Integer> aliveServer;
    public MServer() throws IOException {
        serverSocket = new ServerSocket(9980);
        fileChunks = new HashMap<>();
        chunk_serverMapping = new HashMap<>();
        server_chunkMapping = new HashMap<>();
        lastUpdateTS = new HashMap<>();
        aliveServer = new LinkedList<>();
        init();
    }

    public void test(){
        try{

            listen();
            checkHeartBeats();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    // if time between last and current>15s, server crash
    public void updateLiveServer(){
        Timestamp currentTS = new Timestamp(System.currentTimeMillis());
        for(Integer id:lastUpdateTS.keySet()){
            if((currentTS.getTime()/1000)-(lastUpdateTS.get(id).getTime()/1000)>15){
                aliveServer.remove(id);
                server_chunkMapping.remove(id);
            }
        }

    }

    //every 15 seconds check
    public void checkHeartBeats(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        updateLiveServer();
                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }



    public synchronized void listen() throws Exception {
        MServer server = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = null;
                boolean flag =true;
                while (flag) {
                    try {
                        socket = serverSocket.accept();
                        //ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        new Thread(new messageHandler(socket, server)).start();
                    } catch (IOException e) {
                        //e.printStackTrace();
                        flag = false;
                        System.exit(0);
                    }

                }
            }
        }).start();
    }


    public void updateChunkServerMapping(){
        for(Integer serverid:server_chunkMapping.keySet()){
            List<String> chunks_at_server=server_chunkMapping.get(serverid);
            for(int i=0;i<chunks_at_server.size();i++){
                String chunkname=chunks_at_server.get(i);
                if(chunk_serverMapping.keySet().contains(chunkname)){
                    List<Integer> servers = chunk_serverMapping.get(chunkname);
                    servers.add(serverid);
                    chunk_serverMapping.remove(chunkname);
                    chunk_serverMapping.put(chunkname,servers);
                }else {
                    List<Integer> servers= new LinkedList<>();
                    servers.add(serverid);
                    chunk_serverMapping.remove(chunkname);
                    chunk_serverMapping.put(chunkname,servers);
                }
            }
        }
    }
    public void updateServerChunkMapping(HeartBeatMessage message){
        int id= message.getId();
        if(!aliveServer.contains(id)){
            aliveServer.add(id);
        }
        List<String> chunks = message.getChunks();
        Timestamp ts = message.getTimestamp();
        server_chunkMapping.remove(id);
        server_chunkMapping.put(id,chunks);
        lastUpdateTS.remove(id,ts);
        lastUpdateTS.put(id,ts);
        //update chunk
        //System.out.println(/*"MSever received a HEARTBEAT message from "+*/id);
    }

    public void init() throws IOException {
        initFileList();
        try {
            for (int i = 0; i < filenames.size(); i++) {
                String filename = filenames.get(i);
                List<String> chunks = readConfigFile(filename);
                fileChunks.put(filename, chunks);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void initFileList(){
        File dir = new File(filepath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        File[] files = dir.listFiles();
        List<String> filelist = new ArrayList<>();
        if(files.length>0) {
            for (int i = 0; i < files.length; i++) {
                filelist.add(files[i].getName());
                System.out.println(filelist.get(i));
            }
        }
        this.filenames = filelist;
    }

    public List<String> readConfigFile(String filename) throws IOException {
        List<String> chunks = new LinkedList<>();
        File file= new File(filepath+filename);
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        if(!file.getParentFile().exists()){
            file.mkdirs();
        }else {
            String chunk=null;
            while ( (chunk=bufferedReader.readLine())!=null){
                chunks.add(chunk);
            }
        }
        return  chunks;
    }
    public void replyRead(RequestMessage message,ObjectOutputStream oos) throws IOException {
        String filename = message.getFilename();
        long offset = message.getOffset();
        if(!filenames.contains(filename)){
            oos.writeObject(new ErrorMessage("ERROR",id,"can not find file name"));
            oos.flush();
            System.out.println("ERROR message send to client");
        }else {

            // acording to filename and offset find chunk and offset
            long chunkid=  offset/4096;
            long off = offset%4096;
            List<String> chunks=fileChunks.get(filename);
            //if the offset is too large
            if(chunks.size()<chunkid){
                oos.writeObject(new ErrorMessage("ERROR",id,"the offset is too long"));
                oos.flush();
                System.out.println("ERROR message: offset is too long");
            }else {
                String chunkname = chunks.get((int) chunkid);
                List<Integer> chosenservers = chunk_serverMapping.get(chunkname);
                oos.writeObject(new ReplyMessage("replyREAD", this.id, chosenservers, chunkname, off));
                oos.flush();
                System.out.println("send REPLY READ message to client");
            }
        }

    }

    //reply client's request
    public void replyAppend(RequestMessage message,ObjectOutputStream oos) throws IOException {
        String filename = message.getFilename();
        long offset = message.getOffset();
        if(!filenames.contains(filename)){
            oos.writeObject(new ErrorMessage("ERROR",id,"can not find file name"));
            oos.flush();
            System.out.println("send ERROR message :can not find name");
        }else {
            // acording to filename and offset find chunk and offset
            long chunkid=  offset/4096;
            long off = offset%4096;
            List<String> chunks=fileChunks.get(filename);
            String chunkname = chunks.get((int) chunkid);
            List<Integer> chosenservers = chunk_serverMapping.get(chunkname);
            oos.writeObject(new ReplyMessage("replyAPPEND",this.id,chosenservers,chunkname));
            oos.flush();
            System.out.println("send REPLY APPEND message to client:"+chosenservers.size());
        }
    }


    public void appendFileChunks(String filename,String chunkname){
        RandomAccessFile raf=null;
        try {
            raf = new RandomAccessFile(filename, "rw");
            long len = raf.length();
            raf.seek(len);
            raf.writeBytes(chunkname+"\n" );
            raf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void create(RequestMessage message,ObjectOutputStream oos) throws IOException {
        List<Integer> chosenServers =new LinkedList<>();
        List<Integer> servers = aliveServer;
        Random random = new Random();
        int id=0;
        while (chosenServers.size()<3){
            id=random.nextInt(servers.size());
            chosenServers.add(servers.get(id));
            servers.remove(id);
        }


        String filename = message.getFilename();
        String chunkname = UUID.randomUUID().toString();
        File file = new File(filepath+filename);
        if(file.exists()){
            oos.writeObject(new ErrorMessage("ERROR",this.id,"File already exists"));
            oos.flush();
            System.out.println("send ERROR message:File already exits");
        }else {
            file.createNewFile();
        }

        //String content = message.getContent();
        List<String> chunks = new LinkedList<>();
        //add file chunks to file.txt
        appendFileChunks(filepath+filename,chunkname);
        filenames.add(filename);
        chunks.add(chunkname);
        fileChunks.put(filename,chunks);
        chunk_serverMapping.put(chunkname,chosenServers);
        for(int i=0;i<chosenServers.size();i++){
            int cid = chosenServers.get(i);
            List<String> tchunk = server_chunkMapping.get(cid);
        }
        for(int i=0;i<chosenServers.size();i++){
            int temp = chosenServers.get(i);
            Socket socket = new Socket(serverIPs[temp],serverPorts[temp]);
            ObjectOutputStream tempoos= new ObjectOutputStream(socket.getOutputStream());
            tempoos.writeObject(new RequestMessage("CREATE",this.id,chunkname));
            tempoos.flush();
            System.out.println("MServer send a CREATE message to server_"+temp);
        }


    }

    public void classifyMessage(Message message, ObjectOutputStream oos) throws IOException {
        String type=message.getType();

        switch (type){
            case "HEARTBEAT":
                HeartBeatMessage hearBeatMessage =(HeartBeatMessage) message;
                updateServerChunkMapping(hearBeatMessage);
                updateChunkServerMapping();
                break;
            case "READ":
                RequestMessage requestMessage = (RequestMessage) message;
                replyRead(requestMessage,oos);
                break;
            case "APPEND":
                RequestMessage requestMessage1 =(RequestMessage) message;
                replyAppend(requestMessage1,oos);
                break;
            case "CREATE":
                RequestMessage requestMessage2 =(RequestMessage) message;
                create(requestMessage2,oos);
                break;
            default:
                break;
        }

    }

}
