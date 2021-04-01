package Server;


import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Server {
    private static int serverId;
    private String MServerIp="127.0.0.1";
    private static final int []serverPorts= {9980,9981,9982,9983,9984,9985};
    private String[] serverPath ={"MServer//","Server1//","Server2//","Server3//","Server4//","Server5//"};
    private String path = null;
    private List<String> chunks= new LinkedList<>();

    public Server(int id){
        serverId= id;
        path = serverPath[id];
    }
    public void sendHeartBeat() throws IOException {
        Socket socket = new Socket(MServerIp, serverPorts[0]);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        Enquire();
                        oos.writeObject(new HeartBeatMessage("HEARTBEAT", serverId, chunks, new Timestamp(System.currentTimeMillis())));

                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public  void test() throws Exception{
        try{
            sendHeartBeat();
            ServerSocket serversocket = new ServerSocket(serverPorts[serverId]);
            Socket socket = null;
            while (true){
                System.out.println("The server"+serverId+" is Connecting!");
                socket = serversocket.accept();
                ServerThread serverthread = new ServerThread(socket,serverId);
                serverthread.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void Enquire(){
        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdirs();
        }
        File[] files = dir.listFiles();
        List<String> filelist = new ArrayList<>();
        if(files.length>0) {
            for (int i = 0; i < files.length; i++) {
                filelist.add(files[i].getName());
            }
            this.chunks = filelist;
        }
    }
}
