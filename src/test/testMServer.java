package test;

import MetaServer.MServer;
import Server.Server;


import java.io.IOException;

public class testMServer {
    public static void main(String []args) throws Exception {
        try {
            MServer MServer = new MServer();
            MServer.test();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
