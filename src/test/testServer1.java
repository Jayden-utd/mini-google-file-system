package test;

import Server.Server;

import java.io.IOException;

public class testServer1 {
    public static void main(String []args) throws Exception {
        try {
            Server server1 = new Server(1);
            server1.test();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
